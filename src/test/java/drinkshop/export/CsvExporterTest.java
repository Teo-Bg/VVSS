package drinkshop.export;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvExporter Unit Tests - Cyclomatic Complexity Coverage")
class CsvExporterTest {

    @TempDir
    Path tempDir;

    private List<Product> products;
    private LocalDate today = LocalDate.now();
    private String dateFormat = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    @BeforeEach
    void setUp() {
        // Standard product list
        products = Arrays.asList(
            new Product(1, "Coffee", 3.50, "Bauturi", "Quente"),
            new Product(2, "Juice", 2.50, "Bauturi", "Frigide"),
            new Product(3, "Tea", 2.00, "Bauturi", "Quente")
        );
    }

    // ========== PATH F02_P01: IOException at first FileWriter ==========
    @Test
    @DisplayName("TC_02 - F02_P01: IOException - Invalid path should throw RuntimeException")
    void testExportOrdersInvalidPath_Path01() {
        // Setup
        OrderItem item1 = new OrderItem(products.get(0), 2);
        List<OrderItem> items = Arrays.asList(item1);
        double totalPrice = items.stream().mapToDouble(OrderItem::getTotal).sum();
        List<Order> orders = Arrays.asList(
            new Order(101, items, totalPrice)
        );

        // Invalid path: restricted directory
        String invalidPath = "C:\\invalid_path_that_does_not_exist\\orders.csv";

        // Execute & Verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            CsvExporter.exportOrders(products, orders, invalidPath);
        });
        assertTrue(exception.getCause() instanceof IOException);
    }

    // ========== PATH F02_P03: Empty orders list ==========
    @Test
    @DisplayName("TC_03 - F02_P03: Empty orders - Should create files with headers only")
    void testExportOrdersEmptyOrders_Path03() throws IOException {
        // Setup
        List<Order> orders = new ArrayList<>();  // Empty list
        String path = tempDir.resolve("empty_orders.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify main file exists and contains only header
        File mainFile = new File(path);
        assertTrue(mainFile.exists(), "Main CSV file should exist");
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        assertEquals("OrderId,Product,Quantity,Price\n", mainContent);

        // Verify summary file exists
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + dateFormat + ".csv";
        File summaryFile = new File(summaryPath);
        assertTrue(summaryFile.exists(), "Summary CSV file should exist");
        String summaryContent = new String(Files.readAllBytes(summaryFile.toPath()));
        assertTrue(summaryContent.contains("OrderId,OrderTotal"));
        assertTrue(summaryContent.contains("TOTAL,0.0"));
    }

    // ========== PATH F02_P04: Orders without items ==========
    @Test
    @DisplayName("TC_04 - F02_P04: Orders with no items - Should create files with no data rows")
    void testExportOrdersNoItems_Path04() throws IOException {
        // Setup
        List<Order> orders = Arrays.asList(
            new Order(101),  // Order with no items
            new Order(102)   // Second order with no items
        );
        String path = tempDir.resolve("no_items_orders.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify main file: only header
        File mainFile = new File(path);
        assertTrue(mainFile.exists());
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        assertEquals("OrderId,Product,Quantity,Price\n", mainContent);

        // Verify summary file: entries for both orders
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + dateFormat + ".csv";
        File summaryFile = new File(summaryPath);
        String summaryContent = new String(Files.readAllBytes(summaryFile.toPath()));
        assertTrue(summaryContent.contains("101,0.0"));
        assertTrue(summaryContent.contains("102,0.0"));
    }

    // ========== PATH F02_P05: Order with NULL product (product not found) ==========
    @Test
    @DisplayName("TC_05 - F02_P05: Order with non-existent product - Should skip NULL products")
    void testExportOrdersNullProduct_Path05() throws IOException {
        // Setup - Create a product that doesn't exist in the products list
        Product nonExistentProduct = new Product(999, "NonExistent", 10.0, "Bauturi", "Quente");

        OrderItem item1 = new OrderItem(nonExistentProduct, 2);  // Product 999 not in products list
        List<OrderItem> items = Arrays.asList(item1);
        // Note: totalPrice is set to 0.0 because item won't be written (product filtered out)
        List<Order> orders = Arrays.asList(
            new Order(101, items, 0.0)
        );
        String path = tempDir.resolve("null_product_orders.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify main file: only header (no item written because product is null)
        File mainFile = new File(path);
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        assertEquals("OrderId,Product,Quantity,Price\n", mainContent);

        // Verify summary file: order exists but with 0.0 total
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + dateFormat + ".csv";
        String summaryContent = new String(Files.readAllBytes(new File(summaryPath).toPath()));
        assertTrue(summaryContent.contains("101,0.0"));
    }

    // ========== PATH F02_P06: Valid order then empty second section ==========
    @Test
    @DisplayName("TC_06 - F02_P06: Valid first order, empty second - Mixed valid/empty")
    void testExportOrdersMixedValid_Path06() throws IOException {
        // Setup
        OrderItem item1 = new OrderItem(products.get(0), 2);
        List<OrderItem> items = Arrays.asList(item1);
        double totalPrice = items.stream().mapToDouble(OrderItem::getTotal).sum();
        List<Order> orders = Arrays.asList(
            new Order(101, items, totalPrice)
        );
        String path = tempDir.resolve("mixed_orders.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify main file
        File mainFile = new File(path);
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        assertTrue(mainContent.contains("101,Coffee,2,"));

        // Verify summary file
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + dateFormat + ".csv";
        String summaryContent = new String(Files.readAllBytes(new File(summaryPath).toPath()));
        assertTrue(summaryContent.contains("101,"));
    }

    // ========== PATH F02_P07: Full happy path with multiple products and orders ==========
    @Test
    @DisplayName("TC_01 - F02_P07: Full path - 2 products, 2 orders with items")
    void testExportOrdersFullPath_Path07a() throws IOException {
        // Setup
        List<OrderItem> items1 = Arrays.asList(
            new OrderItem(products.get(0), 2),  // Coffee x2
            new OrderItem(products.get(1), 1)   // Juice x1
        );
        double total1 = items1.stream().mapToDouble(OrderItem::getTotal).sum();
        
        List<OrderItem> items2 = Arrays.asList(
            new OrderItem(products.get(0), 3)   // Coffee x3
        );
        double total2 = items2.stream().mapToDouble(OrderItem::getTotal).sum();
        
        List<Order> orders = Arrays.asList(
            new Order(101, items1, total1),
            new Order(102, items2, total2)
        );
        String path = tempDir.resolve("full_export.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify main file exists and contains all items
        File mainFile = new File(path);
        assertTrue(mainFile.exists());
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));

        // Verify header
        assertTrue(mainContent.contains("OrderId,Product,Quantity,Price"));

        // Verify order items
        assertTrue(mainContent.contains("101,Coffee,2,"));
        assertTrue(mainContent.contains("101,Juice,1,"));
        assertTrue(mainContent.contains("102,Coffee,3,"));

        // Verify summary file
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + dateFormat + ".csv";
        File summaryFile = new File(summaryPath);
        assertTrue(summaryFile.exists());
        String summaryContent = new String(Files.readAllBytes(summaryFile.toPath()));

        // Verify summary header and data
        assertTrue(summaryContent.contains("OrderId,OrderTotal"));
        assertTrue(summaryContent.contains("101,"));
        assertTrue(summaryContent.contains("102,"));
        assertTrue(summaryContent.contains("TOTAL,"));
    }

    // ========== Additional Path F02_P07: Single product, single order ==========
    @Test
    @DisplayName("TC_07 - F02_P07: Single product, single order (loop coverage = 1,1,1)")
    void testExportOrdersFullPath_Path07b() throws IOException {
        // Setup - Minimal full path: 1 product, 1 order, 1 item
        Product coffee = products.get(0);
        OrderItem item1 = new OrderItem(coffee, 2);
        List<OrderItem> items = Arrays.asList(item1);
        double totalPrice = items.stream().mapToDouble(OrderItem::getTotal).sum();
        List<Order> orders = Arrays.asList(
            new Order(101, items, totalPrice)
        );
        String path = tempDir.resolve("single_order.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify main file
        File mainFile = new File(path);
        assertTrue(mainFile.exists());
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        
        // Should have header + one data row
        String[] lines = mainContent.split("\n");
        assertEquals(2, lines.length, "Main file should have header + 1 data row");
        assertTrue(lines[1].contains("101,Coffee,2,"));

        // Verify summary file
        String summaryPath = path.replaceAll("\\.csv$", "") + "_summary_" + dateFormat + ".csv";
        File summaryFile = new File(summaryPath);
        assertTrue(summaryFile.exists());
        String summaryContent = new String(Files.readAllBytes(summaryFile.toPath()));

        // Should have header + one order + total
        String[] summaryLines = summaryContent.split("\n");
        assertEquals(3, summaryLines.length, "Summary file should have header + 1 order + total");
        assertTrue(summaryLines[1].contains("101,"));
        assertTrue(summaryLines[2].contains("TOTAL,"));
    }

    // ========== Extra coverage: Multiple items in single order ==========
    @Test
    @DisplayName("Extra Coverage: Multiple items in single order")
    void testExportOrdersMultipleItems() throws IOException {
        // Setup
        List<OrderItem> items = Arrays.asList(
            new OrderItem(products.get(0), 2),  // Coffee x2
            new OrderItem(products.get(1), 3),  // Juice x3
            new OrderItem(products.get(2), 1)   // Tea x1
        );
        double totalPrice = items.stream().mapToDouble(OrderItem::getTotal).sum();
        List<Order> orders = Arrays.asList(
            new Order(101, items, totalPrice)
        );
        String path = tempDir.resolve("multi_items.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify all items written
        File mainFile = new File(path);
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        String[] lines = mainContent.split("\n");
        
        // Header + 3 items
        assertEquals(4, lines.length);
        assertTrue(lines[1].contains("Coffee"));
        assertTrue(lines[2].contains("Juice"));
        assertTrue(lines[3].contains("Tea"));
    }

    // ========== Edge case: Product list contains null ==========
    @Test
    @DisplayName("Edge case: Handle null in product lookup gracefully")
    void testExportOrdersWithProductFiltering() throws IOException {
        // Setup
        List<OrderItem> items = Arrays.asList(
            new OrderItem(products.get(0), 2),  // Exists
            new OrderItem(new Product(999, "Ghost", 0, "None", "None"), 1)  // Doesn't exist
        );
        double totalPrice = items.stream().mapToDouble(OrderItem::getTotal).sum();
        List<Order> orders = Arrays.asList(
            new Order(101, items, totalPrice)
        );
        String path = tempDir.resolve("filtering_test.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify only valid product is written
        File mainFile = new File(path);
        String mainContent = new String(Files.readAllBytes(mainFile.toPath()));
        String[] lines = mainContent.split("\n");
        
        // Header + 1 valid item (Ghost is filtered out)
        assertEquals(2, lines.length);
        assertTrue(lines[1].contains("Coffee"));
        assertFalse(mainContent.contains("Ghost"));
    }

    // ========== Coverage: Verify file path handling ==========
    @Test
    @DisplayName("Verify correct summary file naming and location")
    void testSummaryFileNaming() throws IOException {
        // Setup
        OrderItem item1 = new OrderItem(products.get(0), 1);
        List<OrderItem> items = Arrays.asList(item1);
        double totalPrice = items.stream().mapToDouble(OrderItem::getTotal).sum();
        List<Order> orders = Arrays.asList(
            new Order(101, items, totalPrice)
        );
        String path = tempDir.resolve("test_orders.csv").toString();

        // Execute
        CsvExporter.exportOrders(products, orders, path);

        // Verify summary path construction
        String expectedSummaryPath = tempDir.resolve("test_orders_summary_" + dateFormat + ".csv").toString();
        File summaryFile = new File(expectedSummaryPath);
        assertTrue(summaryFile.exists(), "Summary file should be at: " + expectedSummaryPath);
    }
}
