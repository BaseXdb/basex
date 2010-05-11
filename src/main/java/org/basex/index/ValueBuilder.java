package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * This main-memory based class builds an index for attribute values and
 * text contents in a tree structure and stores the result to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ValueBuilder extends IndexBuilder {
  /** Temporary value tree. */
  private ValueTree index = new ValueTree();
  /** Index type (attributes/texts). */
  private final boolean text;
  /** Number of cached index structures. */
  private int csize;

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
    Main.debug((text ? "Texts" : "Attributes") + ": ");

    final String f = text ? DATATXT : DATAATV;
    final int type = text ? Data.TEXT : Data.ATTR;

    for(pre = 0; pre < size; pre++) {
      if((pre & 0x0FFF) == 0) {
        check();
        // check if main memory is exhausted
        if(memFull()) {
          write(f + csize++);
          index = new ValueTree();
          Performance.gc(2);
        }
      }
      // skip too long tokens
      if(data.kind(pre) != type || data.textLen(pre, text) > MAXLEN) continue;

      // skip pure whitespace tokens
      final byte[] tok = data.text(pre, text);
      if(!ws(tok)) index.index(tok, pre);
    }

    if(csize == 0) {
      writeSingle(f);
    } else {
      write(f + csize);
      index = null;
      Performance.gc(1);
      csize++;

      final int sz = merge();
      final DataAccess da = new DataAccess(data.meta.file(f + 'l'));
      da.writeInt(sz);
      da.close();
    }

    if(text) data.meta.txtindex = true;
    else data.meta.atvindex = true;

    Main.gc(perf);
    return new Values(data, text);
  }

  /**
   * Merges cached index files.
   * @return returns number of indexed tokens
   * @throws IOException I/O exception
   */
  private int merge() throws IOException {
    final String f = text ? DATATXT : DATAATV;
    final DataOutput outl = new DataOutput(data.meta.file(f + 'l'));
    final DataOutput outr = new DataOutput(data.meta.file(f + 'r'));
    outl.write4(0);

    final ValueMerge[] vm = new ValueMerge[csize];
    for(int i = 0; i < csize; i++) vm[i] = new ValueMerge(data, text, i);

    int min;
    int sz = 0;
    final IntList ml = new IntList();
    while(check(vm)) {
      checkStop();

      sz++;
      outr.write5(outl.size());
      min = 0;
      ml.reset();
      for(int i = 0; i < csize; i++) {
        if(min == i || vm[i].t.length == 0) continue;
        final int d = diff(vm[min].t, vm[i].t);
        if(d > 0 || vm[min].t.length == 0) {
          min = i;
          ml.reset();
        } else if(d == 0 && vm[i].t.length > 0) {
          if(ml.size() == 0) ml.add(min);
          ml.add(i);
        }
      }

      if(ml.size() == 0) {
        writeWithNum(outl, vm[min].p);
        vm[min].next();
      } else {
        final TokenBuilder tb = new TokenBuilder();
        tb.add(new byte[4]);
        int npre = 0;
        int opre = 0;
        for(int j = 0; j < ml.size(); j++) {
          final int m = ml.get(j);
          if(j == 0) {
            int l = 4;
            while(l < vm[m].p.length) {
              final int diff = Num.read(vm[m].p, l);
              opre += diff;
              l += Num.len(diff);
            }
            tb.add(substring(vm[m].p, 4));
          } else {
            npre = Num.read(vm[m].p, 4);
            tb.add(Num.num(npre - opre));
            int l = 4 + Num.len(npre);
            tb.add(substring(vm[m].p, l));
            opre = npre;
            while(l < vm[m].p.length) {
              final int diff = Num.read(vm[m].p, l);
              opre += diff;
              l += Num.len(diff);
            }
          }
          vm[m].next();
        }
        final byte[] tmp = tb.finish();
        Num.size(tmp, tmp.length);
        writeWithNum(outl, tmp);
      }
    }
    outr.close();
    outl.close();
    return sz;
  }

  /**
   * Writes pre values to disk with number (number of byte values) as
   * first value.
   * @param outl DataOutput
   * @param pres pre values
   * @throws IOException I/O exception
   */
  private void writeWithNum(final DataOutput outl, final byte[] pres)
      throws IOException {

    final int is = Num.size(pres);
    int v = 0;
    for(int ip = 4; ip < is; ip += Num.len(pres, ip)) v++;
    outl.writeNum(v);
    outl.write(pres, 4, is - 4);
  }

  /**
   * Checks if any unprocessed pre values are remaining.
   * @param vm merge value array
   * @return boolean
   */
  private boolean check(final ValueMerge[] vm) {
    for(final ValueMerge m : vm) if(m.p.length > 0) return true;
    return false;
  }

  /**
   * Writes the current value tree to disk.
   * @param n name
   * @throws IOException I/O exception
   */
  private void write(final String n) throws IOException {
    // write positions and references
    final DataOutput outl = new DataOutput(data.meta.file(n + 'l'));
    final DataOutput outr = new DataOutput(data.meta.file(n + 'r'));
    outl.write4(index.size());
    index.init();
    while(index.more()) {
      outr.write5(outl.size());
      final int i = index.next();
      final byte[] pres = index.pres.get(i);
      final int is = Num.size(pres);
      final byte[] tmp = new byte[4 + is];
      Num.size(tmp, 4);

      for(int ip = 4, o = 0; ip < is; ip += Num.len(pres, ip)) {
        final int p = Num.read(pres, ip);
        Num.add(tmp, p - o);
        o = p;
      }
      outl.write(tmp, 0, Num.size(tmp));
    }
    outl.close();
    outr.close();

    // write texts
    final DataOutput outt = new DataOutput(data.meta.file(n + 't'));
    index.init();
    while(index.more()) outt.writeToken(index.tokens.get(index.next()));
    outt.close();
  }

  /**
   * Write single index to disk.
   * @param n name
   * @throws IOException IOException
   */
  private void writeSingle(final String n) throws IOException {
    final DataOutput outl = new DataOutput(data.meta.file(n + 'l'));
    final DataOutput outr = new DataOutput(data.meta.file(n + 'r'));
    outl.write4(index.size());
    index.init();
    while(index.more()) {
      outr.write5(outl.size());
      final int i = index.next();
      final byte[] pres = index.pres.get(i);
      final int is = Num.size(pres);
      int v = 0;
      for(int ip = 4; ip < is; ip += Num.len(pres, ip)) v++;
      outl.writeNum(v);

      for(int ip = 4, o = 0; ip < is; ip += Num.len(pres, ip)) {
        final int p = Num.read(pres, ip);
        outl.writeNum(p - o);
        o = p;
      }
    }
    outl.close();
    outr.close();
  }

  @Override
  public void abort() {
    final String f = (text ? DATATXT : DATAATV) + ".*" + IO.BASEXSUFFIX;
    DropDB.delete(data.meta.name, f, data.meta.prop);
    if(text) data.meta.txtindex = false;
    else data.meta.atvindex = false;
  }

  @Override
  public String det() {
    return text ? INDEXTXT : INDEXATT;
  }
}
