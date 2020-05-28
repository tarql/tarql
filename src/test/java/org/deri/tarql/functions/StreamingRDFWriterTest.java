package org.deri.tarql.functions;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.deri.tarql.StreamingRDFWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

/**
 * StreamingRDFWriterTest
 */
public class StreamingRDFWriterTest {

    private final Iterator<Triple> triples;
    private ArrayList<Triple> list = new ArrayList<Triple>();

    public StreamingRDFWriterTest() {
        Triple t = Triple.create(NodeFactory.createURI("<http://ex.org/subject>"),
                NodeFactory.createURI("<http://ex.org/predicate>"), NodeFactory.createURI("<http://ex.org/object>"));

        list.add(t);
        triples = list.listIterator();
    }

    @Test
    public void shouldPassThroughTriples() throws IOException {

        File file = new File("src/test/resources/testJSON.json");
        PrintStream stream = new PrintStream(file);
        System.setOut(stream);

        final StreamingRDFWriter rdfWriter = new StreamingRDFWriter(System.out, triples);
        rdfWriter.writeJsonLD();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("src/test/resources/testJSON.json"));
            JSONObject jsonObject = (JSONObject) obj;
            assert (jsonObject.get("@id").equals("<http://ex.org/subject>") );
            assert (jsonObject.get("@context")) != null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}