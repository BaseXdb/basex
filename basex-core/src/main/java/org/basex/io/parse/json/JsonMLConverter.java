package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class converts a <a href="http://jsonml.org">JsonML</a> document to XML.
 * The specified JSON input is first transformed into a tree representation
 * and then converted to an XML document.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
final class JsonMLConverter extends JsonXmlConverter {
  /** Current attributes. */
  private final TokenSet atts = new TokenSet();

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryException query exception
   */
  JsonMLConverter(final JsonParserOptions opts) throws QueryException {
    super(opts);
  }

  @Override
  FNode finish() {
    return doc.add(stack.pop()).finish();
  }

  @Override
  void openObject() throws QueryException {
    if(curr == null || name != null || stack.peek() != null)
      throw error("No object allowed at this stage");
  }

  @Override
  void closeObject() {
    stack.pop();
    stack.push(curr);
    reset();
  }

  @Override
  void openPair(final byte[] key, final boolean add) throws QueryException {
    name = shared.token(check(key));
    if(!atts.add(name)) throw error("Duplicate attribute found");
  }

  @Override
  void closePair(final boolean add) { }

  @Override
  void openArray() throws QueryException {
    if(!stack.isEmpty()) {
      if(name == null && curr != null && stack.peek() == null) {
        stack.pop();
        stack.push(curr);
      } else if(name != null || curr != null || stack.peek() == null) {
        throw error("No array allowed at this stage");
      }
    }
    stack.push(null);
    reset();
  }

  @Override
  void closeArray() throws QueryException {
    FBuilder value = stack.pop();
    if(value == null) {
      value = curr;
      reset();
    }
    if(value == null) throw error("Missing element name");

    if(stack.isEmpty()) stack.push(value);
    else stack.peek().add(value);
  }

  @Override
  void openItem() { }

  @Override
  void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) throws QueryException {
    if(name == null && curr != null && stack.peek() == null) {
      stack.pop();
      stack.push(curr);
      reset();
    }

    final byte[] val = shared.token(value);
    if(curr == null) {
      final FBuilder elem = stack.isEmpty() ? null : stack.peek();
      if(elem == null) curr = FElem.build(shared.qName(check(val)));
      else elem.add(new FTxt(val));
    } else if(name != null) {
      curr.add(shared.qName(name), val);
      name = null;
    } else {
      throw error("No value allowed at this stage");
    }
  }

  @Override
  void stringLit(final byte[] value) throws QueryException {
    addValue(STRING, value);
  }

  @Override
  void numberLit(final byte[] value) throws QueryException {
    throw error("No numbers allowed");
  }

  @Override
  void nullLit() throws QueryException {
    throw error("No 'null' allowed");
  }

  @Override
  void booleanLit(final byte[] b) throws QueryException {
    throw error("No booleans allowed");
  }

  /**
   * Resets the element creation.
   */
  private void reset() {
    curr = null;
    atts.clear();
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
   * Returns the specified name.
   * @param name name
   * @return cached QName
   * @throws QueryException query exception
   */
  private static byte[] check(final byte[] name) throws QueryException {
    if(!XMLToken.isNCName(name)) throw error("Invalid name: \"%\"", name);
    return name;
  }
}
