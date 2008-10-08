package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.query.ExprInfo;
import org.basex.util.TokenList;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** XML output flag. */
  protected boolean xml;
  /** Attribute Names. */
  private final TokenList nms = new TokenList();
  /** Attribute values. */
  private final TokenList vls = new TokenList();
  /** Parent Stack. */
  private final int[] parent = new int[256];
  /** Token Stack. */
  private final byte[][] token = new byte[256][];

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
    byte[] v = EMPTY;
    final int i = indexOf(n, ' ');
    if(i != -1) {
      v = substring(n, i + 1);
      n = substring(n, 0, i);
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
   * Serializes a node of the specified data reference.
   * @param data data reference
   * @param pre pre value to start from
   * @param level starting level
   * @return last pre value
   * @throws IOException exception
   */
  public final int node(final Data data, final int pre, final int level)
      throws IOException {

    // current output level
    int l = 0;
    int p = pre;
    final int s = pre + data.size(pre, data.kind(p));

    // loop through all table entries
    while(p < s) {
      if(finished()) return s;

      int k = data.kind(p);
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

        nms.reset();
        vls.reset();

        final int ps = p + data.size(p, k);
        final int as = p + data.attSize(p, k);
        int pp = p;

        // serialize attributes
        while(++p != as) attribute(data.attName(p), data.attValue(p));
        
        // add namespace definitions
        if(level == 0 && l == 0) {
          do {
            addNS(data, pp);
            pp = data.parent(pp, k);
            k = data.kind(pp);
          } while(k == Data.ELEM);
        } else {
          addNS(data, pp);
        }
        
        // serialize namespaces
        for(int n = 0; n < nms.size; n++) attribute(nms.list[n], vls.list[n]);

        // check if this is an empty tag
        if(as == ps) {
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
  
  /**
   * Adds namespaces for the specified arguments to the temporary
   * attribute arrays.
   * @param data data reference 
   * @param pre pre value
   */
  private void addNS(final Data data, final int pre) {
    final int[] ns = data.ns(pre);
    for(int n = 0; n < ns.length; n += 2) {
      byte[] key = data.ns.key(ns[n]);
      key = key.length == 0 ? XMLNS : concat(XMLNSC, key);
      if(nms.contains(key)) continue;
      nms.add(key);
      vls.add(data.ns.key(ns[n + 1]));
    }
  }
}
