package org.basex.data;

import org.basex.BaseX;
import org.basex.index.Index;
import org.basex.index.IndexToken;
import org.basex.index.MemValues;
import org.basex.index.Names;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure. The storage equals the
 * disk storage in {@link DiskData}.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemData extends Data {
  /** Value array. */
  private long[] val1;
  /** Value array. */
  private long[] val2;

  /**
   * Constructor.
   * @param cap initial array capacity
   * @param tag tag index
   * @param att attribute name index
   * @param n namespaces
   * @param s skeleton
   */
  public MemData(final int cap, final Names tag, final Names att,
      final Namespaces n, final Skeleton s) {
    val1 = new long[cap];
    val2 = new long[cap];
    txtindex = new MemValues();
    atvindex = new MemValues();
    tags = tag;
    atts = att;
    ns = n;
    skel = s;
  }

  @Override
  public void flush() { }

  @Override
  public void close() { }

  @Override
  public void closeIndex(final IndexToken.Type index) { }

  @Override
  public void setIndex(final IndexToken.Type type, final Index ind) {
    BaseX.notimplemented();
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
  public int kind(final int pre) {
    return (int) (val1[pre] >>> 56);
  }

  @Override
  public int parent(final int pre, final int k) {
    return pre - dist(pre, k);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param k node kind
   * @return distance
   */
  private int dist(final int pre, final int k) {
    switch(k) {
      case ELEM:
        return (int) (val1[pre] & 0xFFFF);
      case TEXT:
      case COMM:
      case PI:
      case ATTR:
        return (int) (val2[pre] >> 32);
      default:
        return pre + 1;
    }
  }

  @Override
  public int attSize(final int pre, final int kind) {
    return kind == ELEM ? (int) (val1[pre] >> 32) & 0xFF : 1;
  }

  @Override
  public int size(final int pre, final int k) {
    return k == ELEM || k == DOC ? (int) (val2[pre] >> 32) : 1;
  }

  @Override
  public int tagID(final int pre) {
    return (int) (val1[pre] >> 40) & 0x07FF;
  }

  @Override
  public int tagNS(final int pre) {
    return (int) (val1[pre] >>> 52) & 0x0F;
  }

  @Override
  public int[] ns(final int pre) {
    return (val1[pre] & 1L << 51) != 0 ? ns.get(pre) : Array.NOINTS;
  }

  @Override
  public int attNameID(final int pre) {
    return (int) (val1[pre] >> 40) & 0x07FF;
  }

  @Override
  public int attNS(final int pre) {
    return (int) (val1[pre] >>> 52) & 0x0F;
  }

  @Override
  public byte[] text(final int pre) {
    return ((MemValues) txtindex).token((int) val1[pre]);
  }

  @Override
  public double textNum(final int pre) {
    return Token.toDouble(text(pre));
  }

  @Override
  public byte[] attValue(final int pre) {
    return ((MemValues) atvindex).token((int) val1[pre]);
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
   * Indexes the specified attribute value and returns the index reference.
   * @param t attribute value
   * @return index reference
   */
  protected int attIndex(final byte[] t) {
    return ((MemValues) atvindex).index(t, meta.size);
  }

  /**
   * Indexes the specified text node and returns the index reference.
   * @param t text node
   * @return index reference
   */
  protected int textIndex(final byte[] t) {
    return ((MemValues) txtindex).index(t, meta.size);
  }

  /**
   * Adds an element.
   * @param t document name
   * @param s node size
   */
  public void addDoc(final byte[] t, final long s) {
    check();
    val1[meta.size] = ((long) DOC << 56) + textIndex(t);
    val2[meta.size++] = (s << 32) + meta.size;
  }

  /**
   * Adds an element.
   * @param t tag
   * @param n namespace
   * @param d distance
   * @param ne element has namespaces
   * @param a number of attributes
   * @param s node size
   */
  public void addElem(final long t, final long n, final long d, final long a,
      final long s, final boolean ne) {

    check();
    val1[meta.size] = ((long) ELEM << 56) + (n << 52) + (ne ? 1L << 51 : 0)
        + (t << 40) + (a << 32) + d;
    val2[meta.size++] = (s << 32) + meta.size;
  }

  /**
   * Adds an attribute.
   * @param t attribute name
   * @param n namespace
   * @param v attribute value
   * @param d distance
   */
  public void addAtt(final long t, final long n, final byte[] v, final long d) {
    check();
    val1[meta.size] = ((long) ATTR << 56) + (n << 52) + (t << 40) + attIndex(v);
    val2[meta.size++] = (d << 32) + meta.size;
  }

  /**
   * Adds a text node.
   * @param t text to be added
   * @param d distance
   * @param k node kind
   */
  public void addText(final byte[] t, final long d, final long k) {
    check();
    val1[meta.size] = (k << 56) + textIndex(t);
    val2[meta.size++] = (d << 32) + meta.size;
  }

  /**
   * Stores a size value to the table.
   * @param pre pre reference
   * @param val value to be stored
   */
  public void setSize(final int pre, final long val) {
    val2[pre] = (int) val2[pre] | val << 32;
  }

  /**
   * Stores an attribute value to the table.
   * @param pre pre reference
   * @param val value to be stored
   */
  public void setAttValue(final int pre, final byte[] val) {
    val1[pre] = val1[pre] & 0xFFFFFF0000000000L | attIndex(val);
  }

  /**
   * Checks the array sizes.
   */
  private void check() {
    if(meta.size == val1.length) {
      val1 = Array.extend(val1);
      val2 = Array.extend(val2);
    }
  }

  @Override
  public void delete(final int pre) {
    BaseX.notimplemented();
  }

  @Override
  public void update(final int pre, final byte[] attName,
      final byte[] attValue) {
    BaseX.notimplemented();
  }

  @Override
  public void insert(final int pre, final int par, final byte[] tag,
      final int kind) {
    BaseX.notimplemented();
  }

  @Override
  public void insert(final int pre, final int par, final byte[] name,
      final byte[] v) {
    BaseX.notimplemented();
  }

  @Override
  public void insert(final int pre, final int par, final Data d) {
    BaseX.notimplemented();
  }

  @Override
  public void update(final int pre, final byte[] text) {
    BaseX.notimplemented();
  }
}
