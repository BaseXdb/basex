package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.ByteList;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * This class builds an index for attribute values and text contents in a
 * tree structure and stores the result to disk.<br/>
 *
 * The data is stored on disk in the following format:<br/>
 *
 * <ul>
 * <li> {@code DATATXT/ATV + 'l'}: contains the index values, which are dense id
 *   lists to all text nodes/attribute values, stored in the {@link Num} format:
 *   [size0, pre1, pre2, ...]. The number of index keys is stored in the first 4
 *   bytes of the file.</li>
 * <li> {@code DATATXT/ATV + 'r'}: contains 5-byte references to the id lists
 *   for all keys. To save space, the keys itself are not stored in the index
 *   structure. Instead, they can be found by following the id references to
 *   the main table.
 * </li>
 * </ul>
 *
 * @author BaseX Team 2005-11, BSD License
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
  public DiskValues build() throws IOException {
    // delete old index
    abort();

    final Performance perf = Util.debug ? new Performance() : null;
    Util.debug(det());

    final String f = text ? DATATXT : DATAATV;
    final int k = text ? Data.TEXT : Data.ATTR;

    for(pre = 0; pre < size; ++pre) {
      if((pre & 0x0FFF) == 0) {
        check();
        // check if main memory is exhausted
        if(memFull()) {
          write(f + csize++, false);
          index = new ValueTree();
          Performance.gc(2);
        }
      }
      // skip too long values
      if(data.kind(pre) != k || data.textLen(pre, text) > MAXLEN) continue;
      index.index(data.text(pre, text), pre);
    }

    if(csize == 0) {
      write(f, true);
    } else {
      write(f + csize++, false);
      index = null;
      Performance.gc(1);
      merge();
    }

    if(text) data.meta.textindex = true;
    else data.meta.attrindex = true;

    Util.gc(perf);
    return new DiskValues(data, text);
  }

  /**
   * Merges cached index files.
   * @throws IOException I/O exception
   */
  private void merge() throws IOException {
    final String f = text ? DATATXT : DATAATV;
    final DataOutput outL = new DataOutput(data.meta.file(f + 'l'));
    final DataOutput outR = new DataOutput(data.meta.file(f + 'r'));
    outL.write4(0);

    // initialize cached index iterators
    final IntList ml = new IntList();
    final ValueMerger[] vm = new ValueMerger[csize];
    for(int i = 0; i < csize; ++i) vm[i] = new ValueMerger(data, text, i);
    int sz = 0;

    // parse through all values
    while(true) {
      checkStop();

      // find first index with valid entries
      int min = -1;
      while(++min < csize && vm[min].pre.length == 0);
      if(min == csize) break;

      // find index entry with smallest value
      ml.reset();
      for(int i = min; i < csize; ++i) {
        if(vm[i].pre.length == 0) continue;
        final int d = diff(vm[min].value, vm[i].value);
        if(d < 0) continue;
        if(d > 0) {
          min = i;
          ml.reset();
        }
        ml.add(i);
      }

      // parse through all indexes and cache id distances
      final int ms = ml.size();
      final ByteList tmp = new ByteList();
      int c = 0;
      for(int m = 0, o = 0; m < ms; ++m) {
        final ValueMerger v = vm[ml.get(m)];
        final int p = Num.read(v.pre, 4);
        tmp.add(Num.num(p - o));
        int l = 4 + Num.len(p);
        o = p;
        c++;
        final int vl = v.pre.length;
        for(int i = l; i < vl; i++) tmp.add(v.pre[i]);
        while(l < vl) {
          final int d = Num.read(v.pre, l);
          l += Num.len(d);
          o += d;
          c++;
        }
        v.next();
      }

      // write id offset and ids to disk
      outR.write5(outL.size());
      outL.writeNum(c);
      final int is = tmp.size();
      for(int i = 0; i < is; i++) outL.write(tmp.get(i));
      ++sz;
    }

    // close index files
    outR.close();
    outL.close();

    // write number of entries to first position
    final DataAccess da = new DataAccess(data.meta.file(f + 'l'));
    da.writeInt(sz);
    da.close();
  }

  /**
   * Writes the current value tree to disk.
   * @param name name
   * @param all writes the complete tree
   * @throws IOException I/O exception
   */
  private void write(final String name, final boolean all) throws IOException {
    // write id arrays and references
    final DataOutput outL = new DataOutput(data.meta.file(name + 'l'));
    final DataOutput outR = new DataOutput(data.meta.file(name + 'r'));
    outL.write4(index.size());

    index.init();
    while(index.more()) {
      outR.write5(outL.size());
      final int i = index.next();
      final byte[] pres = index.pres.get(i);
      final int is = Num.size(pres);

      if(all) {
        // write final structure to disk:
        // number of entries, followed by id distances
        int v = 0;
        for(int ip = 4; ip < is; ip += Num.len(pres, ip)) ++v;
        outL.writeNum(v);

        for(int ip = 4, o = 0; ip < is; ip += Num.len(pres, ip)) {
          final int p = Num.read(pres, ip);
          outL.writeNum(p - o);
          o = p;
        }
      } else {
        // write temporary structure to disk as {@link Num} instance
        // (number of bytes, followed by id distances)
        final byte[] tmp = new byte[4 + is];
        Num.size(tmp, 4);
        for(int ip = 4, o = 0; ip < is; ip += Num.len(pres, ip)) {
          final int p = Num.read(pres, ip);
          Num.add(tmp, p - o);
          o = p;
        }
        outL.write(tmp, 0, Num.size(tmp));
      }
    }
    outL.close();
    outR.close();

    // temporarily write texts
    if(!all) {
      final DataOutput outT = new DataOutput(data.meta.file(name + 't'));
      index.init();
      while(index.more()) outT.writeToken(index.tokens.get(index.next()));
      outT.close();
    }
  }

  @Override
  public void abort() {
    final String f = text ? DATATXT : DATAATV;
    DropDB.drop(data.meta.name, f + ".+" + IO.BASEXSUFFIX, data.meta.prop);
    if(text) data.meta.textindex = false;
    else data.meta.attrindex = false;
  }

  @Override
  public String det() {
    return text ? INDEXTXT : INDEXATT;
  }
}
