package app.model.expr;

import java.util.regex.Pattern;

final class TokenAnalyzer {
    private static final Pattern CELLREF_MATCHER;
    private static final Pattern NUMBER_MATCHER;
    private static final Pattern WORD_MATCHER;

    static {
        final String lCellRefRegex = "R\\d+C\\d+";
        CELLREF_MATCHER = Pattern.compile(lCellRefRegex);

        // checks for negative numbers that have the sign, and positive ones
        // that do not have the sign
        final String lNumberRegex = "(-\\d+\\.\\d+|-\\d+|\\d+\\.\\d+|\\d+)";
        NUMBER_MATCHER = Pattern.compile(lNumberRegex);
        WORD_MATCHER = Pattern.compile("\\w+");
    }

    private TokenAnalyzer() {
        super();
    }

    public static boolean isCellRef(final String pToken) {
        return CELLREF_MATCHER.matcher(pToken).matches();
    }

    public static boolean isWord(final String pToken) {
        return WORD_MATCHER.matcher(pToken).matches();
    }

    public static boolean isNumber(final String pToken) {
        return NUMBER_MATCHER.matcher(pToken).matches();
    }
}
