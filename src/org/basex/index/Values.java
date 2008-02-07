package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.query.xpath.expr.FTOption;

/**
 * This class provides access to attribute values and text contents
 * stored on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Values implements Index {
  /** Number of hash entries. */
  public int size;
  /** Values file. */
  private final Data data;
  /** Hash table buckets. */
  private final DataAccess idxb;
  /** ID array references. */
  private final DataAccess idxi;
  /** Next pointers. */
  private final DataAccess idxn;
  /** ID lists. */
  private final DataAccess id;
  /** Value type (texts/attributes). */
  private final boolean text;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database
   * @param txt value type (texts/attributes)
   * @throws IOException IO Exception
   */
  public Values(final Data d, final String db, final boolean txt)
      throws IOException {
    data = d;
    text = txt;
    final String file = txt ? DATATXT : DATAATV;
    id   = new DataAccess(db, file + 'l');
    idxb = new DataAccess(db, file + 'b');
    idxn = new DataAccess(db, file + 'n');
    idxi = new DataAccess(db, file + 'i');
    size = id.read4(0);
  }

  /** {@inheritDoc} */
  public void info(final PrintOutput out) throws IOException {
    out.println(text ? TEXTINDEX : VALUEINDEX);
    out.println(DISKHASH);
    out.println(HASHBUCKETS + size);
    final long l = idxb.length() + idxi.length() + idxn.length();
    out.println(SIZEDISK + Performance.formatSize(l, true) + Prop.NL);
  }

  /** {@inheritDoc} */
  public int[] ids(final byte[] tok) {
    final int pos = get(tok);
    return pos > 0 ? ids(pos) : Array.NOINTS;
  }

  /** {@inheritDoc} */
  public int[] ids(final byte[] tok, final FTOption ftO) {
    if(ftO != null) BaseX.debug("Values: No fulltext option support.");
    final int pos = get(tok);
    return pos > 0 ? ids(pos) : Array.NOINTS;
  }

  /** {@inheritDoc} */
  public int[][] idPos(final byte[] tok, final FTOption ftO) {
    BaseX.debug("Values: No fulltextposition support.");
    return null;
  }
  
  /** {@inheritDoc} */
   public int[][]  idPosRange(final byte[] tok0, final boolean itok0, 
       final byte[] tok1, final boolean itok1) {
    BaseX.debug("Words: No fulltext range query support.");
    return null;
   }

  /**
   * Returns the id offset for the specified token or a negative value
   * if the token is not found.
   * @param tok token to be found
   * @return id offset
   */
  private int get(final byte[] tok) {
    final int p = Token.hash(tok) & size - 1;

    for(int tid = idxb.read4(p); tid != 0; tid = idxn.read4(tid)) {
      final int pos = idxi.read4(tid);
      final int pre = id.firstID(pos);
      final byte[] txt = text ? data.text(pre) : data.attValue(pre);
      if(Token.eq(tok, txt)) return pos;
    }
    return 0;
  }

  /** {@inheritDoc} */
  public int nrIDs(final byte[] tok) {
    final long pos = get(tok);
    return pos > 0 ? id.readNum(pos) >> 2 : 0;
  }

  /**
   * Returns the decompressed ids for the specified file position.
   * @param pos id position
   * @return position ids
   */
  private int[] ids(final int pos) {
    final byte[] bytes = id.readToken(pos);
    final Num num = new Num(bytes, false);
    final int[] tmp = new int[bytes.length];
    int s = 0;
    while(num.more()) {
      tmp[s++] = num.id();
      num.next();
    }
    return Array.finish(tmp, s);
  }

  /** {@inheritDoc} */
  public synchronized void close() throws IOException {
    id.close();
    idxb.close();
    idxn.close();
    idxi.close();
  }
}
