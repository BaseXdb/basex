package org.basex.build;

import static org.basex.build.BuildText.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides an interface for building database instances.
 * The specified {@link Parser} sends events to this class whenever nodes
 * are to be added or closed. The builder implementation decides whether
 * the nodes are stored on disk or kept in memory.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Builder extends Job {
  /** Tree structure. */
  final PathIndex path = new PathIndex();
  /** Namespace index. */
  final Namespaces nspaces = new Namespaces();
  /** Parser instance. */
  final Parser parser;
  /** Database name. */
  final String dbName;

  /** Number of cached size values. */
  int ssize;
  /** Currently stored size value. */
  int spos;

  /** Meta data on built database. */
  MetaData meta;
  /** Element name index. */
  Names elemNames;
  /** Attribute name index. */
  Names attrNames;

  /** Parent stack. */
  private final IntList parStack = new IntList();
  /** Stack with element names. */
  private final IntList elemStack = new IntList();
  /** Current tree height. */
  private int level;

  /** Optional path to binary files. */
  private IOFile binDir;

  /**
   * Constructor.
   * @param dbName name of database
   * @param parser parser
   */
  Builder(final String dbName, final Parser parser) {
    this.dbName = dbName;
    this.parser = parser;
  }

  // PUBLIC METHODS ===============================================================================

  /**
   * Parses the given input source and builds the database.
   * @throws IOException I/O exception
   */
  final void parse() throws IOException {
    final Performance perf = Prop.debug ? new Performance() : null;
    Util.debug(shortInfo() + DOTS);
    try {
      // add document node and parse document
      parser.parse(this);
    } finally {
      parser.close();
    }
    meta.lastid = meta.size - 1;

    if(Prop.debug) Util.errln(" " + perf + " (" + Performance.getMemory() + ')');
  }

  /**
   * Sets the path to the raw database files. The path might differ from the actual database path
   * if XML data is written to a temporary instance.
   * @param dir database directory (can be {@code null})
   * @return self reference
   */
  public final Builder binaryDir(final IOFile dir) {
    if(dir != null) binDir = new IOFile(dir, IO.RAW);
    return this;
  }

  /**
   * Opens a document node.
   * @param value document name
   * @throws IOException I/O exception
   */
  public final void openDoc(final byte[] value) throws IOException {
    path.index(0, Data.DOC, level);
    parStack.set(level++, meta.size);
    addDoc(value);
    nspaces.open();
  }

  /**
   * Closes a document node.
   * @throws IOException I/O exception
   */
  public final void closeDoc() throws IOException {
    final int pre = parStack.get(--level);
    setSize(pre, meta.size - pre);
    ++meta.ndocs;
    nspaces.close(meta.size);
  }

  /**
   * Opens a new element node.
   * @param name name of element
   * @param att attributes
   * @param nsp namespaces
   * @throws IOException I/O exception
   */
  public final void openElem(final byte[] name, final Atts att, final Atts nsp) throws IOException {
    addElem(name, att, nsp);
    ++level;
  }

  /**
   * Stores an empty element.
   * @param name name of element
   * @param att attributes
   * @param nsp namespaces
   * @throws IOException I/O exception
   */
  public final void emptyElem(final byte[] name, final Atts att, final Atts nsp)
      throws IOException {
    addElem(name, att, nsp);
    final int pre = parStack.get(level);
    nspaces.close(pre);
    if(att.size() >= IO.MAXATTS) setSize(pre, meta.size - pre);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  public final void closeElem() throws IOException {
    checkStop();
    --level;
    final int pre = parStack.get(level);
    setSize(pre, meta.size - pre);
    nspaces.close(pre);
  }

  /**
   * Stores a text node.
   * @param value text value
   * @throws IOException I/O exception
   */
  public final void text(final byte[] value) throws IOException {
    if(value.length != 0) addText(value, Data.TEXT);
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
   * Stores binary data.
   * @param target database target
   * @param data data to store
   * @throws IOException I/O exception
   */
  public final void binary(final String target, final IO data) throws IOException {
    Store.store(data.inputSource(), new IOFile(binDir, target));
  }

  // PROGRESS INFORMATION =========================================================================

  @Override
  public final String shortInfo() {
    return CREATING_DB;
  }

  @Override
  public final String detailedInfo() {
    return spos == 0 ? parser.detailedInfo() : FINISHING_D;
  }

  @Override
  public final double progressInfo() {
    return spos == 0 ? parser.progressInfo() : (double) spos / ssize;
  }

  // ABSTRACT METHODS =============================================================================

  /**
   * Builds the database and returns the resulting database instance.
   * @return data database instance
   * @throws IOException I/O exception
   */
  public abstract Data build() throws IOException;

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
   * @param nameId id of element name
   * @param asize number of attributes
   * @param uriId id of namespace uri
   * @param ne namespace flag (indicates if this element introduces new namespaces)
   * @throws IOException I/O exception
   */
  protected abstract void addElem(int dist, int nameId, int asize, int uriId, boolean ne)
      throws IOException;

  /**
   * Adds an attribute to the database.
   * @param nameId id of attribute name
   * @param value attribute value
   * @param dist distance to parent
   * @param uriId id of namespace uri
   * @throws IOException I/O exception
   */
  protected abstract void addAttr(int nameId, byte[] value, int dist, int uriId) throws IOException;

  /**
   * Adds a text node to the database.
   * @param value the token to be added
   * @param dist distance to parent
   * @param kind the node kind
   * @throws IOException I/O exception
   */
  protected abstract void addText(byte[] value, int dist, byte kind) throws IOException;

  /**
   * Stores a size value to the specified table position.
   * @param pre pre reference
   * @param size value to be stored
   * @throws IOException I/O exception
   */
  protected abstract void setSize(int pre, int size) throws IOException;

  // PRIVATE METHODS ==============================================================================

  /**
   * Adds an element node to the storage.
   * @param name element name
   * @param atts attributes
   * @param nsp namespaces
   * @throws IOException I/O exception
   */
  private void addElem(final byte[] name, final Atts atts, final Atts nsp) throws IOException {
    // get reference of element name
    int nameId = elemNames.index(name);
    path.index(nameId, Data.ELEM, level);

    // cache pre value
    final int pre = meta.size;
    // remember id of element name and parent reference
    elemStack.set(level, nameId);
    parStack.set(level, pre);

    // parse namespaces
    nspaces.open(pre, nsp);

    // get and store element references
    final int dis = level == 0 ? 1 : pre - parStack.get(level - 1);
    final int as = atts.size();
    final byte[] pref = prefix(name);
    int uriId = nspaces.uriIdForPrefix(pref, true);
    if(uriId == 0 && pref.length != 0 && !eq(pref, XML))
      throw new BuildException(WHICHNS, parser.detailedInfo(), prefix(name));
    addElem(dis, nameId, Math.min(IO.MAXATTS, as + 1), uriId, !nsp.isEmpty());

    // get and store attribute references
    for(int a = 0; a < as; ++a) {
      final byte[] an = atts.name(a), av = atts.value(a), ap = prefix(an);
      nameId = attrNames.index(an, av);
      uriId = nspaces.uriIdForPrefix(ap, false);
      if(uriId == 0 && ap.length != 0 && !eq(ap, XML))
        throw new BuildException(WHICHNS, parser.detailedInfo(), an);

      path.index(nameId, Data.ATTR, level + 1, av, meta);
      addAttr(nameId, av, Math.min(IO.MAXATTS, a + 1), uriId);
    }

    // set leaf node information in index
    if(level > 1) elemNames.stats(elemStack.get(level - 1)).setLeaf(false);

    // check if data ranges exceed database limits, based on the storage details in {@link Data}
    limit(elemNames.size(), 0x8000, LIMITELEMS);
    limit(attrNames.size(), 0x8000, LIMITATTS);
    limit(nspaces.size(), 0x100, LIMITNS);
    if(meta.size < 0) limit(0, 0, LIMITRANGE);
  }

  /**
   * Checks a value limit and optionally throws an exception.
   * @param value value
   * @param limit limit
   * @param message error message
   * @throws IOException I/O exception
   */
  private void limit(final int value, final int limit, final String message) throws IOException {
    if(value >= limit) throw new BuildException(message, parser.detailedInfo(), limit);
  }

  /**
   * Adds a simple text, comment or processing instruction to the database.
   * @param value the value to be added
   * @param kind the node type
   * @throws IOException I/O exception
   */
  private void addText(final byte[] value, final byte kind) throws IOException {
    final int l = level;
    if(l > 1) {
      // add text node to statistics, or set leaf flag
      final Stats stats = elemNames.stats(elemStack.get(l - 1));
      if(kind == Data.TEXT) stats.add(value, meta);
      else stats.setLeaf(false);
    }

    path.index(0, kind, l, value, meta);
    addText(value, l == 0 ? 1 : meta.size - parStack.get(l - 1), kind);
  }
}
