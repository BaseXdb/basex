package org.basex.build;

import static org.basex.build.BuildText.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Namespaces;
import org.basex.index.Names;
import org.basex.index.path.PathSummary;
import org.basex.io.IO;
import org.basex.util.Atts;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.basex.util.list.IntList;

/**
 * This class provides an interface for building database instances.
 * The specified {@link Parser} sends events to this class whenever nodes
 * are to be added or closed. The builder implementation decides whether
 * the nodes are stored on disk or kept in memory.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Builder extends Progress {
  /** Meta data on built database. */
  public MetaData meta;

  /** Tree structure. */
  protected final PathSummary path = new PathSummary(null);
  /** Namespace index. */
  protected final Namespaces ns = new Namespaces();
  /** Parser instance. */
  protected final Parser parser;
  /** Property instance. */
  protected final Prop prop;
  /** Database name. */
  protected final String name;

  /** Tag name index. */
  protected final Names tags;
  /** Attribute name index. */
  protected final Names atts;

  /** Number of cached size values. */
  protected int ssize;
  /** Currently stored size value. */
  protected int spos;

  /** Parent stack. */
  private final IntList pstack = new IntList();
  /** Tag stack. */
  private final IntList tstack = new IntList();
  /** Size stack. */
  private boolean inDoc;
  /** Current tree height. */
  private int lvl;
  /** Element counter. */
  private int c;

  /**
   * Constructor.
   * @param nm name of database
   * @param parse parser
   * @param pr properties
   */
  protected Builder(final String nm, final Parser parse, final Prop pr) {
    parser = parse;
    prop = pr;
    name = nm;
    final int cats = pr.num(Prop.CATEGORIES);
    tags = new Names(cats);
    atts = new Names(cats);
  }

  // PUBLIC METHODS ===========================================================

  /**
   * Builds the database.
   * @throws IOException I/O exception
   */
  protected final void parse() throws IOException {
    final Performance perf = Util.debug ? new Performance() : null;
    Util.debug(tit() + DOTS);

    // add document node and parse document
    parser.parse(this);
    if(lvl != 0) error(DOCOPEN, parser.detail(), tags.key(tstack.get(lvl)));

    // no nodes inserted: add default document node
    if(meta.size == 0) {
      startDoc(token(name));
      endDoc();
      setSize(0, meta.size);
    }
    // lastid should reflect the fact that the default document was added
    meta.lastid = meta.size - 1;

    Util.gc(perf);
  }

  /**
   * Opens a document node.
   * @param value document name
   * @throws IOException I/O exception
   */
  public final void startDoc(final byte[] value) throws IOException {
    pstack.set(lvl++, meta.size);
    if(meta.pathindex) path.index(0, Data.DOC, lvl);
    addDoc(value);
    ns.open();
  }

  /**
   * Closes a document node.
   * @throws IOException I/O exception
   */
  public final void endDoc() throws IOException {
    final int pre = pstack.get(--lvl);
    setSize(pre, meta.size - pre);
    meta.ndocs++;
    ns.close(meta.size);
    inDoc = false;
  }

  /**
   * Adds a new namespace; called by the building instance.
   * @param pref the namespace prefix
   * @param uri namespace uri
   */
  public final void startNS(final byte[] pref, final byte[] uri) {
    ns.add(pref, uri, meta.size);
  }

  /**
   * Opens a new element node.
   * @param nm tag name
   * @param att attributes
   * @return preValue of the created node
   * @throws IOException I/O exception
   */
  public final int startElem(final byte[] nm, final Atts att)
      throws IOException {

    final int pre = addElem(nm, att);
    ++lvl;
    return pre;
  }

  /**
   * Stores an empty element.
   * @param nm tag name
   * @param att attributes
   * @throws IOException I/O exception
   */
  public final void emptyElem(final byte[] nm, final Atts att)
      throws IOException {

    addElem(nm, att);
    final int pre = pstack.get(lvl);
    ns.close(pre);
    if(att.size() > IO.MAXATTS) setSize(pre, meta.size - pre);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  public final void endElem() throws IOException {
    checkStop();
    --lvl;
    final int pre = pstack.get(lvl);
    setSize(pre, meta.size - pre);
    ns.close(pre);
  }

  /**
   * Stores a text node.
   * @param value text value
   * @throws IOException I/O exception
   */
  public final void text(final byte[] value) throws IOException {
    // chop whitespaces in text nodes
    final byte[] t = meta.chop ? trim(value) : value;

    // check if text appears before or after root node
    final boolean ignore = !inDoc || lvl == 1;
    if((meta.chop && t.length != 0 || !ws(t)) && ignore)
      error(inDoc ? AFTERROOT : BEFOREROOT, parser.detail());

    if(t.length != 0 && !ignore) addText(t, Data.TEXT);
  }

  /**
   * Stores a comment.
   * @param value comment text
   * @throws IOException I/O exception
   */
  public final void comment(final byte[] value) throws IOException {
    addText(value, Data.COMM);
  }

  /**
   * Stores a processing instruction.
   * @param pi processing instruction name and value
   * @throws IOException I/O exception
   */
  public final void pi(final byte[] pi) throws IOException {
    addText(pi, Data.PI);
  }

  /**
   * Sets the document encoding.
   * @param enc encoding
   */
  public final void encoding(final String enc) {
    meta.encoding = eq(enc, UTF8, UTF82) ? UTF8 : enc;
  }

  // PROGRESS INFORMATION =====================================================

  @Override
  public final String tit() {
    return PROGCREATE;
  }

  @Override
  public final String det() {
    return spos == 0 ? parser.detail() : DBFINISH;
  }

  @Override
  public final double prog() {
    return spos == 0 ? parser.progress() : (double) spos / ssize;
  }

  // ABSTRACT METHODS =========================================================

  /**
   * Builds the database by running the specified parser.
   * @return data database instance
   * @throws IOException I/O exception
   */
  public abstract Data build() throws IOException;

  /**
   * Closes open references.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;

  /**
   * Adds a document node to the database.
   * @param value name of the document
   * @throws IOException I/O exception
   */
  protected abstract void addDoc(byte[] value) throws IOException;

  /**
   * Adds an element node to the database. This method stores a preliminary
   * size value; if this node has further descendants, {@link #setSize} must
   * be called to set the final size value.
   * @param dist distance to parent
   * @param nm the tag name reference
   * @param asize number of attributes
   * @param uri namespace uri reference
   * @param ne namespace flag
   * @throws IOException I/O exception
   */
  protected abstract void addElem(int dist, int nm, int asize,
      int uri, boolean ne) throws IOException;

  /**
   * Adds an attribute to the database.
   * @param nm attribute name
   * @param value attribute value
   * @param dist distance to parent
   * @param uri namespace uri reference
   * @throws IOException I/O exception
   */
  protected abstract void addAttr(int nm, byte[] value, int dist, int uri)
    throws IOException;

  /**
   * Adds a text node to the database.
   * @param value the token to be added (tag name or content)
   * @param dist distance to parent
   * @param kind the node kind
   * @throws IOException I/O exception
   */
  protected abstract void addText(byte[] value, int dist, byte kind)
    throws IOException;

  /**
   * Stores a size value to the specified table position.
   * @param pre pre reference
   * @param size value to be stored
   * @throws IOException I/O exception
   */
  protected abstract void setSize(int pre, int size) throws IOException;

  // PRIVATE METHODS ==========================================================

  /**
   * Adds an element node to the storage.
   * @param nm tag name
   * @param att attributes
   * @return pre value of the created node
   * @throws IOException I/O exception
   */
  private int addElem(final byte[] nm, final Atts att) throws IOException {
    // get tag reference
    int n = tags.index(nm, null, true);

    if(meta.pathindex) path.index(n, Data.ELEM, lvl);

    // cache pre value
    final int pre = meta.size;
    // remember tag id and parent reference
    tstack.set(lvl, n);
    pstack.set(lvl, pre);

    // get and store element references
    final int dis = lvl != 0 ? pre - pstack.get(lvl - 1) : 1;
    final int as = att.size();
    final boolean ne = ns.open();
    int u = ns.uri(nm, true);
    addElem(dis, n, Math.min(IO.MAXATTS, as + 1), u, ne);

    // get and store attribute references
    for(int a = 0; a < as; ++a) {
      n = atts.index(att.key(a), att.value(a), true);
      u = ns.uri(att.key(a), false);
      if(meta.pathindex) path.index(n, Data.ATTR, lvl + 1);
      addAttr(n, att.value(a), Math.min(IO.MAXATTS, a + 1), u);
    }

    if(lvl != 0) {
      if(lvl > 1) {
        // set leaf node information in index
        tags.stat(tstack.get(lvl - 1)).leaf = false;
      } else if(inDoc) {
        // don't allow more than one root node
        error(MOREROOTS, parser.detail(), nm);
      }
    }
    if(meta.size != 1) inDoc = true;

    if(Util.debug && (c++ & 0x7FFFF) == 0) Util.err(".");

    // check if data ranges exceed database limits,
    // based on the storage details in {@link Data}
    limit(tags.size(), 0x8000, LIMITTAGS);
    limit(atts.size(), 0x8000, LIMITATTS);
    limit(ns.size(), 0x100, LIMITNS);
    if(meta.size < 0) limit(0, 0, LIMITRANGE);
    return pre;
  }

  /**
   * Checks a value limit and optionally throws an exception.
   * @param value value
   * @param limit limit
   * @param msg message
   * @throws IOException I/O exception
   */
  private void limit(final int value, final int limit, final String msg)
      throws IOException {
    if(value >= limit) error(msg, parser.detail(), limit);
  }

  /**
   * Adds a simple text, comment or processing instruction to the database.
   * @param value the value to be added
   * @param kind the node type
   * @throws IOException I/O exception
   */
  private void addText(final byte[] value, final byte kind)
      throws IOException {

    // text node processing for statistics
    if(kind == Data.TEXT) tags.index(tstack.get(lvl - 1), value);
    // set leaf node information in index
    else if(lvl > 1) tags.stat(tstack.get(lvl - 1)).leaf = false;

    if(meta.pathindex) path.index(0, kind, lvl);
    addText(value, lvl == 0 ? 1 : meta.size - pstack.get(lvl - 1), kind);
  }

  /**
   * Throws an error message.
   * @param msg message
   * @param ext message extension
   * @throws IOException I/O exception
   */
  private void error(final String msg, final Object... ext) throws IOException {
    throw new BuildException(msg, ext);
  }
}
