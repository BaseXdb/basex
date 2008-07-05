package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.Array;
import org.basex.util.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class provides access to attribute values and text contents
 * stored on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Values extends Index {
  /** Number of hash entries. */
  private int size;
  /** Values file. */
  private final Data data;
  /** ID references. */
  private final DataAccess idxr;
  /** ID lists. */
  private final DataAccess idxl;
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
    idxl  = new DataAccess(db, file + 'l');
    idxr = new DataAccess(db, file + 'r');
    size = idxl.readNum();
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    //tb.add(text ? TEXTINDEX : VALUEINDEX);
    tb.add(TXTINDEX + NL);
    tb.add(IDXENTRIES + size + NL);
    final long l = idxl.length() + idxr.length();
    tb.add(SIZEDISK + Performance.formatSize(l, true) + NL);
    return tb.finish();
  }

  @Override
  public int[] ids(final byte[] tok) {
    final long pos = get(tok);
    if(pos == 0) return Array.NOINTS;
    
    final int ds = idxl.readNum(pos);
    final int[] ids = new int[ds];
    int p = 0;
    for(int d = 0; d < ds; d++) {
      ids[d] = p + idxl.readNum();
      p = ids[d];
    }
    return ids;
  }

  @Override
  public int nrIDs(final byte[] tok, final FTTokenizer ft) {
    final long pos = get(tok);
    return pos > 0 ? idxl.readNum(pos) : 0;
  }

  @Override
  public int[] idRange(final double min, final boolean ge, 
      final double max, final boolean le) {

    final int mx = (long) max == max ? token(max).length : 0;  
    final boolean sl = (long) max == max && (long) min == min && 
      token(min).length == mx;
    
    final IntList ids = new IntList();
    boolean found = false;
    for(int l = 0; l < size - 1; l++) {
      final int ds = idxl.readNum(idxr.read5(l * 5L));
      int pre = idxl.readNum();
      final double v = text ? data.textNum(pre) : data.attNum(pre);
        
      if(!found) {
        found = v == v;
        if(!found || v < min || v > max) continue;
      } else {
        if(v == v) {
          // skip if if min, max and the current value have the same length
          // and if current value is larger
          if(sl && (text ? data.textLen(pre) : data.attLen(pre)) == mx &&
              v > max) break;
          if(v < min || v > max) continue;
        } else {
          // skip if all numbers have been parsed
          if((text ? data.text(pre) : data.attValue(pre))[0] > '9') break;
          continue;
        }
      }
      
      ids.add(pre);
      for(int d = 0; d < ds; d++) {
        pre += idxl.readNum(); 
        ids.add(pre);
      }
    }
    final int[] res = ids.finish();
    Arrays.sort(res);
    return res;
  }
  
  /**
   * Returns the id offset for the specified token or
   * 0 if the token is not found.
   * @param key token to be found
   * @return id offset
   */
  private long get(final byte[] key) {
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = (l + h) >>> 1;
      final long pos = idxr.read5(m * 5L);
      idxl.readNum(pos);
      final int pre = idxl.readNum();
      final byte[] txt = text ? data.text(pre) : data.attValue(pre);
      final int d = Token.diff(txt, key);
      if(d == 0) return pos;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return 0;
  }

  @Override
  public synchronized void close() throws IOException {
    idxl.close();
    idxr.close();
  }
}
