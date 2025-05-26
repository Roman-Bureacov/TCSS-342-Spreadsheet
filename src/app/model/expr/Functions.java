package app.model.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * class that handles carrying out functions for the expression reader.
 *
 * @author Roman Bureacov
 * @version 2025-05-25
 */
final class Functions {
    private static final Map<String, Function<Object[], Double>> FUNC = new HashMap<>();

    static {
        // all recognized functions by the applications
        FUNC.put("AVG", args -> {
            double lAvg = 0;
            final double lDenominator = args.length;
            for (final Object arg : args) lAvg += ((Double) arg) / lDenominator;
            return lAvg;
        });

    }

    private Functions() {

    }

    public static double apply(final String pFunctionName, final Object... pArgs) {
        if (!FUNC.containsKey(pFunctionName))
            throw new IllegalArgumentException("Function %s does not exist".formatted(pFunctionName));
        try {
            return FUNC.get(pFunctionName).apply(pArgs);
        } catch (final ClassCastException lCastExc) {
            throw new IllegalArgumentException("Bad argument(s) for function %s".formatted(pFunctionName));
        }
    }
}
