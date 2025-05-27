package app.model.expr;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class that handles the housekeeping, primarily to tokenize the expressions
 * the reader implementation receives.
 *
 * @author Roman Bureacov
 * @version 2025-05-24
 */
abstract class AbstractExpressionReader implements ExpressionReader {
    private static final Pattern EXPRESSION_MATCHER;

    static {
        // regex that first looks for cellref, then floating-point, then integer,
        // then operators and parentheses, then words, and finally symbols
        final String lExpRegex = "(R\\d+C\\d+|\\d+\\.\\d+|\\d+|[()+\\-*/]|\\w+|\\W)";
        EXPRESSION_MATCHER = Pattern.compile(lExpRegex);
    }

    /**
     * takes in an expression and splits it up into tokens.
     * @param pExpression the expression string to tokenize
     * @return a deque containing all tokens in the overall expression
     * @throws IllegalArgumentException if there were illegal tokens
     */
    static Deque<String> tokenize(final String pExpression) throws IllegalArgumentException {
        final String lWorkingExpression = pExpression.toUpperCase();
        final Deque<String> lExpressionTokens = new LinkedList<>();

        // tokenize the expression using regex
        // TODO: check for bad expressions
        final Matcher lExprTokenizer = EXPRESSION_MATCHER.matcher(lWorkingExpression);
        while (lExprTokenizer.find()) {
            final String lToken = lExprTokenizer.group();
            lExpressionTokens.addLast(lToken);
        }

        return lExpressionTokens;
    }



}
