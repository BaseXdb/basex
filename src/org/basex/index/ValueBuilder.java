package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Num;
import org.basex.util.Token;

/**
 * This main-memory based class builds an index for attribute values and
 * text contents in a tree structure and stores the result to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ValueBuilder extends Progress implements IndexBuilder {
  /** Index type (attributes/texts). */
  private final boolean text;
  /** Temporary value tree. */
  private final ValueTree index = new ValueTree();
  /** Current parsing value. */
  private int id;
  /** Maximum parsing value. */
  private int total;

  /**
   * Constructor.
   * @param txt value type (text/attribute)
   */
  public ValueBuilder(final boolean txt) {
    text = txt;
  }

  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public Values build(final Data data) throws IOException {
    final String db = data.meta.dbname;
    final String f = text ? DATATXT : DATAATV;
    int cap = 1 << 2;
    final int max = (int) (IO.dbfile(db, f).length() >>> 7);
    while(cap < max && cap < (1 << 24)) cap <<= 1;

    total = data.meta.size;
    final int type = text ? Data.TEXT : Data.ATTR;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) != type) continue;
      final byte[] tok = text ? data.text(id) : data.attValue(id);
      // skip too long and pure whitespace tokens
      if(tok.length <= Token.MAXLEN && !Token.ws(tok)) index.index(tok, id);
    }
    index.init();

    final int hs = index.size;
    final DataOutput outl = new DataOutput(db, f + 'l');
    outl.writeNum(hs);
    final DataOutput outr = new DataOutput(db, f + 'r');
    while(index.more()) {
      outr.write5(outl.size());
      final int p = index.next();
      final int ds = index.ns[p];
      outl.writeNum(ds);
      
      // write id lists
      final byte[] tmp = index.pre[p];
      index.pre[p] = null;
      for(int v = 0, ip = 4, o = 0; v < ds; ip += Num.len(tmp, ip), v++) {
        final int pre = Num.read(tmp, ip);
        outl.writeNum(pre - o);
        o = pre;
      }
    }
    index.pre = null;
    index.ns = null;
    
    outl.close();
    outr.close();
    
    return new Values(data, text);
  }
  
  @Override
  public String tit() {
    return PROGINDEX;
  }

  @Override
  public String det() {
    return text ? INDEXTXT : INDEXATT;
  }

  @Override
  public double prog() {
    return (double) id / total;
  }
}
