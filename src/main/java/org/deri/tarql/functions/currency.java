package org.deri.tarql.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Filter function that returns a dateTime representation from an input string. <p/> Usage:
 * <pre>
 *     FILTER tarql:currency(?x, "USD")
 * </pre>
 * <p/> where ?x is a variable with a value like "$2,783,918,982", <p/> The variable ?x will be converted into the
 * standard format: "2783918982"^^<http://dbpedia.org/datatype/usDollar> <p/> <p>The currency abbreviations are
 * according to <a href="http://www.currency-iso.org/dam/downloads/table_a1.xml">ISO 4217</a> specification</p>
 */
public class currency extends FunctionBase2 {

    public currency() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue amount, NodeValue currency) {
        Locale locale = LocaleCurrency.getLocaleForCurrency(currency.asString());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        NodeValue node = null;
        try {
            Number currencyOut = currencyFormat.parse(amount.asString());
            String datatype = String.format("http://dbpedia.org/datatype/%s", LocaleCurrency.locales.get(currency.asString()).getLeft());
            node = NodeValue.makeNode(currencyOut.toString(), "en", datatype);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (node == null) {
            return NodeValue.makeString(amount.asString());
        } else {
            return node;
        }
    }

}

