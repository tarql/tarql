package org.deri.tarql.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Filter function that returns a date representation from an input string. <p/> Usage:
 * <pre>
 *     FILTER tarql:date(?x, "d-MMM-yy")
 * </pre>
 * <p/> where ?x is a variable with a date in "d-MMM-yy" format, For example, "1/May/07". <p/> The variable ?x will be
 * converted into the standard format: "yyyy-MM-dd", For example "2007-05-01" <p/> <p>The Date Patterns used are the
 * ones specified for <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
 * class</p>
 */
public class date extends FunctionBase2 {

    public date() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(v2.asString());
        String formattedDate = null;
        try {
            Date dateStr = inputFormat.parse(v1.asString());
            inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            formattedDate = inputFormat.format(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (formattedDate == null || formattedDate.isEmpty()) {
            return NodeValue.makeString(v1.asString());
        } else {
            return NodeValue.makeDate(formattedDate);
        }
    }

}

