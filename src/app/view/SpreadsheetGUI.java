package app.view;

import app.model.spread.Spreadsheet;
import app.model.spread.SpreadsheetGraph;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * This is the Graphical User Interface (GUI) for the spreadsheet.
 * It creates the main window with the table, input fields for cell and formula,
 * and buttons to apply formulas and resize the spreadsheet.
 *
 * @author David Norman
 * @version Spring 2025
 */

public class SpreadsheetGUI {
    private Spreadsheet myModel;
    private JTable myTable;
    private JTextField myInstructionField;
    private JTextField myCellField;
    private SpreadsheetTableModel myTableModel;
    private JFrame myFrame;
    private JScrollPane myScrollPane;

    public SpreadsheetGUI(int rows, int cols) {
        initUI(rows, cols);
    }

    /**
     * Initializes the user interface components, sets up event listeners,
     * and displays the main frame with the spreadsheet table and controls.
     *
     * @param theRows the initial number of rows in the spreadsheet
     * @param theCols the initial number of columns in the spreadsheet
     */
    private void initUI(int theRows, int theCols) {
        myModel = new SpreadsheetGraph(theRows, theCols);
        myTableModel = new SpreadsheetTableModel(myModel);
        myTable = new JTable(myTableModel);
        myTable.setCellSelectionEnabled(true);

        myTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = myTable.getSelectedRow();
                int col = myTable.getSelectedColumn();
                if (row >= 0 && col >= 0) {
                    String cell = "R%dC%d".formatted(row + 1, col + 1);
                    if (e.getClickCount() == 1) {
                        // Show the formula on single click
                        String formula = null;
                        try {
                            formula = myModel.getCellInstructions(cell);
                        } catch (NullPointerException ex) {
                            // Cell likely uninitialized — do nothing
                        }
                        myCellField.setText(cell);
                        myInstructionField.setText(formula == null ? "" : formula);
                    } else if (e.getClickCount() == 2) {
                        myTable.editCellAt(row, col);
                    }
                }
            }
        });

        myFrame = new JFrame("Spreadsheet App");
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setLayout(new BorderLayout());

        // Setup table scroll with row headers
        myScrollPane = new JScrollPane(myTable);
        updateRowHeader(theRows);
        myFrame.add(myScrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        myCellField = new JTextField("R1C1", 5);
        myInstructionField = new JTextField("=5+3", 20);
        JButton applyButton = new JButton("Apply");
        JButton resizeButton = new JButton("Resize");

        // Listener for Apply button that applies the formula typed in the formula box
        applyButton.addActionListener(e -> {
            String cell = myCellField.getText().toUpperCase().trim();
            String formula = myInstructionField.getText().trim();
            try {
                myModel.setCellInstructions(formula, cell);
                myTableModel.fireTableDataChanged();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(myFrame, "Error: %s".formatted(ex.getMessage()));
            }
        });

        // Updates the selected cell with the formula from the input field when Enter is pressed.
        // Shows an error if no cell is selected or formula is invalid.
        myInstructionField.addActionListener(e -> {
            int row = myTable.getSelectedRow();
            int col = myTable.getSelectedColumn();
            if (row >= 0 && col >= 0) {
                String cell = "R%dC%d".formatted(row + 1, col + 1);
                String formula = myInstructionField.getText().trim();
                try {
                    myModel.setCellInstructions(formula, cell);
                    myTableModel.fireTableDataChanged();
                    myCellField.setText(cell);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(myFrame, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(myFrame, "Please select a cell first.");
            }
        });

        resizeButton.addActionListener(e -> resizeSpreadsheet());

        inputPanel.add(new JLabel("Cell:"));
        inputPanel.add(myCellField);
        inputPanel.add(new JLabel("Formula:"));
        inputPanel.add(myInstructionField);
        inputPanel.add(applyButton);
        inputPanel.add(resizeButton);

        myFrame.add(inputPanel, BorderLayout.SOUTH);

        myFrame.pack();
        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
    }

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
        // Once OK option is selected, the rows and columns change to numbers in the text fields provided
        if (result == JOptionPane.OK_OPTION) {
            try {
                int newRows = Integer.parseInt(rowsField.getText());
                int newCols = Integer.parseInt(colsField.getText());
                if (newRows <= 0 || newCols <= 0) throw new NumberFormatException();
                myModel = new SpreadsheetGraph(newRows, newCols);
                myTableModel.setModel(myModel);
                myTable.setModel(myTableModel);
                updateRowHeader(newRows);
                myTableModel.fireTableStructureChanged();
                // If an invalid number (x <= 0) is chosen throw an exception
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(myFrame, "Please enter valid positive integers.");
            }
        }
    }

    private void updateRowHeader(int rows) {
        JList<String> rowHeader = new JList<>(createRowHeaders(rows));
        rowHeader.setFixedCellWidth(40);
        rowHeader.setFixedCellHeight(myTable.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(myTable));
        myScrollPane.setRowHeaderView(rowHeader);
    }

    private String[] createRowHeaders(int rows) {
        String[] headers = new String[rows];
        for (int i = 0; i < rows; i++) {
            headers[i] = "R%d".formatted(i + 1);
        }
        return headers;
    }

    // Creating the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new app.view.SpreadsheetGUI(10, 5));
    }

    /**
     * This class is used to get the spreadsheets model.
     */
    private static class SpreadsheetTableModel extends AbstractTableModel {
        private Spreadsheet myModel;

        public SpreadsheetTableModel(Spreadsheet theModel) {
            this.myModel = theModel;
        }

        public void setModel(Spreadsheet newModel) {
            this.myModel = newModel;
            fireTableStructureChanged(); // Notify listeners immediately
        }

        @Override
        public int getRowCount() {
            return myModel.rowCount();
        }

        @Override
        public int getColumnCount() {
            return myModel.columnCount();
        }

        @Override
        public Object getValueAt(int row, int col) {
            String cell = "R%dC%d".formatted(row + 1, col + 1);
            String formula = null;

            try {
                formula = myModel.getCellInstructions(cell);
            } catch (Exception e) {
                // Cell might not exist yet; skip
            }

            if (formula != null && formula.startsWith("\"") && formula.endsWith("\"")) {
                return formula.substring(1, formula.length() - 1); // remove quotes for string display
            }

            double value = myModel.getCellValue(cell);
            return value == 0.0 && (formula == null || formula.isBlank()) ? "" : value;
        }

        @Override
        public String getColumnName(int theColumn) {
            return "C%d".formatted(theColumn + 1);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true; // Allow editing by clicking and typing
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            String cellName = "R%dC%d".formatted(row + 1, col + 1);
            try {
                String input = aValue.toString();
                myModel.setCellInstructions(input, cellName); // this must update the formula
                fireTableDataChanged(); // refresh display
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Error: %s".formatted(ex.getMessage()));
            }
        }
    }

    /**
     * This class is used to render all the row headers (Ex: R1, R2, ...)
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
