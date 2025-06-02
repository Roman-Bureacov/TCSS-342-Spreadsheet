# The GUI

# The Spreadsheet ADT

# The Expression Evaluation
Here instead of a binary tree, a grammar algorithm was used to evaluate string expressions into a double value. 

## The Grammar

The grammar rules applied were as follows:

```
Expression
    term
    expression "+" term
    expression "-" term
Term
    primary
    term "*" primary
    term "/" primary
    term "%" primary
Primary
    number
    cellref
    "(" expression ")"
    function "(" args ")"
    primary "^" primary
Function
    AVG
    ... more function names
Args
    expression
    args "," expression
Cellref
    "R" integer "C" integer
Number
    floating-point-literal
```

So if you were given a stream of tokens, say `"5+3"` was converted into `["5", "+", "3"]`, then you would evaluate 
it as the rule, under `Expressions`, as `expression "+" term`, where in this scenario `expression` is a `Term`, 
which evaluates to `Primary`, which evaluates to `Number`, and finally into `floating-point-literal`. Then you would 
look at the second argument, see a `primary`, which is a `Number`, which is a `floating-point-literal`. Finally,
perform the addition because the token is `+` and you get the number `8`!

The implementation is that first the expression reader splits the input into tokens up in the 
`AbstractExpressionReader`, which implements most of the methods for the interface `ExpressionReader` plus a
protected method `tokenize(String)`, which allows whatever implementation to receive a deque of tokens.

The way `tokenize(String)` splits an input expression into tokens follows a two-stage process:

1. Split the string expression by whitespace; this is to allow the expression to include as much whitespace 
as necessary.
2. For each sub-expression (if any), in order, is then repeatedly matched against a regex rule that attempts to find
tokens and put them into a working Deque that will later be passed on to be evaluated. The regex specifies that,
in order, the tokens it should find first are:

   1. Match a cell reference (R1C1 format)
   2. Match a floating-point number
   3. Match an integer
   4. Match binary operators along with parentheses and commas
   5. Match words, which will end up being evaluated as functions
   6. Match anything else that might show up.

So, for example, an expression `"R1C2+3^6"` will first find `"R1C2"` (rather than find `"R"`, `"1"`, `"C"`, and 
`"2"` individually), then `"+"`, then `"3"`, then `"^"`, and finally `"6"`. 

The constructed deque is a deque of strings, where each string is a token extracted from the expression string. 
The choice of deque was simply because there is no official queue implemented in java's utility library.

The way it works is similar to a recursive manner, where it calls upon similar methods until it exhausts the tokens
in the deque. Each token is evaluated against the grammar rule above, where as the methods get called, they expect
certain tokens types to come up.

## Functions
The functions is a simple utility class. It... functions... in a similar method to that of the `Math` static class.

The invocation begins with the grammar reader finding a word token, which it checks if it is an existing function; 
if this word token is not an existing function, it throws an exception. 

The reader then takes in the arguments enclosed in parentheses and turns that into a `Double[]`. Note here that
here the wrapper class is used. You will see shortly why.

The reader then calls upon the `Function` class, which features a static method `Function.apply(String, Object...)`.
The purpose of this method using `Object...` is in the event that future implementation might want to manipulate 
more than just doubles.

Internally, the `Function` utility class houses a `Map<String, Function<Object[], Object>>`. The `String` is 
the function name, and the `Function<Object[], Object>` is the function that is applied. A static initializer exists 
to map all the recongized functions, putting `String` function keys and mapping those to `Function<Object[], Object>`
functions. an example is 

```java
FUNC.put("AVERAGE", args -> {
    double lAvg = 0;
    final double lDenominator = args.length;
    for (final Object arg : args) lAvg += ((Double) arg) / lDenominator;
    return lAvg;
});
```
This will take in an array of `Double` values and take the average of them.