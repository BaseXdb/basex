package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.index.Index;
import org.basex.index.IndexToken;
import org.basex.io.DataAccess;
import org.basex.io.DataOutput;
import org.basex.io.TableAccess;
import org.basex.util.Token;

/**
 * This class stores and organizes the node table and the index structures for
 * textual content with the help of a native library.
 * <br/>
 * In fact it is used to interface with a mounted filesystem in userspace to
 * realize the queryable DeepFS filesystem/database hybrid.
 * <br/>
 * Find more info on the table structure in {@link DiskData}.
 *
 * @author Workgroup DBIS, University of Konstanz 2008, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 * @author Alexander Holupirek
 */
public final class DeepData extends Data {
  /** Table access file. */
  private final TableAccess table;
  /** Texts access file. */
  private final DataAccess texts;
  /** Values access file. */
  private final DataAccess values;

  /** Flag for loaded DeepFS library. */
  private static String libError;

  /* libdeepfs__Vx.xx.xx.so has to be in -Djava.library.path= */
  static {
    try {
      System.loadLibrary(DEEPLIB);
    } catch (final UnsatisfiedLinkError e) {
      libError = e.getMessage();
    }
  }

  /** Initialize native storage (constructor). 
   * @param index whether to use one
   * @return initialization status
   */
  private native boolean jniInit(final boolean index);
  /** Get the size of the file hierarchy table. 
   * @return size of table
   */
  private native int jniGetFileTableSize();
  
  /**
   * Default Constructor.
   * @param db name of database
   * @throws IOException IO Exception
   */
  public DeepData(final String db) throws IOException {
    this(db, false);
  }

  /**
   * Constructor, specifying if indexes are to be opened as well.
   * @param db name of database
   * @param index open indexes
   * @throws IOException IO Exception
   */
  public DeepData(final String db, final boolean index) throws IOException {
    // is caught by proc.Open.java
    if(libError != null) throw new IOException(libError);
    
    jniInit(index);
    
    meta = new MetaData(db);
    size = jniGetFileTableSize();

    // read indexes
    tags = null;
    atts = null;
    skel = null;
    ns = new Namespaces();

    // main memory mode.. keep table in memory
    table = null;
    texts = null;
    values = null;
  }

  @Override
  public synchronized void flush() {
    try {
      table.flush();
      texts.flush();
      values.flush();
      
      final DataOutput out = new DataOutput(meta.dbname, DATAINFO);
      meta.finish(out, size);
      tags.finish(out);
      atts.finish(out);
      skel.finish(out);
      ns.finish(out);
      out.close();
    } catch(final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public synchronized void close() throws IOException {
    flush();
    cls();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    cls();
  }

  /**
   * Closes the database without writing data back to disk.
   * @throws IOException I/O exception
   */
  public void cls() throws IOException {
    table.close();
    texts.close();
    values.close();
    closeIndex(IndexToken.TYPE.TXT);
    closeIndex(IndexToken.TYPE.ATV);
    closeIndex(IndexToken.TYPE.FTX);
  }

  @Override
  public void closeIndex(final IndexToken.TYPE index) throws IOException {
    switch(index) {
      case TXT: if(txtindex != null) txtindex.close(); break;
      case ATV: if(atvindex != null) atvindex.close(); break;
      case FTX: if(ftxindex != null) ftxindex.close(); break;
      default: break;
    }
  }

  @Override
  public void setIndex(final IndexToken.TYPE type, final Index index) {
    switch(type) {
      case TXT: if(meta.txtindex) txtindex = index; break;
      case ATV: if(meta.atvindex) atvindex = index; break;
      case FTX: if(meta.ftxindex) ftxindex = index; break;
      default: break;
    }
  }

  @Override
  public int id(final int pre) {
    return table.read4(pre, 12);
  }

  @Override
  public int pre(final int id) {
    // find pre value in table
    for(int p = id; p < size; p++) if(id == id(p)) return p;
    for(int p = 0; p < id; p++) if(id == id(p)) return p;
    // id not found
    return -1;
  }

  @Override
  public int kind(final int pre) {
    return table.read1(pre, 0);
  }

  @Override
  public int parent(final int pre, final int kind) {
    return pre - dist(pre, kind);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param kind node kind
   * @return distance
   */
  private int dist(final int pre, final int kind) {
   switch(kind) {
      case ELEM: return table.read4(pre, 4);
      case TEXT:
      case COMM:
      case PI:   return table.read4(pre, 8);
      case ATTR: return table.read1(pre, 11);
      default:   return pre + 1;
    }
  }

  @Override
  public int attSize(final int pre, final int kind) {
    return kind == ELEM ? table.read1(pre, 3) : 1;
  }

  @Override
  public int size(final int pre, final int k) {
    return k == ELEM || k == DOC ? table.read4(pre, 8) : 1;
  }

  @Override
  public int tagID(final int pre) {
    return table.read2(pre, 1) & 0x0FFF;
  }

  @Override
  public int tagNS(final int pre) {
    return (table.read2(pre, 1) >>> 12) & 0x0F;
  }

  @Override
  public int[] ns(final int pre) {
    return null;
  }

  @Override
  public int attNameID(final int pre) {
    return table.read2(pre, 1) & 0x0FFF;
  }

  @Override
  public int attNS(final int pre) {
    return (table.read2(pre, 1) >>> 12) & 0x0F;
  }

  @Override
  public byte[] text(final int pre) {
    return txt(pre, texts);
  }

  @Override
  public byte[] attValue(final int pre) {
    return txt(pre, values);
  }

  /**
   * Returns the text of a text/attribute value.
   * @param pre pre value
   * @param da text reference
   * @return disk offset
   */
  private byte[] txt(final int pre, final DataAccess da) {
    final long o = textOff(pre);
    return (o & 0x8000000000L) != 0 ? token(o) : da.readToken(o);
  }

  @Override
  public double textNum(final int pre) {
    return txtNum(pre, texts);
  }

  @Override
  public double attNum(final int pre) {
    return txtNum(pre, values);
  }

  /**
   * Returns the double value of the specified pre value.
   * @param pre pre value
   * @param da text reference
   * @return disk offset
   */
  private double txtNum(final int pre, final DataAccess da) {
    final long off = textOff(pre);
    return (off & 0x8000000000L) != 0 ? (int) off :
      Token.toDouble(da.readToken(off));
  }

  @Override
  public int textLen(final int pre) {
    return txtLen(pre, texts);
  }

  @Override
  public int attLen(final int pre) {
    return txtLen(pre, values);
  }

  /**
   * Returns the disk offset of a text/attribute value.
   * @param pre pre value
   * @param da text reference
   * @return disk offset
   */
  private int txtLen(final int pre, final DataAccess da) {
    final long off = textOff(pre);
    return (off & 0x8000000000L) != 0 ? Token.numDigits((int) off) :
      da.readNum(off);
  }

  /**
   * Returns the disk offset of a text/attribute value.
   * @param pre pre value
   * @return disk offset
   */
  private long textOff(final int pre) {
    return table.read5(pre, 3);
  }

  /**
   * Converts the specified long value into a byte array.
   * @param i int value to be converted
   * @return byte array
   */
  private static byte[] token(final long i) {
    int n = (int) i;
    if(n == 0) return Token.ZERO;
    int j = Token.numDigits(n);
    final byte[] num = new byte[j];

    // faster division by 10 for values < 81920 (see {@link Integer#getChars}
    while(n > 81919) {
      final int q = n / 10;
      num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
      n = q;
    }
    while(n != 0) {
      final int q = (n * 52429) >>> 19;
      num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
      n = q;
    }
    return num;
  }

  @Override
  public void update(final int pre, final byte[] val) {
    if(kind(pre) == ELEM) {
      tagID(pre, tags.index(val, null));
    } else {
      update(pre, val, true);
    }
  }

  @Override
  public void update(final int pre, final byte[] name, final byte[] val) {
    update(pre, val, false);
    attNameID(pre, atts.index(name, val));
  }

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param val content
   * @param txt text flag
   */
  private void update(final int pre, final byte[] val, final boolean txt) {
    final long v = Token.toSimpleInt(val);
    if(v != Integer.MIN_VALUE) {
      textOff(pre, v | 0x8000000000L);
    } else {
      long off = textOff(pre);
      final boolean replace = (off & 0x8000000000L) == 0 &&
        val.length <= (txt ? textLen(pre) : attLen(pre));
      final DataAccess da = txt ? texts : values;

      // default: append new text to the end of file
      if(replace) {
        // new text is shorter than last one; replace it
        da.writeBytes(off, val);
      } else {
        // if current text is placed last, replace it with new one
        if(da.readNum(off) + da.pos() != da.length()) off = da.length();
        da.writeBytes(off, val);
        textOff(pre, off);
      }
    }
  }

  @Override
  public void delete(final int pre) {
    int k = kind(pre);

    // size of the subtree to delete
    final int s = size(pre, k);

    // reduce size of ancestors
    int par = pre;

    // check if we are an attribute (different size counters)
    if(k == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
    }

    // reduce size of remaining ancestors
    while(par > 0) {
      k = kind(par);
      if(k == DOC) break;
      par = parent(par, k);
      size(par, k, size(par, k) - s);
    }

    // delete node from table structure and reduce document size
    table.delete(pre, s);
    size -= s;

    updateDist(pre, -s);
  }

  /**
   * Updates the ancestor sizes and parent references of the following nodes.
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
   * Updates the distance values.
   * @param pre root node
   * @param s size to be added/removed
   */
  private void updateDist(final int pre, final int s) {
    int p = pre;
    while(p < size) {
      final int k = kind(p);
      dist(p, k, dist(p, k) + s);
      p += size(p, kind(p));
    }
  }

  @Override
  public void insert(final int pre, final int par, final byte[] val,
      final int kind) {

    if(kind == ELEM) {
      insertElem(pre - 1, pre - par, val, 1, 1);
    } else if(kind == DOC) {
      insertDoc(pre - 1, 1, val);
    } else {
      insertText(pre - 1, pre - par, val, kind);
    }
    updateTable(pre, par, 1);
  }

  @Override
  public void insert(final int pre, final int par, final byte[] name,
      final byte[] val) {

    // insert attribute and increase attSize of parent element
    insertAttr(pre - 1, pre - par, name, val);
    attSize(par, ELEM, attSize(par, ELEM) + 1);
    updateTable(pre, par, 1);
  }

  @Override
  public void insert(final int pre, final int par, final Data td) {
    // reference to root tag
    final int tr = 0;
    final int ts = td.size(tr, td.kind(tr));
    final int tl = tr + ts;

    for(int i = tr; i < tl; i++) {
      final int tk = td.kind(i);
      int dis = i - td.parent(i, tk);
      if(dis > i) dis = pre - par;
      final int p = pre + i - tr - 1;

      switch(tk) {
        case ELEM:
          // add element
          insertElem(p, dis, td.tag(i), td.attSize(i, tk), td.size(i, tk));
          break;
        case DOC:
          // add document
          insertDoc(p, td.size(i, tk), td.text(i));
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          insertText(p, dis, td.text(i), tk);
          break;
        case ATTR:
          // add attribute
          insertAttr(p, dis, td.attName(i), td.attValue(i));
          break;
      }
    }
    updateTable(pre, par, ts);
  }

  /**
   * Inserts an element node without updating the size and distance values
   * of the table.
   * @param pre insert position
   * @param dis parent distance
   * @param tag tag name index
   * @param as number of attributes
   * @param s node size
   */
  private void insertElem(final int pre, final int dis,
      final byte[] tag, final int as, final int s) {

    final long id = ++meta.lastid;
    final int t = tags.index(tag, null);
    table.insert(pre, new byte[] { ELEM, (byte) (t >> 8), (byte) t, (byte) as,
        (byte) (dis >> 24), (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis, 
        (byte) (s >> 24), (byte) (s >> 16), (byte) (s >> 8), (byte) s,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
    size++;
  }

  /**
   * Insert text node without updating the size and distance values
   * of the table.
   * @param pre insert position
   * @param s node size
   * @param val tag name or text node
   */
  private void insertDoc(final int pre, final int s, final byte[] val) {
    // build and insert new entry
    final long id = ++meta.lastid;
    final long txt = texts.length();
    texts.writeBytes(txt, val);

    table.insert(pre, new byte[] { DOC, 0, 0, (byte) (txt >> 32),
        (byte) (txt >> 24), (byte) (txt >> 16), (byte) (txt >> 8), (byte) txt,
        (byte) (s >> 24), (byte) (s >> 16), (byte) (s >> 8), (byte) s,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
    size++;
  }

  /**
   * Insert text node updating the size and distance values
   * of the table.
   * @param pre insert position
   * @param dis parent distance
   * @param val tag name or text node
   * @param kind node kind
   */
  private void insertText(final int pre, final int dis, final byte[] val,
      final int kind) {

    // build and insert new entry
    final long id = ++meta.lastid;
    final long txt = texts.length();
    texts.writeBytes(txt, val);

    table.insert(pre, new byte[] { (byte) kind, 0, 0, (byte) (txt >> 32),
        (byte) (txt >> 24), (byte) (txt >> 16), (byte) (txt >> 8), (byte) txt,
        (byte) (dis >> 24), (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
    size++;
  }

  /**
   * Insert attribute updating the size and distance values
   * of the table.
   * @param pre pre value
   * @param dis parent distance
   * @param name attribute name
   * @param val attribute value
   */
  private void insertAttr(final int pre, final int dis, final byte[] name,
      final byte[] val) {

    // add attribute to text storage
    final long len = values.length();
    values.writeBytes(len, val);

    // build and insert new entry
    final int att = atts.index(name, val);
    final long id = ++meta.lastid;
    table.insert(pre, new byte[] { ATTR, (byte) (att >> 8), (byte) att,
        (byte) (len >> 32), (byte) (len >> 24), (byte) (len >> 16),
        (byte) (len >> 8), (byte) len, 0, 0, 0, (byte) dis,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
    size++;
  }

  /**
   * Writes the distance for the specified node.
   * @param pre pre value
   * @param kind node kind
   * @param v value
   */
  private void dist(final int pre, final int kind, final int v) {
    if(kind == ATTR) table.write1(pre, 11, v);
    else if(kind != DOC) table.write4(pre, kind == ELEM ? 4 : 8, v);
  }

  /**
   * Writes the tag ID.
   * @param pre pre value
   * @param v tag id
   */
  private void tagID(final int pre, final int v) {
    table.write2(pre, 1, v);
  }

  /**
   * Writes the attribute name ID.
   * @param pre pre value
   * @param v attribute name ID
   */
  private void attNameID(final int pre, final int v) {
    table.write2(pre, 1, v);
  }

  /**
   * Writes the disk offset of a text/attribute value.
   * @param pre pre value
   * @param off offset
   */
  private void textOff(final int pre, final long off) {
    table.write5(pre, 3, off);
  }

  /**
   * Writes the attribute size.
   * @param pre pre value
   * @param kind node kind
   * @param v value
   */
  public void attSize(final int pre, final int kind, final int v) {
    if(kind == ELEM) table.write1(pre, 3, v);
  }

  /**
   * Writes the attribute size.
   * @param pre pre value
   * @param kind node kind
   * @param v value
   */
  public void size(final int pre, final int kind, final int v) {
    if(kind == ELEM || kind == DOC) table.write4(pre, 8, v);
  }
}
