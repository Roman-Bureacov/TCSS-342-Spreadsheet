package app.model.spread;

import app.model.expr.ExpressionReader;
import app.model.expr.GrammarExpressionReader;

import java.util.*;

public class SpreadsheetGraph implements Spreadsheet {
    private final int size;
    private final int rows;
    private final int columns;
    private final Map<String, GraphVertex> adjList;
    private boolean cycle;
    private final ExpressionReader mainReader;

    public SpreadsheetGraph(int theRowNum, int theColumnNum) {
        mainReader = new GrammarExpressionReader();
        rows = theRowNum;
        columns = theColumnNum;
        size = rows * columns;
        adjList = new HashMap<>();
    }

    @Override
    public double getCellValue(String theRowColumn) {
        if (adjList.containsKey(theRowColumn)) {
            return adjList.get(theRowColumn).getCell().getValue();
        } else {
            return 0d;
        }
    }

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

    @Override
    public String getCellInstructions(final int theRow, final int theColumn) {
        return this.getCellInstructions(this.toCellRef(theRow, theColumn));
    }

    @Override
    public void setCellInstructions(String theInstructions, String theRowColumn) {
        if (mainReader.isCellRef(theRowColumn)) {
                adjList.putIfAbsent(theRowColumn, new GraphVertex(theRowColumn));
        } else {
            throw new IllegalArgumentException("Row and column designation is not properly formatted");
        }

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

    @Override
    public void setCellInstructions(final String theInstructions, final int theRow, final int theColumn) {
        this.setCellInstructions(theInstructions, this.toCellRef(theRow, theColumn));
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int columnCount() {
        return columns;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toCellRef(final int pRow, final int pCol) {
        return "R" + (pRow + 1) + "C" + (pCol + 1);
    }

    private void evaluateInstructions() {
        Queue<GraphVertex> ordering = topSort();
        //Cycle checker
        if (!ordering.containsAll(adjList.values())) {
            cycle = true;
            return;
        }

        while (!ordering.isEmpty()) {
            Map<String, Double> readerInput = new HashMap<>();
            Collection<GraphVertex> vertices = adjList.values();
            for (GraphVertex tempStorage : vertices) {
                readerInput.put(tempStorage.getRowColumn(), tempStorage.getCell().getValue());
            }
            GraphVertex nextToCalc = ordering.remove();
            String expression = nextToCalc.getCell().getInstruction();
            if (expression.startsWith("=")) {
                expression = expression.substring(1);
                List<String> cellRefs = mainReader.getCellRefsOf(expression);
                while (!cellRefs.isEmpty()) {
                    if (adjList.get(cellRefs.getFirst()) == null) {
                        expression = expression.replaceAll(cellRefs.getFirst(), "0");
                    }
                    cellRefs.removeFirst();
                }
                nextToCalc.getCell().setValue(mainReader.evaluate(expression, readerInput));
            } else {
                try {
                    double literal = Double.parseDouble(nextToCalc.getCell().getInstruction());
                    nextToCalc.getCell().setValue(literal);
                } catch (NumberFormatException ex) {
                    nextToCalc.getCell().setValue(0);
                }
            }
        }
    }

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

    private void setIndegree() {
        Collection<GraphVertex> vertices = adjList.values();
        for (GraphVertex tempVertex : vertices) {
            tempVertex.setIndegree(0);
            if (tempVertex.getCell().getInstruction().startsWith("=")) {
                String cleanedString = tempVertex.getCell().getInstruction().substring(1);
                List<String> CellRefs = mainReader.getCellRefsOf(cleanedString);
                while (!CellRefs.isEmpty()) {
                    if (adjList.get(CellRefs.getFirst()) != null) {
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
