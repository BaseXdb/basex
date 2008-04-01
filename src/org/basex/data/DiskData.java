package org.basex.data;

import static org.basex.Text.ATTINDEX;
import static org.basex.Text.TAGINDEX;
import static org.basex.data.DataText.DATAATV;
import static org.basex.data.DataText.DATATBL;
import static org.basex.data.DataText.DATATXT;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.index.Index;
import org.basex.index.Names;
import org.basex.index.Values;
import org.basex.index.Words;
import org.basex.index.WordsCTA;
import org.basex.io.DataAccess;
import org.basex.io.PrintOutput;
import org.basex.io.TableAccess;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableMemAccess;
import org.basex.query.xpath.expr.FTOption;
import org.basex.util.Token;

/**
 * This class stores and organizes the node table and the index structures for
 * textual content. All nodes in the table are accessed by their
 * implicit pre value. Some restrictions on the data are currently given:
 * <ul>
 * <li>The table is limited to 2^31 entries (pre values are signed int's)</li>
 * <li>A maximum of 2^16 different tag and attribute names is allowed</li>
 * <li>A tag can have a maximum of 256 attributes</li>
 * </ul>
 * Each node occupies 128 bits. The current storage layout looks like follows:
 *
 * <pre>
 *  ELEMENTS:
 * - Byte     0: Node kind (TAG)
 * - Byte   1-2: Tag Reference
 * - Byte     3: Number of attributes
 * - Byte  4- 7: Number of descendants (size)
 * - Byte  8-11: Relative parent reference
 * - Byte 12-15: Unique Node ID
 * TEXT NODES:
 * - Byte     0: Node kind (TEXT/PI/COMM)
 * - Byte  3- 7: Text reference
 * - Byte  8-11: Relative parent reference
 * - Byte 12-15: Unique Node ID
 * ATTRIBUTE NODES:
 * - Byte     0: Node kind (ATTR)
 * - Byte   1-2: Attribute name reference
 * - Byte  3- 7: Attribute value reference
 * - Byte    11: Relative parent reference
 * - Byte 12-15: Unique Node ID
 * </pre>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Table access file. */
  private TableAccess table;
  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;

  /**
   * Default Constructor.
   * @param db name of database
   * @throws IOException IO Exception
   */
  public DiskData(final String db) throws IOException {
    this(db, true);
  }

  /**
   * Constructor, specifying if indexes are to be opened as well.
   * @param db name of database
   * @param index open indexes
   * @throws IOException IO Exception
   */
  public DiskData(final String db, final boolean index) throws IOException {
    meta = new MetaData(db);
    size = meta.read();
    stats = new Stats(db);
    stats.read();
    
    // read indexes
    tags = new Names(db, true);
    atts = new Names(db, false);
    
    // main memory mode.. keep table in memory
    table = Prop.mainmem ? new TableMemAccess(db, DATATBL, size) :
      new TableDiskAccess(db, DATATBL);
    texts = new DataAccess(db, DATATXT);
    values = new DataAccess(db, DATAATV);

    if(index) {
      if(meta.txtindex) openIndex(Index.TYPE.TXT, new Values(this, db, true));
      if(meta.atvindex) openIndex(Index.TYPE.ATV, new Values(this, db, false));
      if(meta.wrdindex) openIndex(Index.TYPE.WRD, new Words(db));
      if(meta.ftxindex) openIndex(Index.TYPE.FTX, new WordsCTA(db, this));
    }
    initNames();
  }

  @Override
  public synchronized void flush() {
    try {
      table.flush();
      texts.flush();
      values.flush();
      meta.write(size);
//      <LK> writing / refreshing stats after updates?
      tags.finish(meta.dbname);
      atts.finish(meta.dbname);
      
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public synchronized void close() throws IOException {
    meta.write(size);
    stats.write();  
    
    tags.finish(meta.dbname);
    atts.finish(meta.dbname);
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
    closeIndex(Index.TYPE.TXT);
    closeIndex(Index.TYPE.ATV);
    closeIndex(Index.TYPE.WRD);
    closeIndex(Index.TYPE.FTX);
  }

  @Override
  public void closeIndex(final Index.TYPE index)
      throws IOException {
    switch(index) {
      case TXT: if(txtindex != null) txtindex.close(); break;
      case ATV: if(atvindex != null) atvindex.close(); break;
      case WRD: if(wrdindex != null) wrdindex.close(); break;
      case FTX: if(ftxindex != null) ftxindex.close();
    }
  }

  @Override
  public void openIndex(final Index.TYPE type, final Index index) {
    switch(type) {
      case TXT: if(meta.txtindex) txtindex = index; break;
      case ATV: if(meta.atvindex) atvindex = index; break;
      case WRD: if(meta.wrdindex) wrdindex = index; break;
      case FTX: if(meta.ftxindex) ftxindex = index;
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
    return pre - (kind == ATTR ? table.read1(pre, 11) : table.read4(pre, 8));
  }

  @Override
  public int attSize(final int pre, final int kind) {
    return kind == ELEM ? table.read1(pre, 3) : 1;
  }

  @Override
  public int size(final int pre, final int kind) {
    return kind == ELEM || kind == DOC ? table.read4(pre, 4) : 1;
  }

  @Override
  public int tagID(final int pre) {
    return table.read2(pre, 1);
  }

  @Override
  public int attNameID(final int pre) {
    return table.read2(pre, 1);
  }

  @Override
  public byte[] text(final int pre) {
    return txt(pre, texts);
  }

  @Override
  public byte[] text(final int pre, final int off, final int len) {
    final long o = textOff(pre);
    if((o & 0x8000000000L) != 0) {
      final byte[] t = token(o);
      return Token.substring(t, Math.min(t.length, off),
          Math.min(t.length, off + len));
    }
    byte[] t = texts.readToken(off);
    return Token.substring(t, Math.min(t.length, off),
        Math.min(t.length, off + len));
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
    final long off = textOff(pre);
    return (off & 0x8000000000L) != 0 ? token(off) : da.readToken(off);
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
  public void info(final PrintOutput out) throws IOException {
    out.println(TAGINDEX);
    out.println(tags.info());
    out.println(ATTINDEX);
    out.println(atts.info());
    if(meta.txtindex) txtindex.info(out);
    if(meta.atvindex) atvindex.info(out);
    if(meta.wrdindex) wrdindex.info(out);
    if(meta.ftxindex) ftxindex.info(out);
  }

  @Override
  public int[][] fuzzyIDs(final byte[] words, final int ne) {
    return ftxindex.fuzzyIDs(words, ne);
  }
  
  @Override
   public int[][] ftIDs(final byte[] word, final FTOption ftO) {
     return ftxindex.idPos(word, ftO);
   }
  
  @Override
  public int[][] ftIDRange(final byte[] word1, final boolean itok0, 
      final byte[] word2, final boolean itok1) {
    return ftxindex.idPosRange(word1, itok0, word2, itok1);
  }
  
  @Override
  public int nrFTIDs(final byte[] token) {
     return ftxindex.nrIDs(token);
  }

  
  @Override
  public void update(final int pre, final byte[] val) {
    if(kind(pre) == ELEM) {
      tagID(pre, tags.index(val));
    } else {
      update(pre, val, true);
    }
  }

  @Override
  public void update(final int pre, final byte[] name, final byte[] val) {
    update(pre, val, false);
    attNameID(pre, atts.index(name));
  }
  
  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param val content
   * @param txt text flag
   */
  private void update(final int pre, final byte[] val, final boolean txt) {
    long v = Token.toSimpleInt(val);
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
    // size of the subtree to delete
    final int siz = size(pre, kind(pre));

    // reduce size of ancestors
    int par = pre;

    // check if we are an attribute (different size counters)
    if(kind(pre) == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
    }

    // reduce size of remaining ancestors
    while(par > 0) {
      par = parent(par, ELEM);
      size(par, ELEM, size(par, ELEM) - siz);
    }

    // delete node from table structure and reduce document size
    table.delete(pre, siz);
    size -= siz;

    // correct parent values of following nodes
    // we only need to do this once for every following subtree
    int p = pre;
    while(p < size) {
      final int kind = kind(p);
      if(kind == ATTR) {
        dist(p, ATTR, dist(p, ATTR) - siz);
      } else {
        dist(p, ELEM, dist(p, ELEM) - siz);
      }
      p += size(p, kind);
    }
  }

  @Override
  public void insert(final int pre, final int par, final byte[] val,
      final byte kind) {
    if(kind == ELEM) {
      insertElem(pre - 1, pre - par, val, 1, 1);
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
    final int tr = td.kind(0) == DOC ? 1 : 0;
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
   * Increase size of ancestors and parent references of following nodes.
   * @param pre root node
   * @param par parent node
   * @param s size to be added
   */
  private void updateTable(final int pre, final int par, final int s) {
    // increase sizes
    int p = par;
    while(p >= 0) {
      size(p, ELEM, size(p, ELEM) + s);
      p = parent(p, ELEM);
    }

    // increase parent references
    p = pre + s;
    while(p < size) {
      dist(p, ELEM, dist(p, ELEM) + s);
      p += size(p, kind(p));
    }
  }

  /**
   * Insert tag name without updating the table.
   * @param pre insert position
   * @param dis parent distance
   * @param tag tag name index
   * @param as number of attributes
   * @param s node size
   */
  private void insertElem(final int pre, final int dis,
      final byte[] tag, final int as, final int s) {

    final long id = ++meta.lastid;
    final int t = tags.index(tag);
    table.insert(pre, new byte[] { ELEM, (byte) (t >> 8),
        (byte) t, (byte) as, (byte) (s >> 24), (byte) (s >> 16),
        (byte) (s >> 8), (byte) s, (byte) (dis >> 24),
        (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis, (byte) (id >> 24),
        (byte) (id >> 16), (byte) (id >> 8), (byte) id });
    size++;
  }

  /**
   * Insert text node without updating the table.
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
   * Insert attribute without updating the table.
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
    final int att = atts.index(name);
    final long id = ++meta.lastid;
    table.insert(pre, new byte[] { ATTR, (byte) (att >> 8), (byte) att,
        (byte) (len >> 32), (byte) (len >> 24), (byte) (len >> 16),
        (byte) (len >> 8), (byte) len, 0, 0, 0, (byte) dis,
        (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8),
        (byte) id });
    size++;
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param kind node kind
   * @return distance
   */
  private int dist(final int pre, final int kind) {
    return kind == ATTR ? table.read1(pre, 11) : table.read4(pre, 8);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param kind node kind
   * @param v value
   */
  private void dist(final int pre, final int kind, final int v) {
    if(kind == ATTR) table.write1(pre, 11, v);
    else table.write4(pre, 8, v);
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
    if(kind == ELEM || kind == DOC) table.write4(pre, 4, v);
  }
}
