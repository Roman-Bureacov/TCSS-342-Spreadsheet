package app.model.spread;

import app.model.expr.ExpressionReader;
import app.model.expr.GrammarExpressionReader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Iterator;


/**
 * Spreadsheet ADT that holds all data about cells and their relation to other cells in
 * a graph, implemented as an adjacency list.
 *
 * @author Jace Hamblin
 * @author Roman Bureacov
 */
public class SpreadsheetGraph implements Spreadsheet {
    private final int size;
    private int rows;
    private int columns;
    private final Map<String, GraphVertex> adjList;
    private boolean cycle;
    private final ExpressionReader mainReader;

    /**
     * Constructor for the spreadsheet with the given number of rows and columns
     * @param theRowNum The number of rows to be in the spreadsheet.
     * @param theColumnNum The number of columns to be in the spreadsheet.
     */
    public SpreadsheetGraph(int theRowNum, int theColumnNum) {
        mainReader = new GrammarExpressionReader();
        rows = theRowNum;
        columns = theColumnNum;
        size = rows * columns;
        adjList = new HashMap<>();
    }

    /**
     * Gets the cell value at the given row and column
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     * @return The value of the cell.
     */
    @Override
    public Double getCellValue(String theRowColumn) {
        if (adjList.containsKey(theRowColumn)) {
            return adjList.get(theRowColumn).getCell().getValue();
        } else {
            return null;
        }
    }

    /**
     * Gets the cell instructions at the given row and column in string format.
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     * @return The cells instructions.
     */
    @Override
    public String getCellInstructions(String theRowColumn) {
        theRowColumn = theRowColumn.trim();
        String returnInstructions;
        if (mainReader.isCellRef(theRowColumn)) {
            final GraphVertex returnVertex = adjList.get(theRowColumn);
            if (returnVertex != null) {
                returnInstructions = returnVertex.getCell().getInstruction();
            } else returnInstructions = null;
        } else {
            throw  new IllegalArgumentException("Row and column designation is not properly formatted");
        }
        return returnInstructions;
    }

    /**
     * Gets the cell instructions at the given row and column in int format.
     * @param theRow the row to look at
     * @param theColumn the column to look at
     * @return The cell instructions.
     */
    @Override
    public String getCellInstructions(final int theRow, final int theColumn) {
        return this.getCellInstructions(this.toCellRef(theRow, theColumn));
    }

    /**
     * Sets cell instructions and evaluates cell values with provided instructions, cell is found using string row and column
     * @param theInstructions the instructions to be provided to the cell, must begin with "=" to be evaluated as an expression,
     *                        otherwise will attempt to evaluate as a literal. If not a literal and no "=", will evaluate as 0.
     * @param theRowColumn the row and column of the desired cell, as a string, in the format "R#C#"
     */
    @Override
    public void setCellInstructions(String theInstructions, String theRowColumn) {
        theInstructions = theInstructions.trim();
        if (!mainReader.isCellRef(theRowColumn))
            throw new IllegalArgumentException("Row and column designation is not properly formatted");

        if (theInstructions.isEmpty()) {
            adjList.remove(theRowColumn);
            evaluateInstructions();
        } else {
            if (theInstructions.startsWith("=")) theInstructions = theInstructions.toUpperCase();
            adjList.putIfAbsent(theRowColumn, new GraphVertex(theRowColumn));

            GraphVertex temp = adjList.get(theRowColumn);
            String oldInstructions = temp.getCell().getInstruction();
            temp.getCell().setInstruction(theInstructions);
            evaluateInstructions();
            //In case of cycle
            if(cycle) {
                temp.getCell().setInstruction(oldInstructions);
                cycle = false;
                throw new IllegalArgumentException("Cycle detected, cyclic instructions invalid");
            }
        }
    }

    /**
     * Sets cell instructions and evaluates cell values with provided instructions, cell is found using int row and column.
     * @param theInstructions the instruction to be provided to the cell, must begin with "=" to be evaluated
     *                        as an expression, otherwise will attempt to evaluate as a literal. If not a literal
     *                        and no "=", it will evaluate as 0.
     * @param theRow the row to insert the instruction
     * @param theColumn the column to insert the instruction
     */
    @Override
    public void setCellInstructions(final String theInstructions, final int theRow, final int theColumn) {
        this.setCellInstructions(theInstructions, this.toCellRef(theRow, theColumn));
    }

    /**
     * Gets the number of rows in the spreadsheet.
     * @return the number of rows.
     */
    @Override
    public int getRowCount() {
        return rows;
    }

    /**
     * Gets the number of columns in the spreadsheet.
     * @return The number of columns
     */
    @Override
    public int getColumnCount() {
        return columns;
    }

    /**
     * Sets the number of rows in the spreadsheet.
     * @param theCount the number of rows to resize to
     */
    @Override
    public void setRowCount(final int theCount) {
        rows = theCount;
    }

    /**
     * Sets the number of columns in the spreadsheet.
     * @param theCount the number of columns to resize to
     */
    @Override
    public void setColumnCount(final int theCount) {
        columns = theCount;
    }

    /**
     * Gets the size of the spreadsheet by number of cells.
     * @return the size of the spreadsheet in number of cells.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Formats two integers into a CellRef of the format "R#C#".
     * @param pRow the row of interest.
     * @param pCol the column of interest.
     * @return the CellRef string representation of the parameters in the format "R#C#".
     */
    @Override
    public String toCellRef(final int pRow, final int pCol) {
        return "R" + (pRow + 1) + "C" + (pCol + 1);
    }

    //Method to evaluate each cells function and set its value to the result
    private void evaluateInstructions() {
        Queue<GraphVertex> ordering = topSort();
        //Cycle checker, ordering should always contain all values unless cycle
        if (!ordering.containsAll(adjList.values())) {
            cycle = true;
            return;
        }

        while (!ordering.isEmpty()) {
            //Construct a valid map of values for the expression reader to read
            Map<String, Double> readerInput = new HashMap<>();
            Collection<GraphVertex> vertices = adjList.values();
            for (GraphVertex tempStorage : vertices) {
                readerInput.put(tempStorage.getRowColumn(), tempStorage.getCell().getValue());
            }

            GraphVertex nextToCalc = ordering.remove();
            String expression = nextToCalc.getCell().getInstruction();
            if (expression.startsWith("=")) {
                // is an expression

                //Strip "=" so the expression reader can read the expression
                expression = expression.substring(1);
                List<String> cellRefs = mainReader.getCellRefsOf(expression);
                while (!cellRefs.isEmpty()) {
                    //Any nonexistent cell refs are treated as empty cells, thus 0
                    if (adjList.get(cellRefs.getFirst()) == null) {
                        expression = expression.replaceAll(cellRefs.getFirst(), "0");
                    }
                    cellRefs.removeFirst();
                }
                try {
                    nextToCalc.getCell().setValue(mainReader.evaluate(expression, readerInput));
                } catch (Exception exc) {
                    nextToCalc.getCell().setValue(null);
                    throw exc;
                }
            } else {
                // is some literal
                expression = expression.trim();
                nextToCalc.getCell().setInstruction(expression);
                if (mainReader.isNumber(expression)) {
                    double literal = Double.parseDouble(nextToCalc.getCell().getInstruction());
                    nextToCalc.getCell().setValue(literal);
                } else {
                    nextToCalc.getCell().setValue(null);
                }
            }
        }
    }

    //Helper method that performs topological sort on the spreadsheet and returns a queue with the order to evaluate
    private Queue<GraphVertex> topSort() {
        setIndegree();
        Queue<GraphVertex> ordering = new ArrayDeque<>();
        Queue<GraphVertex> noDegree = noDegreeQueue();
        while (!noDegree.isEmpty()) {
            GraphVertex temp = noDegree.remove();
            temp.decrementIndegree();
            ordering.add(temp);
            List<GraphVertex> vertexEdgeList = temp.getAdjList();
            while (!vertexEdgeList.isEmpty()) {
                temp.removeEdge(vertexEdgeList.removeFirst());
            }
            noDegree = noDegreeQueue();
        }
        return ordering;
    }

    //Helper method that finds all vertices with indegree equal to 0, and returns them in a queue
    private Queue<GraphVertex> noDegreeQueue() {
        Collection<GraphVertex> vertices = adjList.values();
        Iterator<GraphVertex> verIterator = vertices.iterator();
        Queue<GraphVertex> noDegree = new ArrayDeque<>();
        while (verIterator.hasNext()) {
            GraphVertex tempVertex = verIterator.next();
            if (tempVertex.getIndegree() == 0) {
                noDegree.add(tempVertex);
            }
        }
        return noDegree;
    }

    //Helper method that sets the indegree of each vertex according to how many cell references are in the cell function
    //Automatically sets edges for these references
    private void setIndegree() {
        Collection<GraphVertex> vertices = adjList.values();
        for (GraphVertex tempVertex : vertices) {
            //Reset indegree, since values are not necessarily 0 after evaluation
            tempVertex.setIndegree(0);
            //If not an expression then no indegree, so do nothing, otherwise:
            if (tempVertex.getCell().getInstruction().startsWith("=")) {
                String cleanedString = tempVertex.getCell().getInstruction().substring(1);
                List<String> CellRefs = mainReader.getCellRefsOf(cleanedString);
                while (!CellRefs.isEmpty()) {
                    if (adjList.get(CellRefs.getFirst()) != null) {
                        //Check to ensure cell being referenced doesn't already have an edge to this cell
                        if (!adjList.get(CellRefs.getFirst()).getAdjList().contains(tempVertex)) {
                            adjList.get(CellRefs.getFirst()).addEdge(tempVertex);
                        }
                    }
                    CellRefs.removeFirst();
                }
            }
        }
    }
}
