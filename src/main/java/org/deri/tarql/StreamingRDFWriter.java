package org.deri.tarql;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;
import org.apache.jena.riot.writer.WriterStreamRDFPlain;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDF;


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
		StreamRDF writer = new WriterStreamRDFPlain(new IndentedWriter(out));
		writer.start();
		StreamOps.sendTriplesToStream(triples, writer);
		writer.finish();
	}

	public void writeTurtle(String baseIRI, PrefixMapping prefixes) {
		// Auto-register RDF prefix so that rdf:type is displayed well
		// All other prefixes come from the query and should be as author intended
		prefixes = ensureRDFPrefix(prefixes);
		WriterStreamRDFBlocks writer = new WriterStreamRDFBlocks(out);
		writer.start();
		writer.base(baseIRI);
		for (Entry<String, String> e : prefixes.getNsPrefixMap().entrySet()) {
			writer.prefix(e.getKey(), e.getValue());
		}
		StreamOps.sendTriplesToStream(triples, writer);
		writer.finish();
	}
	
	private PrefixMapping ensureRDFPrefix(PrefixMapping prefixes) {
		// Some prefix already registered for the RDF namespace -- good enough
		if (prefixes.getNsURIPrefix(RDF.getURI()) != null) return prefixes;
		// rdf: is registered to something else -- give up
		if (prefixes.getNsPrefixURI("rdf") == null) return prefixes;
		// Register rdf:
		PrefixMapping newPrefixes = new PrefixMappingImpl();
		newPrefixes.setNsPrefixes(prefixes);
		newPrefixes.setNsPrefix("rdf", RDF.getURI());
		return newPrefixes;
	}
}
