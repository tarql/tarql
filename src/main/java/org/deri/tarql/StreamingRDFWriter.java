package org.deri.tarql;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;
import org.apache.jena.riot.writer.WriterStreamRDFTuples;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Writes an iterator over triples to N-Triples or Turtle
 * in a streaming fashion, that is, without needing to hold
 * the entire thing in memory.
 * <p>
 * Instances are single-use.
 * <p>
 * There doesn't seem to be a pre-packaged version of this
 * functionality in Jena/ARQ that doesn't require a Graph or Model.
 */
public class StreamingRDFWriter {
	private final OutputStream out;
	private final Iterator<Triple> triples;
	
	public StreamingRDFWriter(OutputStream out, Iterator<Triple> triples) {
		this.out = out;
		this.triples = triples;
	}
	
	public void writeNTriples() {
		StreamRDF stream = new WriterStreamRDFTuples(new IndentedWriter(out));
		stream.start();
        StreamRDFLib.triplesToStream(stream, triples) ;
        stream.finish();
	}
	
	public void writeTurtle(String baseIRI, PrefixMapping prefixes) {
		WriterStreamRDFBlocks writer = new WriterStreamRDFBlocks(out);
		writer.start();
        writer.base(baseIRI);
        for (Entry<String, String> e: prefixes.getNsPrefixMap().entrySet()) {
            writer.prefix(e.getKey(), e.getValue()) ;
        }
        StreamRDFLib.triplesToStream(writer, triples) ;
        writer.finish();
	}
}
