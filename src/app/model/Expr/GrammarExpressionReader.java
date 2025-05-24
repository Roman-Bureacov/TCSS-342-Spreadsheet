package app.model.Expr;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;
import javax.naming.OperationNotSupportedException;

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

    private Map<String, Double> iSpreadsheetCells;

    @Override
    public double evaluate(final String pExpression, final Map<String, Double> pCells)
            throws IllegalArgumentException {
        final Deque<String> lExpressionTokens = tokenize(pExpression);
        this.iSpreadsheetCells = pCells;

        return this.asExpression(lExpressionTokens);
    }

    private double asExpression(final Deque<String> pTokens) {
        final Deque<String> lRight = new LinkedList<>();

        // construct the left side as necessary
        // read the expression from right-to-left (because of subtraction)
        // TODO: might need to rethink this and just extract a single token at a time...
        final Iterator<String> lTokensIter = pTokens.descendingIterator();
        while (lTokensIter.hasNext()) {
            final String lToken = lTokensIter.next();
            lTokensIter.remove();
            switch (lToken) {
                case "+" -> {
                    return this.asExpression(pTokens) + this.asTerm(lRight);
                }
                case "-" -> {
                    return this.asExpression(pTokens) - this.asTerm(lRight);
                }
                // TODO: need to skip parentheses
                default -> lRight.addFirst(lToken);
            }
        }

        // otherwise it is a term
        return this.asTerm(lRight);
    }

    private double asTerm(final Deque<String> pTokens) {
        final Deque<String> lRight = new LinkedList<>();

        // find next operator
        final Iterator<String> lTokenIter = pTokens.descendingIterator();
        while (lTokenIter.hasNext()) {
            final String lToken = lTokenIter.next();
            lTokenIter.remove();
            switch (lToken) {
                case "*" -> {
                    return this.asTerm(pTokens) * this.asPrimary(lRight);
                }
                case "/" -> {
                    return this.asTerm(pTokens) / this.asPrimary(lRight);
                }
                default -> lRight.addFirst(lToken);
            }
        }

        // otherwise it's a primary
        return this.asPrimary(lRight);
    }

    private double asPrimary(final Deque<String> pTokens) {
        final Deque<String> lTokenString = new LinkedList<>();

        // read left-to-right
        final Iterator<String> lTokenIter = pTokens.iterator();
        while (lTokenIter.hasNext()) {
            final String lToken = lTokenIter.next();
            lTokenIter.remove();
            if (isNumber(lToken)) return Double.parseDouble(lToken);
            else if (isCellRef(lToken))
                return this.iSpreadsheetCells.getOrDefault(lToken, 0d);
            if ("(".equals(lToken)) {
                // construct expression
                if ()
            } else {
                // lastly, we know it's a function
                return this.asFunction(pTokens);
            }
        }
    }

    private double asFunction(final Deque<String> pTokens) {
        // TODO: implement function... functionality
        System.out.println("Functions are yet to be implemented!");
        return Double.MAX_VALUE;
    }

}
