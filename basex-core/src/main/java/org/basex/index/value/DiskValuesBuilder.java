package org.basex.index.value;

import static org.basex.util.Token.*;

import java.io.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DiskValuesBuilder extends ValuesBuilder {
  /** Temporary value tree. */
  private IndexTree index;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   */
  public DiskValuesBuilder(final Data data, final IndexType type) {
    super(data, type);
    index = new IndexTree(type);
  }

  @Override
  public DiskValues build() throws IOException {
    Util.debug(detailedInfo());

    try {
      final boolean updindex = data.meta.updindex;
      for(pre = 0; pre < size; ++pre) {
        if((pre & 0x0FFF) == 0) check();
        if(indexEntry()) {
          final int id = updindex ? data.id(pre) : pre;
          if(tokenize) {
            int pos = 0;
            for(final byte[] token : distinctTokens(data.text(pre, text))) {
              index.add(token, id, pos++);
              count++;
            }
          } else if(data.textLen(pre, text) <= data.meta.maxlen) {
            index.add(data.text(pre, text), id, 0);
            count++;
          }
        }
      }

      writeIndex(splits > 0);
      if(splits > 1) {
        index = null;
        clean();
        merge();
      }

      finishIndex();
      return updindex ? new UpdatableDiskValues(data, type) : new DiskValues(data, type);

    } catch(final Throwable th) {
      // drop index files
      data.meta.drop(DiskValues.fileSuffix(type) + ".+");
      throw th;
    }
  }

  @Override
  protected void check() throws IOException {
    super.check();
    // check if main memory is exhausted
    if(splitRequired()) {
      writeIndex(true);
      index = new IndexTree(type);
      clean();
    }
  }

  /**
   * Merges cached index files.
   * @throws IOException I/O exception
   */
  private void merge() throws IOException {
    final String f = DiskValues.fileSuffix(type);
    int entries = 0;
    try(DataOutput outL = new DataOutput(data.meta.dbFile(f + 'l'));
        DataOutput outR = new DataOutput(data.meta.dbFile(f + 'r'))) {
      outL.write4(0);

      // initialize cached index iterators
      final IntList ml = new IntList();
      final IntList id = new IntList(), pos = tokenize ? new IntList() : null;
      final DiskValuesMerger[] vm = new DiskValuesMerger[splits];
      for(int i = 0; i < splits; ++i) vm[i] = new DiskValuesMerger(data, type, i);

      // parse through all values
      while(true) {
        checkStop();

        // find first index which has not completely been parsed yet
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
          final DiskValuesMerger t = vm[ml.get(m)];
          final byte[] values = t.values;
          final int vl = values.length;
          for(int l = 4; l < vl; l += Num.length(values, l)) {
            id.add(Num.get(values, l));
            if(pos != null) {
              l += Num.length(values, l);
              pos.add(Num.get(values, l));
            }
          }
          t.next();
        }
        // write final structure to disk
        write(outL, outR, id, pos);
        ++entries;
      }
    }

    // write number of entries to first position
    try(DataAccess da = new DataAccess(data.meta.dbFile(f + 'l'))) {
      da.write4(entries);
    }
  }

  /**
   * Writes the current index tree to disk.
   * @param partial partial flag
   * @throws IOException I/O exception
   */
  private void writeIndex(final boolean partial) throws IOException {
    // write id arrays and references
    final String name = DiskValues.fileSuffix(type) + (partial ? splits : "");
    try(DataOutput outL = new DataOutput(data.meta.dbFile(name + 'l'));
        DataOutput outR = new DataOutput(data.meta.dbFile(name + 'r'))) {
      outL.write4(index.size());

      final IntList id = new IntList(), pos = tokenize ? new IntList() : null;
      index.init();
      while(index.more()) {
        final byte[] values = index.ids.get(index.next());
        final int vs = Num.size(values);

        if(partial) {
          // write temporary structure to disk: number of entries, absolute values
          outR.write5(outL.size());
          outL.write(values, 0, vs);
        } else {
          // cache and sort all values
          for(int ip = 4; ip < vs; ip += Num.length(values, ip)) {
            id.add(Num.get(values, ip));
            if(pos != null) {
              ip += Num.length(values, ip);
              pos.add(Num.get(values, ip));
            }
          }
          // write final structure to disk
          write(outL, outR, id, pos);
        }
      }
    }

    // temporarily write texts
    if(partial) {
      try(DataOutput outT = new DataOutput(data.meta.dbFile(name + 't'))) {
        index.init();
        while(index.more()) outT.writeToken(index.keys.get(index.next()));
      }
    }
    // increase split counter
    splits++;
  }

  /**
   * Writes the final value structure to disk.
   * @param outL index values
   * @param outR references
   * @param id ids
   * @param pos positions (can be {@code null})
   * @throws IOException I/O exception
   */
  private void write(final DataOutput outL, final DataOutput outR, final IntList id,
      final IntList pos) throws IOException {

    // sort values before writing
    int[] order = null;
    if(tokenize) {
      // tokenization: create array with offsets to ordered values
      order = id.createOrder(true);
    } else {
      // no token index: simple sort
      id.sort();
    }

    final int is = id.size();
    outR.write5(outL.size());
    outL.writeNum(is);
    for(int i = 0, old = 0; i < is; i++) {
      final int value = id.get(i);
      outL.writeNum(value - old);
      if(order != null) outL.writeNum(pos.get(order[i]));
      old = value;
    }
    id.reset();
    if(pos != null) pos.reset();
  }
}
