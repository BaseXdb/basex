package org.basex.io.serial.json;

import static org.junit.Assert.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.*;
import org.basex.util.Util;
import org.junit.*;

/**
 * Tests for the {@link JsonSerializer} classes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class JsonSerializerTest extends SandboxTest {
  /**
   * Tests for the 'direct' serialization format.
   */
  @Test public void direct() {
    final JsonFormat format = JsonFormat.DIRECT;
    error("'x'", format);
    error("1", format);
    error("true()", format);
    error("<_/>", format);
    error("<json/>", format);

    serialize("<json type='object'/>", "{}", format);
    serialize("<json type='object'><_/></json>", "{'':''}", format);
    serialize("<json type='object'><_ type='null'/></json>", "{'':null}", format);
    serialize("<json type='object'><a/></json>", "{'a':''}", format);
    serialize("<json type='object'><a>1</a></json>", "{'a':'1'}", format);
    serialize("<json type='object'><a>\"</a></json>", "{'a':'\\''}", format);
    serialize("<json type='object'><a>1</a><b/></json>", "{'a':'1','b':''}", format);

    error("<json type='object' name=\"x\"/>", format);
    error("<json type='object'><a name='X'/></json>", format);
    error("<json type='object'><_ type='number'/></json>", format);
    error("<json type='object'><_ type='boolean'/></json>", format);
    error("<json type='object'><_ type='x'>1</_></json>", format);

    serialize("<json type='array'/>", "[]", format);
    serialize("<json type='array'><_/></json>", "['']", format);
    serialize("<json type='array'><_>x</_></json>", "['x']", format);

    error("<json type='array'><X/></json>", format);
    error("<json type='array' name='X'><_/></json>", format);
    error("<json type='array'><_ name='X'/></json>", format);
    error("<json type='array'><_ _='_'/></json>", format);
    error("<json type='array'><_ type='number'>x</_></json>", format);
    error("<json type='array'><_ type='boolean'>x</_></json>", format);
  }

  /**
   * Tests for the 'attributes' serialization format.
   */
  @Test public void attributes() {
    final JsonFormat format = JsonFormat.ATTRIBUTES;
    error("'x'", format);
    error("1", format);
    error("true()", format);
    error("<_/>", format);
    error("<json/>", format);

    serialize("<json type='object'/>", "{}", format);
    serialize("<json type='object'><pair name=''/></json>", "{'':''}", format);
    serialize("<json type='object'><pair name='' type='null'/></json>", "{'':null}", format);
    serialize("<json type='object'><pair name='a'/></json>", "{'a':''}", format);
    serialize("<json type='object'><pair name='a'>1</pair></json>", "{'a':'1'}", format);
    serialize("<json type='object'><pair name='a'>\"</pair></json>", "{'a':'\\''}", format);
    serialize("<json type='object'><pair name='a'>1</pair><pair name='b'/></json>",
        "{'a':'1','b':''}", format);

    error("<json type='object'><_/></json>", format);
    error("<json type='object'><pair/></json>", format);
    error("<json type='object'><pair type='null'/></json>", format);
    error("<json type='object'><pair>1</pair></json>", format);

    serialize("<json type='array'/>", "[]", format);
    serialize("<json type='array'><item/></json>", "['']", format);
    serialize("<json type='array'><item/><item/></json>", "['','']", format);
    serialize("<json type='array'><item>x</item></json>", "['x']", format);

    serialize("<json/>", "''", format, JsonSpec.LIBERAL);
    serialize("<json>a</json>", "'a'", format, JsonSpec.LIBERAL);
    serialize("<json type='number'>1</json>", "1", format, JsonSpec.LIBERAL);

    error("<json type='array'><_/></json>", format);
    error("<json type='array'><item type='number'>x</item></json>", format);
    error("<json type='array'><item type='boolean'>x</item></json>", format);
    error("<json type='number'>x</json>", format, JsonSpec.LIBERAL);
    error("<json/>", format);
  }

  /**
   * Tests for the 'map' serialization format.
   */
  @Test public void map() {
    final JsonFormat format = JsonFormat.MAP;

    // objects
    serialize("{ }", "{}", format);
    serialize("{ '': () }", "{'':null}", format);
    serialize("{ 'A' : 'B' }", "{'A':'B'}", format);
    serialize("{ 'A': 1 }", "{'A':1}", format);
    serialize("{ 'A': 1.2 }", "{'A':1.2}", format);
    serialize("{ 'A': .2 }", "{'A':0.2}", format);
    serialize("{ 'A': .0 }", "{'A':0}", format);
    serialize("{ 'A': 1 div 0.0e0 }", "{'A':'INF'}", format);
    serialize("{ 'A': -1 div 0.0e0 }", "{'A':'-INF'}", format);
    serialize("{ 'A': 0 div 0.0e0 }", "{'A':'NaN'}", format);
    serialize("{ 'A': true() }", "{'A':true}", format);
    serialize("{ 'A': false() }", "{'A':false}", format);
    serialize("{ 'A': false() }", "{'A':false}", format);

    error("{ true(): false() }", format);
    error("{ true(): true#0 }", format);
    error("{ 'A': ('B','C') }", format);
    error("{ 'A': 'B', 'C': 'D', 1: 'E' }", format);
    error("{ 1: 'B', 2: 'C', 'C': 'D' }", format);

    // arrays
    serialize("{ 1:() }", "[null]", format);
    serialize("{ 1:2 }", "[2]", format);
    serialize("{ 1:2,2:3 }", "[2,3]", format);
    serialize("{ 1:2,3:4 }", "[2,null,4]", format);
    serialize("{ 3:4,1:2 }", "[2,null,4]", format);

    // mixed
    serialize("{ 'A':{} }", "{'A':{}}", format);
    serialize("{ 'A':{'B':'C'} }", "{'A':{'B':'C'}}", format);
    serialize("{ 'A':{1:'B'} }", "{'A':['B']}", format);
    serialize("{ 'A':{4:true(),2:{'C':'D'},1:0} }", "{'A':[0,{'C':'D'},null,true]}", format);

    // ECMA-262, liberal spec
    serialize("'A'", "'A'", format, JsonSpec.ECMA_262);
    serialize("'A'", "'A'", format, JsonSpec.LIBERAL);
    serialize("true()", "true", format, JsonSpec.LIBERAL);
    serialize("1", "1", format, JsonSpec.LIBERAL);
    serialize("(1,2)", "1 2", format, JsonSpec.LIBERAL);

    error("'A'", format);
    error("{ 0: () }", format);
    error("{ -1: () }", format);
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param expected expected result
   * @param format format
   */
  private void serialize(final String query, final String expected, final JsonFormat format) {
    serialize(query, expected, format, JsonSpec.RFC4627);
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param expected expected result
   * @param format format
   * @param spec spec
   */
  private void serialize(final String query, final String expected, final JsonFormat format,
      final JsonSpec spec) {
    try {
      final String actual = serialize(query, format, spec);
      assertEquals("\n[E] " + expected + "\n[F] " + actual + '\n', expected, actual);
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param format format
   */
  private void error(final String query, final JsonFormat format) {
    error(query, format, JsonSpec.RFC4627);
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param format format
   * @param spec spec
   */
  private void error(final String query, final JsonFormat format, final JsonSpec spec) {
    try {
      serialize(query, format, spec);
      fail("Error expected: " + Err.BXJS_SERIAL);
    } catch(final QueryIOException ex) {
      assertEquals(Err.BXJS_SERIAL, ex.getCause().err());
    } catch(final Exception ex) {
      Util.stack(ex);
      fail(ex.toString());
    }
  }

  /**
   * Serializes the specified input as JSON.
   * @param qu query string
   * @param format format
   * @param spec spec
   * @return result
   * @throws Exception exception
   */
  private static String serialize(final String qu, final JsonFormat format, final JsonSpec spec)
      throws Exception {

    final QueryProcessor qp = new QueryProcessor(qu, context);
    final ArrayOutput ao = new ArrayOutput();

    final JsonSerialOptions jopts = new JsonSerialOptions();
    jopts.set(JsonSerialOptions.INDENT, false);
    jopts.set(JsonOptions.FORMAT, format);
    jopts.set(JsonOptions.SPEC, spec);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);

    final Serializer ser = Serializer.get(ao, sopts);
    for(final Item it : qp.value()) ser.serialize(it);
    // replace quotes with apostrophes to increase legibility of tests
    return ao.toString().replace("\"", "'");
  }
}
