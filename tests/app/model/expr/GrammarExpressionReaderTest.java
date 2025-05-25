package app.model.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GrammarExpressionReaderTest {
    ExpressionReader iReader;
    Map<String, Double> iDummyCells;

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

}
