package app.model.spread;

import java.util.LinkedList;

/**
 * Class representing individual vertices for the spreadsheet graph,
 * containing a list of edges to other vertices, an indegree counter,
 * a row and column designation, and a SpreadsheetCell.
 *
 * @author Jace Hamblin
 */
public class GraphVertex {
    private int indegree;
    private final SpreadsheetCell thisCell;
    private final LinkedList<GraphVertex> adjList;
    String rowColumn;

    /**
     * Constructor for the vertex, takes a string representation of its row/column signature in the form "R#C#".
     * @param theRowColumn string representation vertex row/column signature in the form "R#C#".
     */
    public GraphVertex(String theRowColumn) {
        thisCell = new SpreadsheetCell();
        indegree = 0;
        adjList = new LinkedList<>();
        rowColumn = theRowColumn;
    }

    /**
     * Adds a new edge from this vertex leading to the provided vertex.
     * @param theVertex the vertex the edge leads to.
     */
    public void addEdge(GraphVertex theVertex){
        theVertex.incrementIndegree();
        adjList.add(theVertex);
    }

    /**
     * Removes an edge from this vertex to the given vertex.
     * @param theVertex the vertex the edge leads to.
     */
    public void removeEdge(GraphVertex theVertex) {
        theVertex.decrementIndegree();
        adjList.remove(theVertex);
    }

    /**
     * Returns the indegree of the vertex.
     * @return the indegree of the vertex.
     */
    public int getIndegree() {
        return indegree;
    }

    /**
     * Sets the indegree of the vertex.
     * @param theIndegree the new indegree.
     */
    public void setIndegree(int theIndegree) {
        indegree = theIndegree;
    }

    /**
     * Increments the indegree.
     */
    public void incrementIndegree() {
        indegree++;
    }

    /**
     * Decrements the indegree
     */
    public void decrementIndegree() {
        indegree--;
    }

    /**
     * Gets the cell this vertex represents.
     * @return the cell this vertex represents.
     */
    public SpreadsheetCell getCell() {
        return thisCell;
    }

    /**
     * Gets this vertex's row/column signature.
     * @return this vertex's row/column signature.
     */
    public String getRowColumn() {
        return rowColumn;
    }

    /**
     * Gets this vertex's list of adjacent vertices.
     * @return a list of adjacent vertices.
     */
    public LinkedList<GraphVertex> getAdjList(){
        return adjList;
    }
}
