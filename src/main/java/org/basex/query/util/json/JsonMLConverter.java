package org.basex.query.util.json;

import static org.basex.query.util.Err.*;

import java.util.Stack;

import org.basex.query.QueryException;
import org.basex.query.util.*;
import org.basex.query.util.json.JsonParser.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.TokenObjMap;


/**
 * <p>This class converts a <a href="http://jsonml.org">JsonML</a>
 * document to XML.
 * The specified JSON input is first transformed into a tree representation
 * and then converted to an XML document.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public class JsonMLConverter extends JsonXMLConverter {
  /** Cached names. */
  private final TokenObjMap<QNm> qnames = new TokenObjMap<QNm>();
  /** Element stack. */
  final Stack<FElem> stack = new Stack<FElem>();

  /**
   * Constructor.
   * @param ii input info
   */
  public JsonMLConverter(final InputInfo ii) {
    super(ii);
  }

  @Override
  public ANode convert(final String in) throws QueryException {
    final JsonMLHandler handler = new JsonMLHandler();
    stack.clear();
    JsonParser.parse(in, Spec.RFC4627, true, handler, null);
    return stack.pop();
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws QueryException query exception
   */
  void error(final String msg, final Object... ext) throws QueryException {
    throw BXJS_PARSEML.thrw(info, Util.inf(msg, ext));
  }

  /**
   * Returns a cached {@link QNm} instance for the specified name.
   * @param name name
   * @return cached QName
   * @throws QueryException query exception
   */
  QNm qname(final byte[] name) throws QueryException {
    // retrieve name from cache, or create new instance
    QNm qname = qnames.get(name);
    if(qname == null) {
      if(!XMLToken.isNCName(name)) error("Invalid name: \"%\"", name);
      qname = new QNm(name);
      qnames.add(name, qname);
    }
    return qname;
  }

  /** JSON handler. */
  private class JsonMLHandler implements JsonHandler {
    /** Current element. */
    private FElem curr;
    /** Current attribute name. */
    private QNm attName;

    /** Constructor for visibility. */
    protected JsonMLHandler() { }

    @Override
    public void openObject() throws QueryException {
      if(curr == null || attName != null || stack.peek() != null)
        error("No object allowed at this stage");
    }

    @Override
    public void openEntry(final byte[] key) throws QueryException {
      attName = qname(key);
    }

    @Override
    public void closeEntry() throws QueryException { }

    @Override
    public void closeObject() {
      stack.pop();
      stack.push(curr);
      curr = null;
    }

    @Override
    public void openArray() throws QueryException {
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
    public void openArrayEntry() { }

    @Override
    public void closeArrayEntry() throws QueryException { }

    @Override
    public void closeArray() throws QueryException {
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
    public void stringLit(final byte[] val) throws QueryException {
      if(attName == null && curr != null && stack.peek() == null) {
        stack.pop();
        stack.push(curr);
        curr = null;
      }

      if(curr == null) {
        final FElem elem = stack.peek();
        if(elem == null) curr = new FElem(qname(val));
        else elem.add(new FTxt(val));
      } else if(attName != null) {
        curr.add(attName, val);
        attName = null;
      } else {
        error("No string allowed at this stage");
      }
    }

    @Override
    public void numberLit(final byte[] value) throws QueryException {
      error("No numbers allowed");
    }

    @Override
    public void nullLit() throws QueryException {
      error("No 'null' allowed");
    }

    @Override
    public void booleanLit(final boolean b) throws QueryException {
      error("No booleans allowed");
    }

    @Override
    public void openConstr(final byte[] nm) throws QueryException {
      error("No constructor functions allowed");
    }
    @Override public void openArg() { }
    @Override public void closeArg() throws QueryException { }
    @Override public void closeConstr() throws QueryException { }
  }
}
