package org.basex.data;

import java.util.Arrays;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.index.Index;
import org.basex.index.MemValues;
import org.basex.index.Names;
import org.basex.util.Token;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure. The table mapping
 * is documented in {@link DiskData}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class MemData extends Data {
  /** Value array. */
  protected long[] val1;
  /** Value array. */
  protected long[] val2;

  /**
   * Constructor.
   * @param cap initial array capacity
   * @param tag tag index
   * @param att attribute name index
   * @param n namespaces
   * @param s path summary
   * @param pr database properties
   */
  public MemData(final int cap, final Names tag, final Names att,
      final Namespaces n, final PathSummary s, final Prop pr) {
    val1 = new long[cap];
    val2 = new long[cap];
    txtindex = new MemValues();
    atvindex = new MemValues();
    meta = new MetaData("", pr);
    tags = tag;
    atts = att;
    ns = n;
    path = s;
  }

  /**
   * Constructor, adopting meta data from the specified database.
   * @param cap initial array capacity
   * @param data data reference
   */
  public MemData(final int cap, final Data data) {
    val1 = new long[cap];
    val2 = new long[cap];
    txtindex = new MemValues();
    atvindex = new MemValues();
    meta = new MetaData("", data.meta.prop);
    tags = data.tags;
    atts = data.atts;
    ns = data.ns;
    path = data.path;
  }

  @Override
  public final void flush() { }

  @Override
  public final void cls() { }

  @Override
  public final void closeIndex(final Type index) { }

  @Override
  public final void setIndex(final Type type, final Index ind) {
    Main.notimplemented();
  }

  @Override
  public final int id(final int pre) {
    return pre;
  }

  @Override
  public final int pre(final int id) {
    return id;
  }

  @Override
  public final int kind(final int pre) {
    return (int) (val1[pre] >>> 56) & 0x07;
  }

  @Override
  public final int parent(final int pre, final int k) {
    return pre - dist(pre, k);
  }

  @Override
  protected int dist(final int pre, final int k) {
    switch(k) {
      case ELEM:
        return (int) (val1[pre] & 0xFFFFFFFF);
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
  public final int attSize(final int pre, final int kind) {
    return kind == ELEM ? (int) (val1[pre] >> 32) & 0xFF : 1;
  }

  @Override
  public final int size(final int pre, final int k) {
    return k == ELEM || k == DOC ? (int) (val2[pre] >> 32) : 1;
  }

  @Override
  public final int tagID(final int pre) {
    return (int) (val1[pre] >> 40) & 0xFFFF;
  }

  @Override
  public final int tagNS(final int pre) {
    return (int) (val1[pre] >>> 60) & 0x0F;
  }

  @Override
  public final int[] ns(final int pre) {
    return (val1[pre] & 1L << 59) != 0 ? ns.get(pre) : new int[] {};
  }

  @Override
  public final int attNameID(final int pre) {
    return (int) (val1[pre] >> 40) & 0xFFFF;
  }

  @Override
  public final int attNS(final int pre) {
    return (int) (val1[pre] >>> 60) & 0x0F;
  }

  @Override
  public final byte[] text(final int pre) {
    return ((MemValues) txtindex).token((int) val1[pre]);
  }

  @Override
  public final double textNum(final int pre) {
    return Token.toDouble(text(pre));
  }

  @Override
  public final byte[] attValue(final int pre) {
    return ((MemValues) atvindex).token((int) val1[pre]);
  }

  @Override
  public final double attNum(final int pre) {
    return Token.toDouble(attValue(pre));
  }

  @Override
  public final int textLen(final int pre) {
    return text(pre).length;
  }

  @Override
  public final int attLen(final int pre) {
    return attValue(pre).length;
  }

  /**
   * Indexes the specified attribute value and returns the index reference.
   * @param t attribute value
   * @return index reference
   */
  protected final int attIndex(final byte[] t) {
    return ((MemValues) atvindex).index(t, meta.size);
  }

  /**
   * Indexes the specified text node and returns the index reference.
   * @param t text node
   * @return index reference
   */
  protected final int textIndex(final byte[] t) {
    return ((MemValues) txtindex).index(t, meta.size);
  }

  /**
   * Adds an element.
   * @param t document name
   * @param s node size (+ 1)
   */
  public final void addDoc(final byte[] t, final int s) {
    check();
    val1[meta.size] = ((long) DOC << 56) + textIndex(t);
    val2[meta.size++] = ((long) s << 32) + meta.size;
  }

  /**
   * Adds an element.
   * @param t tag
   * @param n namespace
   * @param d distance
   * @param a number of attributes (+ 1)
   * @param s node size (+ 1)
   * @param ne element has namespaces
   */
  public final void addElem(final int t, final int n, final int d, final int a,
      final int s, final boolean ne) {
    check();
    val1[meta.size] = ((long) n << 60) + (ne ? 1L << 59 : 0) +
      ((long) ELEM << 56) + ((long) t << 40) + ((long) a << 32) + d;
    val2[meta.size++] = ((long) s << 32) + meta.size;
  }

  /**
   * Adds an attribute.
   * @param t attribute name
   * @param n namespace
   * @param v attribute value
   * @param d distance
   */
  public final void addAtt(final int t, final int n, final byte[] v,
      final int d) {
    check();
    val1[meta.size] = ((long) n << 60) + ((long) ATTR << 56) +
      ((long) t << 40) + attIndex(v);
    val2[meta.size++] = ((long) d << 32) + meta.size;
  }

  /**
   * Adds a text node.
   * @param t text to be added
   * @param d distance
   * @param k node kind
   */
  public final void addText(final byte[] t, final int d, final int k) {
    check();
    val1[meta.size] = ((long) k << 56) + textIndex(t);
    val2[meta.size++] = ((long) d << 32) + meta.size;
  }

  /**
   * Checks the array sizes.
   */
  private void check() {
    if(meta.size == val1.length) {
      final int s = val1.length << 1;
      val1 = Arrays.copyOf(val1, s);
      val2 = Arrays.copyOf(val2, s);
    }
  }

  /**
   * Stores an attribute value to the table.
   * @param pre pre reference
   * @param val value to be stored
   */
  public final void attValue(final int pre, final byte[] val) {
    val1[pre] = val1[pre] & 0xFFFFFF0000000000L | attIndex(val);
  }

  @Override
  public void dist(final int pre, final int kind, final int val) {
    if(kind == ELEM) val1[pre] = val1[pre] & 0xFFFFFFFF00000000L | val;
    else if(kind != DOC) val2[pre] = (int) val2[pre] | (long) val << 32;
  }

  @Override
  public void attSize(final int pre, final int kind, final int v) {
    if(kind == ELEM) val1[pre] = val1[pre] & 0xFFFFFF00FFFFFFFFL | v;
  }

  @Override
  public final void size(final int pre, final int kind, final int val) {
    if(kind == ELEM || kind == DOC)
      val2[pre] = (int) val2[pre] | (long) val << 32;
  }

  // UPDATES ON VALUE ARRAYS ==================================================

  @Override
  protected void update(final int pre, final byte[] val, final boolean txt) {
    final long t = ((MemValues) (txt ? txtindex : atvindex)).index(val, pre);
    val1[pre] = val1[pre] & 0xFFFFFFFF00000000L | t;
  }

  @Override
  protected void insertElem(final int pre, final int dis, final byte[] tag,
      final int as, final int s) {
    insert(pre, (long) ELEM << 56 | (long) tags.index(tag, null, false) << 40 |
        (long) as << 32 | dis, (long) s << 32 | ++meta.lastid);
  }

  @Override
  protected void insertDoc(final int pre, final int s, final byte[] val) {
    insert(pre, (long) DOC << 56 | ((MemValues) txtindex).index(val, pre),
      (long) s << 32 | ++meta.lastid);
  }

  @Override
  protected void insertText(final int pre, final int dis, final byte[] val,
      final int kind) {
    insert(pre, (long) TEXT << 56 | ((MemValues) txtindex).index(val, pre),
      (long) dis << 32 | ++meta.lastid);
  }

  @Override
  protected void insertAttr(final int pre, final int dis, final byte[] name,
      final byte[] val) {
    insert(pre, (long) ATTR << 56 | (long) atts.index(name, null, false) << 40 |
      ((MemValues) atvindex).index(val, pre), (long) dis << 32 | ++meta.lastid);
  }

  @Override
  protected void tagID(final int pre, final int v) {
    val1[pre] = val1[pre] & 0xFF0000FFFFFFFFFFL | (long) v << 40;
  }

  @Override
  protected void attNameID(final int pre, final int v) {
    val1[pre] = val1[pre] & 0xFF0000FFFFFFFFFFL | (long) v << 40;
  }

  @Override
  protected void delete(final int pre, final int nr) {
    move(pre + nr, pre);
  }

  /**
   * Moves data inside the value arrays.
   * @param sp source position
   * @param dp destination position
   */
  private void move(final int sp, final int dp) {
    final int l = meta.size - sp;
    while(dp > sp && l + dp >= val1.length) {
      val1 = Arrays.copyOf(val1, val1.length << 1);
      val2 = Arrays.copyOf(val2, val2.length << 1);
    }
    System.arraycopy(val1, sp, val1, dp, l);
    System.arraycopy(val2, sp, val2, dp, l);
  }

  /**
   * Inserts the specified entries into the table.
   * @param pre pre value
   * @param v1 first entry
   * @param v2 second entry
   */
  private void insert(final int pre, final long v1, final long v2) {
    move(pre, pre + 1);
    val1[pre] = v1;
    val2[pre] = v2;
    meta.size++;
  }
}
