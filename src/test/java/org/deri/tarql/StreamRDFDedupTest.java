package org.deri.tarql;

import static org.deri.tarql.Helpers.triple;
import static org.deri.tarql.Helpers.triples;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.junit.Before;
import org.junit.Test;

public class StreamRDFDedupTest {
	List<Object> received;

	@Before
	public void setUp() throws Exception {
		received = new ArrayList<Object>();
	}

	@Test public void shouldPassThroughTriples() {
		StreamRDF dedup = new StreamRDFDedup(new MockStreamRDF());
		dedup.start();
		dedup.triple(triple("<a> <a> <a>"));
		dedup.triple(triple("<b> <b> <b>"));
		dedup.triple(triple("<c> <c> <c>"));
		dedup.finish();
		assertEquals(triples("<a> <a> <a>", "<b> <b> <b>", "<c> <c> <c>"), received);
	}
	
	@Test public void shouldRemoveDuplicateInWindow() {
		StreamRDF dedup = new StreamRDFDedup(new MockStreamRDF());
		dedup.start();
		dedup.triple(triple("<a> <a> <a>"));
		dedup.triple(triple("<a> <a> <a>"));
		dedup.finish();
		assertEquals(triples("<a> <a> <a>"), received);
	}
	
	@Test public void shouldNotRemoveDuplicateOutsideWindow() {
		StreamRDF dedup = new StreamRDFDedup(new MockStreamRDF(), 2);
		dedup.start();
		dedup.triple(triple("<a> <a> <a>"));
		dedup.triple(triple("<b> <b> <b>"));
		dedup.triple(triple("<a> <a> <a>"));
		dedup.triple(triple("<c> <c> <c>"));
		dedup.triple(triple("<a> <a> <a>"));
		dedup.finish();
		assertEquals(triples("<a> <a> <a>", "<b> <b> <b>", "<c> <c> <c>", "<a> <a> <a>"), received);
	}
	
	private class MockStreamRDF implements StreamRDF {
		@Override public void start() {}
		@Override public void triple(Triple triple) { received.add(triple); }
		@Override public void quad(Quad quad) { received.add(quad); }
		@Override public void base(String base) {}
		@Override public void prefix(String prefix, String iri) {}
		@Override public void finish() {}
	}
}
