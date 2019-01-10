package zack.silver_calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private class Input
    {
        public TextView textView = findViewById(R.id.inputTextView);
        public String str = textView.getText().toString();
        public int strLen = str.length();
    }

    private class Output
    {
        public TextView textView = findViewById(R.id.outputTextView);
        public String str = textView.getText().toString();
        public int strLen = str.length();
    }

    /**
     * Appends an entry value from input and or an operator to the
     * output representing the current mathematical expression.
     */
    private void build(String operator)
    {
        Input input = new Input();
        Output output = new Output();
        String newOutput;

        String lastToken = getLastToken(output.str);

        if (input.strLen == 0 && output.strLen == 0)
        {
            showToast("Must input a value before applying an operator.");
        }
        else if (input.strLen == 0 && lastToken.equals(" "))
        {
            // User wants to apply an operator to previous the result
            String prevResult = output.str.substring(0, output.str.indexOf("~") - 2);
            newOutput = prevResult + " " + operator + " ";
            output.textView.setText(newOutput);
        }
        else if (lastToken.equals(")"))
        {
            if (input.strLen != 0)
            {
                showToast("Must apply operator after closing parenthesis before entering a value.");
            }
            else
            {
                newOutput = output.str + " " + operator + " ";
                output.textView.setText(newOutput);
            }
        }
        else
        {
            if (!lastToken.equals("(") && !lastToken.equals(" ") && input.strLen == 0)
            {
                // User wants to change the previously entered operator
                newOutput = output.str.substring(0, output.strLen - 2) + operator + " ";
                output.textView.setText(newOutput);
            }
            else if (!isValidDouble(input.str))
            {
                showToast("Invalid entry!");
            }
            else
            {
                newOutput = output.str + input.str + " " + operator + " ";
                output.textView.setText(newOutput);
                input.textView.setText("");
            }
        }
    }

    /**
     * Constructs the string representing the final expression
     * and passes it into the evaluator, then outputs the result.
     */
    private void process()
    {
        Input input = new Input();
        Output output = new Output();
        String newOutput;

        String lastOutputToken = getLastToken(output.str);

        // If the last token is anything other than a closing parenthesis,
        // an entry value is needed for a valid expression
        if (!lastOutputToken.equals(")") && !isValidDouble(input.str))
        {
            showToast("Invalid entry!");
        }
        else if (lastOutputToken.equals(")") && input.strLen != 0)
        {
            showToast("Must apply an operator before entering a value!");
        }
        else
        {
            // Check for a displayed previous result and ignore it
            // '~' character marks end of a previous result if there is one
            String [] outputStrArr = output.str.split("~");
            String newOutputStr = outputStrArr[outputStrArr.length - 1];

            // Pass the final string representing the entire expression in to evaluate
            Evaluator eval = new Evaluator(newOutputStr + input.str);

            newOutput = eval.getValue() + "  ~  ";
            output.textView.setText(newOutput);
            input.textView.setText("");
        }
    }

    private void openParenth()
    {
        Input input = new Input();
        Output output = new Output();
        String newOutput;

        if (input.strLen != 0)
        {
            showToast("Can't place opening parenthesis after a value!");
        }
        else if (getLastToken(output.str).equals(")"))
        {
            showToast("Can't place opening parenthesis after a closing parenthesis!");
        }
        else
        {
            newOutput = output.str + "( ";
            output.textView.setText(newOutput);
            input.textView.setText("");
        }
    }

    private void closeParenth()
    {
        Input input = new Input();
        Output output = new Output();
        String newOutput;

        // Treated the same as any other operator if a valid entry is present
        if (input.strLen > 0)
        {
            build(")");
        }
        else if (output.strLen == 0)
        {
            showToast("Can't place closing parenthesis at the start of an expression!");
        }
        else if (!getLastToken(output.str).equals(")"))
        {
            showToast("Can't place closing parenthesis after an operator!");
        }
        else
        {
            newOutput = output.str + ") ";
            output.textView.setText(newOutput);
            input.textView.setText("");
        }
    }

    private void negateValue()
    {
        Input input = new Input();
        String newOutput;

        if (input.strLen == 0)
        {
            showToast("No value to negate!");
        }
        else
        {
            boolean isNegative = input.str.contains("-");

            if (input.strLen == 1 && isNegative)
            {
                input.textView.setText("");
            }
            else if (isNegative)
            {
                input.textView.setText(input.str.substring(1, input.strLen));
            }
            else
            {
                newOutput = "-" + input.str;
                input.textView.setText(newOutput);
            }
        }
    }

    private void squareRootValue()
    {
        Input input = new Input();
        String newOutput;

        if (input.strLen == 0)
        {
            showToast("No value to square root!");
        }
        else if (input.str.contains("-"))
        {
            showToast("Can't square root a negative value!");
        }
        else if (isValidDouble(input.str))
        {
            double value = Math.sqrt(Double.parseDouble(input.str));
            newOutput = value + "";
            input.textView.setText(newOutput);

            showToast("Square rooted value.");
        }
        else
        {
            showToast("Invalid value to square root!");
        }
    }

    private boolean isValidDouble(String str)
    {
        try { Double.parseDouble(str); }
        catch (NumberFormatException e) { return false; }

        return true;
    }

    private String getLastToken(String str)
    {
        String lastToken;
        int strLen = str.length();

        if (strLen == 0)
            lastToken = str;
        else // Otherwise, strLen can't be less than 4 in the context of this program
            lastToken = str.substring(strLen - 2, strLen - 1); // Neglect white space at the end

        return lastToken;
    }

    private void clearAll()
    {
        TextView outputText = findViewById(R.id.outputTextView);
        TextView inputText = findViewById(R.id.inputTextView);

        outputText.setText("");
        inputText.setText("");
    }

    private void clearEntry()
    {
        TextView inputText = findViewById(R.id.inputTextView);

        inputText.setText("");
    }

    private void deleteValue()
    {
        Input input = new Input();

        if (input.strLen == 0)
            showToast("No value to delete!");
        else
            input.textView.setText(input.str.substring(0, input.strLen - 1));
    }

    private void inputDecimal()
    {
        Input input = new Input();

        if (!input.str.contains("."))
            inputValue(".");
    }

    private void inputValue(String value)
    {
        Input input = new Input();
        String newOutput = input.str + value;

        input.textView.setText(newOutput);
    }

    private void showToast(String message)
    {
        Toast myToast = Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_LONG);
        myToast.setGravity(Gravity.TOP | Gravity.CENTER,
                0,
                0);
        myToast.show();
    }

    protected void zeroButton(View v) { inputValue("0"); }

    protected void oneButton(View v) { inputValue("1"); }

    protected void twoButton(View v) { inputValue("2"); }

    protected void threeButton(View v) { inputValue("3"); }

    protected void fourButton(View v) { inputValue("4"); }

    protected void fiveButton(View v) { inputValue("5"); }

    protected void sixButton(View v) { inputValue("6"); }

    protected void sevenButton(View v) { inputValue("7"); }

    protected void eightButton(View v) { inputValue("8"); }

    protected void nineButton(View v) { inputValue("9"); }

    protected void addButton(View v) { build("+"); }

    protected void subtractButton(View v) { build("-"); }

    protected void multiplyButton(View v) { build("*"); }

    protected void divideButton(View v) { build("/"); }

    protected void exponentButton(View v) { build("^"); }

    protected void decimalButton(View v) { inputDecimal(); }

    protected void openParenthButton(View v) { openParenth(); }

    protected void closeParenthButton(View v) { closeParenth(); }

    protected void negateButton(View v) { negateValue(); }

    protected void squareRootButton(View v) { squareRootValue(); }

    protected void equalsButton(View v) { process(); }

    protected void deleteButton(View v) { deleteValue(); }

    protected void clearAllButton(View v) { clearAll(); }

    protected void clearEntryButton(View v) { clearEntry(); }
}
