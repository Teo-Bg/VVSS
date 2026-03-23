package drinkshop.service;

import drinkshop.domain.Reteta;
import drinkshop.repository.Repository;
import drinkshop.service.validator.RetetaValidator;

import java.util.List;

public class RetetaService {

    private final Repository<Integer, Reteta> retetaRepo;
    private final RetetaValidator validator = new RetetaValidator();

    public RetetaService(Repository<Integer, Reteta> retetaRepo) {
        this.retetaRepo = retetaRepo;
    }

    public void addReteta(Reteta r) {
        save(r);
    }

    public void updateReteta(Reteta r) {
        save(r);
    }

    /**
     * Metoda unificată de salvare (insert/update) pentru rețete
     * @param reteta rețeta de salvat
     * @throws ValidationException dacă validarea eșuează
     */
    public void save(Reteta reteta) {
        validator.validate(reteta);
        retetaRepo.save(reteta);
    }

    public void deleteReteta(int id) {
        retetaRepo.delete(id);
    }

    public Reteta findById(int id) {
        return retetaRepo.findOne(id);
    }

    public List<Reteta> getAll() {
        return retetaRepo.findAll();
    }
}