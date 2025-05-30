package app.model.spread;

/**
 * Defines the spreadsheet ADT that holds data about
 * cells and their relations to other cells
 *
 * @author Jace Hamblin
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
     */
    String getCellInstructions(String theRowColumn);

    /**
     * @param theInstructions the instructions to be provided to the cell, must begin with "=" to be evaluated as an expression,
     *                        otherwise will attempt to evaluate as a literal. If not a literal and no "=", will evaluate as 0.
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     *
     */
    void setCellInstructions(String theInstructions, String theRowColumn);
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
}
