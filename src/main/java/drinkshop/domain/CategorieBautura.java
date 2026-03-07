package drinkshop.domain;

import java.util.Arrays;
import java.util.List;

public class CategorieBautura {

    public static final String CLASSIC_COFFEE = "CLASSIC_COFFEE";
    public static final String MILK_COFFEE    = "MILK_COFFEE";
    public static final String SPECIAL_COFFEE = "SPECIAL_COFFEE";
    public static final String ICED_COFFEE    = "ICED_COFFEE";
    public static final String TEA            = "TEA";
    public static final String BUBBLE_TEA     = "BUBBLE_TEA";
    public static final String JUICE          = "JUICE";
    public static final String SMOOTHIE       = "SMOOTHIE";
    public static final String ALL            = "ALL";

    public static List<String> values() {
        return Arrays.asList(
                CLASSIC_COFFEE, MILK_COFFEE, SPECIAL_COFFEE, ICED_COFFEE,
                TEA, BUBBLE_TEA, JUICE, SMOOTHIE, ALL
        );
    }

    private CategorieBautura() {}
}