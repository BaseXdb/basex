package org.basex.io.serial;

import org.basex.data.Data;
import static org.basex.data.DataText.*;
import org.basex.data.ExprInfo;
import org.basex.data.FTPos;
import org.basex.data.FTPosData;
import static org.basex.io.serial.SerializerProp.S_METHOD;
import static org.basex.query.QueryText.XMLURI;
import org.basex.query.item.Item;
import org.basex.util.Atts;
import static org.basex.util.Token.*;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an interface for serializing trees.
 *
 * @author BaseX Team 2005-12, BSD License
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

  /** Currently available namespaces. */
  private final Atts ns = new Atts(XML, XMLURI).add(EMPTY, EMPTY);
  /** Namespace stack. */
  private final IntList nsl = new IntList();
  /** Indicates if an element has not been completely opened yet. */
  private boolean elem;

  /**
   * Returns an XML serializer.
   * @param os output stream reference
   * @return serializer
   * @throws IOException I/O exception
   */
  public static XMLSerializer get(final OutputStream os) throws IOException {
    return new XMLSerializer(os, PROPS);
  }

  /**
   * Returns a specific serializer.
   * @param os output stream reference
   * @param props serialization properties (can be {@code null})
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os,
      final SerializerProp props) throws IOException {

    if(props == null) return get(os);
    final String m = props.check(S_METHOD,
        M_XML, M_XHTML, M_HTML, M_TEXT, M_JSON, M_JSONML, M_RAW);
    if(M_XHTML.equals(m))  return new XHTMLSerializer(os, props);
    if(M_HTML.equals(m))   return new HTMLSerializer(os, props);
    if(M_TEXT.equals(m))   return new TextSerializer(os, props);
    if(M_JSON.equals(m))   return new JSONSerializer(os, props);
    if(M_JSONML.equals(m)) return new JsonMLSerializer(os, props);
    if(M_RAW.equals(m))    return new RawSerializer(os, props);
    return new XMLSerializer(os, props);
  }

  // FINAL PUBLIC METHODS =====================================================

  /**
   * Opens an element.
   * @param name tag name
   * @param atts attributes
   * @throws IOException I/O exception
   */
  public final void openElement(final byte[] name, final byte[]... atts)
      throws IOException {

    finishElement();
    nsl.push(ns.size());
    elem = true;
    tag = name;
    startOpen(name);
    for(int i = 0; i < atts.length; i += 2) attribute(atts[i], atts[i + 1]);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  public final void closeElement() throws IOException {
    ns.size(nsl.pop());
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
   * @param name tag name
   * @param atts attributes
   * @throws IOException I/O exception
   */
  public final void emptyElement(final byte[] name, final byte[]... atts)
      throws IOException {

    openElement(name, atts);
    closeElement();
  }


  /**
   * Serializes a text.
   * @param value text bytes
   * @throws IOException I/O exception
   */
  public final void text(final byte[] value) throws IOException {
    finishElement();
    finishText(value);
  }

  /**
   * Serializes a comment.
   * @param value value
   * @throws IOException I/O exception
   */
  public final void comment(final byte[] value) throws IOException {
    finishElement();
    finishComment(value);
  }

  /**
   * Serializes a processing instruction.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  public final void pi(final byte[] name, final byte[] value)
      throws IOException {
    finishElement();
    finishPi(name, value);
  }

  /**
   * Serializes an item. This method is internally called;
   * call {@link Item#serialize} instead.
   * @param item text bytes
   * @throws IOException I/O exception
   */
  public final void item(final Item item) throws IOException {
    finishElement();
    finishItem(item);
  }

  /**
   * Serializes a node of the specified data reference.
   *
   * @param data data reference
   * @param pre pre value to start from
   * @throws IOException I/O exception
   */
  public final void node(final Data data, final int pre) throws IOException {
    node(data, pre, null);
  }

  /**
   * Serializes a node of the specified data reference.
   *
   * @param data data reference
   * @param pre pre value to start from
   * @param ft full-text data
   * @throws IOException I/O exception
   */
  public final void node(final Data data, final int pre, final FTPosData ft)
      throws IOException {

    boolean doc = false;
    final TokenList nsp = data.nspaces.size() != 0 ? new TokenList() : null;
    final IntList pars = new IntList();
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
        if(nsp != null) {
          // add namespaces from database
          nsp.reset();
          int pp = p;

          // check namespace of current element
          byte[] key = prefix(name);
          byte[] val = data.nspaces.uri(data.uri(p, k));
          if(val == null) val = EMPTY;
          // add new or updated namespace
          final byte[] old = ns(key);
          if(old == null || !eq(old, val)) namespace(key, val);

          do {
            final Atts atn = data.ns(pp);
            for(int n = 0; n < atn.size(); ++n) {
              key = atn.name(n);
              val = atn.string(n);
              if(!nsp.contains(key)) {
                nsp.add(key);
                namespace(key, val);
              }
            }
            // check ancestors only on top level
            if(level != 0 || l != 0) break;

            pp = data.parent(pp, data.kind(pp));
          } while(pp >= 0 && data.kind(pp) == Data.ELEM);
        }

        // serialize attributes
        final int as = p + data.attSize(p, k);
        while(++p != as) {
          attribute(data.name(p, Data.ATTR), data.text(p, false));
        }
        pars.set(l++, r);
      }
    }

    // process remaining elements...
    while(--l >= 0) closeElement();
    if(doc) closeDoc();
  }

  /**
   * Opens an element, adopting the expression name as element name.
   * @param expr expression info
   * @param atts attributes
   * @throws IOException I/O exception
   */
  public final void openElement(final ExprInfo expr, final byte[]... atts)
      throws IOException {
    openElement(info(expr), atts);
  }

  /**
   * Opens and closes an empty element, adopting the expression name as
   * element name.
   * @param expr expression info
   * @param atts attributes
   * @throws IOException I/O exception
   */
  public final void emptyElement(final ExprInfo expr, final byte[]... atts)
      throws IOException {
    emptyElement(info(expr), atts);
  }

  // OVERWRITABLE PUBLIC METHODS ==============================================

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
   * Serializes a namespace if it has not been serialized by an ancestor yet.
   * @param pref prefix
   * @param uri URI
   * @throws IOException I/O exception
   */
  public void namespace(final byte[] pref, final byte[] uri)
      throws IOException {

    if(!undecl && pref.length != 0 && uri.length == 0) return;
    final byte[] u = ns(pref);
    if(u == null || !eq(u, uri)) {
      attribute(pref.length == 0 ? XMLNS : concat(XMLNSC, pref), uri);
      ns.add(pref, uri);
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
   * Resets the serializer (indentation, etc).
   */
  public void reset() { }

  /**
   * Serializes an attribute.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  public abstract void attribute(final byte[] name, final byte[] value)
      throws IOException;

  // PROTECTED METHODS ========================================================

  /**
   * Starts an element.
   * @param n tag name
   * @throws IOException I/O exception
   */
  protected abstract void startOpen(final byte[] n) throws IOException;

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
   * @param v value
   * @throws IOException I/O exception
   */
  protected abstract void finishText(final byte[] v) throws IOException;

  /**
   * Serializes a comment.
   * @param v value
   * @throws IOException I/O exception
   */
  protected abstract void finishComment(final byte[] v) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param n name
   * @param v value
   * @throws IOException I/O exception
   */
  protected abstract void finishPi(final byte[] n, final byte[] v)
      throws IOException;

  /**
   * Serializes an item.
   * @param i item
   * @throws IOException I/O exception
   */
  protected abstract void finishItem(final Item i) throws IOException;

  /**
   * Serializes a text.
   * @param v value
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void finishText(final byte[] v, final FTPos ftp)
      throws IOException {
    text(v);
  }

  /**
   * Opens a document.
   * @param n name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void openDoc(final byte[] n) throws IOException { }

  /**
   * Closes a document.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void closeDoc() throws IOException { }

  /**
   * Returns the name of the specified expression.
   * @param expr expression
   * @return name
   */
  byte[] info(final ExprInfo expr) {
    return token(expr.info());
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Gets the URI currently bound by the given prefix.
   * @param pref namespace prefix
   * @return URI if found, {@code null} otherwise
   */
  private byte[] ns(final byte[] pref) {
    for(int i = ns.size() - 1; i >= 0; i--)
      if(eq(ns.name(i), pref)) return ns.string(i);
    return null;
  }

  /**
   * Serializes a text.
   * @param v text bytes
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  private void text(final byte[] v, final FTPos ftp) throws IOException {
    finishElement();
    finishText(v, ftp);
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
