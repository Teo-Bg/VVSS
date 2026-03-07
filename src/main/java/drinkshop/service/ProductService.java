package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService {

    private final Repository<Integer, Product> productRepo;
    private final ProductValidator validator = new ProductValidator();

    public ProductService(Repository<Integer, Product> productRepo) {
        this.productRepo = productRepo;
    }

    public void addProduct(Product p) {
        validator.validate(p);
        productRepo.save(p);
    }

    public void updateProduct(int id, String name, double price, String categorie, String tip) {
        Product updated = new Product(id, name, price, categorie, tip);
        validator.validate(updated);
        productRepo.save(updated);
    }

    public void deleteProduct(int id) {
        productRepo.delete(id);
    }

    public List<Product> getAllProducts() {
//        Iterable<Product> it=productRepo.findAll();
//        ArrayList<Product> products=new ArrayList<>();
//        it.forEach(products::add);
//        return products;

//        return StreamSupport.stream(productRepo.findAll().spliterator(), false)
//                    .collect(Collectors.toList());
        return productRepo.findAll();
    }

    public Product findById(int id) {
        return productRepo.findOne(id);
    }

    public List<Product> filterByCategorie(String categorie) {
        if (CategorieBautura.ALL.equals(categorie)) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getCategorie().equals(categorie))
                .collect(Collectors.toList());
    }

    public List<Product> filterByTip(String tip) {
        if (TipBautura.ALL.equals(tip)) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getTip().equals(tip))
                .collect(Collectors.toList());
    }
}