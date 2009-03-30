package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.query.ExprInfo;
import org.basex.util.Atts;
import org.basex.util.TokenList;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Namespaces. */
  public Atts ns = new Atts();
  /** Opened tags. */
  public TokenList tags = new TokenList();
  /** Current default namespace. */
  public byte[] dn = EMPTY;
  /** Flag for opened tag. */
  protected boolean inTag;

  // === abstract methods =====================================================

  /**
   * Finishes the serializer.
   * @throws IOException exception
   */
  public abstract void close() throws IOException;

  /**
   * Starts a result.
   * @throws IOException exception
   */
  public abstract void openResult() throws IOException;

  /**
   * Closes a result.
   * @throws IOException exception
   */
  public abstract void closeResult() throws IOException;

  /**
   * Serializes an attribute.
   * @param n name
   * @param v value
   * @throws IOException exception
   */
  public abstract void attribute(final byte[] n, final byte[] v)
    throws IOException;

  /**
   * Serializes a text.
   * @param b text bytes
   * @throws IOException exception
   */
  public abstract void text(final byte[] b) throws IOException;

  /**
   * Serializes a text.
   * @param b text bytes
   * @param ftd fulltext positions
   * @param fta fulltext ftand colorinfo
   * @throws IOException exception
   */
  public abstract void text(final byte[] b, final int[][] ftd, 
      final TokenList fta)
    throws IOException;

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
   * Serializes an item.
   * @param b text bytes
   * @throws IOException exception
   */
  public abstract void item(final byte[] b) throws IOException;

  // === protected abstract methods ===========================================

  /**
   * Starts an element.
   * @param t tag
   * @throws IOException exception
   */
  protected abstract void start(final byte[] t) throws IOException;

  /**
   * Finishes an empty element.
   * @throws IOException exception
   */
  protected abstract void empty() throws IOException;

  /**
   * Finishes an element.
   * @throws IOException exception
   */
  protected abstract void finish() throws IOException;

  /**
   * Closes an element.
   * @param t tag
   * @throws IOException exception
   */
  protected abstract void close(final byte[] t) throws IOException;

  // === implemented methods ==================================================

  /**
   * Opens an element.
   * @param expr expression info
   * @param a attributes
   * @throws IOException exception
   */
  public final void openElement(final ExprInfo expr, final byte[]... a)
      throws IOException {
    openElement(name(expr), a);
  }

  /**
   * Opens an element.
   * @param t tag
   * @param a attributes
   * @throws IOException exception
   */
  public final void openElement(final byte[] t, final byte[]... a)
      throws IOException {
    finishElement();
    tags.add(t);
    inTag = true;
    start(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
  }

  /**
   * Serializes a namespace.
   * @param n name
   * @param v value
   * @throws IOException exception
   */
  public final void namespace(final byte[] n, final byte[] v)
      throws IOException {
    attribute(n.length == 0 ? XMLNS : concat(XMLNSC, n), v);
    ns.add(n, v);
  }

  /**
   * Opens and closes an empty element.
   * @param expr expression info
   * @param a attributes
   * @throws IOException exception
   */
  public final void emptyElement(final ExprInfo expr, final byte[]... a)
      throws IOException {
    openElement(name(expr));
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    closeElement();
  }

  /**
   * Closes an element.
   * @throws IOException exception
   */
  public final void closeElement() throws IOException {
    if(inTag) {
      inTag = false;
      tags.size--;
      empty();
    } else {
      close(tags.list[--tags.size]);
    }
  }

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
   * @throws IOException exception
   */
  @SuppressWarnings("unused")
  protected byte[] name(final ExprInfo expr) throws IOException {
    return expr.name();
  }

  /**
   * Finishes a new element node.
   * @throws IOException exception
   */
  protected final void finishElement() throws IOException {
    if(!inTag) return;
    inTag = false;
    finish();
  }

  /**
   * Serializes a node of the specified data reference.
   * @param data data reference
   * @param pre pre value to start from
   * @return last pre value
   * @throws IOException exception
   */
  public final int node(final Data data, final int pre) throws IOException {
    return node(data, pre, null);
  }

  /**
   * Serializes a node of the specified data reference.
   * @param data data reference
   * @param pre pre value to start from
   * @param ft fulltext data
   * @return last pre value
   * @throws IOException exception
   */
  public final int node(final Data data, final int pre,
      final FTPosData ft) throws IOException {
    /** Namespaces. */
    final Atts nsp = new Atts();
    /** Parent Stack. */
    final int[] parent = new int[IO.MAXHEIGHT];
    /** Namespace Stack. */
    final byte[][] names = new byte[IO.MAXHEIGHT][];
    
    // current output level
    int l = 0;
    int p = pre;
    final int s = pre + data.size(pre, data.kind(p));

    names[l] = dn;
    
    // loop through all table entries
    while(p < s && !finished()) {
      int k = data.kind(p);
      final int pa = data.parent(p, k);

      // close opened tags...
      while(l > 0 && parent[l - 1] >= pa) {
        closeElement();
        l--;
      }

      if(k == Data.DOC) {
        p++;
      } else if(k == Data.TEXT) {
        final int[][] ftd = ft != null ? ft.get(p) : null;
        if(ftd != null) 
          text(data.text(p++), ftd, ft.col);
        else text(data.text(p++));
      } else if(k == Data.COMM) {
        comment(data.text(p++));
      } else if(k == Data.ATTR) {
        attribute(data.attName(p), data.attValue(p++));
      } else if(k == Data.PI) {
        byte[] n = data.text(p++);
        byte[] v = EMPTY;
        final int i = indexOf(n, ' ');
        if(i != -1) {
          v = substring(n, i + 1);
          n = substring(n, 0, i);
        }
        pi(n, v);
      } else {
        // add element node
        final byte[] name = data.tag(p);
        openElement(name);

        final int as = p + data.attSize(p, k);

        // add namespace definitions
        byte[] empty = null;
        if(data.ns.size() != 0) {
          nsp.reset();
          int pp = p;
          do {
            addNS(data, pp, nsp);
            pp = data.parent(pp, k);
            k = data.kind(pp);
          } while(tags.size == 1 && l == 0 && k == Data.ELEM);
          
          // serialize namespaces
          for(int n = 0; n < nsp.size; n++) {
            namespace(nsp.key[n], nsp.val[n]);
            if(nsp.key[n].length == 0) empty = nsp.val[n];
          }
          
          // add namespace for tag
          final byte[] pref = pre(name);
          byte[] uri = data.ns.key(data.tagNS(p));
          if(uri == null) uri = EMPTY;
          if(pref.length != 0) {
            if(ns.get(pref) == -1) namespace(pref, uri);
          } else if(!eq(uri, names[l])) {
            dn = uri;
            if(empty == null) {
              namespace(EMPTY, uri);
              empty = uri;
            }
          }
        }

        // serialize attributes
        while(++p != as) attribute(data.attName(p), data.attValue(p));
        parent[l++] = pa;
        names[l] = empty == null ? EMPTY : empty;
      }
    }
    // process nodes that remain in the stack
    while(--l >= 0) closeElement();
    return s;
  }

  /**
   * Adds namespaces for the specified arguments to the temporary
   * attribute arrays.
   * @param data data reference
   * @param pre pre value
   * @param nsp attribute reference
   */
  private void addNS(final Data data, final int pre, final Atts nsp) {
    final int[] nm = data.ns(pre);
    for(int n = 0; n < nm.length; n += 2) {
      final byte[] key = data.ns.key(nm[n]);
      nsp.addUnique(key, data.ns.key(nm[n + 1]));
      if(key.length == 0) dn = data.ns.key(nm[n + 1]);
    }
  }
}
