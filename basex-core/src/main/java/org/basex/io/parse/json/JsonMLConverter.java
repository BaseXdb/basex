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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
final class JsonMLConverter extends JsonXmlConverter {
  /** Current attributes. */
  private final TokenSet atts = new TokenSet();

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryIOException query I/O exception
   */
  JsonMLConverter(final JsonParserOptions opts) throws QueryIOException {
    super(opts);
  }

  @Override
  FNode finish() {
    return doc.add(stack.pop()).finish();
  }

  @Override
  void openObject() throws QueryIOException {
    if(curr == null || name != null || stack.peek() != null)
      error("No object allowed at this stage");
  }

  @Override
  void closeObject() {
    stack.pop();
    stack.push(curr);
    reset();
  }

  @Override
  void openPair(final byte[] key, final boolean add) throws QueryIOException {
    name = check(key);
    if(!atts.add(name)) error("Duplicate attribute found");
  }

  @Override
  void closePair(final boolean add) { }

  @Override
  void openArray() throws QueryIOException {
    if(!stack.isEmpty()) {
      if(name == null && curr != null && stack.peek() == null) {
        stack.pop();
        stack.push(curr);
      } else if(name != null || curr != null || stack.peek() == null) {
        error("No array allowed at this stage");
      }
    }
    stack.push(null);
    reset();
  }

  @Override
  void closeArray() throws QueryIOException {
    FBuilder value = stack.pop();
    if(value == null) {
      value = curr;
      reset();
    }
    if(value == null) error("Missing element name");

    if(stack.isEmpty()) stack.push(value);
    else stack.peek().add(value);
  }

  @Override
  void openItem() { }

  @Override
  void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) throws QueryIOException {
    if(name == null && curr != null && stack.peek() == null) {
      stack.pop();
      stack.push(curr);
      reset();
    }

    if(curr == null) {
      final FBuilder elem = stack.isEmpty() ? null : stack.peek();
      if(elem == null) curr = FElem.build(check(value));
      else elem.add(new FTxt(value));
    } else if(name != null) {
      curr.add(name, value);
      name = null;
    } else {
      error("No value allowed at this stage");
    }
  }

  @Override
  void stringLit(final byte[] value) throws QueryIOException {
    addValue(STRING, value);
  }

  @Override
  void numberLit(final byte[] value) throws QueryIOException {
    error("No numbers allowed");
  }

  @Override
  void nullLit() throws QueryIOException {
    error("No 'null' allowed");
  }

  @Override
  void booleanLit(final byte[] b) throws QueryIOException {
    error("No booleans allowed");
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
   * @throws QueryIOException query I/O exception
   */
  private static void error(final String msg, final Object... ext) throws QueryIOException {
    throw JSON_PARSE_X.getIO(Util.info(msg, ext) + '.');
  }

  /**
   * Returns the specified name.
   * @param name name
   * @return cached QName
   * @throws QueryIOException query I/O exception
   */
  private static byte[] check(final byte[] name) throws QueryIOException {
    if(!XMLToken.isNCName(name)) error("Invalid name: \"%\"", name);
    return name;
  }
}
