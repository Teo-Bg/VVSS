package drinkshop.service.validator;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;

import java.util.List;

public class RetetaValidator implements Validator<Reteta> {

    @Override
    public void validate(Reteta reteta) {

        StringBuilder errors = new StringBuilder();

        if (reteta.getId() <= 0)
            errors.append("Product ID invalid!\n");

        List<IngredientReteta> ingrediente = reteta.getIngrediente();
        if (ingrediente == null || ingrediente.isEmpty()) {
            errors.append("Ingrediente empty!\n");
        } else {
            for (IngredientReteta entry : ingrediente) {
                if (entry.getDenumire() == null || entry.getDenumire().isBlank()) {
                    errors.append("Numele ingredientului nu poate fi gol!\n");
                }
                if (entry.getCantitate() <= 0) {
                    errors.append("[").append(entry.getDenumire()).append("] cantitate negativa sau zero\n");
                }
            }
        }

        if (errors.length() > 0)
            throw new ValidationException(errors.toString());
    }
}

