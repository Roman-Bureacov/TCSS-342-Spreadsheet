package app.model.expr;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
    private static final Pattern CELLREF_MATCHER;
    private static final Pattern NUMBER_MATCHER;
    private static final Pattern WORD_MATCHER;

    static {
        // regex that first looks for cellref, then floating-point, then integer,
        // then operators and parentheses, then words, and finally symbols
        final String lExpRegex = "(R\\d+C\\d+|\\d+\\.\\d+|\\d+|[()+\\-*/]|\\w+|\\W)";
        EXPRESSION_MATCHER = Pattern.compile(lExpRegex);
        final String lCellRefRegex = "R\\d+C\\d+";
        CELLREF_MATCHER = Pattern.compile(lCellRefRegex);

        // checks for negative numbers that have the sign, and positive ones
        // that do not have the sign
        final String lNumberRegex = "(-\\d+\\.\\d+|-\\d+|\\d+\\.\\d+|\\d+)";
        NUMBER_MATCHER = Pattern.compile(lNumberRegex);
        WORD_MATCHER = Pattern.compile("\\w+");
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
        // trim out whitespace
        String lWorkingExpression = pExpression.toUpperCase();
        lWorkingExpression = lWorkingExpression.replaceAll("\\s", "");
        // TODO: check for bad expressions
        final Matcher lExprTokenizer = EXPRESSION_MATCHER.matcher(lWorkingExpression);
        while (lExprTokenizer.find()) {
            final String lToken = lExprTokenizer.group();
            lExpressionTokens.addLast(lToken);
        }

        return lExpressionTokens;
    }

    @Override
    public List<String> getCellRefsOf(final String pExpression) {
        final List<String> lCellRefs = new LinkedList<>();
        final Matcher lCellRefMatcher = CELLREF_MATCHER.matcher(pExpression);
        while (lCellRefMatcher.find()) lCellRefs.add(lCellRefMatcher.group());
        return lCellRefs;
    }

    @Override
    public boolean isCellRef(final String pToken) {
        return CELLREF_MATCHER.matcher(pToken).matches();
    }

    @Override
    public boolean isWord(final String pToken) {
        return WORD_MATCHER.matcher(pToken).matches();
    }

    @Override
    public boolean isNumber(final String pToken) {
        return NUMBER_MATCHER.matcher(pToken).matches();
    }


}
