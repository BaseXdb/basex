package org.basex.io.serial.json;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.options.Options.YesNo;
import org.junit.*;

/**
 * Tests for the {@link AdaptiveSerializer} classes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class AdaptiveSerializerTest extends SandboxTest {
  /**
   * Tests for the 'adaptive' serialization method.
   */
  @Test public void serialize() {
    // atomic items
    serialize("()", "");
    serialize("1", "1");
    serialize("1,2", "1\n2");

    // nodes
    serialize("<x/>", "<x/>");
    serialize("<x y='z'/>/@y", " y='z'");
    serialize("namespace x { 'y' }", " xmlns:x='y'");

    // function items
    serialize("exists#1", "function exists#1");
    serialize("fn:exists#1", "function fn:exists#1");
    serialize("Q{http://www.w3.org/2005/xpath-functions}exists#1", "function exists#1");
    serialize("function($a) { $a }", "function (anonymous)#1");
    serialize("exists(?)", "function (anonymous)#1");
    serialize("exists#1(?)", "function (anonymous)#1");
    serialize("true#0", "function true#0");

    // maps
    serialize("map { 'x': 'y' }", "{'x':'y'}");
    serialize("map { 'x': () }", "{'x':null}");
    serialize("map { 'x': (1,2) }", "{'x':1\n2}");
    serialize("map { 'x': true#0 }", "{'x':function true#0}");
    serialize("map { 'x': (true#0, false#0) }", "{'x':function true#0\nfunction false#0}");
    serialize("map { xs:date('2001-01-01'): 'd', '2001-01-01': 'd' }",
        "{'2001-01-01':'d','2001-01-01':'d'}");

    // arrays
    serialize("[ true#0 ]", "[function true#0]");
    serialize("[ (true#0, false#0) ]", "[function true#0\nfunction false#0]");
    serialize("[ (1,2) ]", "[1\n2]");
    serialize("[ () ]", "[null]");
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param expected expected result
   */
  private static void serialize(final String query, final String expected) {
    final ArrayOutput ao = new ArrayOutput();
    try(final QueryProcessor qp = new QueryProcessor(query, context)) {
      final SerializerOptions sopts = new SerializerOptions();
      sopts.set(SerializerOptions.METHOD, SerialMethod.ADAPTIVE);
      sopts.set(SerializerOptions.INDENT, YesNo.NO);

      final Serializer ser = Serializer.get(ao, sopts);
      for(final Item it : qp.value()) ser.serialize(it);
      ser.close();

      final String actual = normNL(ao.toString().replace("\"", "'"));
      assertEquals("\n[E] " + expected + "\n[F] " + actual + '\n', expected, actual);
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }
}
