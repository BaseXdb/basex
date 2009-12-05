package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.proc.InfoTable;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.Names;
import org.basex.io.TableAccess;
import org.basex.query.ft.StopWords;
import org.basex.util.Atts;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;
import org.deepfs.fs.DeepFS;

/**
 * This class provides access to the database storage.
 * Note that the methods of this class are optimized for performance.
 * They will not check if correct data is requested, i.e. if a text is
 * requested, a pre value must points to a text node.
 *
 * All nodes in the table are accessed by their
 * implicit pre value. The following restrictions are imposed on the data:
 * <ul>
 * <li>The table is limited to 2^31 entries (pre values are signed int's)</li>
 * <li>A maximum of 2^15 different tag and attribute names is allowed</li>
 * <li>A maximum of 2^8 different namespaces is allowed</li>
 * <li>A tag can have a maximum of 32 attributes</li>
 * </ul>
 * Each node occupies 128 bits. The current storage layout looks as follows:
 *
 * <pre>
 * COMMON ATTRIBUTES:
 * - Byte     0:  KIND: Node kind (2-0)
 * ELEMENT NODES:
 * - Byte     0:  ATTS: Number of attributes (7-3)
 * - Byte   1-2:  NAME: Namespace Flag (15), Name (14-0)
 * - Byte     3:  NURI: Namespace URI
 * - Byte  4- 7:  DIST: Distance to parent node
 * - Byte  8-11:  SIZE: Number of descendants
 * - Byte 12-15:  UNID: Unique Node ID
 * DOCUMENT NODES:
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  SIZE: Number of descendants
 * - Byte 12-15:  UNID: Unique Node ID
 * TEXT, COMMENT, PI NODES:
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  DIST: Distance to parent node
 * - Byte 12-15:  UNID: Unique Node ID
 * ATTRIBUTE NODES:
 * - Byte     0:  DIST: Distance to parent node (7-3)
 * - Byte   1-2:  NAME: Namespace Flag (15), Name (14-0)
 * - Byte  3- 7:  TEXT: Attribute value reference
 * - Byte    11:  NURI: Namespace (7-3)
 * - Byte 12-15:  UNID: Unique Node ID
 * </pre>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Data {
  /** Node kind: Document. */
  public static final byte DOC = 0x00;
  /** Node kind: Element. */
  public static final byte ELEM = 0x01;
  /** Node kind: Text. */
  public static final byte TEXT = 0x02;
  /** Node kind: Attribute. */
  public static final byte ATTR = 0x03;
  /** Node kind: Comment. */
  public static final byte COMM = 0x04;
  /** Node kind: Processing Instruction. */
  public static final byte PI = 0x05;

  /** Index types. */
  public enum Type {
    /** Attribute index. */ ATN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Full-text index. */ FTX,
  };

  /** Table access file. */
  protected TableAccess table;

  /** Meta data. */
  public MetaData meta;
  /** Tag index. */
  public Names tags;
  /** Attribute name index. */
  public Names atts;
  /** Namespace index. */
  public Namespaces ns;
  /** Path Summary. */
  public PathSummary path;

  /** File system reference. */
  public DeepFS fs;
  /** Index Reference for name tag. */
  public int nameID;
  /** Index References. */
  public int sizeID;

  /** Text index. */
  protected Index txtindex;
  /** Attribute value index. */
  protected Index atvindex;
  /** Full-text index instance. */
  protected Index ftxindex;
  /** Stopword list access file. */
  protected StopWords swl;

  /**
   * Dissolves the references to often used tag names and attributes.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void init() throws IOException {
    if(meta.deepfs) fs = new DeepFS(this);
    nameID = atts.id(DataText.NAME);
    sizeID = atts.id(DataText.SIZE);
  }

  /**
   * Closes the current database.
   * @throws IOException I/O exception
   */
  public final void close() throws IOException {
    if(fs != null) fs.close();
    cls();
  }

  /**
   * Internal method to close the database.
   * @throws IOException I/O exception
   */
  protected abstract void cls() throws IOException;

  /**
   * Flushes the table data.
   */
  public abstract void flush();

  /**
   * Closes the specified index.
   * @param index index to be closed
   * @throws IOException I/O exception
   */
  public abstract void closeIndex(Type index) throws IOException;

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param ind index instance
   */
  public abstract void setIndex(Type type, Index ind);

  // RETRIEVING VALUES ========================================================

  /**
   * Returns a pre value.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  public final int pre(final int id) {
    // find pre value in table
    for(int p = id; p < meta.size; p++) if(id == id(p)) return p;
    for(int p = 0; p < id; p++) if(id == id(p)) return p;
    // id not found
    return -1;
  }

  /**
   * Returns a unique node id.
   * @param pre pre value
   * @return node id
   */
  public final int id(final int pre) {
    return table.read4(pre, 12);
  }

  /**
   * Returns a node kind.
   * @param pre pre value
   * @return node kind
   */
  public final int kind(final int pre) {
    return table.read1(pre, 0) & 0x07;
  }

  /**
   * Returns a pre value of the parent node.
   * @param pre pre value
   * @param k node kind
   * @return pre value of the parent node
   */
  public final int parent(final int pre, final int k) {
    return pre - dist(pre, k);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param k node kind
   * @return distance
   */
  protected final int dist(final int pre, final int k) {
    switch(k) {
      case ELEM: return table.read4(pre, 4);
      case TEXT:
      case COMM:
      case PI:   return table.read4(pre, 8);
      case ATTR: return table.read1(pre, 0) >> 3 & 0x1F;
      default:   return pre + 1;
    }
  }

  /**
   * Returns a size value (number of descendant table entries).
   * @param pre pre value
   * @param k node kind
   * @return size value
   */
  public final int size(final int pre, final int k) {
    return k == ELEM || k == DOC ? table.read4(pre, 8) : 1;
  }

  /**
   * Returns a number of attributes.
   * @param pre pre value
   * @param k node kind
   * @return number of attributes
   */
  public final int attSize(final int pre, final int k) {
    return k == ELEM ? table.read1(pre, 0) >> 3 & 0x1F : 1;
  }

  /**
   * Returns a reference to the tag or attribute name id
   * (reference to the tag index).
   * @param pre pre value
   * @return token reference
   */
  public final int name(final int pre) {
    return table.read2(pre, 1) & 0x7FFF;
  }

  /**
   * Returns a tag or attribute name.
   * @param pre pre value
   * @param elem element/attribute flag
   * @return name reference
   */
  public final byte[] name(final int pre, final boolean elem) {
    return (elem ? tags : atts).key(name(pre));
  }

  /**
   * Returns a reference to the tag or attribute namespace URI.
   * @param pre pre value
   * @return token reference
   * @param k node kind
   */
  public final int uri(final int pre, final int k) {
    return k == ELEM || k == ATTR ?
        table.read1(pre, k == ELEM ? 3 : 11) & 0xFF : 0;
  }

  /**
   * Returns a namespace flag.
   * Should be only called for element nodes.
   * @param pre pre value
   * @return namespace flag
   */
  public final boolean nsFlag(final int pre) {
    return (table.read1(pre, 1) & 0x80) != 0;
  }

  /**
   * Returns namespace key and value ids.
   * Should be only called for element nodes.
   * @param pre pre value
   * @return key and value ids
   */
  public final Atts ns(final int pre) {
    final Atts as = new Atts();
    if(nsFlag(pre)) {
      final int[] nsp = ns.get(pre);
      for(int n = 0; n < nsp.length; n += 2)
        as.add(ns.pref(nsp[n]), ns.uri(nsp[n + 1]));
    }
    return as;
  }

  /**
   * Returns the disk offset of a text/attribute value.
   * @param pre pre value
   * @return disk offset
   */
  protected final long textOff(final int pre) {
    return table.read5(pre, 3);
  }

  /**
   * Returns a text or attribute value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return atomized value
   */
  public abstract byte[] text(int pre, boolean text);

  /**
   * Returns a text as double value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract double textNum(int pre, boolean text);

  /**
   * Returns a text length.
   * @param pre pre value
   * @param text text/attribute flag
   * @return length
   */
  public abstract int textLen(int pre, boolean text);

  // STORING VALUES ===========================================================

  /**
   * Sets the distance.
   * @param pre pre value
   * @param k node kind
   * @param v value
   */
  public final void dist(final int pre, final int k, final int v) {
    if(k == ATTR) table.write1(pre, 0, v << 3 | ATTR);
    else if(k != DOC) table.write4(pre, k == ELEM ? 4 : 8, v);
  }

  /**
   * Sets the size value.
   * @param pre pre reference
   * @param k node kind
   * @param v value to be stored
   */
  public final void size(final int pre, final int k, final int v) {
    if(k == ELEM || k == DOC) table.write4(pre, 8, v);
  }

  /**
   * Sets the attribute size.
   * @param pre pre value
   * @param k node kind
   * @param v value
   */
  public final void attSize(final int pre, final int k, final int v) {
    if(k == ELEM) table.write1(pre, 0, v << 3 | ELEM);
  }

  /**
   * Sets a reference to the tag or attribute namespace URI.
   * @param pre pre reference
   * @param k node kind
   * @param u namespace uri
   */
  public final void uri(final int pre, final int k, final int u) {
    table.write1(pre, k == ELEM ? 3 : 11, u);
  }

  /**
   * Stores the tag/attribute name.
   * @param pre pre value
   * @param v tag id
   */
  protected final void name(final int pre, final int v) {
    table.write2(pre, 1, v);
  }

  /**
   * Sets the disk offset of a text/attribute value.
   * @param pre pre value
   * @param off offset
   */
  protected final void textOff(final int pre, final long off) {
    table.write5(pre, 3, off);
  }

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param val content
   * @param txt text (text, comment or pi) or attribute flag
   */
  public abstract void text(final int pre, final byte[] val,
      final boolean txt);

  // OTHER DATA METHODS =======================================================

  /**
   * Returns the indexed id references for the specified token.
   * @param token index token reference
   * @return id array
   */
  public final IndexIterator ids(final IndexToken token) {
    if(token.get().length > MAXLEN) return null;
    switch(token.type()) {
      case TXT: return txtindex.ids(token);
      case ATV: return atvindex.ids(token);
      case FTX: return ftxindex.ids(token);
      default:  return null;
    }
  }

  /**
   * Returns the number of indexed id references for the specified token.
   * @param token text to be found
   * @return id array
   */
  public final int nrIDs(final IndexToken token) {
    // token to long.. no results can be expected
    if(token.get().length > MAXLEN) return Integer.MAX_VALUE;
    switch(token.type()) {
      case TXT: return txtindex.nrIDs(token);
      case ATV: return atvindex.nrIDs(token);
      case FTX: return ftxindex.nrIDs(token);
      default:  return Integer.MAX_VALUE;
    }
  }

  /**
   * Returns the document nodes.
   * @return root nodes
   */
  public final int[] doc() {
    final IntList il = new IntList();
    for(int i = 0; i < meta.size; i += size(i, Data.DOC)) il.add(i);
    return il.finish();
  }

  /**
   * Returns info on the specified index structure.
   * @param type index type
   * @return info
   */
  public final byte[] info(final Type type) {
    switch(type) {
      case TAG: return tags.info();
      case ATN: return atts.info();
      case TXT: return txtindex.info();
      case ATV: return atvindex.info();
      case FTX: return ftxindex.info();
      default: return EMPTY;
    }
  }

  /**
   * Returns an atomized content for any node kind.
   * The atomized value can be an attribute value or XML content.
   * @param pre pre value
   * @return atomized value
   */
  public final byte[] atom(final int pre) {
    switch(kind(pre)) {
      case TEXT: case COMM:
        return text(pre, true);
      case ATTR:
        return text(pre, false);
      case PI:
        final byte[] txt = text(pre, true);
        return substring(txt, indexOf(txt, ' ') + 1);
      default:
        // create atomized text node
        final TokenBuilder tb = new TokenBuilder();
        int p = pre;
        final int s = p + size(p, kind(p));
        while(p != s) {
          final int k = kind(p);
          if(k == TEXT) tb.add(text(p, true));
          p += attSize(p, k);
        }
        return tb.finish();
    }
  }

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public final void delete(final int pre) {
    meta.update();
    //if(fs != null) fs.delete(pre);

    // size of the subtree to delete
    int k = kind(pre);
    int s = size(pre, k);
    ns.delete(pre, s);

    // reduce size of ancestors
    int par = pre;
    // check if we are an attribute (different size counters)
    if(k == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
      k = kind(par);
    }

    // reduce size of remaining ancestors
    while(par > 0 && k != DOC) {
      par = parent(par, k);
      k = kind(par);
      size(par, k, size(par, k) - s);
    }

    // preserve empty root node
    int p = pre;
    final boolean empty = p == 0 && s == meta.size;
    if(empty) {
      p++;
      s--;
    }

    // delete node from table structure and reduce document size
    table.delete(pre, s);
    updateDist(p, -s);

    // restore empty document node
    if(empty) table.set(0, doc(0, 1, EMPTY));
  }

  /**
   * Renames an element, name or pi name.
   * @param pre pre value
   * @param k node kind
   * @param nm new tag, attribute or pi name
   */
  public final void rename(final int pre, final int k, final byte[] nm) {
    meta.update();
    if(k == PI) {
      final byte[] val = text(pre, true);
      final int i = indexOf(val, ' ');
      text(pre, i == -1 ? nm : concat(nm, SPACE, substring(val, i + 1)), true);
    } else {
      name(pre, (k == ELEM ? tags : atts).index(nm, null, false));
    }
  }

  /**
   * Replaces a text, comment, pi or attribute value.
   * @param pre pre value
   * @param k node kind
   * @param val value to be updated (tag name, text, comment, pi)
   */
  public final void replace(final int pre, final int k, final byte[] val) {
    meta.update();
    byte[] v = val;
    if(k == PI) {
      final byte[] o = text(pre, true);
      final int i = indexOf(o, ' ');
      v = concat(i == -1 ? o : substring(o, 0, i), SPACE, v);
    }
    text(pre, v, k != ATTR);
  }

  /**
   * Inserts attributes.
   * @param pre pre value
   * @param par parent of node
   * @param dt data instance to copy from
   */
  public final void insertAttr(final int pre, final int par, final Data dt) {
    meta.update();
    insert(pre, par, dt);
    attSize(par, ELEM, attSize(par, ELEM) + dt.meta.size);
  }

  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param pre value at which to insert new data
   * @param par parent pre value of node
   * @param dt data instance to copy from
   */
  public final void insert(final int pre, final int par, final Data dt) {
    meta.update();

    // copy database entries
    final int ss = dt.meta.size;
    for(int s = 0; s < ss; s++) {
      final int k = dt.kind(s);
      final int r = dt.parent(s, k);
      final int p = s + pre;
      final int d = r >= 0 ? s - r : p - par;

      switch(k) {
        case DOC:
          // add document
          insertDoc(p, dt.size(s, k), dt.text(s, true));
          break;
        case ELEM:
          // add element
          int n = tags.index(dt.name(s, true), null, false);
          insertElem(p, d, n, dt.attSize(s, k), dt.size(s, k), dt.uri(s, k),
              dt.nsFlag(s));
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          insertText(p, d, dt.text(s, true), k);
          break;
        case ATTR:
          // add attribute
          n = atts.index(dt.name(s, false), null, false);
          insertAttr(p, d, n, dt.text(s, false), dt.uri(s, k));
          break;
      }
    }
    // no document was inserted - update table structure
    if(par >= 0) updateTable(pre, par, ss);
    // delete old empty root node
    if(size(0, DOC) == 1 && name(0, true).length == 0) delete(0);
  }

  /**
   * This method is called after a table modification. It updates the
   * size values of the ancestors and the distance values of the
   * following siblings.
   * @param pre root node
   * @param par parent node
   * @param s size to be added
   */
  private void updateTable(final int pre, final int par, final int s) {
    // increase sizes
    int p = par;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + s);
      p = parent(p, k);
    }
    updateDist(pre + s, s);
  }

  /**
   * This method updates the distance values of the specified pre value
   * and the following siblings.
   * @param pre root node
   * @param s size to be added/removed
   */
  private void updateDist(final int pre, final int s) {
    int p = pre;
    while(p < meta.size) {
      final int k = kind(p);
      dist(p, k, dist(p, k) + s);
      p += size(p, kind(p));
    }
  }

  // UPDATE OPERATIONS ========================================================

  /**
   * Inserts a text node without updating the table structure.
   * @param pre insert position
   * @param sz node size
   * @param val document name
   */
  public final void insertDoc(final int pre, final int sz, final byte[] val) {
    table.insert(pre, doc(pre, sz, val));
  }

  /**
   * Returns a document entry.
   * @param pre pre value
   * @param sz node size
   * @param val document name
   * @return document entry
   */
  public final byte[] doc(final int pre, final int sz, final byte[] val) {
    // build and insert new entry
    final long id = ++meta.lastid;
    final long v = index(val, pre, true);
    return new byte[] {
        DOC, 0, 0, (byte) (v >> 32),
        (byte) (v >> 24), (byte) (v >> 16), (byte) (v >> 8), (byte) v,
        (byte) (sz >> 24), (byte) (sz >> 16), (byte) (sz >> 8), (byte) sz,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id };
  }
  
  
  /**
   * Inserts an element node without updating the table structure.
   * @param pre insert position
   * @param dis parent distance
   * @param tn tag name index
   * @param as number of attributes
   * @param s node size
   * @param u namespace uri reference
   * @param ne namespace flag
   */
  public final void insertElem(final int pre, final int dis,
      final int tn, final int as, final int s, final int u, final boolean ne) {

    final long id = ++meta.lastid;
    table.insert(pre, new byte[] {
        (byte) (as << 3 | ELEM), (byte) ((ne ? 1 << 7 : 0) | (byte) (tn >> 8)),
        (byte) tn, (byte) u,
        (byte) (dis >> 24), (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis,
        (byte) (s >> 24), (byte) (s >> 16), (byte) (s >> 8), (byte) s,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
  }

  /**
   * Inserts a text, comment or pi without updating the table structure.
   * @param pre insert position
   * @param dis parent distance
   * @param val tag name or text node
   * @param k node kind
   */
  public final void insertText(final int pre, final int dis,
      final byte[] val, final int k) {

    // build and insert new entry
    final long id = ++meta.lastid;
    final long len = index(val, pre, true);

    table.insert(pre, new byte[] {
        (byte) k, 0, 0, (byte) (len >> 32),
        (byte) (len >> 24), (byte) (len >> 16), (byte) (len >> 8), (byte) len,
        (byte) (dis >> 24), (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
  }

  /**
   * Inserts an attribute without updating the table structure.
   * @param pre pre value
   * @param dis parent distance
   * @param name attribute name
   * @param val attribute value
   * @param u namespace uri reference
   */
  public final void insertAttr(final int pre, final int dis, final int name,
      final byte[] val, final int u) {

    // add attribute to text storage
    final long len = index(val, pre, false);

    // build and insert new entry
    final long id = ++meta.lastid;
    table.insert(pre, new byte[] {
        (byte) (dis << 3 | ATTR), (byte) (name >> 8),
        (byte) name, (byte) (len >> 32),
        (byte) (len >> 24), (byte) (len >> 16), (byte) (len >> 8), (byte) len,
        0, 0, 0, (byte) u,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
  }

  /**
   * Indexes a text and returns the reference.
   * @param txt text to be indexed
   * @param pre pre value
   * @param text text/attribute flag
   * @return reference
   */
  protected abstract long index(final byte[] txt, final int pre,
      final boolean text);

  /**
   * Returns a string representation of the specified table range.
   * @param s start pre value
   * @param e end pre value
   * @return table
   */
  public String toString(final int s, final int e) {
    return string(InfoTable.table(this, s, e));
  }

  @Override
  public String toString() {
    //return string(InfoTable.table(this, 0, meta.size));
    return ns + string(InfoTable.table(this, 0, meta.size));
  }
}
