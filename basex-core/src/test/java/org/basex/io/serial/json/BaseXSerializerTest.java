package org.basex.io.serial.json;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.util.options.Options.YesNo;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link BaseXSerializer} classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXSerializerTest extends SandboxTest {
  /**
   * Tests for the 'basex' serialization method.
   */
  @Test public void serialize() {
    // atomic items
    serialize("()", "");
    serialize("1", "1");
    serialize("1,2", "1\n2");

    // nodes
    serialize("<x/>", "<x/>");
    serialize("<x y='z'/>/@y", "y='z'");
    serialize("namespace x { 'y' }", "xmlns:x='y'");

    // function items
    serialize("exists#1", "fn:exists#1");
    serialize("fn:exists#1", "fn:exists#1");
    serialize("Q{http://www.w3.org/2005/xpath-functions}exists#1", "fn:exists#1");
    serialize("function($a) { $a }", "(anonymous-function)#1");
    serialize("exists(?)", "(anonymous-function)#1");
    serialize("exists#1(?)", "(anonymous-function)#1");
    serialize("true#0", "fn:true#0");

    // maps
    serialize("map { 'x': 'y' }", "map{'x':'y'}");
    serialize("map { 'x': () }", "map{'x':()}");
    serialize("map { 'x': (1,2) }", "map{'x':(1,2)}");
    serialize("map { 'x': true#0 }", "map{'x':fn:true#0}");
    serialize("map { 'x': (true#0, false#0) }", "map{'x':(fn:true#0,fn:false#0)}");
    serialize("map { xs:date('2001-01-01'): 'd', '2001-01-01': 'd' }",
        "map{xs:date('2001-01-01'):'d','2001-01-01':'d'}");

    // arrays
    serialize("[ true#0 ]", "[fn:true#0]");
    serialize("[ (true#0, false#0) ]", "[(fn:true#0,fn:false#0)]");
    serialize("[ (1,2) ]", "[(1,2)]");
    serialize("[ () ]", "[()]");

    serialize("[ <a/> ]", "[<a/>]");
    serialize("[ <a/> update () ]", "[<a/>]");
    serialize("[ document { <a/> } ]", "[<a/>]");
    serialize("[ document { <a/> } update () ]", "[<a/>]");
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param expected expected result
   */
  private static void serialize(final String query, final String expected) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      final SerializerOptions sopts = new SerializerOptions();
      sopts.set(SerializerOptions.INDENT, YesNo.NO);

      final ArrayOutput ao = qp.value().serialize(sopts);
      final String actual = normNL(ao.toString().replace("\"", "'"));
      assertEquals(expected, actual, "\n[E] " + expected + "\n[F] " + actual + '\n');
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }
}
