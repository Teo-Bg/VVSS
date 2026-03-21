package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;
    private MockProductRepository mockProductRepository;

    static class MockProductRepository implements Repository<Integer, Product> {
        private final List<Product> storage = new ArrayList<>();
        private int saveCount = 0;

        @Override
        public Product save(Product entity) {
            saveCount++;
            storage.add(entity);
            return entity;
        }

        @Override
        public Product findOne(Integer id) {
            return storage.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public Product delete(Integer id) {
            Product toDelete = findOne(id);
            storage.removeIf(p -> p.getId() == id);
            return toDelete;
        }

        public int getSaveCount() {
            return saveCount;
        }

        public void reset() {
            storage.clear();
            saveCount = 0;
        }
    }

    @BeforeEach
    @DisplayName("Setup - Initialize test environment")
    void setUp() {
        mockProductRepository = new MockProductRepository();
        productService = new ProductService(mockProductRepository);
    }

    @AfterEach
    @DisplayName("Teardown - Cleanup after test")
    void tearDown() {
        productService = null;
        mockProductRepository.reset();
        mockProductRepository = null;
    }

    // ==================== ECP (4 cazuri) ====================

    @Nested
    @DisplayName("ECP - Equivalence Class Partition Tests")
    class ECPTests {

        @Test
        @DisplayName("TC1_ECP: Valid - 'Coca Cola', price 5.99, tip WATER_BASED")
        void testAddProduct_ValidNameAndPrice_Success() {
            // ARRANGE
            Product product = new Product(1, "Coca Cola", 5.99,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT
            productService.addProduct(product);

            // ASSERT
            assertEquals(1, mockProductRepository.getSaveCount());
            assertNotNull(mockProductRepository.findOne(1));
        }

        @Test
        @DisplayName("TC2_ECP: Valid - 'Orange Juice', price 7.50, tip WATER_BASED")
        void testAddProduct_ValidOrangeJuice_Success() {
            // ARRANGE
            Product product = new Product(2, "Orange Juice", 7.50,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT
            productService.addProduct(product);

            // ASSERT
            assertEquals(1, mockProductRepository.getSaveCount());
            Product saved = mockProductRepository.findOne(2);
            assertNotNull(saved);
        }

        @Test
        @DisplayName("TC3_ECP: Invalid - name is not String (Integer 123), price 3.99")
        void testAddProduct_InvalidNameType_ThrowsException() {
            // ARRANGE - Testing with invalid name type (this would need casting in real scenario)
            // Since Java is strongly typed, we simulate by passing null to demonstrate validation
            Product product = new Product(3, null, 3.99,
                    CategorieBautura.CLASSIC_COFFEE, TipBautura.WATER_BASED);

            // ACT & ASSERT
            assertThrows(ValidationException.class, () ->
                    productService.addProduct(product)
            );
            assertEquals(0, mockProductRepository.getSaveCount());
        }

        @Test
        @DisplayName("TC4_ECP: Invalid - 'Coffee' (valid), price 0.0 (violates pret > 0)")
        void testAddProduct_InvalidPrice_ThrowsException() {
            // ARRANGE
            Product product = new Product(4, "Coffee", 0.0,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT & ASSERT
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    productService.addProduct(product)
            );

            StringBuilder errorMsg = new StringBuilder(ex.getMessage());
            assertTrue(errorMsg.toString().contains("Pret invalid"));
            assertEquals(0, mockProductRepository.getSaveCount());
        }
    }

    // ==================== BVA (4 cazuri: 2 valide + 2 invalide) ====================

    @Nested
    @DisplayName("BVA - Boundary Value Analysis Tests")
    class BVATests {

        @Test
        @DisplayName("TC5_BVA: Valid - name 'Coffee', price 0.01 (minimum valid)")
        void testAddProduct_MinBoundaryPrice_Success() {
            // ARRANGE
            Product product = new Product(5, "Coffee", 0.01,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT
            productService.addProduct(product);

            // ASSERT
            assertEquals(1, mockProductRepository.getSaveCount());
            assertNotNull(mockProductRepository.findOne(5));
        }

        @Test
        @DisplayName("TC6_BVA: Valid - name 'Tea', price 50.0 (in-range)")
        void testAddProduct_MidRangePrice_Success() {
            // ARRANGE
            Product product = new Product(6, "Tea", 50.0,
                    CategorieBautura.CLASSIC_COFFEE, TipBautura.WATER_BASED);

            // ACT
            productService.addProduct(product);

            // ASSERT
            assertEquals(1, mockProductRepository.getSaveCount());
            Product saved = mockProductRepository.findOne(6);
            assertNotNull(saved);
        }

        @Test
        @DisplayName("TC7_BVA: Invalid - name 'Water', price 0.0 (boundary invalid)")
        void testAddProduct_ZeroBoundaryPrice_ThrowsException() {
            // ARRANGE
            Product product = new Product(7, "Water", 0.0,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT & ASSERT
            assertThrows(ValidationException.class, () ->
                    productService.addProduct(product)
            );
            assertEquals(0, mockProductRepository.getSaveCount());
        }

        @Test
        @DisplayName("TC8_BVA: Invalid - name 'Juice', price -5.0 (negative)")
        void testAddProduct_NegativePrice_ThrowsException() {
            // ARRANGE
            Product product = new Product(8, "Juice", -5.0,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT & ASSERT
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    productService.addProduct(product)
            );

            StringBuilder errors = new StringBuilder(ex.getMessage());
            assertTrue(errors.toString().contains("Pret invalid"));
            assertEquals(0, mockProductRepository.getSaveCount());
        }
    }

    @Nested
    @DisplayName("Parameterized Tests - Additional Coverage")
    class ParameterizedTests {

        @ParameterizedTest
        @DisplayName("Valid prices: multiple boundary values")
        @ValueSource(doubles = {0.01, 1.5, 9.99, 50.0})
        void testAddProduct_ValidPricesRange(double price) {
            // ARRANGE
            Product product = new Product(100 + (int)price, "Test Drink", price,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT
            productService.addProduct(product);

            // ASSERT
            assertEquals(1, mockProductRepository.getSaveCount());
            mockProductRepository.reset();
        }

        @ParameterizedTest
        @DisplayName("Invalid prices: zero and negative values")
        @ValueSource(doubles = {0.0, -0.01, -5.0})
        void testAddProduct_InvalidPrices(double price) {
            // ARRANGE
            Product product = new Product(200, "Bad Price Drink", price,
                    CategorieBautura.JUICE, TipBautura.WATER_BASED);

            // ACT & ASSERT
            assertThrows(ValidationException.class, () ->
                    productService.addProduct(product)
            );
            assertEquals(0, mockProductRepository.getSaveCount());
            mockProductRepository.reset();
        }
    }
}

