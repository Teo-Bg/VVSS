package drinkshop.domain;

import java.util.Arrays;
import java.util.List;

public class TipBautura {

    public static final String BASIC         = "BASIC";
    public static final String DAIRY         = "DAIRY";
    public static final String LACTOSE_FREE  = "LACTOSE_FREE";
    public static final String WATER_BASED   = "WATER_BASED";
    public static final String PLANT_BASED   = "PLANT_BASED";
    public static final String POWDER        = "POWDER";
    public static final String ALL           = "ALL";

    public static List<String> values() {
        return Arrays.asList(
                BASIC, DAIRY, LACTOSE_FREE, WATER_BASED, PLANT_BASED, POWDER, ALL
        );
    }

    private TipBautura() {}
}