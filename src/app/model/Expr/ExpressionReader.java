package app.model.Expr;

import java.util.Map;

/**
 * Defines an expression reader that will parse a given string expression.
 *
 * @author Roman Bureacov
 */
public interface ExpressionReader {

    /**
     * Evaluates an expression provided by the string input
     * @param pExpression the expression, as a string, to evaluate
     * @param pCells the table of cells and their values
     * @return the value of the expression
     */
    double evaluate(String pExpression, Map<String, Double> pCells);

}
