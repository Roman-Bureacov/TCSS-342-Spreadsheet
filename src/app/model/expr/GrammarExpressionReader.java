package app.model.expr;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An expression reader than reads in expressions as a grammar.
 *
 * @author Roman Bureacov
 * @version 2025-05-24
 */
public final class GrammarExpressionReader extends AbstractExpressionReader {
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
            cellref
            "(" expression ")"
            function "(" args ")"
            primary "^" primary
        Function
            AVG
            ... more function names
        args
            expression
            args "," expression
        cellref
            "R" integer "C" integer
        number
            floating-point-literal
     */

    private Map<String, Double> iSpreadsheetCells;

    @Override
    public double evaluate(final String pExpression, final Map<String, Double> pCells)
            throws IllegalArgumentException {
        final Deque<String> lExpressionTokens = tokenize(pExpression);
        if (lExpressionTokens.isEmpty()) return 0d;
        else {
            this.iSpreadsheetCells = pCells;
            final double lResult;
            try {
                lResult = this.nextExpression(lExpressionTokens);
            } catch (final NoSuchElementException lException) { // if unexpectedly ran out of tokens
                throw new IllegalArgumentException(
                        "Insufficient tokens in expression \"%s\"".formatted(pExpression)
                );
            }
            if (!lExpressionTokens.isEmpty()) throw new IllegalArgumentException(
                    "Bad expression \"%s\"".formatted(pExpression)
            );

            return lResult;
        }
    }

    private double nextExpression(final Deque<String> pTokens) {
        // append a zero to achieve the desired leading unary minus effect
        if ("-".equals(pTokens.peekFirst())) {
            pTokens.addFirst("0");
        }

        double lLeftToken = this.nextTerm(pTokens);

        while (!pTokens.isEmpty()) {
            final String lNextToken = pTokens.removeFirst();
            switch (lNextToken) {
                case "+" -> lLeftToken += this.nextTerm(pTokens);
                case "-" -> lLeftToken -= this.nextTerm(pTokens);
                default -> {
                    pTokens.addFirst(lNextToken);
                    return lLeftToken;
                }
            }
        }

        return lLeftToken;
    }

    private double nextTerm(final Deque<String> pTokens) {
        double lLeftToken = this.nextPrimary(pTokens);

        while (!pTokens.isEmpty()) {
            final String lNextToken = pTokens.removeFirst();
            switch (lNextToken) {
                case "*" -> lLeftToken *= this.nextPrimary(pTokens);
                case "/", "%" -> {
                    final double lDenominator = this.nextPrimary(pTokens);
                    if (lDenominator == 0d) throw new IllegalArgumentException("Divide by zero");
                    else {
                        if ("/".equals(lNextToken)) lLeftToken /= lDenominator;
                        else lLeftToken %= lDenominator;
                    }
                }
                default -> {
                    pTokens.addFirst(lNextToken);
                    return lLeftToken;
                }
            }
        }

        return lLeftToken;
    }

    private double nextPrimary(final Deque<String> pTokens) {
        final String lLeftToken = pTokens.removeFirst();
        final Double lLeftValue;

        if (this.isNumber(lLeftToken)) lLeftValue = Double.parseDouble(lLeftToken);
        else if (this.isCellRef(lLeftToken))
            lLeftValue = this.iSpreadsheetCells.getOrDefault(lLeftToken, 0d);
        else if ("(".equals(lLeftToken)) {
            final double lExpr = this.nextExpression(pTokens);
            if (!")".equals(pTokens.peekFirst()))
                throw new IllegalArgumentException("Missing closing parenthesis");
            else {
                pTokens.removeFirst();
                lLeftValue = lExpr;
            }
        } else if (this.isWord(lLeftToken)) {
            pTokens.addFirst(lLeftToken);
            lLeftValue = this.nextFunction(pTokens);
        } else throw new IllegalArgumentException(
                "Unexpected symbol \"%s\" in place of primary".formatted(lLeftToken)
        );

        if ("^".equals(pTokens.peekFirst())) {
            pTokens.removeFirst();
            return Math.pow(lLeftValue, this.nextPrimary(pTokens));
        } else return lLeftValue;
    }

    private double nextFunction(final Deque<String> pTokens) {
        final String lLeftToken = pTokens.removeFirst();
        if (pTokens.isEmpty())
            throw new IllegalArgumentException("Function opening parenthesis expected");
        if (!"(".equals(pTokens.removeFirst()))
            throw new IllegalArgumentException("missing opening parenthesis");
        return Functions.apply(lLeftToken, this.nextArgs(pTokens));
    }

    private Object[] nextArgs(final Deque<String> pTokens) {
        final Deque<Double> lArgs = new LinkedList<>();

        if (")".equals(pTokens.peekFirst())) return lArgs.toArray();
        else {
            lArgs.addLast(this.nextExpression(pTokens));
            while (!pTokens.isEmpty()) {
                final String lLeftToken = pTokens.removeFirst();
                if (",".equals(lLeftToken)) lArgs.addLast(this.nextExpression(pTokens));
                else if (")".equals(lLeftToken)) return lArgs.toArray();
            }
        }
        throw new IllegalArgumentException("Missing closing parenthesis");
    }

}
