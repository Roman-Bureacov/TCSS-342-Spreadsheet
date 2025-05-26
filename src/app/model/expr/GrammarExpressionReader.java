package app.model.expr;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

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
            term "%" primary        // might be too complex to implement
        Primary
            number
            cellref
            "(" expression ")"
            function "(" args ")"
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
            return this.nextExpression(lExpressionTokens);
        }
    }

    private double nextExpression(final Deque<String> pTokens) {
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
                case "/" -> {
                    final double lDenominator = this.nextPrimary(pTokens);
                    if (lDenominator == 0d) throw new IllegalArgumentException("Divide by zero");
                    else lLeftToken /= lDenominator;
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

        if (isNumber(lLeftToken)) return Double.parseDouble(lLeftToken);
        else if (isCellRef(lLeftToken))
            return this.iSpreadsheetCells.getOrDefault(lLeftToken, 0d);
        else if ("(".equals(lLeftToken)) {
            final double lExpr = this.nextExpression(pTokens);
            if (!")".equals(pTokens.removeFirst()))
                throw new IllegalArgumentException("Missing closing parenthesis");
            return lExpr;
        } else if (isWord(lLeftToken)) {
            pTokens.addFirst(lLeftToken);
            return this.nextFunction(pTokens);
        } else throw new IllegalArgumentException("Unknown symbol %s".formatted(lLeftToken));
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
