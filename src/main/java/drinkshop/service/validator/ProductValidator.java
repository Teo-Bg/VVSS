package drinkshop.service.validator;

import drinkshop.domain.Product;

public class ProductValidator implements Validator<Product> {

    @Override
    public void validate(Product product) {
        StringBuilder errors = new StringBuilder();

        if (product.getId() <= 0)
            errors.append("ID invalid!\n");

        if (product.getNume() == null || product.getNume().isBlank())
            errors.append("Numele nu poate fi gol!\n");

        if (product.getPret() <= 0)
            errors.append("Pret invalid!\n");

        if (errors.length() > 0)
            throw new ValidationException(errors.toString());
    }
}


