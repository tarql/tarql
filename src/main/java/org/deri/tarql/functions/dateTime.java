package org.deri.tarql.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Filter function that returns a dateTime representation from an input string.
 *
 * Usage:
 * <pre>
 *     FILTER tarql:date(?x, "d-MMM-yy")
 * </pre>
 *
 * where ?x is a variable with a date in "d-MMM-yy" format, For example, "1/May/07".
 *
 * The variable ?x will be converted into the standard format: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", For example
 * "2007-05-01T00:00:00.000+01:00"
 *
 * <p>The Date and Time Patterns used are the ones specified for <a href="http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
 * class</p>
 */
public class dateTime extends FunctionBase2 {

    public dateTime() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(v2.asString());
        String formattedDateTime = null;
        try {
            Date dateStr = inputFormat.parse(v1.asString());
            inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            formattedDateTime = inputFormat.format(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (formattedDateTime == null || formattedDateTime.isEmpty()) {
            return NodeValue.makeString(v1.asString());
        } else {
            return NodeValue.makeDateTime(formattedDateTime);
        }
    }

}

