package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.Options.YesNo;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link JsonSerializer} classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JsonSerializerTest extends SandboxTest {
  /**
   * Tests for the 'direct' serialization format.
   */
  @Test public void direct() {
    final JsonFormat format = JsonFormat.DIRECT;
    serialize("'x'", "'x'", format);
    serialize("1", "1", format);
    serialize("true()", "true", format);
    serialize("<_/>", "'<_\\/>'", format);

    serialize("<json/>", "''", format);
    serialize("<json type='object'/>", "{}", format);
    serialize("<json type='object'><_/></json>", "{'':''}", format);
    serialize("<json type='object'><_ type='null'/></json>", "{'':null}", format);
    serialize("<json type='object'><a/></json>", "{'a':''}", format);
    serialize("<json type='object'><a>1</a></json>", "{'a':'1'}", format);
    serialize("<json type='object'><a>\"</a></json>", "{'a':'\\''}", format);
    serialize("<json type='object'><a>1</a><b/></json>", "{'a':'1','b':''}", format);

    error("<json type='object' name=\"x\"/>", format, JSON_SERIALIZE_X);
    error("<json type='object'><a name='X'/></json>", format, JSON_SERIALIZE_X);
    error("<json type='object'><_ type='number'/></json>", format, JSON_SERIALIZE_X);
    error("<json type='object'><_ type='boolean'/></json>", format, JSON_SERIALIZE_X);
    error("<json type='object'><_ type='x'>1</_></json>", format, JSON_SERIALIZE_X);

    serialize("<json type='array'/>", "[]", format);
    serialize("<json type='array'><_/></json>", "['']", format);
    serialize("<json type='array'><_>x</_></json>", "['x']", format);

    error("<json type='array'><X/></json>", format, JSON_SERIALIZE_X);
    error("<json type='array' name='X'><_/></json>", format, JSON_SERIALIZE_X);
    error("<json type='array'><_ name='X'/></json>", format, JSON_SERIALIZE_X);
    error("<json type='array'><_ _='_'/></json>", format, JSON_SERIALIZE_X);
    error("<json type='array'><_ type='number'>x</_></json>", format, JSON_SERIALIZE_X);
    error("<json type='array'><_ type='boolean'>x</_></json>", format, JSON_SERIALIZE_X);
  }

  /**
   * Tests for the 'attributes' serialization format.
   */
  @Test public void attributes() {
    final JsonFormat format = JsonFormat.ATTRIBUTES;
    serialize("'x'", "'x'", format);
    serialize("1", "1", format);
    serialize("true()", "true", format);
    serialize("<_/>", "'<_\\/>'", format);

    serialize("<json/>", "''", format);
    serialize("<json type='object'/>", "{}", format);
    serialize("<json type='object'><pair name=''/></json>", "{'':''}", format);
    serialize("<json type='object'><pair name='' type='null'/></json>", "{'':null}", format);
    serialize("<json type='object'><pair name='a'/></json>", "{'a':''}", format);
    serialize("<json type='object'><pair name='a'>1</pair></json>", "{'a':'1'}", format);
    serialize("<json type='object'><pair name='a'>\"</pair></json>", "{'a':'\\''}", format);
    serialize("<json type='object'><pair name='a'>1</pair><pair name='b'/></json>",
        "{'a':'1','b':''}", format);

    error("<json type='object'><_/></json>", format, JSON_SERIALIZE_X);
    error("<json type='object'><pair/></json>", format, JSON_SERIALIZE_X);
    error("<json type='object'><pair type='null'/></json>", format, JSON_SERIALIZE_X);
    error("<json type='object'><pair>1</pair></json>", format, JSON_SERIALIZE_X);

    serialize("<json type='array'/>", "[]", format);
    serialize("<json type='array'><item/></json>", "['']", format);
    serialize("<json type='array'><item/><item/></json>", "['','']", format);
    serialize("<json type='array'><item>x</item></json>", "['x']", format);

    serialize("<json/>", "''", format);
    serialize("<json>a</json>", "'a'", format);
    serialize("<json type='number'>1</json>", "1", format);

    error("<json type='array'><_/></json>", format, JSON_SERIALIZE_X);
    error("<json type='array'><item type='number'>x</item></json>", format, JSON_SERIALIZE_X);
    error("<json type='array'><item type='boolean'>x</item></json>", format, JSON_SERIALIZE_X);
    error("<json type='number'>x</json>", format, JSON_SERIALIZE_X);
  }

  /**
   * Tests for the 'map' serialization format.
   */
  @Test public void map() {
    final JsonFormat format = JsonFormat.XQUERY;

    // objects
    serialize("map { }", "{}", format);
    serialize("map { '': () }", "{'':null}", format);
    serialize("map { 'A' : 'B' }", "{'A':'B'}", format);
    serialize("map { 'A': 1 }", "{'A':1}", format);
    serialize("map { 'A': 1.2 }", "{'A':1.2}", format);
    serialize("map { 'A': .2 }", "{'A':0.2}", format);
    serialize("map { 'A': .0 }", "{'A':0}", format);
    serialize("map { 'A': true() }", "{'A':true}", format);
    serialize("map { 'A': false() }", "{'A':false}", format);
    serialize("map { 'A': false() }", "{'A':false}", format);
    serialize("map { true(): false() }", "{'true':false}", format);
    serialize("map { 1: 'E' }", "{'1':'E'}", format);

    error("map { 'A': 1 div 0.0e0 }", format, SERNUMBER_X);
    error("map { 'A': -1 div 0.0e0 }", format, SERNUMBER_X);
    error("map { 'A': 0 div 0.0e0 }", format, SERNUMBER_X);

    error("map { true(): true#0 }", format, SERJSONFUNC_X);
    error("map { 'A': ('B','C') }", format, SERJSONSEQ);

    // arrays
    serialize("[()]", "[null]", format);
    serialize("[2]", "[2]", format);
    serialize("[2, 3]", "[2,3]", format);
    serialize("[2, (), 4]", "[2,null,4]", format);

    error("[ (1,2) ]", format, SERJSONSEQ);

    // mixed
    serialize("map { 'A': map {} }", "{'A':{}}", format);
    serialize("map { 'A': map { 'B': 'C' } }", "{'A':{'B':'C'}}", format);
    serialize("map { 'A': array { 'B' } }", "{'A':['B']}", format);
    serialize("map { '0': () }", "{'0':null}", format);
    serialize("map { '-1': () }", "{'-1':null}", format);

    // atomic values
    serialize("()", "null", format);
    serialize("'A'", "'A'", format);
    serialize("true()", "true", format);
    serialize("1", "1", format);
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param expected expected result
   * @param format format
   */
  private static void serialize(final String query, final String expected,
      final JsonFormat format) {
    try {
      final String actual = serialize(query, format);
      assertEquals(expected, actual, "\n[E] " + expected + "\n[F] " + actual + '\n');
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param format format
   * @param err expected error
   */
  private static void error(final String query, final JsonFormat format, final QueryError err) {
    try {
      final String s = serialize(query, format);
      fail("Expected: " + err + ", Returned: " + s);
    } catch(final QueryIOException ex) {
      assertEquals(err.name(), ex.getCause().error().name());
    } catch(final Exception ex) {
      Util.stack(ex);
      fail(ex.toString());
    }
  }

  /**
   * Serializes the specified input as JSON.
   * @param qu query string
   * @param format format
   * @return result
   * @throws Exception exception
   */
  private static String serialize(final String qu, final JsonFormat format) throws Exception {
    final ArrayOutput ao = new ArrayOutput();
    try(QueryProcessor qp = new QueryProcessor(qu, context)) {
      final JsonSerialOptions jopts = new JsonSerialOptions();
      jopts.set(JsonOptions.FORMAT, format);

      final SerializerOptions sopts = new SerializerOptions();
      sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
      sopts.set(SerializerOptions.INDENT, YesNo.NO);
      sopts.set(SerializerOptions.JSON, jopts);

      try(Serializer ser = Serializer.get(ao, sopts)) {
        for(final Item item : qp.value()) ser.serialize(item);
      }
    }
    // replace quotes with apostrophes to increase legibility of tests
    return ao.toString().replace("\"", "'");
  }
}
