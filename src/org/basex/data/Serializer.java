package org.basex.data;

import java.io.IOException;
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
   * @throws IOException exception
   */
  public abstract void open(final int s) throws IOException;

  /**
   * Initializes the serializer.
   * @param s number of results
   * @throws IOException exception
   */
  public abstract void close(final int s) throws IOException;

  /**
   * Starts a result.
   * @throws IOException exception
   */
  public abstract void openResult() throws IOException;

  /**
   * Ends a result.
   * @throws IOException exception
   */
  public abstract void closeResult() throws IOException;

  /**
   * Starts an element.
   * @param expr expression info
   * @throws Exception exception
   */
  public final void startElement(final ExprInfo expr) throws Exception {
    startElement(name(expr));
  }

  /**
   * Starts an element.
   * @param t tag
   * @throws IOException exception
   */
  public abstract void startElement(final byte[] t) throws IOException;

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
   * @throws IOException exception
   */
  public final void openElement(final byte[] t, final byte[]... a)
      throws IOException {
    startElement(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    finishElement();
  }

  /**
   * Serializes an attribute.
   * @param n name
   * @param v value
   * @throws IOException exception
   */
  public abstract void attribute(final byte[] n, final byte[] v)
    throws IOException;

  /**
   * Finishes an empty element.
   * @param expr expression info
   * @param a attributes
   * @throws Exception exception
   */
  public final void emptyElement(final ExprInfo expr, final byte[]... a)
      throws Exception {
    emptyElement(name(expr), a);
  }

  /**
   * Finishes an empty element.
   * @param t tag
   * @param a attributes
   * @throws Exception exception
   */
  public final void emptyElement(final byte[] t, final byte[]... a)
      throws Exception {
    startElement(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    emptyElement();
  }

  /**
   * Finishes an empty element.
   * @throws IOException exception
   */
  public abstract void emptyElement() throws IOException;

  /**
   * Finishes an element.
   * @throws IOException exception
   */
  public abstract void finishElement() throws IOException;

  /**
   * Closes an element.
   * @param expr expression info
   * @throws Exception exception
   */
  public final void closeElement(final ExprInfo expr) throws Exception {
    closeElement(name(expr));
  }

  /**
   * Closes an element.
   * @param t tag
   * @throws IOException exception
   */
  public abstract void closeElement(final byte[] t) throws IOException;

  /**
   * Serializes a text.
   * @param b text bytes
   * @throws IOException exception
   */
  public abstract void text(final byte[] b) throws IOException;

  /**
   * Serializes a comment.
   * @param b comment
   * @throws IOException exception
   */
  public abstract void comment(final byte[] b) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param n name
   * @param v value
   * @throws IOException exception
   */
  public abstract void pi(final byte[] n, final byte[] v) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param c content
   * @throws IOException exception
   */
  public final void pi(final byte[] c) throws IOException {
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
   * @throws IOException exception
   */
  public abstract void item(final byte[] b) throws IOException;

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
   * @throws IOException exception
   */
  public final int xml(final Data data, final int pre) throws IOException {
    int p = pre;
    final int kind = data.kind(p);
    if(kind == Data.TEXT) {
      text(data.text(p++));
    } else if(kind == Data.ATTR) {
      attribute(data.attName(p), data.attValue(p++));
    } else if(kind == Data.DOC) {
      p = elem(data, p);
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
   * Writes the specified node and all its sub nodes.
   * @param data data reference
   * @param pr pre value to be written
   * @return last pre value
   * @throws IOException exception
   */
  public final int elem(final Data data, final int pr) throws IOException {
    // stacks
    final int[] parent = new int[data.meta.height];
    final byte[][] token = new byte[data.meta.height][];
    // current output level
    int l = 0;

    // loop through all table entries
    int p = pr;
    final int s = pr + data.size(pr, data.kind(pr));
    while(p < s) {
      if(finished()) return s;

      final int k = data.kind(p);
      final int pa = data.parent(p, k);
      // close opened tags...
      while(l > 0 && parent[l - 1] >= pa) closeElement(token[--l]);

      if(k == Data.DOC) {
        p++;
      } else if(k == Data.TEXT) {
        text(data.text(p++));
      } else if(k == Data.COMM) {
        comment(data.text(p++));
      } else if(k == Data.PI) {
        pi(data.text(p++));
      } else {
        // add element node
        final byte[] name = data.tag(p);
        startElement(name);

        // add element attributes
        final int ps = p + data.size(p, k);
        final int as = p + data.attSize(p, k);
        while(++p != as) attribute(data.attName(p), data.attValue(p));

        // check if this is an empty tag
        if(p == ps) {
          emptyElement();
        } else {
          finishElement();
          token[l] = name;
          parent[l++] = pa;
        }
      }
    }
    // process nodes that remain in the stack
    while(l > 0) closeElement(token[--l]);
    return p;
  }
}
