package drinkshop.it.service.td.depthfirst;

import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileStocRepository;
import drinkshop.service.StocService;
import drinkshop.service.validator.StocValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StocServiceIntTest {
    private Stoc stoc;
    private StocValidator stocValidator; // REAL
    private Repository<Integer, Stoc> stocRepo;

    private StocService stocService;

    @BeforeEach
    void setUp() {
        stocValidator = new StocValidator();
        stocRepo = new FileStocRepository("data/stocuri.txt"); // integram al doilea branch (top down depth first)
        stoc = null;
        stocService = new StocService(stocRepo);
    }

    @Test
    @Order(1)
    void testAddValid_withRealRepo() {
        Stoc stoc = new Stoc(100, "Apa", 5.0, 1.0);

        //apelam metoda add si evaluam apelul cu fail
        try{
            stocService.add(stoc);
        }catch (Exception e){
            fail("Invalid add operation " + e);
        }

        // Verify the stock was added
        Stoc retrievedStoc = stocRepo.findOne(100);
        assertNotNull(retrievedStoc, "Stock should be found in repository after adding");
        assertEquals("Apa", retrievedStoc.getIngredient());
        assertEquals(5.0, retrievedStoc.getCantitate());
        assertEquals(1.0, retrievedStoc.getStocMinim());
    }

    @Test
    @Order(2)
    void testAddInvalid_withRealRepo() {
        Stoc stoc = new Stoc(-1, "Apa", 5.0, 10.0);

        //apelam metoda si evaluam invalidarea obiectului
        Assertions.assertThrows(ValidationException.class, () -> {
            stocService.add(stoc);
        });

        // Verify invalid stock was not added
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