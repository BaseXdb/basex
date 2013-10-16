package org.basex.test.io.serial.json;

import static org.junit.Assert.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.io.serial.json.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.test.*;
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

    error("<json type='array'><_/></json>", format);
    error("<json type='array'><item type='number'>x</item></json>", format);
    error("<json type='array'><item type='boolean'>x</item></json>", format);
  }

  /**
   * Serializes the specified input as JSON.
   * @param query query string
   * @param expected expected result
   * @param format format
   */
  private void serialize(final String query, final String expected, final JsonFormat format) {
    try {
      final String actual = serialize(query, format);
      assertEquals("\n[E] " + expected + "\n[F] " + actual + "\n", expected, actual);
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
    try {
      serialize(query, format);
      fail("Error expected: " + Err.BXJS_SERIAL);
    } catch(final QueryIOException ex) {
      assertEquals(Err.BXJS_SERIAL, ex.getCause().err());
    } catch(final Exception ex) {
      ex.printStackTrace();
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
  private String serialize(final String qu, final JsonFormat format) throws Exception {
    final QueryProcessor qp = new QueryProcessor(qu, context);
    final ArrayOutput ao = new ArrayOutput();

    final JsonSerialOptions jopts = new JsonSerialOptions();
    jopts.set(JsonSerialOptions.INDENT, false);
    jopts.set(JsonOptions.FORMAT, format);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON.toString());
    sopts.set(SerializerOptions.JSON, jopts);

    final Serializer ser = Serializer.get(ao, sopts);
    for(final Item it : qp.value()) ser.serialize(it);
    // replace quotes with apostrophes to increase readibility of tests
    return ao.toString().replace("\"", "'");
  }
}
