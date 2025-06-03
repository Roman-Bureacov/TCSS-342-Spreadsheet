package app.model.spread;

import java.util.regex.Pattern;

/**
 * Class representing a single spreadsheet cell
 * @author Jace Hamblin
 */
public class SpreadsheetCell {
    private Double value;
    private String instruction;

    public SpreadsheetCell() {
        value = null;
        instruction = "";
    }

    /**
     * Sets the value of the cell to theValue
     * @param theValue value cell will be set to
     */
    public void setValue(Double theValue) {
        value = theValue;
    }

    /**
     * Gets the value of the cell
     * @return the value of the cell
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the equation for the cell
     * @param theInstruction the equation to be inputted, as a string
     */
    public void setInstruction(String theInstruction) {
        instruction = theInstruction;
    }

    /**
     * Gets the equation for the cell
     * @return the equation for the cell
     */
    public String getInstruction() {
        return instruction;
    }
}
