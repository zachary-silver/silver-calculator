package zack.silver_calculator;

import java.util.Stack;

public class Evaluator
{
    /**
     * Construct an evaluator object.
     * @param s the string containing the infix expression.
     */
    public Evaluator(String s)
    {
        operatorStack = new Stack<Integer>();
        postfixStack = new Stack<Double>();

        expression = s.split(" +"); // Split the infix expression into operators and operands

        operatorStack.push(END); // Mark the end of the expression for use in evaluation
    }

    private Stack<Integer> operatorStack;
    private Stack<Double> postfixStack;
    private String [] expression;

    private static final int END    = 0;
    private static final int VALUE  = 1;
    private static final int OPAREN = 2;
    private static final int CPAREN = 3;
    private static final int EXP    = 4;
    private static final int MULT   = 5;
    private static final int DIV    = 6;
    private static final int ADD    = 7;
    private static final int SUB    = 8;

    /**
     * Precedence table whose indices correspond to the appropriate types
     * from the above constants. Weights correspond to operator precedence and associativity.
     * Right associative operators have a higher inWeights and the opposite is true
     * for left associative operators.
     */
    private static final Precedence [] precTable = new Precedence[]
    {
        new Precedence(0, -1),      // END
        new Precedence(0,  0),      // VALUE
        new Precedence(100,  0),    // OPAREN
        new Precedence(0, 99),      // CPAREN
        new Precedence(6,  5),      // EXP
        new Precedence(3,  4),      // MULT
        new Precedence(3,  4),      // DIV
        new Precedence(1,  2),      // ADD
        new Precedence(1,  2)       // SUB
    };

    private static class Precedence
    {
        public int inputWeight;
        public int stackWeight;

        public Precedence(int inWeight, int stacWeight)
        {
            inputWeight = inWeight;
            stackWeight = stacWeight;
        }
    }

    private static class Token
    {
        public Token() { this(END); }

        public Token(int t) { this(t, 0); }

        public Token(int t, double v)
        {
            type = t;
            value = v;
        }

        public int getType() { return type; }

        public double getValue() { return value; }

        private int type = END;
        private double value = 0;
    }

    /**
     * Public routine that performs the evaluation.
     * Examine the postfix stack to see if a single result is
     * left and if so, return it; otherwise print error.
     * @return the result of the expression.
     */
    public double getValue()
    {
        double theResult;

        for (String str: expression)
            processToken(getToken(str));

        // Dummy END token to pop rest of operators off of stack and generate final result.
        processToken(new Token());

        if (postfixStack.isEmpty())
        {
            System.err.println("Missing operand!");
            theResult = 0;
        }
        else
        {
            theResult = postfixStack.pop();

            if (!postfixStack.isEmpty())
                System.err.println("Warning: missing operators!");
        }

        return theResult;
    }

    /**
     * Produces a Token for each string sent from the expression
     * array for use in generating the new postfix expression
     */
    private static Token getToken(String str)
    {
        Token theToken;
        double theValue;

        if (str.equals("^"))
        {
            theToken = new Token(EXP);
        }
        else if (str.equals("/"))
        {
            theToken = new Token(DIV);
        }
        else if (str.equals("*"))
        {
            theToken = new Token(MULT);
        }
        else if (str.equals("("))
        {
            theToken = new Token(OPAREN);
        }
        else if (str.equals(")"))
        {
            theToken = new Token(CPAREN);
        }
        else if (str.equals("+"))
        {
            theToken = new Token(ADD);
        }
        else if (str.equals("-"))
        {
            theToken = new Token(SUB);
        }
        else
        {
            // Otherwise, the token should be of type VALUE and needs to be parsed
            try
            {
                theValue = Double.parseDouble(str);
                theToken = new Token(VALUE, theValue);
            }
            catch(NumberFormatException e)
            {
                System.err.println("Error occurred when parsing supposed VALUE token: " + str);
                theToken = new Token();
            }
        }

        return theToken;
    }

    /**
     * After a token is read, use operator precedence parsing
     * algorithm to process it; missing opening parentheses
     * are detected here.
     */
    private void processToken(Token theToken)
    {
        int topOperator;
        int tokenType = theToken.getType();

        switch(tokenType)
        {
            // Operands get pushed immediately onto the postfix stack
            case VALUE:
                postfixStack.push(theToken.getValue());
                break;

            // Closing parenthesis imply operators must be popped and applied to the
            // appropriate operands until its respective opening parenthesis is found
            case CPAREN:
                while ((topOperator = operatorStack.peek()) != OPAREN && topOperator != END)
                    binaryOp(topOperator);
                if (topOperator == OPAREN)
                    operatorStack.pop();  // Get rid of respective opening parenthesis
                else
                    System.err.println("Missing open parenthesis");
                break;

            // General operator case pops all operators of higher precedence and applies them
            // to their appropriate operands and then places the operator on the operator stack.
            default:
                while (precTable[tokenType].inputWeight <=
                        precTable[topOperator = operatorStack.peek()].stackWeight)
                    binaryOp(topOperator);
                if (tokenType != END)
                    operatorStack.push(tokenType);
                break;
        }
    }

    /**
     * Process an operator by taking two items off the postfix
     * stack, applying the operator, and pushing the result.
     * Print error if missing closing parenthesis or division by 0.
     */
    private void binaryOp(int topOperator)
    {
        if (topOperator == OPAREN)
        {
            System.err.println("Unbalanced parentheses");
            operatorStack.pop();
        }
        else
        {
            double rhs = postfixPop();
            double lhs = postfixPop();

            if (topOperator == EXP)
            {
                postfixStack.push(Math.pow(lhs, rhs));
            }
            else if (topOperator == ADD)
            {
                postfixStack.push(lhs + rhs);
            }
            else if (topOperator == SUB)
            {
                postfixStack.push(lhs - rhs);
            }
            else if (topOperator == MULT)
            {
                postfixStack.push(lhs * rhs);
            }
            else if (topOperator == DIV)
            {
                if (rhs != 0)
                {
                    postfixStack.push(lhs / rhs);
                }
                else
                {
                    System.err.println("Division by zero");
                    postfixStack.push(lhs);
                }
            }

            operatorStack.pop();
        }
    }

    private double postfixPop()
    {
        double value;

        if (postfixStack.isEmpty())
        {
            System.err.println("Missing operand");
            value = 0;
        }
        else
        {
            value = postfixStack.pop();
        }

        return value;
    }
}
