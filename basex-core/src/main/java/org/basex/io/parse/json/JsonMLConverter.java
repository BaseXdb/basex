package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class converts a <a href="http://jsonml.org">JsonML</a> document to XML.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
final class JsonMLConverter extends JsonXmlConverter {
  /** State: element name expected. */
  private static final int UNNAMED = 0;
  /** State: element name assigned. */
  private static final int NAMED = 1;
  /** State: attributes assigned. */
  private static final int ATTRIBUTED = 2;
  /** State: element opened. */
  private static final int OPENED = 3;

  /** States of the open elements. */
  private final IntList states = new IntList();
  /** Name of the element to be opened (can be {@code null}). */
  private byte[] name;
  /** Name of the attribute to be added (can be {@code null}). */
  private byte[] attribute;
  /** Attributes object is being parsed. */
  private boolean inAtts;

  /**
   * Constructor.
   * @param opts JSON options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  JsonMLConverter(final JsonParserOptions opts, final XmlHandler handler) {
    super(opts, handler);
  }

  @Override
  protected void init(final String uri) {
    super.init(uri);
    states.reset();
    name = null;
    attribute = null;
    inAtts = false;
  }

  @Override
  protected void openObject() throws QueryException {
    if(inAtts || states.isEmpty() || states.peek() != NAMED) {
      throw error("No object allowed at this stage");
    }
    inAtts = true;
  }

  @Override
  protected void closeObject() {
    inAtts = false;
    states.pop();
    states.add(ATTRIBUTED);
  }

  @Override
  protected void openPair(final byte[] key) throws QueryException {
    attribute = check(key);
  }

  @Override
  protected void closePair() { }

  @Override
  protected void openArray() throws QueryException, IOException {
    if(inAtts || !states.isEmpty() && states.peek() == UNNAMED) {
      throw error("No array allowed at this stage");
    }
    if(!states.isEmpty()) flush();
    states.add(UNNAMED);
  }

  @Override
  protected void closeArray() throws QueryException, IOException {
    if(states.peek() == UNNAMED) throw error("Missing element name");
    flush();
    handler.closeElem();
    states.pop();
  }

  @Override
  protected void openItem() { }

  @Override
  protected void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) throws QueryException, IOException {
    if(inAtts) {
      atts.add(attribute, value);
      attribute = null;
    } else if(states.isEmpty()) {
      throw error("No value allowed at this stage");
    } else if(states.peek() == UNNAMED) {
      name = check(value);
      states.pop();
      states.add(NAMED);
    } else {
      flush();
      handler.text(value);
    }
  }

  @Override
  protected void numberLit(final byte[] value) throws QueryException {
    throw error("No numbers allowed");
  }

  @Override
  protected void nullLit() throws QueryException {
    throw error("No 'null' allowed");
  }

  @Override
  protected void booleanLit(final byte[] value) throws QueryException {
    throw error("No booleans allowed");
  }

  /**
   * Opens the current element if it has not been opened yet.
   * @throws IOException I/O exception
   */
  private void flush() throws IOException {
    if(states.peek() != OPENED) {
      handler.openElem(name, atts, NO_NSP);
      atts.reset();
      states.pop();
      states.add(OPENED);
    }
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return exception
   */
  private static QueryException error(final String msg, final Object... ext) {
    return JSON_PARSE_X.get(null, Util.info(msg, ext) + '.');
  }

  /**
   * Checks if the specified name is a valid NCName.
   * @param name name
   * @return name
   * @throws QueryException query exception
   */
  private static byte[] check(final byte[] name) throws QueryException {
    if(!XMLToken.isNCName(name)) throw error("Invalid name: \"%\"", name);
    return name;
  }
}
