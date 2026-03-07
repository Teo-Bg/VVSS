package drinkshop.export;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter {

    private CsvExporter() {}
    public static void exportOrders(List<Product> products, List<Order> orders, String path) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        // Write main orders CSV (valid tabular format)
        try (FileWriter w = new FileWriter(path)) {
            w.write("OrderId,Product,Quantity,Price\n");
            for (Order o : orders) {
                for (OrderItem i : o.getItems()) {
                    Product p = products.stream()
                            .filter(prod -> i.getProduct().getId() == prod.getId())
                            .findFirst()
                            .orElse(null);
                    if (p == null) continue;
                    w.write(o.getId() + "," + p.getNume() + "," + i.getQuantity() + "," + i.getTotal() + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Write daily summary to a separate file
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + date.replace(".", "-") + ".csv";
        try (FileWriter sw = new FileWriter(summaryPath)) {
            sw.write("OrderId,OrderTotal\n");
            double sum = 0.0;
            for (Order o : orders) {
                sw.write(o.getId() + "," + o.getTotalPrice() + "\n");
                sum += o.getTotalPrice();
            }
            sw.write("TOTAL," + sum + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

