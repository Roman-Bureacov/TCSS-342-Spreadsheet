package app.view;

import app.model.spread.Spreadsheet;
import app.model.spread.SpreadsheetGraph;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * This is the Graphical User Interface (GUI) for the spreadsheet application.
 * It allows users to view, edit, and resize the spreadsheet, enter formulas,
 * and see results dynamically.
 *
 * @author David Norman
 * @author Roman Bureacov
 * @version Spring 2025
 */
public class SpreadsheetGUI {
    private Spreadsheet myModel;             // The spreadsheet data model
    private JTable myTable;                  // Table component displaying the spreadsheet
    private JTextField myInstructionField;  // Field for entering formulas/instructions
    private JTextField myCellField;         // Field for specifying the cell reference (e.g., R1C1)
    private SpreadsheetTableModel myTableModel; // Table model wrapping the spreadsheet data
    private JFrame myFrame;                  // Main application window
    private JScrollPane myScrollPane;       // Scroll pane containing the spreadsheet table

    /**
     * Constructor initializes the UI with given rows and columns.
     */
    public SpreadsheetGUI(int theRows, int theCols) {
        initUI(theRows, theCols);
    }

    /**
     * Sets up the UI components and event handlers.
     *
     * @param theRows Number of rows in the spreadsheet
     * @param theCols Number of columns in the spreadsheet
     */
    private void initUI(int theRows, int theCols) {
        myModel = new SpreadsheetGraph(theRows, theCols);  // Initialize spreadsheet data model
        myTableModel = new SpreadsheetTableModel();
        myTable = new JTable(myTableModel);
        myTable.setCellSelectionEnabled(true);

        // Configure custom cell editor to show formulas on edit
        this.setCellEditor();

        // Setup main application window
        myFrame = new JFrame("Spreadsheet App");
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setLayout(new BorderLayout());

        // Add spreadsheet table inside a scroll pane with row headers
        myScrollPane = new JScrollPane(myTable);
        updateRowHeader(theRows); // Setup row headers on the left
        myFrame.add(myScrollPane, BorderLayout.CENTER);

        // Create input panel for cell reference and formula entry
        JPanel inputPanel = new JPanel(new FlowLayout());
        myCellField = new JTextField("R1C1", 5);
        myInstructionField = new JTextField("=5+3", 20);
        JButton applyButton = new JButton("Apply");
        JButton resizeButton = new JButton("Resize");

        // When 'Apply' clicked, update the cell formula and refresh table
        applyButton.addActionListener(e -> {
            String cell = myCellField.getText().toUpperCase().trim();
            String formula = myInstructionField.getText().trim();
            try {
                myModel.setCellInstructions(formula, cell);
                myTableModel.fireTableDataChanged();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(myFrame, String.format("Error: %s", ex.getMessage()));
            }
        });

        // When 'Resize' clicked, open dialog to resize spreadsheet
        resizeButton.addActionListener(e -> resizeSpreadsheet());

        // Add all input controls to the input panel
        inputPanel.add(new JLabel("Cell:"));
        inputPanel.add(myCellField);
        inputPanel.add(new JLabel("Formula:"));
        inputPanel.add(myInstructionField);
        inputPanel.add(applyButton);
        inputPanel.add(resizeButton);

        myFrame.add(inputPanel, BorderLayout.SOUTH);

        // Finalize window size and make it visible
        myFrame.pack();
        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
    }

    /**
     * Sets a custom cell editor for the JTable so that
     * when a cell is edited, the formula/instruction is shown,
     * not just the evaluated value.
     */
    private void setCellEditor() {
        final JTextField lTextField = new JTextField();
        lTextField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        final DefaultCellEditor lEditor = new DefaultCellEditor(lTextField) {
            @Override
            public Component getTableCellEditorComponent(final JTable table,
                                                         final Object value,
                                                         final boolean isSelected,
                                                         final int row, final int column) {

                // Construct cell reference string like "R1C1"
                final String lCellRef = String.format("R%dC%d", row + 1, column + 1);
                String lExpression;
                // Get the formula/instruction from the model for this cell
                lExpression = myModel.getCellInstructions(lCellRef);
                if (lExpression == null) lExpression = "";

                // set the formula bar components
                myCellField.setText(lCellRef);
                myInstructionField.setText(lExpression);

                // Return editor component showing the formula string
                return super.getTableCellEditorComponent(table, lExpression, isSelected, row, column);
            }
        };

        // Set the default editor for all table cells to our custom editor
        myTable.setDefaultEditor(Object.class, lEditor);
    }

    /**
     * Opens a dialog box allowing the user to resize
     * the spreadsheet (change rows and columns).
     * Updates the model and refreshes the UI accordingly.
     */
    private void resizeSpreadsheet() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField rowsField = new JTextField(String.valueOf(myModel.rowCount()), 5);
        JTextField colsField = new JTextField(String.valueOf(myModel.columnCount()), 5);
        panel.add(new JLabel("Rows:"));
        panel.add(rowsField);
        panel.add(new JLabel("Columns:"));
        panel.add(colsField);

        int result = JOptionPane.showConfirmDialog(myFrame, panel, "Resize Spreadsheet",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int newRows = Integer.parseInt(rowsField.getText());
                int newCols = Integer.parseInt(colsField.getText());
                if (newRows <= 0 || newCols <= 0) throw new NumberFormatException();

                // Create new spreadsheet model with new size
                myModel = new SpreadsheetGraph(newRows, newCols);
                myTable.setModel(myTableModel);
                updateRowHeader(newRows);
                myTableModel.fireTableStructureChanged();
            } catch (NumberFormatException ex) {
                // Show error if user inputs invalid sizes
                JOptionPane.showMessageDialog(myFrame, "Please enter valid positive integers.");
            }
        }
    }

    /**
     * Updates the row header JList on the left of the table
     * to show row labels (R1, R2, etc.) according to row count.
     *
     * @param theRows Number of rows in the spreadsheet
     */
    private void updateRowHeader(int theRows) {
        JList<String> rowHeader = new JList<>(createRowHeaders(theRows));
        rowHeader.setFixedCellWidth(40);
        rowHeader.setFixedCellHeight(myTable.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(myTable));
        myScrollPane.setRowHeaderView(rowHeader);
    }

    /**
     * Creates an array of row header strings ("R1", "R2", etc.)
     * based on the number of rows.
     *
     * @param theRows Number of rows
     * @return Array of row header labels
     */
    private String[] createRowHeaders(int theRows) {
        String[] headers = new String[theRows];
        for (int i = 0; i < theRows; i++) {
            headers[i] = String.format("R%d", i + 1);
        }
        return headers;
    }

    /**
     * Main entry point. Launches the GUI.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new app.view.SpreadsheetGUI(10, 5));
    }

    /**
     * Table model bridging the Spreadsheet data to JTable.
     * Handles cell value retrieval, editing, and column/row counts.
     */
    private class SpreadsheetTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return myModel.rowCount();
        }

        @Override
        public int getColumnCount() {
            return myModel.columnCount();
        }

        @Override
        public Object getValueAt(int theRow, int theCol) {
            // Use formatted cell reference string
            String cell = String.format("R%dC%d", theRow + 1, theCol + 1);
            String cellInstr = myModel.getCellInstructions(cell);
            if (cellInstr == null) return "";
            else {
                if (cellInstr.startsWith("=")) return myModel.getCellValue(cell);
                else return cellInstr;
            }
        }

        @Override
        public String getColumnName(int theCol) {
            return String.format("C%d", theCol + 1);
        }

        @Override
        public boolean isCellEditable(int theRow, int theCol) {
            return true; // All cells are editable
        }

        @Override
        public void setValueAt(Object aValue, int theRow, int theCol) {
            String cellName = String.format("R%dC%d", theRow + 1, theCol + 1);
            try {
                String input = aValue.toString();
                // Set new formula/instruction in the model
                myModel.setCellInstructions(input, cellName);
                myInstructionField.setText(input);
                fireTableDataChanged(); // Refresh table view
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, String.format("Error: %s", ex.getMessage()));
            }
        }
    }

    /**
     * Renderer for row headers shown to the left of the spreadsheet.
     * Matches the style of the table header.
     */
    private static class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {
        public RowHeaderRenderer(JTable table) {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setForeground(table.getTableHeader().getForeground());
            setBackground(table.getTableHeader().getBackground());
            setFont(table.getTableHeader().getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value);
            return this;
        }
    }
}
