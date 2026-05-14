package drinkshop.service;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;
import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.service.validator.StocValidator;

import java.util.List;
import java.util.stream.Collectors;

public class StocService {

    private final Repository<Integer, Stoc> stocRepo;
    private final StocValidator validator = new StocValidator();

    public StocService(Repository<Integer, Stoc> stocRepo) {
        this.stocRepo = stocRepo;
    }

    public List<Stoc> getAll() {
        return stocRepo.findAll();
    }

    public void add(Stoc s) {
        validator.validate(s);
        stocRepo.save(s);
    }

    public void update(Stoc s) {
        validator.validate(s);
        stocRepo.save(s);
    }

    public void delete(int id) {
        stocRepo.delete(id);
    }

    public boolean areSuficient(Reteta reteta) {
        List<IngredientReteta> ingredienteNecesare = reteta.getIngrediente();

        for (IngredientReteta ingredientEntry : ingredienteNecesare) {
            String ingredient = ingredientEntry.getDenumire();
            double necesar = ingredientEntry.getCantitate();

            double disponibil = stocRepo.findAll().stream()
                    .filter(s -> s.getIngredient().equalsIgnoreCase(ingredient))
                    .mapToDouble(Stoc::getCantitate)
                    .sum();

            if (disponibil < necesar) {
                return false;
            }
        }
        return true;
    }

    public void consuma(Reteta reteta) {
        if (!areSuficient(reteta)) {
            throw new IllegalStateException("Stoc insuficient pentru rețeta.");
        }

        for (IngredientReteta ingredientEntry : reteta.getIngrediente()) {
            String ingredient = ingredientEntry.getDenumire();
            double necesar = ingredientEntry.getCantitate();

            List<Stoc> ingredienteStoc = stocRepo.findAll().stream()
                    .filter(s -> s.getIngredient().equalsIgnoreCase(ingredient))
                    .collect(Collectors.toList());

            double ramas = necesar;

            for (Stoc s : ingredienteStoc) {
                if (ramas <= 0) break;

                double deScazut = Math.min(s.getCantitate(), ramas);
                s.setCantitate(s.getCantitate() - deScazut);
                ramas -= deScazut;

                stocRepo.save(s);
            }
        }
    }
}