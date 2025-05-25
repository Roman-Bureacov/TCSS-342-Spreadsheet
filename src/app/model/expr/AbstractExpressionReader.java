package app.model.expr;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractExpressionReader implements ExpressionReader {
    private static final Pattern EXPRESSION_MATCHER;
    private static final Pattern CELLREF_MATCHER;
    private static final Pattern NUMBER_MATCHER;

    static {
        // regex that searches for floating points, integers
        // expression symbols, and cell references (R#C#)
        final String lExpRegex = "(R\\d+C\\d+|\\d+\\.\\d+|\\d+|[()+\\-*/])";
        EXPRESSION_MATCHER = Pattern.compile(lExpRegex);
        final String lCellRefRegex = "R\\d+C\\d+";
        CELLREF_MATCHER = Pattern.compile(lCellRefRegex);
        final String lNumberRegex = "(\\d+\\.\\d+|\\d+)";
        NUMBER_MATCHER = Pattern.compile(lNumberRegex);
    }

    /**
     * takes in an expression and splits it up into tokens.
     * @param pExpression the expression string to tokenize
     * @return a deque containing all tokens in the overall expression
     * @throws IllegalArgumentException if there were illegal tokens
     */
    static Deque<String> tokenize(final String pExpression) throws IllegalArgumentException {
        final Deque<String> lExpressionTokens = new LinkedList<>();

        // tokenize the expression using regex
        // TODO: check for bad expressions
        final Matcher lExprTokenizer = EXPRESSION_MATCHER.matcher(pExpression);
        while (lExprTokenizer.find()) lExpressionTokens.addLast(lExprTokenizer.group());

        return lExpressionTokens;
    }

    static boolean isExpressionToken(final String pToken) {
        return EXPRESSION_MATCHER.matcher(pToken).matches();
    }

    static boolean isCellRef(final String pToken) {
        return CELLREF_MATCHER.matcher(pToken).matches();
    }

    static boolean isNumber(final String pToken) {
        return NUMBER_MATCHER.matcher(pToken).matches();
    }
}
