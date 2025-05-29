package app.view;

import app.model.expr.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * This is the Graphical User Interface (GUI) for the spreadsheet.
 * @author David Norman
 * @version Spring 2025
 */
public class SpreadsheetGUI {
    private JTable myTable;
    private JTextField myFormulaField;
    private JLabel myStatusLabel;

    private int rows = 5;
    private int cols = 5;

    public SpreadsheetGUI() {
        initUI();
    }

    private void initUI() {
        JFrame myFrame = new JFrame("Spreadsheet App");
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setLayout(new BorderLayout());

        // Table setup
        DefaultTableModel model = new DefaultTableModel(rows, cols);
        myTable = new JTable(model);
        myTable.setCellSelectionEnabled(true);

        JScrollPane scrollPane = new JScrollPane(myTable);

        // Add row numbers on the side
        RowNumberTable rowNumberTable = new RowNumberTable(myTable);
        scrollPane.setRowHeaderView(rowNumberTable);

        // Formula panel setup
        JPanel formulaPanel = new JPanel(new BorderLayout());
        myFormulaField = new JTextField();
        JButton setButton = new JButton("Set");
        JButton evalButton = new JButton("Evaluate");
        JButton resizeButton = new JButton("Resize");

        formulaPanel.add(new JLabel("Formula:"), BorderLayout.WEST);
        formulaPanel.add(myFormulaField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(setButton);
        buttonPanel.add(evalButton);
        buttonPanel.add(resizeButton);
        formulaPanel.add(buttonPanel, BorderLayout.EAST);

        // Status label
        myStatusLabel = new JLabel(" ");

        // Add components to frame
        myFrame.add(formulaPanel, BorderLayout.NORTH);
        myFrame.add(scrollPane, BorderLayout.CENTER);
        myFrame.add(myStatusLabel, BorderLayout.SOUTH);

        // Button listeners
        setButton.addActionListener(this::handleSet);
        evalButton.addActionListener(this::handleEvaluate);
        resizeButton.addActionListener(this::handleResize);

        myFrame.setSize(800, 600);
        myFrame.setVisible(true);
    }

    // Handler examples (you can customize these)
    private void handleSet(ActionEvent e) {
        int row = myTable.getSelectedRow();
        int col = myTable.getSelectedColumn();
        if (row == -1 || col == -1) {
            myStatusLabel.setText("Please select a cell.");
            return;
        }
        String formula = myFormulaField.getText();
        myTable.setValueAt(formula, row, col);
        myStatusLabel.setText("Set formula in cell " + cellName(row, col));
    }

    private void handleEvaluate(ActionEvent e) {
        ExpressionReader evaluator = new GrammarExpressionReader();
        Map<String, Double> cellValues = new java.util.HashMap<>();

        // First pass: extract all literal numeric values from the table into the map
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Object val = myTable.getValueAt(row, col);
                String cellName = cellName(row, col);
                if (val instanceof String str && !str.startsWith("=")) {
                    try {
                        double num = Double.parseDouble(str);
                        cellValues.put(cellName, num);
                    } catch (NumberFormatException ex) {
                        cellValues.put(cellName, 0.0);  // Treat non-numeric literals as zero
                    }
                } else if (val instanceof Number n) {
                    cellValues.put(cellName, n.doubleValue());
                }
            }
        }

        // Second pass: evaluate formulas
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Object val = myTable.getValueAt(row, col);
                if (val instanceof String str && str.startsWith("=")) {
                    try {
                        double result = evaluator.evaluate(str.substring(1), cellValues);
                        myTable.setValueAt(result, row, col);
                        cellValues.put(cellName(row, col), result); // Update map for future references
                    } catch (Exception ex) {
                        myStatusLabel.setText("Error in " + cellName(row, col) + ": " + ex.getMessage());
                    }
                }
            }
        }

        myStatusLabel.setText("Evaluation complete.");
    }

    private void handleResize(ActionEvent e) {
        // Example: resize to current rows and cols entered in formula field, format "rows,cols"
        String input = myFormulaField.getText();
        try {
            String[] parts = input.split(",");
            int newRows = Integer.parseInt(parts[0].trim());
            int newCols = Integer.parseInt(parts[1].trim());
            resizeTable(newRows, newCols);
            myStatusLabel.setText("Resized to " + newRows + " rows and " + newCols + " columns.");
        } catch (Exception ex) {
            myStatusLabel.setText("Invalid resize input. Use format: rows,cols");
        }
    }

    private void resizeTable(int theRows, int theCols) {
        DefaultTableModel model = (DefaultTableModel) myTable.getModel();
        model.setRowCount(theRows);
        model.setColumnCount(theCols);
        rows = theRows;
        cols = theCols;

        // Notify the row number panel to update & repaint
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, myTable);
        if (scrollPane != null) {
            Component rowHeader = scrollPane.getRowHeader().getView();
            if (rowHeader != null) {
                rowHeader.revalidate();
                rowHeader.repaint();
            }
        }
    }

    private String cellName(int row, int col) {
        return "R" + (row + 1) + "C" + (col + 1);
    }

    // Inner class to paint row numbers
    static class RowNumberTable extends JPanel {
        private final JTable table;

        public RowNumberTable(JTable table) {
            this.table = table;
            setPreferredSize(new Dimension(40, 0));
            setBackground(Color.LIGHT_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Fill background
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            // Text setup
            g.setColor(Color.BLACK);
            g.setFont(table.getFont());

            int firstVisibleRow = table.rowAtPoint(new Point(0, 0));
            int lastVisibleRow = table.rowAtPoint(new Point(0, getHeight()));

            if (firstVisibleRow == -1) {
                firstVisibleRow = 0;
            }
            if (lastVisibleRow == -1) {
                lastVisibleRow = table.getRowCount() - 1;
            }

            for (int row = firstVisibleRow; row <= lastVisibleRow; row++) {
                Rectangle rect = table.getCellRect(row, 0, true);
                String rowNum = Integer.toString(row + 1);
                int y = rect.y + rect.height - 6;  // vertical position

                // Right-align the numbers with some padding
                FontMetrics fm = g.getFontMetrics();
                int x = getWidth() - fm.stringWidth(rowNum) - 5;

                g.drawString(rowNum, x, y);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpreadsheetGUI::new);
    }
}
