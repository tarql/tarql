package org.deri.tarql.functions;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Parse date test.
 */
public class ParseDateTest {

    @Test
    public void parseDate() throws ParseException {
        String strDate = "1-May-07";
        SimpleDateFormat format = new SimpleDateFormat("d-MMM-yy");
        Date dateStr = format.parse(strDate);
        // System.out.println(dateStr);
        // should return Tue May 01 00:00:00 IST 2007
        assertEquals("Tue May 01 00:00:00 IST 2007", dateStr.toString());

        format = new SimpleDateFormat("yyy-MM-dd");
        String formattedDate = format.format(dateStr);
        // System.out.println(formattedDate);
        // should return 2007-05-01
        assertEquals("2007-05-01", formattedDate);
    }

    @Test
    public void parseDateTime() throws ParseException {
        // "2005-01-01T00:00:00Z"^^xsd:dateTime
        // "2004-12-31T19:01:00-05:00"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
        // "2011-01-10T14:45:13.815-05:00"^^xsd:dateTime
        // "2011-01-10T14:45:13.815"^^xsd:dateTime (without timezone)

        String strDateTime = "11-5-2014 11:11:51";
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Date dateStr = format.parse(strDateTime);
        // System.out.println(dateStr);
        // should print: Sun May 11 11:11:51 IST 2014
        assertEquals("Sun May 11 11:11:51 IST 2014", dateStr.toString());

        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formattedDateTime = format.format(dateStr);
        // System.out.println(formattedDateTime);
        // should print: 2014-05-11T11:11:51.000+01:00
        assertEquals("2014-05-11T11:11:51.000+01:00", formattedDateTime);
    }

}
