package org.basex.data;

import org.basex.BaseX;
import org.basex.index.Index;
import org.basex.index.MemValues;
import org.basex.index.Names;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure.
 * Each node occupies 64 bits. The current storage layout looks like follows:
 * 
 * <pre>
 * ELEMENTS:
 * - Bit 0-2   : Node kind (ELEM/DOC)
 * - Bit 3-7   : Number of attributes
 * - Byte  1- 3: Number of descendants (size)
 * - Byte  4   : Namespace and tag name
 * - Byte  5- 7: Relative parent reference
 * TEXT NODES:
 * - Bit 0-2   : Node kind (TEXT/PI/COMM)
 * - Byte  0- 3: Text reference
 * - Byte  5- 7: Relative parent reference
 * ATTRIBUTE NODES:
 * - Bit 0-2   : Node kind (ATTR)
 * - Byte  0- 3: Attribute value
 * - Byte     4: Namespace and attribute name
 * - Byte     7: Relative parent reference
 * </pre>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemData extends Data {
  /** Value array. */
  private long[] val;

  /**
   * Constructor.
   * @param cap initial array capacity
   * @param tag tag index
   * @param att attribute name index
   * @param n namespaces
   */
  public MemData(final int cap, final Names tag, final Names att,
      final Namespaces n) {
    val = new long[cap];
    txtindex = new MemValues();
    atvindex = new MemValues();
    tags = tag;
    atts = att;
    ns = n;
  }

  @Override
  public void flush() { }

  @Override
  public void close() { }

  @Override
  public void closeIndex(final Index.TYPE index) { }

  @Override
  public void openIndex(final Index.TYPE type, final Index ind) { }

  @Override
  public int kind(final int pre) {
    return (int) (val[pre] >>> 61);
  }

  @Override
  public int parent(final int pre, final int kind) {
    return pre - (int) (val[pre] & 0xFFFFFF);
  }

  @Override
  public int size(final int pre, final int kind) {
    return kind == ELEM || kind == DOC ? (int) (val[pre] >> 32 & 0xFFFFFF) : 1;
  }

  @Override
  public int tagID(final int pre) {
    return (int) (val[pre] >>> 24) & 0x3F;
  }

  @Override
  public int tagNS(final int pre) {
    return (int) (val[pre] >>> 30) & 0x3;
  }

  @Override
  public int attSize(final int pre, final int kind) {
    return kind == ELEM ? (int) (val[pre] >> 56 & 0x1F) : 1;
  }

  @Override
  public int attNameID(final int pre) {
    return (int) (val[pre] >>> 24) & 0x3F;
  }

  @Override
  public int attNS(final int pre) {
    return (int) (val[pre] >>> 30) & 0x3;
  }

  @Override
  public byte[] text(final int pre) {
    return textToken((int) (val[pre] >> 32) & 0x1FFFFFFF);
  }

  @Override
  public byte[] text(final int pre, final int off, final int len) {
    BaseX.notimplemented();
    return null;
  }

  @Override
  public int id(final int pre) {
    return pre;
  }

  @Override
  public int pre(final int id) {
    return id;
  }

  @Override
  public double textNum(final int pre) {
    return Token.toDouble(text(pre));
  }

  @Override
  public byte[] attValue(final int pre) {
    return attToken((int) (val[pre] >> 32) & 0x1FFFFFFF);
  }

  @Override
  public double attNum(final int pre) {
    return Token.toDouble(attValue(pre));
  }

  @Override
  public int textLen(final int pre) {
    return text(pre).length;
  }

  @Override
  public int attLen(final int pre) {
    return attValue(pre).length;
  }

  /**
   * Returns the id for the specified attribute value.
   * @param v attribute value
   * @return id
   */
  public int attID(final byte[] v) {
    return ((MemValues) atvindex).get(v);
  }

  /**
   * Returns the index value for the specified attribute value id.
   * @param id index id
   * @return value
   */
  public byte[] attToken(final int id) {
    return ((MemValues) atvindex).token(id);
  }

  /**
   * Indexes the specified attribute value and returns the index reference.
   * @param t attribute value
   * @return index reference
   */
  protected int attIndex(final byte[] t) {
    return ((MemValues) atvindex).index(t, size);
  }

  /**
   * Indexes the specified text node and returns the index reference.
   * @param t text node
   * @return index reference
   */
  protected int textIndex(final byte[] t) {
    return ((MemValues) txtindex).index(t, size);
  }

  /**
   * Returns the index value for the specified index id.
   * @param id index id
   * @return value
   */
  public byte[] textToken(final int id) {
    return ((MemValues) txtindex).token(id);
  }

  /**
   * Convenience method for adding an element.
   * @param t tag
   * @param d distance
   * @param a number of attributes
   * @param s node size
   * @param k node kind
   */
  public void addElem(final byte[] t, final int d, final int a,
      final int s, final int k) {
    addElem(tags.index(t, null), ns.get(t), d, a, s, k);
  }

  /**
   * Adds an element.
   * @param t tag
   * @param n namespace
   * @param d distance
   * @param a number of attributes
   * @param s node size
   * @param k node kind
   */
  public void addElem(final int t, final int n, final int d, final int a,
      final int s, final int k) {

    check();
    val[size++] = ((long) k << 61) + ((long) a << 56) + ((long) n << 38) +
      ((long) s << 32) + ((long) t << 24) + d;
  }

  /**
   * Adds an attribute.
   * @param a attribute name
   * @param n namespace
   * @param v attribute value
   * @param d distance
   */
  public void addAtt(final int a, final int n, final byte[] v,
      final int d) {

    check();
    final long ai = attIndex(v);
    val[size++] = ((long) Data.ATTR << 61) + ((long) n << 38) + (ai << 32) +
      ((long) a << 24) + d;
  }

  /**
   * Adds a text node.
   * @param t text to be added
   * @param d distance
   * @param k node kind
   */
  public void addText(final byte[] t, final int d, final int k) {
    check();
    final long ti = textIndex(t);
    val[size++] = ((long) k << 61) + (ti << 32) + d;
  }

  /**
   * Adds the size value to the table.
   * @param pre closing pre tag
   */
  public void finishElem(final int pre) {
    val[pre] = (val[pre] & 0xFF000000FFFFFFFFL) + ((long) (size - pre) << 32);
  }

  /**
   * Copies some data.
   * @param d data reference
   * @param p position
   */
  public void append(final MemData d, final int p) {
    check();
    val[size++] = d.val[p];
  }

  /**
   * Convenience method for adding an attribute.
   * @param a attribute name
   * @param v attribute value
   * @param p parent
   */
  public void addAtt(final byte[] a, final byte[] v, final int p) {
    addAtt(atts.index(a, v), ns.get(a), v, p);
  }

  /**
   * Inserts a data instance at the specified position.
   * Attention: get sure that both data instances are based on the same
   * indexes as the references are simply copied and not checked at all...
   * @param d data instance
   * @param pos insertion position
   */
  public void insert(final MemData d, final int pos) {
    final int s = d.size;
    while(size + s >= val.length) val = Array.extend(val);

    check();
    System.arraycopy(val, pos, val, pos + s, size - pos);
    System.arraycopy(d.val, 0, val, pos, s);
    size += s;
  }

  /**
   * Checks the array sizes.
   */
  private void check() {
    if(size == val.length) val = Array.extend(val);
  }

  @Override
  public int[][] ftIDs(final byte[] word, final boolean cs) {
    BaseX.notimplemented();
    return null;
  }

  @Override
  public int[][] fuzzyIDs(final byte[] word, final int ne) {
    BaseX.notimplemented();
    return null;
  }

  @Override
  public int[][] wildcardIDs(final byte[] word, final int posw) {
    BaseX.notimplemented();
    return null;
  }

  
  @Override
  public int[] idRange(final Index.TYPE type, final double word0,
      final boolean iword0, final double word1, final boolean iword1) {
    BaseX.notimplemented();
    return null;
  }

  @Override
  public int nrFTIDs(final byte[] token) {
    BaseX.notimplemented();
    return 0;
  }

  @Override
  public void delete(final int pre) {
    BaseX.noupdates();
  }

  @Override
  public void update(final int pre, final byte[] attName,
      final byte[] attValue) {
    BaseX.noupdates();
  }

  @Override
  public void insert(final int pre, final int par, final byte[] tag,
      final byte kind) {
    BaseX.noupdates();
  }

  @Override
  public void insert(final int pre, final int par, final byte[] name,
      final byte[] v) {
    BaseX.noupdates();
  }

  @Override
  public void insert(final int pre, final int par, final Data d) {
    BaseX.noupdates();
  }

  @Override
  public void update(final int pre, final byte[] text) {
    BaseX.noupdates();
  }
}

