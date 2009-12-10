package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

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
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This main-memory based class builds an index for attribute values and
 * text contents in a tree structure and stores the result to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ValueBuilder extends IndexBuilder {
  /** Temporary value tree. */
  private ValueTree index = new ValueTree();
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
    final Runtime rt = Runtime.getRuntime();
    Performance.gc(2);
    System.out.println("[0] " + (rt.totalMemory() - rt.freeMemory()));

    final String f = text ? DATATXT : DATAATV;
    int cap = 1 << 2;
    final int max = (int) (data.meta.file(f).length() >>> 7);
    while(cap < max && cap < 1 << 24) cap <<= 1;
    final long maxMem = (long) (rt.maxMemory() * 0.8);

    final int type = text ? Data.TEXT : Data.ATTR;
    int cf = 0;
    for(pre = 0; pre < total; pre++) {
      if(data.kind(pre) != type) continue;
      checkStop();
      final byte[] tok = data.text(pre, text);
      // skip too long and pure whitespace tokens
      if(tok.length <= Token.MAXLEN && !Token.ws(tok)) index.index(tok, pre);


      if((pre & 0x3F) == 0 && rt.totalMemory() - rt.freeMemory() > maxMem) {
        final DataOutput outl = new DataOutput(data.meta.file(f + cf + 'l'));
        outl.writeInt(index.size());
        final DataOutput outr = new DataOutput(data.meta.file(f + cf + 'r'));
        write(index, outl, outr);
        cf++;
        index = new ValueTree();
        Performance.gc(1);
      }
    }

    if (cf == 0) {
      final DataOutput outl = new DataOutput(data.meta.file(f + 'l'));
      final DataOutput outr = new DataOutput(data.meta.file(f + 'r'));
      writeSingle(outl, outr);
    } else {
      final DataOutput outl = new DataOutput(data.meta.file(f + cf + 'l'));
      outl.writeInt(0);
      final DataOutput outr = new DataOutput(data.meta.file(f + cf + 'r'));

      write(index, outl, outr);
      cf++;
      index = null;

      final int size = mergeFiles(cf);
      final DataAccess da = new DataAccess(data.meta.file(f + 'l'));
      da.writeInt(size);
      da.close();
      DropDB.delete(data.meta.name, f + "\\d+." + IO.BASEXSUFFIX,
          data.meta.prop);
    }

    if(perf != null) {
      Performance.gc(4);
      Main.debug((text ? "Texts" : "Attributes") + ": " + perf + " (" +
          Performance.getMem() + ")");
    }
    return new Values(data, text);
  }

  /**
   * Write single index to disk.
   * @param outl DataOutput
   * @param outr DataOutput
   * @throws IOException IOException
   */
  private void writeSingle(final DataOutput outl,
      final DataOutput outr) throws IOException {
    outl.writeInt(index.size());
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
  }

  /**
   * Merges several Value files.
   * @param cf number of value files
   * @return returns number of indexed tokens
   * @throws IOException I/O exception
   */
  private int mergeFiles(final int cf) throws IOException {
    final String f = text ? DATATXT : DATAATV;
    final Values[] v = new Values[cf];
    final DataOutput outl = new DataOutput(data.meta.file(f + 'l'));
    int size = 0;
    final DataOutput outr = new DataOutput(data.meta.file(f + 'r'));

    final byte[][] t = new byte[cf][];
    final byte[][] pres = new byte[cf][];
    for(int i = 0; i < cf; i++) {
      v[i] = new Values(data, text, f + i);
      pres[i] = v[i].nextPres();
      t[i] = pres[i].length > 0 ? data.text(Num.read(pres[i], 4), text) :
        Token.EMPTY;
    }
    outl.writeInt(size);

    int min;
    IntList merge;
    while(check(pres)) {
      size++;
      outr.write5(outl.size());
      min = 0;
      merge = new IntList();
      for(int i = 0; i < cf; i++) {
        if (min == i || t[i].length == 0) continue;
        final int d = Token.diff(t[min], t[i]);
        if(d > 0 || t[min].length == 0) {
          min = i;
          merge = new IntList();
        } else if(d == 0 && t[i].length > 0) {
          if(merge.size() == 0) merge.add(min);
          merge.add(i);
        }
      }

      if (merge.size() == 0) {
        writeWithNum(outl, pres[min]);
        pres[min] = v[min].nextPres();
        t[min] = pres[min].length > 0 ?
            data.text(Num.read(pres[min], 4), text) : Token.EMPTY;
      } else {
        final TokenBuilder p = new TokenBuilder();
        p.add(new byte[4]);
        int npre = 0;
        int opre = 0;
        for(int j = 0; j < merge.size(); j++) {
          final int m = merge.get(j);
          if(j == 0) {
            int l = 4;
            while(l < pres[m].length) {
              final int diff = Num.read(pres[m], l);
              opre += diff;
              l += Num.len(diff);
            }
            p.add(Token.substring(pres[m], 4));
          } else {
            npre = Num.read(pres[m], 4);
            p.add(Num.num(npre - opre));
            int l = 4 + Num.len(npre);
            p.add(Token.substring(pres[m], l));
            opre = npre;
            while(l < pres[m].length) {
              final int diff = Num.read(pres[m], l);
              opre += diff;
              l += Num.len(diff);
            }
          }
          pres[m] = v[m].nextPres();
          t[m] = pres[m].length > 0 ? data.text(Num.read(pres[m], 4), text) :
            Token.EMPTY;
        }
        final byte[] tmp = p.finish();
        Num.size(tmp, tmp.length);
        writeWithNum(outl, tmp);
      }
    }
    outr.close();
    outl.close();
    for(final Values vv : v) vv.close();
    return size;
  }

  /**
   * Checks if any unprocessed pre values are remaining.
   * @param pres pre values
   * @return boolean
   */
  private boolean check(final byte[][] pres) {
    for (final byte[] b : pres) if (b.length > 0) return true;
    return false;
  }

  /**
   * Writes pre values to disk with size (number of bytes) as first value.
   * @param outl DataOutput
   * @param pres pre values
   * @return number of written bytes
   * @throws IOException I/O exception
   */
  private long writeWithSize(final DataOutput outl, final byte[] pres)
  throws IOException {
    final int is = Num.size(pres);
    final byte[] tmp = new byte[4 + is];
    Num.size(tmp, 4);

    for(int ip = 4, o = 0; ip < is; ip += Num.len(pres, ip)) {
      final int p = Num.read(pres, ip);
      Num.add(tmp, p - o);
      o = p;
    }
    outl.write(tmp, 0, Num.size(tmp));
    return outl.size();
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
   * Writes a value tree after its creation to disk.
   * @param i value tree to write
   * @param outl DataOutput
   * @param outr DataOutput
   * @throws IOException IOException
   */
  private void write(final ValueTree i, final DataOutput outl,
      final DataOutput outr) throws IOException {
    i.init();
    while(i.more()) {
      outr.write5(outl.size());
      final byte[] pres = i.next();
      writeWithSize(outl, pres);
    }
    outl.close();
    outr.close();
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
