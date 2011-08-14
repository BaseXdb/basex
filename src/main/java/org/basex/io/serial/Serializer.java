package org.basex.io.serial;

import static org.basex.util.Token.*;
import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import java.io.OutputStream;

import org.basex.data.Data;
import org.basex.data.ExprInfo;
import org.basex.data.FTPos;
import org.basex.data.FTPosData;
import org.basex.util.Atts;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * This is an interface for serializing trees.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Default serialization parameters. */
  protected static final SerializerProp PROPS = new SerializerProp();
  /** Stack of opened tags. */
  protected final TokenList tags = new TokenList();
  /** Current level. */
  protected int level;
  /** Current tag. */
  protected byte[] tag;
  /** Declare namespaces flag. */
  protected boolean undecl;

  /** Namespaces. */
  private final Atts ns = new Atts().add(XML, XMLURI).add(EMPTY, EMPTY);
  /** Stack of namespace levels. */
  private final IntList nsl = new IntList();
  /** Indicates if an element has not been completely opened yet. */
  private boolean elem;

  /**
   * Returns a specific serializer.
   * @param os output stream reference
   * @param props serialization properties (can be {@code null})
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os,
      final SerializerProp props) throws IOException {

    if(props != null) {
      final String m = props.check(
          S_METHOD, M_XML, M_XHTML, M_HTML, M_TEXT, M_JSON);
      if(M_JSON.equals(m)) return new JSONSerializer(os, props);
      if(M_TEXT.equals(m)) return new TextSerializer(os, props);
      if(M_HTML.equals(m)) return new HTMLSerializer(os, props);
      if(M_XHTML.equals(m)) return new XHTMLSerializer(os, props);
    }
    return new XMLSerializer(os, props);
  }

  // ABSTRACT METHODS =========================================================

  /**
   * Starts an element.
   * @param t tag
   * @throws IOException I/O exception
   */
  protected abstract void startOpen(final byte[] t) throws IOException;

  /**
   * Serializes an attribute.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  public abstract void attribute(final byte[] n, final byte[] v)
    throws IOException;

  /**
   * Finishes an opening element node.
   * @throws IOException I/O exception
   */
  protected abstract void finishOpen() throws IOException;

  /**
   * Closes an empty element.
   * @throws IOException I/O exception
   */
  protected abstract void finishEmpty() throws IOException;

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  protected abstract void finishClose() throws IOException;

  /**
   * Serializes a text.
   * @param b text bytes
   * @throws IOException I/O exception
   */
  public abstract void finishText(final byte[] b) throws IOException;

  /**
   * Serializes a comment.
   * @param b comment
   * @throws IOException I/O exception
   */
  public abstract void finishComment(final byte[] b) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  public abstract void finishPi(final byte[] n, final byte[] v)
      throws IOException;

  /**
   * Serializes an item.
   * @param b text bytes
   * @throws IOException I/O exception
   */
  public abstract void finishItem(final byte[] b) throws IOException;

  // IMPLEMENTED METHODS ======================================================

  /**
   * Serializes a text.
   * @param b text bytes
   * @throws IOException I/O exception
   */
  public final void text(final byte[] b) throws IOException {
    finishElement();
    finishText(b);
  }

  /**
   * Serializes a comment.
   * @param b comment
   * @throws IOException I/O exception
   */
  public final void comment(final byte[] b) throws IOException {
    finishElement();
    finishComment(b);
  }

  /**
   * Serializes a processing instruction.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  public final void pi(final byte[] n, final byte[] v) throws IOException {
    finishElement();
    finishPi(n, v);
  }

  /**
   * Serializes an item.
   * @param b text bytes
   * @throws IOException I/O exception
   */
  public final void item(final byte[] b) throws IOException {
    finishElement();
    finishItem(b);
  }

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
    nsl.push(ns.size);
    elem = true;
    tag = t;
    startOpen(t);
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
   * Closes an element.
   * @throws IOException I/O exception
   */
  public final void closeElement() throws IOException {
    ns.size = nsl.pop();
    if(elem) {
      finishEmpty();
      elem = false;
    } else {
      tag = tags.pop();
      level--;
      finishClose();
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
    emptyElement(name(expr), a);
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
  public final int node(final Data data, final int pre, final FTPosData ft)
      throws IOException {

    boolean doc = false;
    final TokenList nsp = data.ns.size() != 0 ? new TokenList() : null;
    final IntList pars = new IntList();
    final TokenList names = new TokenList();
    names.set(0, ns(EMPTY));

    int l = 0;
    int p = pre;

    // loop through all table entries
    final int s = pre + data.size(pre, data.kind(p));
    while(p < s && !finished()) {
      final int k = data.kind(p);
      final int r = data.parent(p, k);

      // close opened elements...
      while(l > 0 && pars.get(l - 1) >= r) {
        closeElement();
        --l;
      }

      if(k == Data.DOC) {
        if(doc) closeDoc();
        openDoc(data.text(p++, true));
        doc = true;
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
        byte[] empty = names.get(l);
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
              l == 0 && level == 1);

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
        pars.set(l++, r);
        names.set(l, empty);
      }
    }

    // process remaining elements...
    while(--l >= 0) closeElement();
    if(doc) closeDoc();
    return s;
  }

  /**
   * Serializes a text.
   * @param b text bytes
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  public final void text(final byte[] b, final FTPos ftp) throws IOException {
    finishElement();
    finishText(b, ftp);
  }

  /**
   * Serializes a text.
   * @param b text bytes
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void finishText(final byte[] b, final FTPos ftp) throws IOException {
    text(b);
  }

  // OVERWRITABLE METHODS =====================================================

  /**
   * Resets the serializer (indentation, etc).
   */
  public void reset() { }

  /**
   * Starts a result.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void openResult() throws IOException { }

  /**
   * Closes a result.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void closeResult() throws IOException { }

  /**
   * Closes the serializer.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }

  /**
   * Opens a document.
   * @param name name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void openDoc(final byte[] name) throws IOException { };

  /**
   * Closes a document.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void closeDoc() throws IOException { };

  /**
   * Returns the name of the specified expression.
   * @param expr expression
   * @return name
   */
  protected byte[] name(final ExprInfo expr) {
    return token(expr.name());
  }

  /**
   * Finishes an opening element node if necessary.
   * @throws IOException I/O exception
   */
  private void finishElement() throws IOException {
    if(!elem) return;
    elem = false;
    finishOpen();
    tags.push(tag);
    level++;
  }
}
