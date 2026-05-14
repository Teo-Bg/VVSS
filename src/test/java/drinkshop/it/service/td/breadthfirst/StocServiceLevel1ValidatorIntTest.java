package drinkshop.it.service.td.breadthfirst;

import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.service.StocService;
import drinkshop.service.validator.StocValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StocServiceLevel1ValidatorIntTest {
    private Stoc stoc;
    private StocValidator stocValidator;
    private Repository<Integer, Stoc> stocRepo;

    private StocService stocService;

    @BeforeEach
    void setUp() {
        stoc = new Stoc(1, "Apa", 5.0, 1.0);  // Use real Stoc instead of mock
        stocValidator = new StocValidator(); // integram primul nivel (top down breadth first)
        stocRepo = mock(Repository.class);//mock

        stocService = new StocService(stocRepo);
    }

    @Test
    @Order(1)
    void testAddValid_withRealValidator() {
        //apelam metoda add si evaluam apelul cu fail
        when(stocRepo.save(stoc)).thenReturn(stoc);
        
        try{
            stocService.add(stoc);
        }catch (Exception e){
            fail("Invalid add operation");
        }

        // verificam interactiunea obiectului testat cu obiectele mock ramase, i.e., repository
        verify(stocRepo, times(1)).save(stoc);
    }

    @Test
    @Order(2)
    void testAddInvalid_withRealValidator() {
        //Create invalid stock with negative ID
        Stoc invalidStoc = new Stoc(-1, "Invalid", 5.0, 1.0);

        //apelam metoda si evaluam invalidarea obiectului
        Assertions.assertThrows(ValidationException.class, () -> {
            stocService.add(invalidStoc);
        });

        //Verify it was not saved
        verify(stocRepo, never()).save(invalidStoc);
    }

    @Test
    @Order(3)
    @DisplayName("TC_V1: Validation Succeeds - With valid stock and real validator")
    void testValidationSucceedsWithValidStock() {
        // ARRANGE - Setup a valid stock mock
        Stoc validStoc = new Stoc(5, "Coffee", 100.0, 20.0);
        stoc = validStoc;  // Use real Stoc object instead of mock
        stocValidator = new StocValidator();  // Real validator

        // ACT
        try {
            stocService.add(stoc);
        } catch (ValidationException e) {
            fail("Validation should succeed for valid stock: " + e.getMessage());
        }

        // ASSERT
        verify(stocRepo, times(1)).save(stoc);
        assertEquals(5, stoc.getId());
        assertEquals("Coffee", stoc.getIngredient());
        assertEquals(100.0, stoc.getCantitate());
        assertEquals(20.0, stoc.getStocMinim());
        assertTrue(stoc.getCantitate() >= stoc.getStocMinim(), "Stock should be >= minimum");
    }
}