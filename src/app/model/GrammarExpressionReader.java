package app.model;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GrammarExpressionReader implements ExpressionReader {
    /*
    GRAMMAR
        Expression
            term
            expression "+" term
            expression "-" term
        Term
            primary
            term "*" primary
            term "/" primary
            term "%" primary
        Primary
            number
            "(" expression ")"
            function "(" args ")"
            cellref
        Function
            AVG
            ... more functions
        args
            args "," expression
            expression
        cellref
            "R" integer "C" integer
        number
            floating-point-literal
            integer
     */

    @Override
    public double evaluate(final String pExpression, final Map<String, Double> pCells)
            throws IllegalArgumentException {
        final Deque<String> lExpressionTokens = new LinkedList<>();

        return 0;
    }

    private Deque<String> tokenize(final String pExpression) throws IllegalArgumentException {
        final Deque<String> lExpressionTokens = new LinkedList<>();

        final int lExprLen = pExpression.length();
        for (int i = 0; i < lExprLen; ) {
            final char lNextChar = pExpression.charAt(i);
            final StringBuilder lToken = new StringBuilder();
            lToken.append(lNextChar);

            if (Character.isDigit(lNextChar)) {
                // insert full (possibly floating-point) number
                i++;
                insertRemaining(pExpression, lToken, i, GrammarExpressionReader::isNumberComponent);
            } else if (Character.isAlphabetic(lNextChar)) {
                if ('R' == lNextChar) {
                    // insert the cellref
                    i++;
                    insertRemaining(pExpression, lToken, i, GrammarExpressionReader::isCellRefComponent);
                    // is this actually a cell reference?
                    if (!Pattern.matches("^R[0-9]+C[0-9]+", lToken.toString())) {
                        throw new IllegalArgumentException(
                                "invalid cell reference %s".formatted(lToken.toString())
                        );
                    }
                } else // TODO: implement function comprehension
                    throw new IllegalArgumentException("Bad expression");

            } else if (isOperator(lNextChar)) {
                // insert the operator
                lExpressionTokens.addLast(lToken.toString());
            } else
                throw new IllegalArgumentException("Bad expression");
        }

        return lExpressionTokens;
    }

    private static void insertRemaining(final String pExpression,
                                        final StringBuilder pToken,
                                        int pIndex,
                                        final Predicate<Character> pCondition) {
        final int lExprLen = pExpression.length();
        while (pIndex < lExprLen) {
            final char lChar = pExpression.charAt(pIndex);
            if (pCondition.test(lChar)) pToken.append(lChar);
            else break;
            pIndex++;
        }
    }

    private static boolean isOperator(final char pChar) {
        return switch(pChar) {
            case '(', ')', '/', '*', '+', '-' -> true;
            default -> false;
        };
    }

    private static boolean isNumberComponent(final char pChar) {
        return Character.isDigit(pChar) || '.' == pChar;
    }

    private static boolean isCellRefComponent(final char pChar) {
        return Character.isDigit(pChar) || 'R' == pChar || 'C' == pChar;
    }

}
