package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Num;
import org.basex.util.Performance;
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
  private final ValueTree index = new ValueTree();
  /** Index type (attributes/texts). */
  private final boolean text;

  /**
   * Constructor.
   * @param d data reference
   * @param txt value type (text/attribute)
   */
  public ValueBuilder(final Data d, final boolean txt) {
    super(d);
    text = txt;
  }

  @Override
  public Values build() throws IOException {
    final Performance perf = Prop.debug ? new Performance() : null;

    final String f = text ? DATATXT : DATAATV;
    int cap = 1 << 2;
    final int max = (int) (data.meta.file(f).length() >>> 7);
    while(cap < max && cap < 1 << 24) cap <<= 1;

    final int type = text ? Data.TEXT : Data.ATTR;
    for(pre = 0; pre < total; pre++) {
      if(data.kind(pre) != type) continue;
      checkStop();
      final byte[] tok = data.text(pre, text);
      // skip too long and pure whitespace tokens
      if(tok.length <= Token.MAXLEN && !Token.ws(tok)) index.index(tok, pre);
    }

    final DataOutput outl = new DataOutput(data.meta.file(f + 'l'));
    outl.writeNum(index.size());
    final DataOutput outr = new DataOutput(data.meta.file(f + 'r'));
    index.init();
    while(index.more()) {
      final byte[] pres = index.next();
      final int is = Num.size(pres);
      int v = 0;
      for(int ip = 4; ip < is; ip += Num.len(pres, ip)) v++;

      outr.write5(outl.size());
      outl.writeNum(v);

      for(int ip = 4, o = 0; ip < is; ip += Num.len(pres, ip)) {
        final int p = Num.read(pres, ip);
        outl.writeNum(p - o);
        o = p;
      }
    }
    outl.close();
    outr.close();

    if(perf != null) {
      Performance.gc(4);
      Main.debug((text ? "Texts" : "Attributes") + ": " + perf + " (" +
          Performance.getMem() + ")");
    }
    return new Values(data, text);
  }

  @Override
  public void abort() {
    if(text) data.meta.txtindex = false;
    else data.meta.atvindex = false;
    super.abort();
  }

  @Override
  public String det() {
    return text ? INDEXTXT : INDEXATT;
  }
}
