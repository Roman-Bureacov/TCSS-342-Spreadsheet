package app.model.expr;

import java.util.List;
import java.util.Map;

/**
 * Defines an expression reader that will parse a given string expression.
 *
 * @author Roman Bureacov
 * @version 2025-05-24
 */
public interface ExpressionReader {

    /**
     * Evaluates an expression provided by the string input, such as "3+4-(-5)"
     * @param pExpression the expression, as a string, to evaluate
     * @param pCells the table of cells and their values
     * @return the value of the expression
     * @throws IllegalArgumentException when an expression is either invalid
     * or produces a bad result (such as division by zero)
     */
    double evaluate(String pExpression, Map<String, Double> pCells) throws IllegalArgumentException;

    /**
     * Returns a list of the cell references used in the expression
     * @param pExpression the expression to look in
     * @return a list of cell references used in the argument expression
     */
    List<String> getCellRefsOf(String pExpression);

    /**
     * Returns if the token provided is a cell reference
     * @param pToken the token to evaluate
     * @return if the token is of the format "R#C#" where "#" is any positive integer
     */
    boolean isCellRef(String pToken);

    /**
     * Returns if the token is a word
     * @param pToken the token to evaluate
     * @return if the token is a word with only alpha characters
     */
    boolean isWord(String pToken);

    /**
     * Returns if the token is a number
     * @param pToken the token to evaluate
     * @return if the token is floating-point or integer, positive or negative
     */
    boolean isNumber(String pToken);
}
