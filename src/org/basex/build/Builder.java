package org.basex.build;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.index.Names;
import org.basex.index.Namespaces;
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

  /** Tag name index. */
  protected Names tags;
  /** Attribute name index. */
  protected Names atts;
  /** Namespace index. */
  protected Namespaces ns = new Namespaces();

  /** Parser instance. */
  protected Parser parser;
  /** Meta data on built database. */
  protected MetaData meta;
  /** Table size. */
  protected int size;

  /** Parent stack. */
  private final int[] parStack = new int[CAP];
  /** Tag stack. */
  private final int[] tagStack = new int[CAP];
  /** Flag for parsing inside the document. */
  private boolean inDoc;
  /** Current tree height. */
  private int level;

  /**
   * Constructor.
   */
  protected Builder() {
    tags = new Names(true);
    atts = new Names(false);
  }

  // abstract methods

  /**
   * Initializes the table construction.
   * @param db name of database
   * @return builder instance
   * @throws IOException in case of parsing or writing problems
   */
  public abstract Builder init(String db) throws IOException;

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
   * Adds a complex node to the database.
   * @param tok the tag name reference
   * @param tns the tag namespace
   * @param par relative parent value (distance)
   * @param at optional attribute tokens
   * @param atr numeric attribute references
   * @param kind node type
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addNode(int tok, int tns, int par, byte[][] at,
      int[] atr, byte kind) throws IOException;
  
  /**
   * Adds the size value to the table.
   * @param pre closing pre tag
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addSize(int pre) throws IOException;

  /**
   * Adds a simple node to the database.
   * @param tok the token to be added (tag name or content)
   * @param par relative parent value (distance)
   * @param kind the node kind
   * @throws IOException in case of parsing or writing problems 
   */
  protected abstract void addText(byte[] tok, int par, byte kind)
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
    final byte[] name = token(db);
    addNode(Data.DOC, name, null, true);
    parser.parse(this);
    endNode(name);
    
    meta.lastid = size;
    return finish();
  }

  /**
   * Adds a new namespace; called by the building instance.
   * @param name the attribute name to be processed
   * @param val attribute value
   */
  public final void startNS(final byte[] name, final byte[] val) {
    ns.add(name, val, size);
  }

  /**
   * Opens a new tag; called by the building instance.
   * @param tag the tag to be processed
   * @param att the tag attributes
   * @throws IOException in case of parsing or writing problems 
   */
  public final void startNode(final byte[] tag, final byte[][] att)
      throws IOException {

    addNode(Data.ELEM, tag, att, true);
  }

  /**
   * Stores an empty tag; called by the building instance.
   * @param tag the tag to be processed
   * @param att the tag attributes
   * @throws IOException in case of parsing or writing problems 
   */
  public final void emptyNode(final byte[] tag, final byte[][] att)
      throws IOException {

    addNode(Data.ELEM, tag, att, false);
  }
  
  /**
   * Closes a tag; called by the building instance.
   * @param tag the tag to be processed
   * @throws IOException in case of parsing or writing problems 
   */
  public final void endNode(final byte[] tag) throws IOException {
    checkStop();
    final byte[] t = utf8(tag, meta.encoding);
    if(level-- == 0 || tags.id(t) != tagStack[level])
      error(CLOSINGTAG, parser.det(), t, tags.key(tagStack[level]));

    addSize(parStack[level]);
  }

  /**
   * Stores an node.
   * @param kind node type
   * @param name tag to be processed
   * @param att attribute names and values
   * @param open opening tag
   * @throws IOException in case of parsing or writing problems 
   */
  protected final void addNode(final byte kind, final byte[] name,
      final byte[][] att, final boolean open) throws IOException {
    
    // convert tag to utf8
    final byte[] tag = utf8(name, meta.encoding);

    // set leaf node information in index and add tag and atts to statistics 
    if(level != 0) {
      if(level == 1 && inDoc) error(MOREROOTS, parser.det(), tag);
      tags.noleaf(tagStack[level - 1], true);
    }

    // create numeric attribute references and check if they appear only once
    final int al = att != null ? att.length : 0;
    final int[] at = al != 0 ? new int[al] : null;
    for(int a = 0; a < al; a += 2) {
      at[a] = atts.index(att[a], att[a + 1]);
      at[a + 1] = ns.get(att[a]);
    }
    for(int a = 0; a < al - 1; a += 2) {
      for(int b = a + 2; b < al; b += 2) {
        if(at[a] == at[b]) error(DUPLATT, parser.det(), att[a]);
      }
    }

    // get tag and namespaces references
    final int tid = tags.index(tag, null);
    final int tns = ns.get(tag);

    // remember tag id and parent reference
    tagStack[level] = tid;
    parStack[level] = size;

    // add node
    final int par = level != 0 ? size - parStack[level - 1] : 1;
    addNode(tid, tns, par, att, at, kind);

    if(open && meta.height < ++level) meta.height = level;
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
   * @throws BuildException in case of parsing or writing problems 
   */
  private void error(final String m, final Object... e)
      throws BuildException {
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

    //parStack[level] = size; // ...necessary?
    final byte[] t = utf8(txt.finish(), meta.encoding);
    
    // text node processing for statistics 
    if(type == Data.TEXT) tags.index(tagStack[level - 1], t);
    
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
   * @param txt text node
   * @throws IOException in case of parsing or writing problems
   */
  public final void nodeAndText(final byte[] tag, final byte[] txt)
      throws IOException {
    startNode(tag, null);
    text(new TokenBuilder(txt), false);
    endNode(tag);
  }

  /**
   * Returns current progress header.
   * @return progress information
   */
  public final Object[] state() {
    return new Object[] { parser.head(), parser.toString() };
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
