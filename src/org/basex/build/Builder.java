package org.basex.build;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Namespaces;
import org.basex.data.PathSummary;
import org.basex.index.Names;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;
import org.basex.io.IO;

/**
 * This class provides an interface for building database instances.
 * The specified {@link Parser} send events to this class whenever nodes
 * are to be added or closed. The builder implementation decides whether
 * the nodes are stored on disk or kept in memory.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Builder extends Progress {
  /** Meta data on built database. */
  public MetaData meta;

  /** Tag name index. */
  protected Names tags;
  /** Attribute name index. */
  protected Names atts;
  /** Namespace index. */
  protected Namespaces ns = new Namespaces();
  /** Tree structure. */
  protected PathSummary path = new PathSummary();

  /** Parser instance. */
  protected Parser parser;
  /** Element counter. */
  public int elms;

  /** Parent stack. */
  private final int[] parStack = new int[IO.MAXHEIGHT];
  /** Tag stack. */
  private final int[] tagStack = new int[IO.MAXHEIGHT];
  /** Namespace stack. */
  private final int[] nsStack = new int[IO.MAXHEIGHT];
  /** Size Stack. */
  private boolean inDoc;
  /** Current tree height. */
  private int level;

  /**
   * Default constructor.
   */
  protected Builder() {
    tags = new Names();
    atts = new Names();
  }

  // Abstract Methods ==========================================================

  /**
   * Initializes the database construction.
   * @param db name of database
   * @return builder instance
   * @throws IOException in case of parsing or writing problems
   */
  public abstract Builder init(String db) throws IOException;

  /**
   * Finishes the build process and returns a database reference.
   * @return data database instance
   * @throws IOException in case of parsing or writing problems
   */
  public abstract Data finish() throws IOException;

  /**
   * Closes open references.
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void close() throws IOException;

  /**
   * Adds a document node to the database.
   * @param tok the token to be added (tag name or content)
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void addDoc(byte[] tok) throws IOException;

  /**
   * Adds an element node to the database. This method stores a preliminary
   * size value; if this node has further descendants, {@link #setSize} has
   * to be called with the final size value.
   * @param tok the tag name reference
   * @param tns the tag namespace
   * @param dis distance (relative parent reference)
   * @param as number of attributes
   * @param n element has namespaces
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void addElem(int tok, int tns, int dis, int as, boolean n)
    throws IOException;

  /**
   * Adds an attribute to the database.
   * @param n attribute name
   * @param s namespace
   * @param v attribute value
   * @param d distance (relative parent reference)
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void addAttr(int n, int s, byte[] v, int d)
    throws IOException;

  /**
   * Adds a text node to the database.
   * @param tok the token to be added (tag name or content)
   * @param dis distance (relative parent reference)
   * @param kind the node kind
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void addText(byte[] tok, int dis, byte kind)
    throws IOException;

  /**
   * Stores a size value to the specified table position.
   * @param pre pre reference
   * @param val value to be stored
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void setSize(int pre, int val)
    throws IOException;

  /**
   * Stores an attribute value to the specified table position.
   * @param pre pre reference
   * @param val value to be stored
   * @throws IOException in case of parsing or writing problems
   */
  public abstract void setAttValue(int pre, byte[] val)
    throws IOException;

  // Public Methods ============================================================

  /**
   * Builds the database by running the specified parser.
   * @param p parser instance
   * @param db name of database
   * @return data database instance
   * @throws IOException in case of parsing or writing problems
   */
  public final Data build(final Parser p, final String db) throws IOException {
    parser = p;
    init(db);

    // add document node and parse document
    parser.parse(this);

    meta.lastid = meta.size;
    if(meta.size == 0) {
      addDoc(utf8(token(db), Prop.ENCODING));
      setSize(0, meta.size);
    }
    return finish();
  }

  /**
   * Opens a document node.
   * @param doc document name
   * @throws IOException in case of parsing or writing problems
   */
  public final void startDoc(final byte[] doc) throws IOException {
    parStack[level++] = meta.size;
    path.add(0, level, Data.DOC);
    addDoc(utf8(doc, Prop.ENCODING));
  }

  /**
   * Closes a document node.
   * @throws IOException in case of parsing or writing problems
   */
  public final void endDoc() throws IOException {
    final int pre = parStack[--level];
    setSize(pre, meta.size - pre);
    meta.ndocs++;
    inDoc = false;
    if(Prop.debug) BaseX.err("\n");
  }

  /**
   * Adds a new namespace; called by the building instance.
   * @param name the attribute name to be processed
   * @param val attribute value
   */
  public final void startNS(final byte[] name, final byte[] val) {
    final int n = ns.add(name, val);
    // default namespace...
    if(name.length == 0) nsStack[level] = n;
  }

  /**
   * Opens a new element node.
   * @param tag tag name
   * @param att attributes
   * @return preValue of the created node
   * @throws IOException in case of parsing or writing problems
   */
  public final int startElem(final byte[] tag, final Atts att)
      throws IOException {
    final int pre = addElem(tag, att);

    if(meta.height < ++level) meta.height = level;
    return pre;
  }

  /**
   * Stores an empty element.
   * @param tag tag name
   * @param att attributes
   * @throws IOException in case of parsing or writing problems
   */
  public final void emptyElem(final byte[] tag, final Atts att)
      throws IOException {

    addElem(tag, att);
    ns.close(parStack[level]);
  }

  /**
   * Closes an element.
   * @param tag tag name
   * @throws IOException in case of parsing or writing problems
   */
  public final void endElem(final byte[] tag) throws IOException {
    checkStop();
    final byte[] t = utf8(tag, meta.encoding);
    if(level-- == 0 || tags.id(t) != tagStack[level])
      error(CLOSINGTAG, parser.det(), t, tags.key(tagStack[level]));

    final int pre = parStack[level];
    setSize(pre, meta.size - pre);
    ns.close(pre);
    nsStack[level] = nsStack[level - 1];
  }

  /**
   * Stores a text node.
   * @param t text value
   * @param w whitespace flag
   * @throws IOException in case of parsing or writing problems
   */
  public final void text(final TokenBuilder t, final boolean w)
      throws IOException {

    // checks if text appears before or after root node
    final boolean out = !inDoc || level == 1;
    if(!w) {
      if(out) error(inDoc ? AFTERROOT : BEFOREROOT, parser.det());
    } else if(t.size == 0 || out) {
      return;
    }

    // chop whitespaces in text nodes
    if(meta.chop) t.chop();
    if(t.size != 0) addText(t, Data.TEXT);
  }

  /**
   * Stores a comment.
   * @param com comment text
   * @throws IOException in case of parsing or writing problems
   */
  public final void comment(final TokenBuilder com) throws IOException {
    addText(com, Data.COMM);
  }

  /**
   * Stores a processing instruction.
   * @param pi processing instruction name and value
   * @throws IOException in case of parsing or writing problems
   */
  public final void pi(final TokenBuilder pi) throws IOException {
    addText(pi, Data.PI);
  }

  /**
   * Sets the document encoding.
   * @param enc encoding
   */
  public final void encoding(final String enc) {
    meta.encoding = enc.equals(UTF8) || enc.equals(UTF82) ? UTF8 : enc;
  }

  /**
   * Convenience method for adding an element and text node.
   * @param tag the tag to be processed
   * @param att attributes
   * @param txt text node
   * @throws IOException in case of parsing or writing problems
   */
  public final void nodeAndText(final byte[] tag, final Atts att,
      final byte[] txt) throws IOException {
    startElem(tag, att);
    text(new TokenBuilder(txt), false);
    endElem(tag);
  }

  @Override
  public final String tit() {
    return parser.head();
  }

  @Override
  public final String det() {
    return parser.det();
  }

  @Override
  public final double prog() {
    return parser.prog();
  }

  // Private Methods ===========================================================

  /**
   * Adds an element node to the storage.
   * @param name tag name
   * @param att attributes
   * @return pre value of the created node
   * @throws IOException in case of parsing or writing problems
   */
  private int addElem(final byte[] name, final Atts att) throws IOException {

    // convert tag to utf8
    final byte[] tag = utf8(name, meta.encoding);

    // get tag reference
    final int tid = tags.index(tag, null, true);
    path.add(tid, level, Data.ELEM);

    // remember tag id and parent reference
    tagStack[level] = tid;
    parStack[level] = meta.size;
    // cache pre value
    final int pre = meta.size;

    // add node
    final int dis = level != 0 ? meta.size - parStack[level - 1] : 1;
    final int al = att.size;

    // get namespaces reference
    int tns = ns.get(tag);
    if(tns == 0) tns = nsStack[level];
    nsStack[level + 1] = nsStack[level];

    // store namespaces
    final boolean n = ns.open(meta.size);

    addElem(tid, tns, dis, al + 1, n);

    // create numeric attribute references
    for(int a = 0; a < al; a++) {
      final byte[] av = att.val[a];
      final int an = atts.index(att.key[a], av, true);
      final int ans = ns.get(att.key[a]);
      path.add(an, level + 1, Data.ATTR);
      addAttr(an, ans, av, a + 1);
    }

    // set leaf node information in index and add tag and atts to statistics
    if(level != 0) {
      if(level == 1) {
        if(inDoc && !Prop.fuse) error(MOREROOTS, parser.det(), tag);
      } else {
        tags.stat(tagStack[level - 1]).leaf = false;
      }
    }
    if(meta.size != 1) inDoc = true;

    if(Prop.debug) {
      if(++elms % 500000 == 0) BaseX.err(" " + elms + "\n");
      else if(elms % 50000 == 0) BaseX.err("!");
      else if(elms % 10000 == 0) BaseX.err(".");
    }

    return pre;
  }

  /**
   * Throws an error message.
   * @param m message
   * @param e message extension
   * @throws IOException I/O Exception
   */
  private void error(final String m, final Object... e) throws IOException {
    throw new BuildException(m, e);
  }

  /**
   * Adds a simple text, comment or processing instruction to the database.
   * @param txt the value to be added
   * @param kind the node type
   * @throws IOException in case of parsing or writing problems
   */
  private void addText(final TokenBuilder txt, final byte kind)
      throws IOException {

    final byte[] t = utf8(txt.finish(), meta.encoding);

    // text node processing for statistics
    if(kind == Data.TEXT) tags.index(tagStack[level - 1], t);
    path.add(0, level, kind, t.length);
    addText(t, level == 0 ? 1 : meta.size - parStack[level - 1], kind);
  }
}
