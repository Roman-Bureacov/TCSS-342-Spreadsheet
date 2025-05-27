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
    public void evaluateInstructions() {
        setIndegree();
        Queue<GraphVertex> ordering = new ArrayDeque<>();
        Queue<GraphVertex> noDegree = noDegreeQueue();
        while (!noDegree.isEmpty()) {
            GraphVertex temp = noDegree.remove();
            temp.decrementIndegree();
            ordering.add(temp);
            List<GraphVertex> vertexEdgeList = temp.getAdjList();
            for (int i = 0; i < vertexEdgeList.size(); i++) {
                vertexEdgeList.get(i).decrementIndegree();
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
            if (expression.startsWith("=")) {
                expression = expression.substring(1);
            }
            nextToCalc.getCell().setValue(mainReader.evaluate(expression, readerInput));
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
                List<String> CellRefs = mainReader.getCellRefsOf(tempVertex.getCell().getInstruction());
                while (!CellRefs.isEmpty()) {
                    adjList.get(CellRefs.getFirst()).addEdge(tempVertex);
                    CellRefs.removeFirst();
                }
            }
        }
    }
}
