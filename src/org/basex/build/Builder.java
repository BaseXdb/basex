package org.basex.build;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Namespaces;
import org.basex.data.Skeleton;
import org.basex.index.Names;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;

/**
 * This class provides an interface for building database instances.
 * The specified {@link Parser} send events to this class whenever nodes
 * are to be added or closed. The builder implementation decides whether
 * the nodes are stored on disk or kept in memory.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Builder extends Progress {
  /** Maximum level depth. */
  private static final int CAP = 1 << 8;

  /** Meta data on built database. */
  public MetaData meta;

  /** Tag name index. */
  protected Names tags;
  /** Attribute name index. */
  protected Names atts;
  /** Namespace index. */
  protected Namespaces ns = new Namespaces();
  /** Tree structure. */
  protected Skeleton skel = new Skeleton();

  /** Parser instance. */
  protected Parser parser;
  /** Table size. */
  protected int size;

  /** Parent stack. */
  private final int[] parStack = new int[CAP];
  /** Tag stack. */
  private final int[] tagStack = new int[CAP];
  /** Namespace stack. */
  private final int[] nsStack = new int[CAP];
  /** Flag for parsing inside the document. */
  private boolean inDoc;
  /** Current tree height. */
  private int level;

  /**
   * Constructor.
   */
  protected Builder() {
    tags = new Names();
    atts = new Names();
  }

  // abstract methods

  /**
   * Initializes the table construction.
   * @param db name of database
   * @return builder instance
   * @throws IOException in case of parsing or writing problems
   */
  protected abstract Builder init(String db) throws IOException;

  /**
   * Finishes the build process and returns a database instance.
   * @return data database instance
   * @throws IOException in case of parsing or writing problems
   */
  protected abstract Data finish() throws IOException;

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
  protected abstract void addDoc(byte[] tok) throws IOException;
  
  /**
   * Adds an element node to the database. This method stores a preliminary
   * size value; if this node has further descendants, {@link #addSize} has
   * to be called eventually to finish this node.
   * @param tok the tag name reference
   * @param tns the tag namespace
   * @param dis distance (relative parent reference)
   * @param as number of attributes
   * @param n element has namespaces
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addElem(int tok, int tns, int dis, int as, boolean n)
    throws IOException;
  
  /**
   * Adds the size value to the table.
   * @param pre closing pre tag
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addSize(int pre) throws IOException;

  /**
   * Adds an attribute to the database.
   * @param n attribute name
   * @param s namespace
   * @param v attribute value
   * @param d distance (relative parent reference)
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addAttr(int n, int s, byte[] v, int d)
    throws IOException;

  /**
   * Adds a simple node to the database.
   * @param tok the token to be added (tag name or content)
   * @param dis distance (relative parent reference)
   * @param kind the node kind
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addText(byte[] tok, int dis, byte kind)
    throws IOException;


  // Final Methods ============================================================
    
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
    
    meta.lastid = size;
    if(size == 0) {
      addDoc(utf8(token("empty"), Prop.ENCODING));
      addSize(0);
    }
    return finish();
  }

  /**
   * Opens a document node.
   * @param doc document name
   * @throws IOException in case of parsing or writing problems 
   */
  public final void startDoc(final byte[] doc) throws IOException {
    parStack[level++] = size;
    skel.add(0, level, Data.DOC);
    addDoc(utf8(doc, Prop.ENCODING));
  }

  /**
   * Closes a document node.
   * @throws IOException in case of parsing or writing problems 
   */
  public final void endDoc() throws IOException {
    addSize(parStack[--level]);
    meta.ndocs++;
    inDoc = false;
  }

  /**
   * Adds a new namespace; called by the building instance.
   * @param name the attribute name to be processed
   * @param val attribute value
   */
  public final void startNS(final byte[] name, final byte[] val) {
    ns.add(name, val);
    // default namespace...
    if(name.length == 0) nsStack[level] = ns.id(val);
  }

  /**
   * Opens a new tag; called by the building instance.
   * @param tag the tag to be processed
   * @param att the tag attributes
   * @throws IOException in case of parsing or writing problems 
   */
  public final void startElem(final byte[] tag, final Atts att)
      throws IOException {
    addElem(tag, att, true);
  }

  /**
   * Stores an empty tag; called by the building instance.
   * @param tag the tag to be processed
   * @param att the tag attributes
   * @throws IOException in case of parsing or writing problems 
   */
  public final void emptyElem(final byte[] tag, final Atts att)
      throws IOException {

    addElem(tag, att, false);
    ns.close(parStack[level]);
  }
  
  /**
   * Closes a tag; called by the building instance.
   * @param tag the tag to be processed
   * @throws IOException in case of parsing or writing problems 
   */
  public final void endElem(final byte[] tag) throws IOException {
    checkStop();
    final byte[] t = utf8(tag, meta.encoding);
    if(level-- == 0 || tags.id(t) != tagStack[level])
      error(CLOSINGTAG, parser.det(), t, tags.key(tagStack[level]));

    addSize(parStack[level]);
    ns.close(parStack[level]);
  }

  /**
   * Stores an element node.
   * @param name tag to be processed
   * @param att attribute names and values
   * @param open opening tag
   * @throws IOException in case of parsing or writing problems 
   */
  private void addElem(final byte[] name, final Atts att,
      final boolean open) throws IOException {

    // convert tag to utf8
    final byte[] tag = utf8(name, meta.encoding);

    // set leaf node information in index and add tag and atts to statistics 
    if(level != 0) {
      if(level == 1 && inDoc) error(MOREROOTS, parser.det(), tag);
      tags.noleaf(tagStack[level - 1], true);
    }

    // get tag reference
    final int tid = tags.index(tag, null);
    skel.add(tid, level, Data.ELEM);

    // remember tag id and parent reference
    tagStack[level] = tid;
    parStack[level] = size;

    // store namespaces
    final boolean n = ns.open(size);

    // add node
    final int dis = level != 0 ? size - parStack[level - 1] : 1;
    final int al = att.size;
    
    // get namespaces reference
    int tns = ns.get(tag);
    if(tns == 0) tns = nsStack[level];

    addElem(tid, tns, dis, al + 1, n);

    // create numeric attribute references
    for(int a = 0; a < al; a++) {
      final byte[] av = att.val[a];
      final int an = atts.index(att.key[a], av);
      final int ans = ns.get(att.key[a]);
      skel.add(an, level + 1, Data.ATTR);
      addAttr(an, ans, av, a + 1);
    }

    if(open) {
      if(meta.height < ++level) meta.height = level;
      nsStack[level] = nsStack[level - 1];
    }
    if(size != 1) inDoc = true;
  }

  /**
   * Stores text nodes; called by the building instance.
   * @param t the text to be processed
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
   * Throws an error message.
   * @param m message
   * @param e message extension
   * @throws IOException I/O Exception
   */
  private void error(final String m, final Object... e) throws IOException {
    throw new BuildException(m, e);
  }

  /**
   * Stores comments; called by the building instance.
   * @param com the comment to be processed
   * @throws IOException in case of parsing or writing problems 
   */
  public final void comment(final TokenBuilder com) throws IOException {
    addText(com, Data.COMM);
  }

  /**
   * Stores processing instructions; called by the building instance.
   * @param pi the processing instruction to be processed
   * @throws IOException in case of parsing or writing problems 
   */
  public final void pi(final TokenBuilder pi) throws IOException {
    addText(pi, Data.PI);
  }

  /**
   * Adds a simple text, comment or pi to the database.
   * @param txt the token to be added
   * @param type the node type
   * @throws IOException in case of parsing or writing problems 
   */
  private void addText(final TokenBuilder txt, final byte type)
      throws IOException {

    final byte[] t = utf8(txt.finish(), meta.encoding);
    
    // text node processing for statistics 
    if(type == Data.TEXT) tags.index(tagStack[level - 1], t);

    skel.add(0, level, type);
    addText(t, level == 0 ? 1 : size - parStack[level - 1], type);
  }

  /**
   * Sets the document encoding.
   * @param enc encoding
   */
  public final void encoding(final String enc) {
    meta.encoding = enc.equals(UTF8) || enc.equals(UTF82) ? UTF8 : enc;
  }

  /**
   * Convenience method for adding a tag and a text node;
   * called by the building instance.
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
    return parser.percent();
  }
}
