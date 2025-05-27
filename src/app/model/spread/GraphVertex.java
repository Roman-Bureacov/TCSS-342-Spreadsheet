package app.model.spread;

import java.util.LinkedList;

public class GraphVertex {
    private int indegree;
    private final SpreadsheetCell thisCell;
    private final LinkedList<GraphVertex> adjList;
    String rowColumn;

    public GraphVertex(String theRowColumn) {
        thisCell = new SpreadsheetCell();
        indegree = 0;
        adjList = new LinkedList<>();
        rowColumn = theRowColumn;
    }

    public void addEdge(GraphVertex theVertex){
        theVertex.incrementIndegree();
        adjList.add(theVertex);
    }

    public int getIndegree() {
        return indegree;
    }

    public void setIndegree(int theIndegree) {
        indegree = theIndegree;
    }

    public void incrementIndegree() {
        indegree++;
    }

    public void decrementIndegree() {
        indegree--;
    }

    public SpreadsheetCell getCell() {
        return thisCell;
    }

    public String getRowColumn() {
        return rowColumn;
    }

    public LinkedList<GraphVertex> getAdjList(){
        return adjList;
    }
}
