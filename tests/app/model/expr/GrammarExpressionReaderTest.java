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
    private Map<String, Double> iTestExpressions;

    private static final String GENERIC_ERROR_MSG = "Expression did not evaluate correctly";

    @BeforeEach
    public void setup() {
        this.iReader = new GrammarExpressionReader();
        this.iDummyCells = new HashMap<>();
        this.iTestExpressions = new HashMap<>();
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
    public void testSubtraction() {
        this.iTestExpressions.put("5-3", 5d-3d);
        this.iTestExpressions.put("5-4-3", 5d-4d-3d);
        this.iTestExpressions.put("5-4-3-2-1", 5d-4d-3d-2d-1d);

        this.basicTests(this.iTestExpressions);
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
        this.iTestExpressions.put("5+3*11", (double) (5+3*11));
        this.iTestExpressions.put("5-4-3-2-1", (double) (5-4-3-2-1));
        this.iTestExpressions.put("50+3*(5-2)", (double) (50 + 3 * (5-2)));
        this.iTestExpressions.put("5-(4-3)-2-1", (double) (5-(4-3)-2-1));

        this.basicTests(this.iTestExpressions);
    }

    @Test
    public void floatTests() {
        this.iTestExpressions.put("5.5*3", 5.5d*3d);
        this.iTestExpressions.put("1.001*100", 1.001d*100d);
        this.iTestExpressions.put("2.1/1", 2.1d/1d);
        this.iTestExpressions.put("5/2.5", 5d/2.5d);
        this.iTestExpressions.put("5.5-2.5", 5.5d-2.5d);
        this.iTestExpressions.put("5.0-(3-3.1)", 5.0d-(3d-3.1d));

        this.basicTests(this.iTestExpressions);
    }

    @Test
    public void cellRefTest() {
        final double lR1C1 = 3d;
        final double lR22C35 = 6.5d;
        final double lR2C3 = 0d;

        this.iDummyCells.put("R1C1", lR1C1);
        this.iDummyCells.put("R22C35", lR22C35);
        this.iDummyCells.put("R2C3", lR2C3);

        this.iTestExpressions.put("R1C1+3", lR1C1 + 3d);
        this.iTestExpressions.put("R22C35/3", lR22C35/3d);
        this.iTestExpressions.put("R1C1*R22C35", lR1C1*lR22C35);
        this.iTestExpressions.put("R999C999", 0d); // test for non-existent cell

        this.basicTests(this.iTestExpressions, this.iDummyCells);

        assertThrows(
                IllegalArgumentException.class,
                () -> this.iReader.evaluate("3/R2C3", this.iDummyCells),
                "cell ref expression with divide zero did not return exception"
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> this.iReader.evaluate("RC", this.iDummyCells),
                "expression reader did not throw illegal argument exception"
        );
    }

    @Test
    public void badExpressionTest() { // TODO: run these tests
        final String[] lBadExpressions = {
                "33(55)",
                "2++",
                "2-=5",
                "3..5+21",
                "((3+1)",
                "100)-5",
                "3*/5",
                "5+-8",
                "5++1",
                "5--1",
                "R1C*0",
                "5+3*-5",
                "+",
                "-",
                "#",
                "3$5",
                "hello world",
                "5+3-AVG(5,3",
                "-5+-5",
        };

        for (final String expression : lBadExpressions) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> this.iReader.evaluate(expression, this.iDummyCells),
                    "No exception or wrong exception thrown for expression %s".formatted(expression)
            );
        }
    }

    @Test
    public void leadingSignTest() {
        this.iTestExpressions.put("-5+1", -5d+1d);
        this.iTestExpressions.put("5+3/(-5)", 5d+3d/(-5d));
        this.iTestExpressions.put("5+3/(5)", 5d+3d/(5d));
        this.iTestExpressions.put(
                "5-(-3+AVG(-3,-3,-2))",
                5d-(-3d+Functions.apply("AVG", -3d,-3d,-2d)));

        assertThrows(
                IllegalArgumentException.class,
                () -> this.iReader.evaluate("+2-100", this.iDummyCells),
                "Did not throw IllegalArgumentException for leading + sign"
        );

        this.basicTests(this.iTestExpressions);
    }

    @Test
    public void functionsTest() {
        this.iTestExpressions.put(
                "5+AVG(3,4,5)",
                5d + Functions.apply("AVG", 3d, 4d, 5d)
        );
        this.iTestExpressions.put(
                "AVG(5,2,999,2.5)",
                Functions.apply("AVG", 5d, 2d, 999d, 2.5d)
        );
        this.iTestExpressions.put(
                "5+AVG(3+4,5*6,7/8)",
                5d + Functions.apply("AVG", 3d+4d, 5d*6d, 7d/8d)
        );

        this.basicTests(this.iTestExpressions);
    }

    /**
     * tests all expressions in the map
     * @param pExpressions a string-double pair of the expression as a string and the expected value
     */
    private void basicTests(final Map<String, Double> pExpressions) {
        this.basicTests(pExpressions, this.iDummyCells);
    }

    /**
     * tests all expressions in the map
     * @param pExpressions a string-double pair of the expression as a string and the expected value
     * @param pCells the dummy map of the cells and their values
     */
    private void basicTests(final Map<String, Double> pExpressions, final Map<String, Double> pCells) {
        for (final Map.Entry<String, Double> exprPair : pExpressions.entrySet()) {
            assertEquals(
                    exprPair.getValue(),
                    this.iReader.evaluate(exprPair.getKey(), pCells),
                    "Incorrect value for %s: %f".formatted(exprPair.getKey(), exprPair.getValue())
            );
        }
    }

}
