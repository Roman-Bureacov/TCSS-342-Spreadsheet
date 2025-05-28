package app.model.spread;

import app.model.expr.ExpressionReader;
import app.model.expr.GrammarExpressionReader;

import java.util.*;

public class SpreadsheetGraph implements Spreadsheet {
    private final int size;
    private final int rows;
    private final int columns;
    private final Map<String, GraphVertex> adjList;
    ExpressionReader mainReader;

    public SpreadsheetGraph(int theRowNum, int theColumnNum) {
        mainReader = new GrammarExpressionReader();
        rows = theRowNum;
        columns = theColumnNum;
        size = rows * columns;
        adjList = new HashMap<>();
    }

    @Override
    public double getCellValue(String theRowColumn) {
        return adjList.get(theRowColumn).getCell().getValue();
    }

    @Override
    public String getCellInstructions(String theRowColumn) {
        theRowColumn = theRowColumn.trim();
        String returnInstructions;
        if (mainReader.isCellRef(theRowColumn)) {
            returnInstructions = adjList.get(theRowColumn).getCell().getInstruction();
        } else {
            throw  new IllegalArgumentException("Row and column designation is not properly formatted");
        }
        return returnInstructions;
    }

    @Override
    public void setCellInstructions(String theInstructions, String theRowColumn) {
        if (mainReader.isCellRef(theRowColumn)) {
                adjList.putIfAbsent(theRowColumn, new GraphVertex(theRowColumn));
        } else {
            throw new IllegalArgumentException("Row and column designation is not properly formatted");
        }

        GraphVertex temp = adjList.get(theRowColumn);
        temp.getCell().setInstruction(theInstructions);
        evaluateInstructions();
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

    private void evaluateInstructions() {
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

        while (!ordering.isEmpty()) {
            Map<String, Double> readerInput = new HashMap<>();
            Collection<GraphVertex> vertices = adjList.values();
            Iterator<GraphVertex> verIterator = vertices.iterator();
            while (verIterator.hasNext()) {
                GraphVertex tempStorage = verIterator.next();
                readerInput.put(tempStorage.getRowColumn(), tempStorage.getCell().getValue());
            }
            GraphVertex nextToCalc = ordering.remove();
            String expression = nextToCalc.getCell().getInstruction();
            expression = expression.replaceAll(" ", "");
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
        Iterator<GraphVertex> verIterator = vertices.iterator();
        while (verIterator.hasNext()) {
            GraphVertex tempVertex = verIterator.next();
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
