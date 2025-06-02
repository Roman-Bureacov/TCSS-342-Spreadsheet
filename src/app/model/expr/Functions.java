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
        // all functions must be capitalized and only have alpha characters, otherwise
        // they will never be discovered by the expression reader
        FUNC.put("AVERAGE", args -> {
            double lAvg = 0;
            final double lDenominator = args.length;
            for (final Object arg : args) lAvg += ((Double) arg) / lDenominator;
            return lAvg;
        });
        FUNC.put("AVG", args -> FUNC.get("AVERAGE").apply(args)); // synonym
        FUNC.put("PRODUCT", args -> {
            double lProduct = 1;
            for (final Object arg : args) lProduct *= (Double) arg;
            return lProduct;
        });
        FUNC.put("PROD", args -> FUNC.get("PRODUCT").apply(args)); // synonym
        FUNC.put("SUM", args -> {
            double lSum = 0;
            for (final Object arg : args) lSum += (Double) arg;
            return lSum;
        });
    }

    private Functions() {
        super();
    }

    public static double apply(final String pFunctionName, final Object... pArgs) {
        try {
            return FUNC.get(pFunctionName).apply(pArgs);
        } catch (final ClassCastException lCastExc) {
            throw new IllegalArgumentException("Bad argument(s) for function %s".formatted(pFunctionName));
        } catch (final NullPointerException lNullPtrExc) {
            throw new IllegalArgumentException("Function %s does not exist".formatted(pFunctionName));
        }
    }

    public static boolean functionExists(final String pFunctionName) {
        return FUNC.containsKey(pFunctionName);
    }
}
