package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>This class builds an index for attribute values and text contents in a
 * tree structure and stores the result to disk.</p>
 *
 * <p>The data is stored on disk in the following format:</p>
 * <ul>
 * <li> {@code DATATXT/ATV + 'l'}: contains the index values, which are dense id
 *   lists to all text nodes/attribute values, stored in the {@link Num} format:
 *   [size0, id1, id2, ...]. The number of index keys is stored in the first 4
 *   bytes of the file.</li>
 * <li> {@code DATATXT/ATV + 'r'}: contains 5-byte references to the id lists
 *   for all keys. To save space, the keys itself are not stored in the index
 *   structure. Instead, they can be found by following the id references to
 *   the main table.
 * </li>
 * </ul>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ValueIndexBuilder extends IndexBuilder {
  /** Temporary value tree. */
  private IndexTree index = new IndexTree();
  /** Index type (attributes/texts). */
  private final boolean text;

  /**
   * Constructor.
   * @param d data reference
   * @param txt value type (text/attribute)
   */
  public ValueIndexBuilder(final Data d, final boolean txt) {
    super(d, d.meta.options.num(Options.INDEXSPLITSIZE));
    text = txt;
  }

  @Override
  public DiskValues build() throws IOException {
    // delete old index
    abort();

    final Performance perf = Prop.debug ? new Performance() : null;
    Util.debug(det());

    final int k = text ? Data.TEXT : Data.ATTR;

    for(pre = 0; pre < size; ++pre) {
      if((pre & 0x0FFF) == 0) {
        check();
        // check if main memory is exhausted
        if(split()) {
          writeIndex(true);
          index = new IndexTree();
          finishSplit();
        }
      }
      // skip too long values
      if(data.kind(pre) == k && data.textLen(pre, text) <= data.meta.maxlen) {
        index.index(data.text(pre, text), data.meta.updindex ? data.id(pre) : pre);
        count++;
      }
    }

    writeIndex(splits > 0);
    // merge partial index structures
    if(splits > 1) {
      index = null;
      Performance.gc(1);
      merge();
    }

    if(text) data.meta.textindex = true;
    else data.meta.attrindex = true;

    finishIndex(perf);
    return data.meta.updindex ?
        new UpdatableDiskValues(data, text) : new DiskValues(data, text);
  }

  /**
   * Merges cached index files.
   * @throws IOException I/O exception
   */
  private void merge() throws IOException {
    final String f = text ? DATATXT : DATAATV;
    final DataOutput outL = new DataOutput(data.meta.dbfile(f + 'l'));
    final DataOutput outR = new DataOutput(data.meta.dbfile(f + 'r'));
    outL.write4(0);

    // initialize cached index iterators
    final IntList ml = new IntList();
    final IntList il = new IntList();
    final ValueIndexMerger[] vm = new ValueIndexMerger[splits];
    for(int i = 0; i < splits; ++i) vm[i] = new ValueIndexMerger(data, text, i);
    int sz = 0;

    // parse through all values
    while(true) {
      checkStop();

      // find first index which is not completely parsed yet
      int min = -1;
      while(++min < splits && vm[min].values.length == 0);
      if(min == splits) break;

      // find index entry with smallest key
      ml.reset();
      for(int i = min; i < splits; ++i) {
        if(vm[i].values.length == 0) continue;
        final int d = diff(vm[min].key, vm[i].key);
        if(d < 0) continue;
        if(d > 0) {
          min = i;
          ml.reset();
        }
        ml.add(i);
      }

      // parse through all values, cache and sort id values
      final int ms = ml.size();
      for(int m = 0; m < ms; ++m) {
        final ValueIndexMerger t = vm[ml.get(m)];
        final int vl = t.values.length;
        for(int l = 4, v; l < vl; l += Num.length(v)) {
          v = Num.get(t.values, l);
          il.add(v);
        }
        t.next();
      }
      // write final structure to disk
      write(outL, outR, il);
      ++sz;
    }

    // close index files
    outR.close();
    outL.close();

    // write number of entries to first position
    final DataAccess da = new DataAccess(data.meta.dbfile(f + 'l'));
    da.write4(sz);
    da.close();
  }

  /**
   * Writes the current index tree to disk.
   * @param partial partial flag
   * @throws IOException I/O exception
   */
  private void writeIndex(final boolean partial) throws IOException {
    // write id arrays and references
    final String name = (text ? DATATXT : DATAATV) + (partial ? splits : "");
    final DataOutput outL = new DataOutput(data.meta.dbfile(name + 'l'));
    final DataOutput outR = new DataOutput(data.meta.dbfile(name + 'r'));
    outL.write4(index.size());

    final IntList il = new IntList();
    index.init();
    while(index.more()) {
      final byte[] values = index.values.get(index.next());
      final int vs = Num.size(values);

      if(partial) {
        // write temporary structure to disk: number of entries, absolute values
        outR.write5(outL.size());
        outL.write(values, 0, vs);
      } else {
        // cache and sort all values
        for(int ip = 4; ip < vs; ip += Num.length(values, ip)) {
          il.add(Num.get(values, ip));
        }
        // write final structure to disk
        write(outL, outR, il);
      }
    }
    outL.close();
    outR.close();

    // temporarily write texts
    if(partial) {
      final DataOutput outT = new DataOutput(data.meta.dbfile(name + 't'));
      index.init();
      while(index.more()) outT.writeToken(index.keys.get(index.next()));
      outT.close();
    }
    // increase split counter
    splits++;
  }

  /**
   * Writes the final value structure to disk.
   * @param outL index values
   * @param outR references
   * @param il values
   * @throws IOException I/O exception
   */
  private static void write(final DataOutput outL, final DataOutput outR,
      final IntList il) throws IOException {

    // sort values before writing
    il.sort();
    final int is = il.size();
    outR.write5(outL.size());
    outL.writeNum(is);
    for(int i = 0, o = 0; i < is; i++) {
      final int v = il.get(i);
      outL.writeNum(v - o);
      o = v;
    }
    il.reset();
  }

  @Override
  public void abort() {
    data.meta.drop((text ? DATATXT : DATAATV) + ".+");
    if(text) data.meta.textindex = false;
    else data.meta.attrindex = false;
  }

  @Override
  protected String det() {
    return text ? INDEX_TEXT_D : INDEX_ATTRIBUTES_D;
  }
}
