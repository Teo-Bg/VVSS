package drinkshop.service;

import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StocService Unit Tests - With Mocks")
class StocServiceTest {

    private StocService stocService;
    private Repository<Integer, Stoc> mockStocRepository;

    static class MockStocRepository implements Repository<Integer, Stoc> {
        private final List<Stoc> storage = new ArrayList<>();

        @Override
        public Stoc save(Stoc entity) {
            storage.add(entity);
            return entity;
        }

        @Override
        public Stoc findOne(Integer id) {
            return storage.stream()
                    .filter(s -> s.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Stoc> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public Stoc delete(Integer id) {
            Stoc toDelete = findOne(id);
            storage.removeIf(s -> s.getId() == id);
            return toDelete;
        }
    }

    @BeforeEach
    void setUp() {
        mockStocRepository = new MockStocRepository();
        stocService = new StocService(mockStocRepository);
    }

    @Test
    @DisplayName("TC_01: Check Sufficient Stock - With sufficient quantity")
    void testCheckSufficientStockWithMocks() {
        // ARRANGE
        Stoc stoc = new Stoc(1, "Apa", 100.0, 20.0);  // Stock: 100, Minimum: 20
        stocService.add(stoc);

        // ACT
        boolean hasSufficientStock = stoc.getCantitate() >= stoc.getStocMinim();

        // ASSERT
        assertTrue(hasSufficientStock, "Stock should be sufficient (100 >= 20)");
        assertEquals(1, mockStocRepository.findAll().size());
        assertNotNull(mockStocRepository.findOne(1));
    }

    @Test
    @DisplayName("TC_02: Check Insufficient Stock - Below minimum threshold")
    void testCheckInsufficientStockWithMocks() {
        // ARRANGE
        Stoc stoc = new Stoc(2, "Suc", 10.0, 50.0);  // Stock: 10, Minimum: 50

        // ACT & ASSERT - Should throw ValidationException because stock < minimum
        assertThrows(ValidationException.class, () -> {
            stocService.add(stoc);
        });
        assertEquals(0, mockStocRepository.findAll().size());
    }

    @Test
    @DisplayName("TC_03: Check Stock at Boundary - Exactly at minimum")
    void testCheckStockAtBoundaryWithMocks() {
        // ARRANGE
        Stoc stoc = new Stoc(3, "Cafea", 25.0, 25.0);  // Stock: 25, Minimum: 25
        stocService.add(stoc);

        // ACT
        boolean hasSufficientStock = stoc.getCantitate() >= stoc.getStocMinim();

        // ASSERT
        assertTrue(hasSufficientStock, "Stock at boundary should be sufficient (25 >= 25)");
    }

    @Test
    @DisplayName("TC_04: Check Stock with Invalid Data - Negative stock")
    void testCheckInvalidStockWithMocks() {
        // ARRANGE
        Stoc stoc = new Stoc(-1, "Invalid", 50.0, 10.0);

        // ACT & ASSERT
        assertThrows(ValidationException.class, () -> {
            stocService.add(stoc);
        });
        assertEquals(0, mockStocRepository.findAll().size());
    }

    @Test
    @DisplayName("TC_05: Check Multiple Stocks - Different sufficiency levels")
    void testCheckMultipleStocksWithMocks() {
        // ARRANGE
        Stoc stoc1 = new Stoc(1, "Apa", 100.0, 20.0);   // Sufficient
        Stoc stoc3 = new Stoc(3, "Cafea", 25.0, 25.0);  // Boundary

        stocService.add(stoc1);
        stocService.add(stoc3);

        // ACT
        List<Stoc> allStocks = mockStocRepository.findAll();

        // ASSERT
        assertEquals(2, allStocks.size());
        assertTrue(stoc1.getCantitate() >= stoc1.getStocMinim());
        assertTrue(stoc3.getCantitate() >= stoc3.getStocMinim());
    }

    @Test
    @DisplayName("TC_06: Validation Succeeds - With valid stock data")
    void testValidationSucceedsWithValidStock() {
        // ARRANGE
        Stoc validStoc = new Stoc(10, "ChocLate", 75.5, 15.0);

        // ACT
        stocService.add(validStoc);
        Stoc retrievedStoc = mockStocRepository.findOne(10);

        // ASSERT
        assertNotNull(retrievedStoc, "Valid stock should be saved");
        assertEquals(10, retrievedStoc.getId());
        assertEquals("ChocLate", retrievedStoc.getIngredient());
        assertEquals(75.5, retrievedStoc.getCantitate());
        assertEquals(15.0, retrievedStoc.getStocMinim());
        assertTrue(retrievedStoc.getCantitate() > retrievedStoc.getStocMinim());
        assertEquals(1, mockStocRepository.findAll().size());
    }
}
