package drinkshop.it.service.td.breadthfirst;

import drinkshop.domain.Stoc;
import drinkshop.domain.Reteta;
import drinkshop.domain.IngredientReteta;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileStocRepository;
import drinkshop.service.StocService;
import drinkshop.service.validator.StocValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StocServiceLevel1RepoIntTest {
    private Stoc stoc;
    private StocValidator stocValidator;
    private Repository<Integer, Stoc> stocRepo;

    private StocService stocService;

    @BeforeEach
    void setUp() {
        stocValidator = new StocValidator();//deja integrat
        stocRepo = new FileStocRepository("data/stocuri.txt"); // integram primul nivel (top down breadth first)
        stoc = new Stoc(1, "TestStoc", 50.0, 10.0);  // Use real Stoc instead of mock
        stocService = new StocService(stocRepo);
    }

    @Test
    @Order(1)
    void testAddValid_withRealRepo() {
        //apelam (metoda add) si evaluam apelul (cu fail)
        try{
            stocService.add(stoc);
        }catch (Exception e){
            fail("Invalid add operation " + e);
        }

        // Verify the stock was added to repository
        Stoc retrievedStoc = stocRepo.findOne(stoc.getId());
        assertNotNull(retrievedStoc);
        assertEquals("TestStoc", retrievedStoc.getIngredient());
        assertEquals(50.0, retrievedStoc.getCantitate());
        assertEquals(10.0, retrievedStoc.getStocMinim());
    }

    @Test
    @Order(2)
    void testAddInvalid_withRealRepo() {
        //Create invalid stock with negative ID
        Stoc invalidStoc = new Stoc(-1, "Invalid", 50.0, 10.0);

        //apelam metoda si invalidarea obiectului
        Assertions.assertThrows(ValidationException.class, () -> {
            stocService.add(invalidStoc);
        });

        //Verify it was not saved
        Stoc notFound = stocRepo.findOne(-1);
        assertNull(notFound, "Invalid stock should not be saved");
    }

    @Test
    @Order(3)
    @DisplayName("TC_ReadRepo - Read valid stock from real repository")
    void testReadFromRealRepository() {
        // Setup - Create and add a stock to repository
        Stoc newStoc = new Stoc(99, "Coffee", 100.0, 20.0);
        
        try {
            stocService.add(newStoc);
        } catch (Exception e) {
            fail("Failed to add stock: " + e);
        }

        // Execute - Read the stock from real repository
        Stoc retrievedStoc = stocRepo.findOne(99);

        // Verify - Assert the retrieved stock matches what was added
        assertNotNull(retrievedStoc, "Stock should be found in repository");
        assertEquals(99, retrievedStoc.getId(), "ID should match");
        assertEquals("Coffee", retrievedStoc.getIngredient(), "Ingredient should match");
        assertEquals(100.0, retrievedStoc.getCantitate(), "Quantity should match");
        assertEquals(20.0, retrievedStoc.getStocMinim(), "Minimum stock should match");
    }

    @Test
    @Order(4)
    @DisplayName("TC_CompleteFlow - Complete flow with Reteta integration")
    void testCompleteFlowWithReteta() {
        // Setup Phase 1: Create and add multiple stocks for a recipe
        Stoc stockCoffee = new Stoc(200, "Coffee", 150.0, 30.0);
        Stoc stockMilk = new Stoc(201, "Milk", 200.0, 50.0);
        Stoc stockSugar = new Stoc(202, "Sugar", 100.0, 20.0);

        // Add all stocks to repository
        try {
            stocService.add(stockCoffee);
            stocService.add(stockMilk);
            stocService.add(stockSugar);
        } catch (Exception e) {
            fail("Failed to add stocks: " + e);
        }

        // Setup Phase 2: Create a recipe with ingredients
        List<IngredientReteta> ingredients = new ArrayList<>();
        ingredients.add(new IngredientReteta(stockCoffee, 20.0));  // 20g coffee
        ingredients.add(new IngredientReteta(stockMilk, 150.0));   // 150ml milk
        ingredients.add(new IngredientReteta(stockSugar, 5.0));    // 5g sugar
        
        Reteta cappuccinoRecipe = new Reteta(1, "Cappuccino", ingredients);

        // Execute Phase 1: Verify all stocks are in repository
        Stoc retrievedCoffee = stocRepo.findOne(200);
        Stoc retrievedMilk = stocRepo.findOne(201);
        Stoc retrievedSugar = stocRepo.findOne(202);

        // Verify Phase 1: All stocks retrieved successfully
        assertNotNull(retrievedCoffee, "Coffee stock should exist in repository");
        assertNotNull(retrievedMilk, "Milk stock should exist in repository");
        assertNotNull(retrievedSugar, "Sugar stock should exist in repository");

        // Verify Phase 2: Stock quantities are correct
        assertEquals(150.0, retrievedCoffee.getCantitate(), "Coffee quantity should be 150");
        assertEquals(200.0, retrievedMilk.getCantitate(), "Milk quantity should be 200");
        assertEquals(100.0, retrievedSugar.getCantitate(), "Sugar quantity should be 100");

        // Verify Phase 3: Minimum stock levels are respected
        assertTrue(retrievedCoffee.getCantitate() >= retrievedCoffee.getStocMinim(), 
                   "Coffee stock should be >= minimum");
        assertTrue(retrievedMilk.getCantitate() >= retrievedMilk.getStocMinim(), 
                   "Milk stock should be >= minimum");
        assertTrue(retrievedSugar.getCantitate() >= retrievedSugar.getStocMinim(), 
                   "Sugar stock should be >= minimum");

        // Verify Phase 4: Recipe ingredients reference correct stocks
        assertEquals(3, cappuccinoRecipe.getIngredients().size(), 
                     "Recipe should have 3 ingredients");
        assertEquals("Cappuccino", cappuccinoRecipe.getNume(), 
                     "Recipe name should be Cappuccino");
    }

    @Test
    @Disabled
    void add() {
    }

    @Test
    @Disabled
    void update() {
    }

    @Test
    @Disabled
    void delete() {
    }

    @Test
    @Disabled
    void areSuficient() {
    }

    @Test
    @Disabled
    void consuma() {
    }
}