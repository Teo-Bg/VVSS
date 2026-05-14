package drinkshop.it.service.td.breadthfirst;

import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileStocRepository;
import drinkshop.service.StocService;
import drinkshop.service.validator.StocValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

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