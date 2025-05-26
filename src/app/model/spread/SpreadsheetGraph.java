package app.model.spread;
import java.util.*;

public class SpreadsheetGraph implements Spreadsheet {
    private final int size;
    private final int rows;
    private final int columns;
    private final Map<String, GraphVertex> adjList;

    public SpreadsheetGraph(int theRowNum, int theColumnNum) {
        rows = theRowNum;
        columns = theColumnNum;
        size = rows * columns;
        adjList = new HashMap<>(size);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                String designation = "R" + i + "C" + j;
                adjList.put(designation, new GraphVertex(designation));
            }
        }
    }

    @Override
    public SpreadsheetCell getCell(String theRowColumn) {
        return adjList.get(theRowColumn).getCell();
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

    }
}
