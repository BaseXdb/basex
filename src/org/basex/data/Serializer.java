package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.query.ExprInfo;
import org.basex.query.expr.Expr;
import org.basex.util.Atts;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * This is an interface for serializing XML results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Namespaces. */
  public final Atts ns = new Atts();
  /** Current default namespace. */
  public byte[] dn = EMPTY;

  /** Opened tags. */
  private final TokenList tags = new TokenList();
  /** Namespace levels. */
  private final IntList nsl = new IntList();
  /** Flag for opened tag. */
  private boolean inTag;

  // === abstract methods =====================================================

  /**
   * Starts a result.
   * @throws IOException I/O exception
   */
  public abstract void openResult() throws IOException;

  /**
   * Closes a result.
   * @throws IOException I/O exception
   */
  public abstract void closeResult() throws IOException;

  /**
   * Serializes an attribute.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  public abstract void attribute(final byte[] n, final byte[] v)
    throws IOException;

  /**
   * Serializes a text.
   * @param b text bytes
   * @throws IOException I/O exception
   */
  public abstract void text(final byte[] b) throws IOException;

  /**
   * Serializes a text.
   * @param b text bytes
   * @param ftp full-text positions
   * @throws IOException I/O exception
   */
  public abstract void text(final byte[] b, final FTPos ftp) throws IOException;

  /**
   * Serializes a comment.
   * @param b comment
   * @throws IOException I/O exception
   */
  public abstract void comment(final byte[] b) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  public abstract void pi(final byte[] n, final byte[] v) throws IOException;

  /**
   * Serializes an item.
   * @param b text bytes
   * @throws IOException I/O exception
   */
  public abstract void item(final byte[] b) throws IOException;

  // === protected abstract methods ===========================================

  /**
   * Starts an element.
   * @param t tag
   * @throws IOException I/O exception
   */
  protected abstract void start(final byte[] t) throws IOException;

  /**
   * Finishes an empty element.
   * @throws IOException I/O exception
   */
  protected abstract void empty() throws IOException;

  /**
   * Finishes an element.
   * @throws IOException I/O exception
   */
  protected abstract void finish() throws IOException;

  /**
   * Closes an element.
   * @param t tag
   * @throws IOException I/O exception
   */
  protected abstract void close(final byte[] t) throws IOException;

  /**
   * Closes the serializer.
   * @throws IOException I/O exception
   */
  protected abstract void cls() throws IOException;

  // === implemented methods ==================================================

  /**
   * Opens an element.
   * @param expr expression info
   * @param a attributes
   * @throws IOException I/O exception
   */
  public final void openElement(final ExprInfo expr, final byte[]... a)
      throws IOException {
    openElement(name(expr), a);
  }

  /**
   * Opens an element.
   * @param t tag
   * @param a attributes
   * @throws IOException I/O exception
   */
  public final void openElement(final byte[] t, final byte[]... a)
      throws IOException {

    finishElement();
    tags.add(t);
    nsl.add(ns.size);
    inTag = true;
    start(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
  }

  /**
   * Serializes a namespace.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  public final void namespace(final byte[] n, final byte[] v)
      throws IOException {
    attribute(n.length == 0 ? XMLNS : concat(XMLNSC, n), v);
    ns.add(n, v);
  }

  /**
   * Opens and closes an empty element.
   * @param t tag
   * @param a attributes
   * @throws IOException I/O exception
   */
  public final void emptyElement(final byte[] t, final byte[]... a)
      throws IOException {
    openElement(t);
    for(int i = 0; i < a.length; i += 2) attribute(a[i], a[i + 1]);
    closeElement();
  }

  /**
   * Opens and closes an empty element.
   * @param expr expression info
   * @param a attributes
   * @throws IOException I/O exception
   */
  public final void emptyElement(final Expr expr, final byte[]... a)
      throws IOException {
    emptyElement(name(expr), a);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  public final void closeElement() throws IOException {
    if(tags.size() == 0) throw new IOException("All elements closed.");
    final int s = tags.size() - 1;
    tags.size(s);
    ns.size = nsl.get(s);
    if(inTag) {
      inTag = false;
      empty();
    } else {
      close(tags.get(tags.size()));
    }
  }

  /**
   * Closes the serializer.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    cls();
    if(tags.size() > 0) throw new IOException(
        "<" + string(tags.get(tags.size())) + "> still opened");
  }

  /**
   * Tests if the serialization was interrupted.
   * @return result of check
   */
  public boolean finished() {
    return false;
  }

  /**
   * Returns the current level.
   * @return level
   */
  public final int level() {
    return tags.size();
  }

  /**
   * Serializes a node of the specified data reference.
   * @param data data reference
   * @param pre pre value to start from
   * @return last pre value
   * @throws IOException I/O exception
   */
  public final int node(final Data data, final int pre) throws IOException {
    return node(data, pre, null);
  }

  /**
   * Serializes a node of the specified data reference.
   * @param data data reference
   * @param pre pre value to start from
   * @param ft full-text data
   * @return last pre value
   * @throws IOException I/O exception
   */
  public final int node(final Data data, final int pre, final FTPosData ft)
      throws IOException {

    final TokenList nsp = data.ns.size() != 0 ? new TokenList() : null;
    final int[] parent = new int[IO.MAXHEIGHT];
    final byte[][] names = new byte[IO.MAXHEIGHT][];
    names[0] = dn;

    int l = 0;
    int p = pre;

    // loop through all table entries
    final int s = pre + data.size(pre, data.kind(p));
    while(p < s && !finished()) {
      int k = data.kind(p);
      final int r = data.parent(p, k);

      // close opened elements...
      while(l > 0 && parent[l - 1] >= r) {
        closeElement();
        l--;
      }

      if(k == Data.DOC) {
        p++;
      } else if(k == Data.TEXT) {
        final FTPos ftd = ft != null ? ft.get(p) : null;
        if(ftd != null) text(data.text(p++), ftd);
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

        // add namespace definitions
        byte[] empty = names[l];
        if(nsp != null) {
          nsp.reset();
          int pp = p;
          // collect namespaces from database
          do {
            final int[] nm = data.ns(pp);
            for(int n = 0; n < nm.length; n += 2) {
              final byte[] key = data.ns.key(nm[n]);
              if(!nsp.contains(key)) {
                final byte[] val = data.ns.key(nm[n + 1]);
                nsp.add(key);
                namespace(key, val);
                if(key.length == 0) empty = val;
              }
            }
            pp = data.parent(pp, k);
            k = data.kind(pp);
          } while(k == Data.ELEM && l == 0 && tags.size() == 1);

          // check namespace of current element
          final byte[] key = pref(name);
          byte[] val = data.ns.key(data.tagNS(p));
          if(val == null) val = EMPTY;
          if(key.length != 0) {
            if(ns.get(key) == -1) namespace(key, val);
          } else if(!eq(val, empty)) {
            namespace(key, val);
            empty = val;
          }
        } else if(l == 0 && dn != EMPTY) {
          namespace(EMPTY, EMPTY);
        }

        // serialize attributes
        final int as = p + data.attSize(p, k);
        while(++p != as) attribute(data.attName(p), data.attValue(p));
        parent[l++] = r;
        names[l] = empty;
      }
    }

    // process remaining elements...
    while(--l >= 0) closeElement();
    return s;
  }

  // === protected methods ====================================================

  /**
   * Returns the name of the specified expression.
   * @param expr expression
   * @return name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected byte[] name(final ExprInfo expr) throws IOException {
    return token(expr.name());
  }

  /**
   * Finishes a new element node.
   * @throws IOException I/O exception
   */
  protected final void finishElement() throws IOException {
    if(!inTag) return;
    inTag = false;
    finish();
  }
}
