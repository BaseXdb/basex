package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Num;
import org.basex.util.Token;

/**
 * This main-memory based class builds an index for attribute values and
 * text contents in a tree structure and stores the result to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ValueBuilder extends IndexBuilder {
  /** Temporary value tree. */
  private final ValueTreeNew index;
  /** Index type (attributes/texts). */
  private final boolean text;

  /**
   * Constructor.
   * @param d data reference
   * @param txt value type (text/attribute)
   */
  public ValueBuilder(final Data d, final boolean txt) {
    super(d);
    index = new ValueTreeNew(d.meta.lastid);
    text = txt;
  }

  @Override
  public Values build() throws IOException {
    final Prop prop = data.meta.prop;
    final String db = data.meta.name;
    final String f = text ? DATATXT : DATAATV;
    int cap = 1 << 2;
    final int max = (int) (prop.dbfile(db, f).length() >>> 7);
    while(cap < max && cap < 1 << 24) cap <<= 1;

    final int type = text ? Data.TEXT : Data.ATTR;
    for(pre = 0; pre < total; pre++) {
      if(data.kind(pre) != type) continue;
      checkStop();
      final byte[] tok = text ? data.text(pre) : data.attValue(pre);
      // skip too long and pure whitespace tokens
      if(tok.length <= Token.MAXLEN && !Token.ws(tok)) index.index(tok, pre);
    }

    index.init();
    final int hs = index.tokens.size();
    final DataOutput outl = new DataOutput(prop.dbfile(db, f + 'l'));
    outl.writeNum(hs);
    final DataOutput outr = new DataOutput(prop.dbfile(db, f + 'r'));
    while(index.more()) {
      index.nextTok();
      final byte[] pres = index.nextPres();
      final int size = Num.size(pres);
      int v = 0;
      for(int ip = 4; ip < size; v++) {
        pre = Num.read(pres, ip);
        ip += Num.len(pres, ip);        
      }

      outr.write5(outl.size());
      outl.writeNum(v);
      
      for(int j = 0, ip = 4, o = 0; j < v; ip += Num.len(pres, ip), j++) {
        final int pr = Num.read(pres, ip);
        outl.writeNum(pr - o);
        o = pr;
      }
    }

    outl.close();
    outr.close();

    return new Values(data, text);
  }
  
  @Override
  public String det() {
    return text ? INDEXTXT : INDEXATT;
  }
}
