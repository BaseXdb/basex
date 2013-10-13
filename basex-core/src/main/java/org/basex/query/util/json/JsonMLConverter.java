package org.basex.query.util.json;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;


/**
 * <p>This class converts a <a href="http://jsonml.org">JsonML</a>
 * document to XML.
 * The specified JSON input is first transformed into a tree representation
 * and then converted to an XML document.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public class JsonMLConverter extends JsonXMLConverter {
  /** Element stack. */
  final Stack<FElem> stack = new Stack<FElem>();

  /**
   * Constructor.
   * @param opts json options
   */
  public JsonMLConverter(final JsonOptions opts) {
    super(opts);
  }

  @Override
  public ANode convert(final String in) throws QueryIOException {
    final JsonMLHandler handler = new JsonMLHandler();
    stack.clear();
    JsonParser.parse(in, jopts, handler);
    return stack.pop();
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws QueryIOException query I/O exception
   */
  void error(final String msg, final Object... ext) throws QueryIOException {
    throw BXJS_PARSEML.thrwIO(Util.inf(msg, ext));
  }

  /**
   * Returns the specified name.
   * @param name name
   * @return cached QName
   * @throws QueryIOException query I/O exception
   */
  byte[] check(final byte[] name) throws QueryIOException {
    // retrieve name from cache, or create new instance
    if(!XMLToken.isNCName(name)) error("Invalid name: \"%\"", name);
    return name;
  }

  /** JSON handler. */
  private class JsonMLHandler implements JsonHandler {
    /** Current element. */
    private FElem curr;
    /** Current attribute name. */
    private byte[] attName;

    /** Constructor for visibility. */
    protected JsonMLHandler() { }

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
    public void closePair() throws QueryIOException { }

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
    public void closeItem() throws QueryIOException { }

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
    public void stringLit(final byte[] val) throws QueryIOException {
      if(attName == null && curr != null && stack.peek() == null) {
        stack.pop();
        stack.push(curr);
        curr = null;
      }

      if(curr == null) {
        final FElem elem = stack.isEmpty() ? null : stack.peek();
        if(elem == null) curr = new FElem(check(val));
        else elem.add(new FTxt(val));
      } else if(attName != null) {
        curr.add(attName, val);
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
    @Override public void closeArg() throws QueryIOException { }
    @Override public void closeConstr() throws QueryIOException { }
  }
}
