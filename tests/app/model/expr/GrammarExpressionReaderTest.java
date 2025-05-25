package app.model.expr;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GrammarExpressionReaderTest {
    private ExpressionReader iReader;
    private Map<String, Double> iDummyCells;

    private static final String GENERIC_ERROR_MSG = "Expression did not evaluate correctly";

    @BeforeEach
    public void setup() {
        this.iReader = new GrammarExpressionReader();
        this.iDummyCells = new HashMap<>();
    }

    @Test
    public void testAddition() {
        final double lExpected = 10d;
        final String[] lExpressions = new String[] {
                "0+10", "10+0",
                "1+9",  "9+1",
                "2+8",  "8+2",
                "3+7",  "7+3",
                "4+6",  "6+4",
                "5+5"
        };

        for (final String expr : lExpressions) {
            assertEquals(
                    lExpected,
                    this.iReader.evaluate(expr, this.iDummyCells),
                    "Expression did not match target"
            );
        }
    }

    @Test
    public void testMultiplication() {
        assertAll(
                "tests to see if multiplication works",
                () -> assertEquals(
                        20d,
                        this.iReader.evaluate("10*2", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertEquals(
                        0d,
                        this.iReader.evaluate("10*0", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertEquals(
                        0d,
                        this.iReader.evaluate("1*2*3*0", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertEquals(
                        2d,
                        this.iReader.evaluate("2*1", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertEquals(
                        66d,
                        this.iReader.evaluate("2*3*11", this.iDummyCells),
                        GENERIC_ERROR_MSG
                )
        );
    }

    @Test
    public void testDivision() {
        // this test is very specific and might not work with other expression readers
        // solely due to the fact that binary division is not perfect and imprecise in computers
        assertAll(
                "Tests for division",
                () -> assertEquals(
                        3d/7d,
                        this.iReader.evaluate("3/7", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertEquals(
                        5d/7d/8d,
                        this.iReader.evaluate("5/7/8", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertEquals(
                        0d/5d,
                        this.iReader.evaluate("0/5", this.iDummyCells),
                        GENERIC_ERROR_MSG
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> this.iReader.evaluate("0/0", this.iDummyCells),
                        GENERIC_ERROR_MSG
                )
        );
    }

    @Test
    public void complexFunctionTest() {
        final Map<String, Double> lExpressions = new HashMap<>();
        lExpressions.put("5+3*11", (double) (5+3*11));
        lExpressions.put("5-4-3-2-1", (double) (5-4-3-2-1));
        lExpressions.put("50+3*(5-2)", (double) (50 + 3 * (5-2)));
        lExpressions.put("5-(4-3)-2-1", (double) (5-(4-3)-2-1));

        for (final Map.Entry<String, Double> exprPair : lExpressions.entrySet()) {
            assertEquals(
                    exprPair.getValue(),
                    this.iReader.evaluate(exprPair.getKey(), this.iDummyCells),
                    "Incorrect value for %s: %f".formatted(exprPair.getKey(), exprPair.getValue())
            );
        }
    }

}
