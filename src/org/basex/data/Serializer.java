package org.basex.data;

import org.basex.query.ExprInfo;
import org.basex.util.Token;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** XML output flag. */
  protected boolean xml;

  /**
   * Initializes the serializer.
   * @param s number of results
   * @throws Exception exception
   */
  public abstract void open(final int s) throws Exception;

  /**
   * Initializes the serializer.
   * @param s number of results
   * @throws Exception exception
   */
  public abstract void close(final int s) throws Exception;

  /**
   * Starts a result.
   * @throws Exception exception
   */
  public abstract void openResult() throws Exception;

  /**
   * Ends a result.
   * @throws Exception exception
   */
  public abstract void closeResult() throws Exception;

  /**
   * Starts an element.
   * @param expr expression info
   * @throws Exception exception
   */
  public void startElement(final ExprInfo expr) throws Exception {
    startElement(name(expr));
  }

  /**
   * Starts an element.
   * @param t tag
   * @throws Exception exception
   */
  public abstract void startElement(final byte[] t) throws Exception;

  /**
   * Opens an element.
   * @param expr expression info
   * @param a attributes
   * @throws Exception exception
   */
  public final void openElement(final ExprInfo expr, final byte[]... a)
      throws Exception {
    startElement(name(expr));
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    finishElement();
  }

  /**
   * Opens an element.
   * @param t tag
   * @param a attributes
   * @throws Exception exception
   */
  public final void openElement(final byte[] t, final byte[]... a)
      throws Exception {
    startElement(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    finishElement();
  }

  /**
   * Serializes an attribute.
   * @param n name
   * @param v value
   * @throws Exception exception
   */
  public abstract void attribute(final byte[] n, final byte[] v)
    throws Exception;

  /**
   * Finishes an empty element.
   * @param expr expression info
   * @param a attributes
   * @throws Exception exception
   */
  public void emptyElement(final ExprInfo expr, final byte[]... a)
      throws Exception {
    emptyElement(name(expr), a);
  }

  /**
   * Finishes an empty element.
   * @param t tag
   * @param a attributes
   * @throws Exception exception
   */
  public void emptyElement(final byte[] t, final byte[]... a) throws Exception {
    startElement(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    emptyElement();
  }

  /**
   * Finishes an empty element.
   * @throws Exception exception
   */
  public abstract void emptyElement() throws Exception;

  /**
   * Finishes an element.
   * @throws Exception exception
   */
  public abstract void finishElement() throws Exception;

  /**
   * Closes an element.
   * @param expr expression info
   * @throws Exception exception
   */
  public void closeElement(final ExprInfo expr) throws Exception {
    closeElement(name(expr));
  }

  /**
   * Closes an element.
   * @param t tag
   * @throws Exception exception
   */
  public abstract void closeElement(final byte[] t) throws Exception;

  /**
   * Serializes a text.
   * @param b text bytes
   * @throws Exception exception
   */
  public abstract void text(final byte[] b) throws Exception;

  /**
   * Serializes a comment.
   * @param b comment
   * @throws Exception exception
   */
  public abstract void comment(final byte[] b) throws Exception;

  /**
   * Serializes a processing instruction.
   * @param n name
   * @param v value
   * @throws Exception exception
   */
  public abstract void pi(final byte[] n, final byte[] v) throws Exception;

  /**
   * Serializes a processing instruction.
   * @param c content
   * @throws Exception exception
   */
  public void pi(final byte[] c) throws Exception {
    byte[] n = c;
    byte[] v = Token.EMPTY;
    final int i = Token.indexOf(n, ' ');
    if(i != -1) {
      v = Token.substring(n, i + 1);
      n = Token.substring(n, 0, i);
    }
    pi(n, v);
  }

  /**
   * Serializes an item.
   * @param b text bytes
   * @throws Exception exception
   */
  public abstract void item(final byte[] b) throws Exception;

  /**
   * Tests if the serialization was interrupted.
   * @return result of check
   */
  public boolean finished() {
    return false;
  }

  /**
   * Returns the name of the specified expression.
   * @param expr expression
   * @return name
   */
  protected byte[] name(final ExprInfo expr) {
    return expr.name();
  }
  
  /**
   * Serializes the specified node and all its sub nodes.
   * @param data data reference
   * @param pre table position
   * @return last p value
   * @throws Exception exception
   */
  public int xml(final Data data, final int pre) throws Exception {
    int p = pre;
    final int kind = data.kind(p);
    if(kind == Data.TEXT) {
      text(data.text(p++));
    } else if(kind == Data.ATTR) {
      attribute(data.attName(p), data.attValue(p++));
    } else if(kind == Data.DOC) {
      p = elem(data, p + 1);
    } else if(kind == Data.COMM) {
      comment(data.text(p++));
    } else if(kind == Data.PI) {
      pi(data.text(p++));
    } else {
      p = elem(data, p);
    }
    return p;
  }
  
  /**
   * Writes the specified XML node and all its sub nodes to the specified
   * output stream.
   * @param data data reference
   * @param pos XML node to be written
   * @return last p value
   * @throws Exception exception
   */
  public int elem(final Data data, final int pos) throws Exception {
    // stacks
    final int[] parent = new int[256];
    final byte[][] token = new byte[256][];
    // current output level
    int l = 0;
    int p = pos;

    // start with the root node
    final int root = p;

    // loop through all table entries
    final int s = data.size;
    while(p < s) {
      if(finished()) return p;

      final int kind = data.kind(p);
      final int par = data.parent(p, kind);
      // skip writing if all sub nodes were processed
      if(root != 1 && p > root && par < root) break;
      
      // close opened tags...
      while(l > 0) {
        if(parent[l - 1] < par) break;
        closeElement(token[--l]);
      }

      if(kind == Data.TEXT) {
        text(data.text(p++));
      } else if(kind == Data.COMM) {
        comment(data.text(p++));
      } else if(kind == Data.PI) {
        pi(data.text(p++));
      } else {
        // add element node
        final byte[] tok = data.tag(p);

        // add element attributes
        final int ps = p + data.size(p, kind);
        final int as = p + data.attSize(p, kind);
        startElement(tok);
        while(++p != as) attribute(data.attName(p), data.attValue(p));

        // check if this is an empty tag
        if(p == ps) {
          emptyElement();
        } else {
          finishElement();
          token[l] = tok;
          parent[l++] = par;
        }
      }
    }
    // process nodes that remain in the stack
    while(l > 0) closeElement(token[--l]);
    return p;
  }
}
