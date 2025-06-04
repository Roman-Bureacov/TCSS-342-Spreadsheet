# David Norman
Created the `SpreadsheetGUI` class which makes it so that you are able to view, edit, and resize the spreadsheet.
You are also able to add formulas to the cells and be able to see the evaluated value while not editing the 
current cell. If you are editing the cell, you will see the originally enter item (whether that be a formula, 
string, integer, etc.).
# Jace Hamblin
Created the `Spreadsheet` interface, its implementation in `SpreadsheetGraph`, the `GraphVertex` class, and the
`SpreadsheetCell`class. Did the work setting up data storage in the ADT as an adjacency list implemented graph, as well
as ways to access and manipulate said data. Also set up topological sort and subsequent passing of evaluation to the
expression reader.
# Roman Bureacov
Created the `ExpressionReader` interface and its concrete implementation `GrammarExpressionReader`. Did the work
behind reading expressions and evaluating them correctly. In addition, implemented utility class `Functions` that 
works like the static `Math` class, in that it supplies functions that can be called on by the expression reader.