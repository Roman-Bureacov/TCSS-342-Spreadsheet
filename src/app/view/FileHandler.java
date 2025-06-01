package app.view;

import app.model.spread.Spreadsheet;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;

public final class FileHandler {

    private static File sWorkingFile;

    private FileHandler() {
        super();
    }

    public void saveAs() {
        JFileChooser lChooser = new JFileChooser();
        //sWorkingFile = lChooser.showSaveDialog(null);
    }

    public void save(final Spreadsheet pSpreadsheet) {
        final StringBuilder lFileBuilder = new StringBuilder();
        for (int col = 0; col < pSpreadsheet.columnCount(); col++) {
            for (int row = 0; row < pSpreadsheet.rowCount(); row++) {
                final String lCellContents = pSpreadsheet.getCellInstructions(row, col);
                if (lCellContents == null) {
                    lFileBuilder.append(",");
                } else {
                    lFileBuilder.append(lCellContents).append(",");
                }
                lFileBuilder.deleteCharAt(lFileBuilder.length() - 1); // remove trailing comma
                lFileBuilder.append("\n");
            }
        }
    }

    public Spreadsheet open() {
        return null;
    }
}
