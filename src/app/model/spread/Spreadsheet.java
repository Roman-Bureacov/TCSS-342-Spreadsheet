package app.model.spread;

/**
 * Defines the spreadsheet ADT that holds data about
 * cells and their relations to other cells
 *
 * @author Jace Hamblin
 */
public interface Spreadsheet {

    /**
     * Gets a cell using its row and column signature: "R#C#"
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     * @return the spreadsheet cell corresponding to the provided row/column
     */
    public SpreadsheetCell getCell(String theRowColumn);

    /**
     *
     * @return the amount of rows in the spreadsheet
     */
    public int rowCount();

    /**
     *
     * @return the amount of columns in the spreadsheet
     */
    public int columnCount();

    /**
     *
     * @return the total size of the spreadsheet by count of cells
     */
    public int size();

    /**
     * Updates all cell values to represent what their instructions evaluate to
     */
    public void evaluateInstructions();
}
