package com.aarush.linearequationsolver;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DecimalFormat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String variable;
    String equation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.solve:
                solve();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //calling out the given ids
    public void setClassVariable() {
        for (int i =0; i < equation.length(); i++) {
            char chr = equation.charAt(i);
            if (Character.isLetter(chr)) {
                variable = Character.toString(chr);
                return;
            }
        }
        variable = "None";
    }

    public ArrayList<String> split(String equation) {
        String expression = "";
        ArrayList<String> splitedEquation = new ArrayList();
        boolean isInsideBracket = false;

        for (int i = 0; i < equation.length(); i++) {
            String chr = Character.toString(equation.charAt(i));


            if ((chr.equals("+") || chr.equals("-")) && i != 0 && !isInsideBracket) {
                splitedEquation.add(expression);
                expression = chr;

            } else {
                expression +=chr;
            }

            if (chr.equals("(")) {
                isInsideBracket = true;
            } else if (chr.equals(")")) {
                isInsideBracket = false;
            }
        }
        splitedEquation.add(expression);

        return splitedEquation;
    }

    public boolean containsBracket(String expression) {
        return  expression.contains("(");
    }
//checks for brackets

    public ArrayList<String> openBracket(ArrayList<String> equationComponents) {
        ArrayList<String> eqtComponents = new ArrayList<>();
        for (int i = 0; i < equationComponents.size(); i++) {
            if (containsBracket(equationComponents.get(i))) {
                eqtComponents.addAll(expand(equationComponents.get(i)));
            } else {
                eqtComponents.add(equationComponents.get(i));
            }
        }

        return eqtComponents;
    }

    public ArrayList<String> expand(String expression) {

        String[] expr = expression.split("[(]");

        int multiplier = expr[0].equals("-") || expr[0].equals("+") ? Integer.parseInt(expr[0] + "1") : Integer.parseInt(expr[0]);
        ArrayList<String> result = new ArrayList<>();

        ArrayList<String> exprInBracket = split(getExprInBracket(expr[1]));

        for (int i =0; i < exprInBracket.size(); i++) {
            String elem = exprInBracket.get(i);
            try {
                Integer constant = multiplier * Integer.parseInt(elem);
                result.add(constant.toString());
            } catch(Exception e) {
                Integer newCoefficient = getCoefficient(elem) * multiplier;
                result.add(newCoefficient.toString() + variable);
            }
        }

        return result;
    }



    public static int getCoefficient(String variable){
        String coefficient = "";
        if(variable.length() == 1) return 1;
        else if(variable.length() == 2 && variable.charAt(0) == '-') return -1;

        for(int i = 0; i < variable.length(); i++){
            if(Character.isDigit(variable.charAt(i)))coefficient+=variable.charAt(i);
        }
        if(variable.charAt(0) == '-')return Integer.parseInt("-" + coefficient);
        return Integer.parseInt(coefficient);
    }

    public String getExprInBracket(String expr) {
        String exprInBracket = "";
        for (int i =0; i < expr.length() - 1; i++) {
            exprInBracket +=expr.charAt(i);
        }

        return exprInBracket;
    }

    public ArrayList<String>[] collectLikeTerms(ArrayList<String> leftHandSide, ArrayList<String> rightHandSide) {
        ArrayList<String> variables = new ArrayList<>();
        ArrayList<String> constants = new ArrayList<>();

        for (int i = 0; i < leftHandSide.size(); i++) {
            String elem = leftHandSide.get(i);
            try {
                Integer constant =  -1 * Integer.parseInt(elem);
                constants.add(constant.toString());
            } catch(Exception e) {
                variables.add(elem);
            }
        }

        for (int j = 0; j < rightHandSide.size(); j++) {
            String elem = rightHandSide.get(j);
            try {
                Integer constant =  Integer.parseInt(elem);
                constants.add(constant.toString());
            } catch(Exception e) {
                variables.add(changeVariableSign(elem));
            }
        }

        ArrayList<String>[] result = new ArrayList[2];
        result[0] = variables;
        result[1] = constants;
        return result;
    }
    // this then replace + with - vice versa
    public String changeVariableSign(String variable) {
        char firstChar = variable.charAt(0);
        if (Character.toString(firstChar).equals("+")){
            return variable.replace("+", "-");
        } else if (Character.toString(firstChar).equals("-")) {
            return variable.replace("-", "+");
        } else {
            return "-" + variable;
        }
    }


    public void solve() {
        LinearLayout wrapper = findViewById(R.id.wrapper);
        TextView errorTextView = findViewById(R.id.error);
        wrapper.removeAllViews();
        errorTextView.setText("");

        Integer nextStep = 1;
        EditText equationTextView = findViewById(R.id.equation);
        equation = equationTextView.getText().toString();
        equation = equation.replaceAll("\\s+",""); //removing all spaces from equation
        setClassVariable();

        if (!isValidEquation()) {
            return;
        }
        displaySteps(equation, "Step" + nextStep.toString() + ": ", "Write down the equation");
        nextStep +=1;

        String[] divEquation = equation.split("=");

        String leftHandSide = divEquation[0]; // The left handside of the equation
        String rightHandSide = divEquation[1];
        ArrayList<String> leftHandSideComps = split(leftHandSide);
        ArrayList<String> rightHandSideComps = split(rightHandSide);

        if (containsBracket(equation)) {
            if (containsBracket(leftHandSide)) leftHandSideComps = openBracket(leftHandSideComps);
            if (containsBracket(rightHandSide)) rightHandSideComps = openBracket(rightHandSideComps);

            displaySteps(getSolution(leftHandSideComps, rightHandSideComps), "Step" + nextStep.toString() + ": ", "Open bracket");
            nextStep +=1;
        }

        ArrayList<String>[] likeTerms = collectLikeTerms(leftHandSideComps, rightHandSideComps);

        leftHandSideComps = likeTerms[0]; // Now holds the variables.
        rightHandSideComps = likeTerms[1]; // Now holds the constants.

        displaySteps(getSolution(leftHandSideComps, rightHandSideComps), "Step" + nextStep.toString() + ": ", "Collect like terms");
        nextStep +=1;

        String variableSum = simplifyExpression(leftHandSideComps);
        String constantSum = simplifyConstants(rightHandSideComps);
        Integer coef = getCoefficient(variableSum);

        displaySteps(variableSum + " = " + constantSum, "Step" + nextStep.toString() + ": ", "Simplify both side of the equation");


        if (variableSum.equals(variable)) {
            displaySteps("Therefore " + variable + " = " + constantSum, "Step" + nextStep.toString() + ": ", "Write down the final answer");
            return;
        }

        if (coef == -1) {
            constantSum = Integer.toString(Integer.parseInt(constantSum) * -1);
            displaySteps(variable + " = " + constantSum, "Step" + nextStep.toString() + ": ", "Multiply through by -1");
            nextStep +=1;
            displaySteps("Therefore " + variable + " = " + constantSum, "Step" + nextStep.toString() + ": ", "Write down the final answer");
            return;
        }
        float constant = Float.parseFloat(constantSum)/coef;

        DecimalFormat df = new DecimalFormat("0.00");

        displaySteps(variableSum + "/" + coef.toString() + " = " + constantSum + "/" + coef.toString(), "Step" + nextStep.toString() + ": ", "Divide both side of the equation by " + coef.toString());

        if (coef == 0) {
            displaySteps("Therefore " + variable + " = " + "NAN or undefined", "Step" + nextStep.toString() + ": ", "Write down the final answer");
            return;
        }

        displaySteps("Therefore " + variable + " = " + df.format(constant), "Step" + nextStep.toString() + ": ", "Write down the final answer");

    }

    public void displaySteps(String solution, String step, String content) {
        int textColor = Integer.parseInt("000000", 16)+0xFF000000;

        LinearLayout wrapper = findViewById(R.id.wrapper);
        // wrapper.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(this);
        TextView stepTextView = new TextView(this);
        TextView stepContentTextView = new TextView(this);
        TextView solutionTextView = new TextView(this);

        LinearLayout.LayoutParams linearLayouParams = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams textParams = new LinearLayout
                .LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams solutionTextParams = new LinearLayout
                .LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        solutionTextParams.setMargins(40, 18, 0, 0);
        linearLayouParams.setMargins(40, 20, 0, 0);

        stepTextView.setLayoutParams(textParams);
        stepTextView.setText(step);

        stepContentTextView.setLayoutParams(textParams);
        stepContentTextView.setText(content);
        stepContentTextView.setTextSize(17);
        stepContentTextView.setTextColor(textColor);

        stepTextView.setLayoutParams(textParams);
        stepTextView.setText(step);
        stepTextView.setTextSize(17);
        stepTextView.setTextColor(textColor);
        stepTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        solutionTextView.setLayoutParams(solutionTextParams);
        solutionTextView.setText(solution);
        solutionTextView.setTextSize(17);
        solutionTextView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        solutionTextView.setTextColor(textColor);

        linearLayout.setLayoutParams(linearLayouParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(stepTextView);
        linearLayout.addView(stepContentTextView);
        wrapper.addView(linearLayout);
        wrapper.addView(solutionTextView);

    }
    public String getSolution(ArrayList<String> leftHandSide, ArrayList<String> rightHandSide) {
        String leftHandSideSolution = "";
        String rightHandSideSolution = "";
        System.out.println(leftHandSide);

        for (int i=0; i < leftHandSide.size(); i++) {
            if (i == 0) {
                leftHandSideSolution = leftHandSide.get(0);
            } else {
                leftHandSideSolution = leftHandSideSolution + " " + getSolutionVar(leftHandSide.get(i));
            }
        }

        for (int i=0; i < rightHandSide.size(); i++) {
            if (i == 0) {
                rightHandSideSolution = rightHandSide.get(0);
            } else {
                rightHandSideSolution = rightHandSideSolution + " " + getSolutionVar(rightHandSide.get(i));
            }
        }
        return leftHandSideSolution + " = " + rightHandSideSolution;
    }

    public String getSolutionVar(String var) {
        char firstChar = var.charAt(0);
        if (Character.toString(firstChar).equals("+")) {
            return var.replace("+", "+ ");
        } else if (Character.toString(firstChar).equals("-")) {
            return var.replace("-", "- ");
        } else {
            return "+ " + var;
        }
    }

    // this code here simplifies the equation if it can be done
    public String simplifyExpression(ArrayList<String> expression) {
        Integer coefficient = 0;
        for (int i = 0; i < expression.size(); i++) {
            coefficient += getCoefficient(expression.get(i));
        }

        if (coefficient == 1) return variable;
        if (coefficient == -1) return "-" + variable;

        return coefficient.toString(coefficient) + variable;
    }

    public String simplifyConstants(ArrayList<String> constants) {

        Integer constantSum = 0;
        for (int i = 0; i < constants.size(); i++) {
            constantSum += Integer.parseInt(constants.get(i));
        }

        return constantSum.toString();
    }

    public boolean isValidEquation(){
        String sign = "";  //this store the sign
        int ovar = 0;
        String num = "";
        TextView textView = findViewById(R.id.error);


        if(equation.length() == 0){
            textView.setText("You did not input anything!");
            return false;
        }

        if(variable.equals("None")){
            textView.setText("Invalid equation! There is no variable.");
            return false;
        }

        if (!equation.contains("=")){
            textView.setText("Invalid equation! There is no equality sign in your equation");
            return false;
        }

        //Checking if there is more than one variable and equals sign(=) in the equation
        int numOfEqualitySign = 0;
        for(int j = 0; j < equation.length(); j++){
            if (equation.charAt(j) == '=') {
                numOfEqualitySign +=1;
                if (numOfEqualitySign > 1) {
                    textView.setText("Invalid equation! You have more than one equality sign in your equation");
                    return false;
                }
                numOfEqualitySign +=1;
            }

            if(Character.isLetter(equation.charAt(j)) && !Character.toString(equation.charAt(j)).equals(variable)) {
                textView.setText("Come on this is a linear equation solver you cant have more than one variable!");
                return false;
            }
        }

        if(equation.charAt(equation.length()-1) == '='){
            textView.setText("Invalid equation! You did not input anything after the equality sign(=)");
            return false;

        }

        else if(equation.charAt(0) == '='){
            textView.setText("Invalid equation! You did not input anything before the equality sign(=)");
            return false;
        }

        //checking if the equation ends with either + or - Or two or more signs come together
        for(int k = 0; k < equation.length(); k++) {
            if((equation.charAt(0) == '+' && equation.charAt(1) == '=') || (equation.charAt(0) == '-' && equation.charAt(1) == '=')){
                System.out.println("Invalid equation! It is wrong to have only " + equation.charAt(0) + " in the left hand side of the equation");
                return true;
            }
            if (equation.charAt(k) == '-' || equation.charAt(k) == '+'){
                sign = "" + equation.charAt(k);
                if(k == equation.length() -1){
                    System.out.println("Invalid equation! You can not end an equation with " + sign);
                    return true;
                }
                else if(equation.charAt(k+1) == '+' || equation.charAt(k+1) == '-'){

                    textView.setText("Invalid equation! Two or more signs(e.g +, -) can not be together ");
                    return true;
                }
            }
            if(Character.isLetter(equation.charAt(k))){
                if(k != equation.length() -1){

                    if(Character.isLetter(equation.charAt(k+1))){
                        textView.setText("Invalid equation! Two or more  variables can not be together. ");
                        return false;
                    }
                }
            }
            if(k != equation.length() -1){
                if(Character.isLetter(equation.charAt(k)) && Character.isDigit(equation.charAt(k+1)))
                {
                    textView.setText("Invalid equation! You are expected to input either \"+\" or \"-\"  sign after " +  equation.charAt(k) + " but you input " + equation.charAt(k+1) + " which is a number");
                    return false;
                }
            }
            if(equation.charAt(k) == '(' || equation.charAt(k) == ')'){
                sign = "" + equation.charAt(k);
                if(k != equation.length() -1){
                    if(equation.charAt(k+1) == '(' || equation.charAt(k+1) == ')'){
                        textView.setText("Invalid equation! You cant have two or more " + sign + " together");
                        return false;
                    }
                }
            }
        }


        for(int m = 0; m < equation.length(); m++) {
            if(equation.charAt(m) == '(' )ovar += 1;
            else if(equation.charAt(m) ==  ')')ovar -= 1;
            if(ovar > 1){
                textView.setText("Invalid equation! You are expected to put a close bracket \")\" after " + equation.charAt(m-1) + " not an open bracket \"(\" ");
                return false;
            }
            else if (ovar < 0){
                textView.setText("Invalid equation! You are expected to put a open bracket \"(\" after " + equation.charAt(m-1) + " not a close bracket \") \" ");
                return false;
            }

        }
        if(ovar == 1){
            textView.setText("Invalid equation! You didnt close a bracket you opened.");
            return false;
        }
        String hold = "";

        //making sure that the factorize number has nno variable;
        for(int i = 0; i < equation.length(); i++){
            if(equation.charAt(i) == '('){
                // check1= 0;
                // making the variable hold empty after each passing
                hold = "";
                // the variable j tells if the open bracket is the first element in the string and also try to get the first number in num.
                int j = i-1;

                int k = i+1;
                //trying to get the number that is used to multily everything in the bracket and assign it to the variable num
                if(j>= 0){
                    while(Character.isDigit(equation.charAt(j)) || Character.isLetter(equation.charAt(j))){
                        num = num + equation.charAt(j);
                        j -=1;
                        if (j < 0) break;
                    }
                }

                while (equation.charAt(k) != ')'){
                    hold = hold + equation.charAt(k);
                    k +=1;
                }
                for(int q = 0; q  < num.length(); q++){
                    if(Character.isLetter(num.charAt(q))){
                        textView.setText("Invalid equation! You are not allowed to open a bracket with a variable");
                        return false;
                    }
                }
                for(int q = 0; q  < hold.length(); q++){
                    if(hold.charAt(q) == '='){
                        textView.setText("Invalid equation! It does not make sense to have an equality sign inside a bracket");
                        return false;
                    }
                    if(hold.length() == 1 ){
                        if(hold.charAt(q) == '+' || hold.charAt(q) == '-'){
                            // TextView textView = findViewById(R.id.);
                            textView.setText("Invalid equation! It is wrong to have only " + hold.charAt(q) + " in a bracket");
                            return false;
                        }
                    }
                }
                num = "";
            }
        }
        return true;
    }

}