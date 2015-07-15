package org.deri.tarql.functions;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import org.junit.Test;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Parse date test. <p/> Documentation: <ul> <li><a href="http://stackoverflow.com/questions/9777689/how-to-get-numberformat-instance-from-currency-code"></a></li>
 * <li><a href="http://stackoverflow.com/questions/15586099/numberformat-parse-fails-for-some-currency-strings"></a></li>
 * <li><a href="http://tutorials.jenkov.com/java-internationalization/numberformat.html"></a></li> <li><a
 * href="https://nikhilsidhaye.wordpress.com/2011/03/31/formatting-and-parsing-currency/"></a></li> </ul>
 */
public class ParseCurrencyTest {

    @Test
    public void parseAmount() throws ParseException {
        String strCurrency = "$2,783,918,982";

        // treating the input as string, we can remove the currency symbol using string replace
        // strCurrency = strCurrency.replace(new DecimalFormatSymbols(Locale.US).getCurrencySymbol(), "");
        // or
        // String newStr = oldStr.replaceAll("[^\\d.]+", "")

        String thisCurrency = "USD";

        Locale locale = LocaleCurrency.getLocaleForCurrency(thisCurrency);
        // NumberFormat currencyFormat = NumberFormat.getInstance(Locale.US);
        // NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        Number currencyOut = currencyFormat.parse(strCurrency);
        assertEquals("$2,783,918,982.00", currencyFormat.format(currencyOut));

        String datatype = String.format("http://dbpedia.org/datatype/%s", LocaleCurrency.locales.get(thisCurrency).getLeft());
        NodeValue node = NodeValue.makeNode(currencyOut.toString(), "en", datatype);
        assertEquals("\"2783918982\"^^http://dbpedia.org/datatype/usDollar", node.asNode().toString());
    }

    @Test
    public void getCurrencySymbol() {
        Currency currency = Currency.getInstance("GBP");
        assertNotNull(currency);
        System.out.println(currency.getSymbol(Locale.UK));
    }

    @Test
    public void currency3() {
        // http://www.avajava.com/tutorials/lessons/how-do-i-display-the-currency-for-a-locale.html
        // http://lh.2xlibre.net/locales/

        Locale defaultLocale = Locale.getDefault();
        System.out.println(LocaleCurrency.displayCurrencyInfoForLocale(defaultLocale));

        Locale swedishLocale = new Locale("sv", "SE");
        System.out.println(LocaleCurrency.displayCurrencyInfoForLocale(swedishLocale));

        Locale chileanLocale = new Locale("es", "CL");
        System.out.println(LocaleCurrency.displayCurrencyInfoForLocale(chileanLocale));
    }

}
