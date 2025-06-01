package app.model.expr;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the grammar expression reader specifically,
 * but may be applied to other expression readers if necessary.
 *
 * @author Roman Bureacov
 * @version 2025-05
 */
public class GrammarExpressionReaderTest {
    private ExpressionReader iReader;
    private Map<String, Double> iDummyCells; // (cellref, valueAtLocation)
    private Map<String, Double> iTestExpressions; // (expression, expectedValue)

    private static final String GENERIC_ERROR_MSG = "Expression did not evaluate correctly";

    /**
     * Sets up the testing environment
     */
    @BeforeEach
    public void setup() {
        this.iReader = new GrammarExpressionReader();
        this.iDummyCells = new HashMap<>();
        this.iTestExpressions = new HashMap<>();
    }

    /**
     * Tests if addition works as intended, including a whitespace test
     */
    @Test
    public void testAddition() {
        final double lExpected = 10d;
        final String[] lExpressions = new String[] {
                "0+10", "10+0",
                "1+9",  "9+1",
                "2+8",  "8+2",
                "3+7",  "7+3",
                "4+6",  "6+4",
                "5+5", "5      +\n5"
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

    /**
     * Tests if multiplication works as intended
     */
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

    /**
     * Tests for division, including if an exception is thrown on divide-by-zero
     */
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

    /**
     * Tests the general precedence to see if it applies
     */
    @Test
    public void complexExpressionsTest() {
        this.iTestExpressions.put("5+3*11", (double) (5+3*11));
        this.iTestExpressions.put("5-4-3-2-1", (double) (5-4-3-2-1));
        this.iTestExpressions.put("50+3*(5-2)", (double) (50 + 3 * (5-2)));
        this.iTestExpressions.put("5-(4-3)-2-1", (double) (5-(4-3)-2-1));
        this.iTestExpressions.put("5/4-3/2-1", 5d/4d-3d/2d-1);

        this.basicTests(this.iTestExpressions);
    }

    /**
     * Tests if the expression reader is able to recognize floating-point numbers
     */
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

    /**
     * Tests if the expression reader is able to digest cellrefs
     */
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

    /**
     * Tests if all trule terrible expressions truly do cause exceptions
     */
    @Test
    public void badExpressionTest() {
        final String[] lBadExpressions = {
                "33(55)",
                "33)55",
                "33,55",
                "33((55)",
                "33(55))",
                "33 55",
                "33 55)",
                "33*55)",
                "(33*55",
                "2++",
                "++2",
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
                "hello",
                "5+3-AVG(5,3",
                "5AVG(5,3)",
                "-5+-5",
        };

        for (final String expression : lBadExpressions) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> this.iReader.evaluate(expression, this.iDummyCells),
                    "Expected IllegalArgumentException for expression %s".formatted(expression)
            );
        }
    }

    /**
     * Test if the exponentiation works as expected
     */
    @Test
    public void exponentTest() {
        this.iTestExpressions.put("2^2", Math.pow(2d, 2d));
        this.iTestExpressions.put("2^(2)", Math.pow(2d, 2d));
        this.iTestExpressions.put("30^(-18)", Math.pow(30d, -18d));
        this.iTestExpressions.put("(5+1)^3", Math.pow(5d+1d, 3d));
        this.iTestExpressions.put("3^(5+1)", Math.pow(3d, 5d+1d));
        this.iTestExpressions.put("3*3^7", 3d * Math.pow(3d, 7d));
        this.iTestExpressions.put("3/3^7", 3d / Math.pow(3d, 7d));
        this.iTestExpressions.put("3+3^7", 3d + Math.pow(3d, 7d));
        this.iTestExpressions.put("3+3^7-900", 3d + Math.pow(3d, 7d) - 900d);
        this.iTestExpressions.put(
                "5^AVG(3.15, 4)",
                Math.pow(5d, Functions.apply("AVG", 3.15d, 4d))
        );
        this.iTestExpressions.put("3^3^3", Math.pow(3d, Math.pow(3d, 3d)));
        this.iTestExpressions.put("3^(3^3)", Math.pow(3d, Math.pow(3d, 3d)));
        this.iTestExpressions.put("(3^3)^3", Math.pow(Math.pow(3d, 3d), 3d));


        this.runTestsOnExpressions();
    }

    /**
     * Tests for unary minus (which only appears as a leading minus sign)
     */
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
        this.iTestExpressions.put("-3^9", Math.pow(-3d, 9d));
        this.iTestExpressions.put("-3^(-9)", Math.pow(-3d, -9d));

        this.basicTests(this.iTestExpressions);
    }

    /**
     * Tests if the modulus operator works as expected
     */
    @Test
    public void modulusTest() {
        this.iTestExpressions.put("5%3", 5d%3d);
        this.iTestExpressions.put("3.33%3", 3.33d%3d);
        this.iTestExpressions.put("3.125%3.2", 3.125d%3.2d);
        this.iTestExpressions.put("-3.125%3.2", -3.125d%3.2d);
        this.iTestExpressions.put("3.125%(-3.2)", 3.125d%(-3.2d));

        assertThrows(
                IllegalArgumentException.class,
                () -> this.iReader.evaluate("3%0", this.iDummyCells),
                "Illegal Argument exception was not thrown for mod 0"
        );

        this.basicTests(this.iTestExpressions);
    }

    /**
     * Tests if the functions work as expected
     */
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
        this.iTestExpressions.put( // synonym test
                "AVERAGE(5,2,999,2.5)",
                Functions.apply("AVERAGE", 5d, 2d, 999d, 2.5d)
        );
        this.iTestExpressions.put(
                "5+AVG(3+4,5*6,7/8)",
                5d + Functions.apply("AVG", 3d+4d, 5d*6d, 7d/8d)
        );
        this.iTestExpressions.put(
                "SUM(5,3,8)",
                Functions.apply("SUM", 5d, 3d, 8d)
        );
        this.iTestExpressions.put(
                "PROD(5,3,7)",
                Functions.apply("PROD", 5d, 3d, 7d)
        );
        this.iTestExpressions.put( // synonym test
                "PRODUCT(5,3,7)",
                Functions.apply("PRODUCT", 5d, 3d, 7d)
        );

        this.basicTests(this.iTestExpressions);
    }

    /**
     * Tests if functions can be nested as expected
     */
    @Test
    public void nestedFunctionTest() {
        this.iTestExpressions.put(
                "AVG(AVG(5,3))",
                Functions.apply("AVG", Functions.apply("AVG", 5d, 3d))
        );
        this.iTestExpressions.put(
                "AVG(AVG(5,3), 9)",
                Functions.apply("AVG", Functions.apply("AVG", 5d, 3d), 9d)
        );

        this.runTestsOnExpressions();
    }

    /**
     * Tests if the expression reader can fetch the cellrefs out of an expression correctly
     */
    @Test
    public void getCellRefTest() {
        final List<String> lTestExpression1 = this.iReader.getCellRefsOf("5+R1C3");
        final List<String> lTestExpression2 = this.iReader.getCellRefsOf("5+R1C3-R33C1");
        final List<String> lTestExpression3 = this.iReader.getCellRefsOf("5+R1C3-R6C2*R1C2");

        assertAll(
                "Test for correct evaluation of how many cell references there are",
                () -> assertEquals(1, lTestExpression1.size()),
                () -> assertEquals(2, lTestExpression2.size()),
                () -> assertEquals(3, lTestExpression3.size())
        );

        assertAll(
                "Test for if the correct cell references were returned",
                () -> assertEquals("R1C3", lTestExpression1.getFirst()),
                () -> assertEquals("R1C3", lTestExpression2.get(0)),
                () -> assertEquals("R33C1", lTestExpression2.get(1)),
                () -> assertEquals("R1C3", lTestExpression3.get(0)),
                () -> assertEquals("R6C2", lTestExpression3.get(1)),
                () -> assertEquals("R1C2", lTestExpression3.get(2))
        );
    }

    /**
     * runs tests on all the expression in the map stored in this instance
     */
    private void runTestsOnExpressions() {
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
