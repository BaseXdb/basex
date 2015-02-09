package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * <p>This class converts a <a href="http://jsonml.org">JsonML</a>
 * document to XML.
 * The specified JSON input is first transformed into a tree representation
 * and then converted to an XML document.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
final class JsonMLConverter extends JsonXmlConverter {
  /** Element stack. */
  private final Stack<FElem> stack = new Stack<>();
  /** Current attribute name. */
  private byte[] attName;

  /**
   * Constructor.
   * @param opts json options
   */
  JsonMLConverter(final JsonParserOptions opts) {
    super(opts);
  }

  @Override
  public FDoc finish() {
    return new FDoc().add(stack.pop());
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws QueryIOException query I/O exception
   */
  private static void error(final String msg, final Object... ext) throws QueryIOException {
    throw BXJS_PARSEML_X.getIO(Util.inf(msg, ext));
  }

  /**
   * Returns the specified name.
   * @param name name
   * @return cached QName
   * @throws QueryIOException query I/O exception
   */
  private static byte[] check(final byte[] name) throws QueryIOException {
    // retrieve name from cache, or create new instance
    if(!XMLToken.isNCName(name)) error("Invalid name: \"%\"", name);
    return name;
  }

  @Override
  public void openObject() throws QueryIOException {
    if(curr == null || attName != null || stack.peek() != null)
      error("No object allowed at this stage");
  }

  @Override
  public void openPair(final byte[] key) throws QueryIOException {
    attName = check(key);
  }

  @Override
  public void closePair(final boolean add) { }

  @Override
  public void closeObject() {
    stack.pop();
    stack.push(curr);
    curr = null;
  }

  @Override
  public void openArray() throws QueryIOException {
    if(!stack.isEmpty()) {
      if(attName == null && curr != null && stack.peek() == null) {
        stack.pop();
        stack.push(curr);
        curr = null;
      } else if(attName != null || curr != null || stack.peek() == null) {
        error("No array allowed at this stage");
      }
    }
    stack.push(null);
    curr = null;
  }

  @Override
  public void openItem() { }

  @Override
  public void closeItem() { }

  @Override
  public void closeArray() throws QueryIOException {
    FElem val = stack.pop();
    if(val == null) {
      val = curr;
      curr = null;
    }

    if(val == null) error("Missing element name");

    if(stack.isEmpty()) stack.push(val);
    else stack.peek().add(val);
  }

  @Override
  public void stringLit(final byte[] value) throws QueryIOException {
    if(attName == null && curr != null && stack.peek() == null) {
      stack.pop();
      stack.push(curr);
      curr = null;
    }

    if(curr == null) {
      final FElem e = stack.isEmpty() ? null : stack.peek();
      if(e == null) curr = new FElem(check(value));
      else e.add(new FTxt(value));
    } else if(attName != null) {
      curr.add(attName, value);
      attName = null;
    } else {
      error("No string allowed at this stage");
    }
  }

  @Override
  public void numberLit(final byte[] value) throws QueryIOException {
    error("No numbers allowed");
  }

  @Override
  public void nullLit() throws QueryIOException {
    error("No 'null' allowed");
  }

  @Override
  public void booleanLit(final byte[] b) throws QueryIOException {
    error("No booleans allowed");
  }

  @Override
  public void openConstr(final byte[] nm) throws QueryIOException {
    error("No constructor functions allowed");
  }

  @Override public void openArg() { }

  @Override public void closeArg() { }

  @Override public void closeConstr() { }
}
