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
 * This class builds an index for attribute values and text contents
 * in a hash map.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ValueBuilder extends Progress implements IndexBuilder {
  /** Value type (attributes/texts). */
  private final boolean text;
  /** Value type (attributes/texts). */
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

    total = data.size;
    final int type = text ? Data.TEXT : Data.ATTR;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) != type) continue;
      index(text ? data.text(id) : data.attValue(id), id);
    }
    index.init();

    int hs = index.size;
    final DataOutput outl = new DataOutput(db, f + 'l');
    outl.writeNum(hs);
    final DataOutput outr = new DataOutput(db, f + 'r');
    while(index.more()) {
      outr.write5(outl.size());
      int p = index.next();
      int ds = index.ns[p];
      outl.writeNum(ds);
      
      // write id lists
      byte[] tmp = index.pre[p];
      index.pre[p] = null;
      for(int v = 0, ip = 4, o = 0; v < ds; ip += Num.len(tmp, ip), v++) {
        int pre = Num.read(tmp, ip);
        outl.writeNum(pre - o);
        o = pre;
      }
    }
    index.pre = null;
    index.ns = null;
    
    outl.close();
    outr.close();
    
    return new Values(data, db, text);
  }

  /**
   * Indexes a single token and returns its unique id.
   * @param tok token to be indexed
   * @param pre pre value
   */
  private void index(final byte[] tok, final int pre) {
    // check if token exists
    if(tok.length > Token.MAXLEN || Token.ws(tok)) return;

    // resize tables if necessary
    index.index(tok, pre);
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
