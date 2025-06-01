package app.model.spread;

/**
 * Defines the spreadsheet ADT that holds data about
 * cells and their relations to other cells
 *
 * @author Jace Hamblin
 * @author Roman Bureacov
 */
public interface Spreadsheet {

    /**
     *
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     * @return the value of the spreadsheet cell corresponding to the provided row/column.
     */
    double getCellValue(String theRowColumn);

    /**
     *
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     * @return the instructions of the spreadsheet cell corresponding to the provided row/column.
     * returns null if there is nothing at this cell.
     */
    String getCellInstructions(String theRowColumn);

    /**
     * retrieves the contents of a cell at the specified row and column
     * @param theRow the row to look at
     * @param theColumn
     * @return the instructions of the spreadsheet cell corresponding to the provided row/column.
     * Returns null if there is nothing at this cell.
     */
    String getCellInstructions(int theRow, int theColumn);

    /**
     * @param theInstructions the instructions to be provided to the cell, must begin with "=" to be evaluated as an expression,
     *                        otherwise will attempt to evaluate as a literal. If not a literal and no "=", will evaluate as 0.
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     *
     */
    void setCellInstructions(String theInstructions, String theRowColumn);

    /**
     * Sets the instruction in the specified cell using the row and column of interest
     * @param theInstructions the instruction to be provided to the cell, must begin with "=" to be evaluated
     *                        as an expression, otherwise will attempt to evaluate as a literal. If not a literal
     *                        and no "=", it will evaluate as 0.
     * @param theRow the row to insert the instruction
     * @param theColumn the column to insert the instruction
     */
    void setCellInstructions(String theInstructions, int theRow, int theColumn);

    /**
     *
     * @return the amount of rows in the spreadsheet.
     */
    int rowCount();

    /**
     *
     * @return the amount of columns in the spreadsheet.
     */
    int columnCount();

    /**
     *
     * @return the total size of the spreadsheet by count of cells.
     */
    int size();

    /**
     * Translates a row and column into a cellref "R#C#" (where # is a positive integer) format.
     * @param pRow the row of interest
     * @param pCol the column of interest
     * @return a formatted cellref
     */
    String toCellRef(int pRow, int pCol);
}
