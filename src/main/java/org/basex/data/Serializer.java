package org.basex.data;

import static org.basex.util.Token.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.util.Atts;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * This is an interface for serializing trees.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Namespaces. */
  private final Atts ns = new Atts().add(XML, XMLURI).add(EMPTY, EMPTY);

  /** Stack of opened tags. */
  protected final TokenList tags = new TokenList();
  /** Declare namespaces flag. */
  protected boolean undecl;

  /** Stack of namespace levels. */
  private final IntList nsl = new IntList();
  /** Flag for opened tag. */
  protected boolean inTag;

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
   * @param ftp full-text positions, used for visualization highlighting
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
    finishElement();
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
    tags.push(t);
    nsl.push(ns.size);
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

    if(!undecl && n.length != 0 && v.length == 0) return;
    final byte[] uri = ns(n);
    if(uri == null || !eq(uri, v)) {
      attribute(n.length == 0 ? XMLNS : concat(XMLNSC, n), v);
      ns.add(n, v);
    }
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
  public final void emptyElement(final ExprInfo expr, final byte[]... a)
      throws IOException {
    finishElement();
    emptyElement(name(expr), a);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  public final void closeElement() throws IOException {
    if(tags.size() == 0) throw new IOException("All elements closed.");
    final byte[] open = tags.pop();
    ns.size = nsl.pop();
    if(inTag) {
      inTag = false;
      empty();
    } else {
      close(open);
    }
  }

  /**
   * Closes the serializer.
   * @throws IOException I/O exception
   */
  public final void close() throws IOException {
    cls();
    if(tags.size() > 0) throw new IOException(
        "<" + string(tags.peek()) + "> still opened");
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
   * Gets the URI currently bound by the given prefix.
   * @param pre namespace prefix
   * @return URI if found, {@code null} otherwise
   */
  public final byte[] ns(final byte[] pre) {
    for(int i = ns.size - 1; i >= 0; i--)
      if(eq(ns.key[i], pre)) return ns.val[i];
    return null;
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
  final int node(final Data data, final int pre, final FTPosData ft)
      throws IOException {

    final TokenList nsp = data.ns.size() != 0 ? new TokenList() : null;
    final int[] parStack = new int[IO.MAXHEIGHT];
    final byte[][] names = new byte[IO.MAXHEIGHT][];
    names[0] = ns(EMPTY);

    int l = 0;
    int p = pre;

    // loop through all table entries
    final int s = pre + data.size(pre, data.kind(p));
    while(p < s && !finished()) {
      final int k = data.kind(p);
      final int r = data.parent(p, k);

      // close opened elements...
      while(l > 0 && parStack[l - 1] >= r) {
        closeElement();
        --l;
      }

      if(k == Data.DOC) {
        ++p;
      } else if(k == Data.TEXT) {
        final FTPos ftd = ft != null ? ft.get(data, p) : null;
        if(ftd != null) text(data.text(p++, true), ftd);
        else text(data.text(p++, true));
      } else if(k == Data.COMM) {
        comment(data.text(p++, true));
      } else if(k == Data.ATTR) {
        attribute(data.name(p, k), data.text(p++, false));
      } else if(k == Data.PI) {
        pi(data.name(p, k), data.atom(p++));
      } else {
        // add element node
        final byte[] name = data.name(p, k);
        openElement(name);

        // add namespace definitions
        byte[] empty = names[l];
        if(nsp != null) {
          // collect namespaces from database
          nsp.reset();
          int pp = p;
          do {
            final Atts atn = data.ns(pp);
            for(int n = 0; n < atn.size; ++n) {
              final byte[] key = atn.key[n];
              final byte[] val = atn.val[n];
              if(!nsp.contains(key)) {
                nsp.add(key);
                namespace(key, val);
                if(key.length == 0) empty = val;
              }
            }
            pp = data.parent(pp, data.kind(pp));
          } while(pp >= 0 && data.kind(pp) == Data.ELEM &&
              l == 0 && level() == 1);

          // check namespace of current element
          final byte[] key = pref(name);
          byte[] val = data.ns.uri(data.uri(p, k));
          if(val == null) val = EMPTY;
          if(key.length != 0) {
            if(ns.get(key) == -1) namespace(key, val);
          } else if(!eq(val, empty)) {
            namespace(key, val);
            empty = val;
          }
        } else if(l == 0 && ns(EMPTY) != EMPTY) {
          namespace(EMPTY, EMPTY);
        }

        // serialize attributes
        final int as = p + data.attSize(p, k);
        while(++p != as) {
          attribute(data.name(p, Data.ATTR), data.text(p, false));
        }
        parStack[l++] = r;
        names[l] = empty;
      }
    }

    // process remaining elements...
    while(--l >= 0) closeElement();
    return s;
  }

  // PROTECTED METHODS ========================================================

  /**
   * Returns the name of the specified expression.
   * @param expr expression
   * @return name
   */
  protected byte[] name(final ExprInfo expr) {
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
