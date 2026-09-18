package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>This class builds an index for attribute values and text contents in a
 * tree structure and stores the result to disk.</p>
 *
 * <p>The data is stored on disk in the following format:</p>
 * <ul>
 * <li> {@code DATATXT/ATV + 'l'}: contains the index values, which are dense ID
 *   lists to all text nodes/attribute values, stored in the {@link Num} format:
 *   [size0, id1, id2, ...]. The number of index keys is stored in the first 4
 *   bytes of the file.</li>
 * <li> {@code DATATXT/ATV + 'r'}: contains 5-byte references to the ID lists
 *   for all keys. To save space, the keys itself are not stored in the index
 *   structure. Instead, they can be found by following the ID references to
 *   the main table.
 * </li>
 * </ul>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DiskValuesBuilder extends IndexBuilder {
  /** Number of keys after which a partial index is written ({@code 0}: decided by memory). */
  static int splitKeys;

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
  public ValueIndex build() throws IOException {
    Util.debugln(detailedInfo());

    final String prefix = DiskValues.fileSuffix(type);
    try {
      build(0, size, new ValueWriter(data, type, prefix, false));
      finishIndex();
      return data.meta.updindex ? new SegmentedValues(data, type) : new DiskValues(data, type);
    } catch(final Throwable th) {
      // drop index files
      data.meta.drop(prefix + ".+");
      throw th;
    }
  }

  /**
   * Indexes the units of the specified range and writes them to an index structure.
   * @param first PRE value of the first node
   * @param last PRE value of the last node (exclusive)
   * @param output writer of the index structure (closed by this method)
   * @throws IOException I/O exception
   */
  void build(final int first, final int last, final SegmentWriter output) throws IOException {
    try(SegmentWriter writer = output) {
      final boolean updindex = data.meta.updindex;
      for(pre = first; pre < last; pre++) {
        if((pre & 0x0FFF) == 0) check();
        if(includeNames.unit(pre)) {
          final int id = updindex ? data.id(pre) : pre;
          ValueIndex.keys(data, type, pre, (key, pos) -> {
            index.add(key, id, pos);
            count++;
          });
        }
      }

      if(splits == 0) {
        // single tree: write final structure
        final IntList ids = new IntList(), poss = new IntList();
        index.init();
        while(index.more()) {
          final int n = index.next();
          final byte[] values = index.ids.get(n);
          ids.reset();
          poss.reset();
          decode(values, Num.size(values), type == IndexType.TOKEN, ids, poss);
          writer.write(index.keys.get(n), ids, poss);
        }
      } else {
        // merge partial structures
        writeIndex();
        index = null;
        SegmentedIndex.merge(splits, s -> new DiskValuesMerger(data, type, s), writer,
          Token::compare, null);
      }
    }
  }

  /**
   * Returns the file prefix of a partial index structure.
   * @param type index type
   * @param split split counter
   * @return prefix
   */
  static String partial(final IndexType type, final int split) {
    return DiskValues.fileSuffix(type) + "tmp" + split;
  }

  /**
   * Decodes compressed references (see {@link Num}).
   * @param values compressed values, starting with a 4-byte header
   * @param length length of the values
   * @param token references have positions
   * @param ids IDs
   * @param poss positions
   */
  static void decode(final byte[] values, final int length, final boolean token,
      final IntList ids, final IntList poss) {
    for(int v = 4; v < length; v += Num.length(values, v)) {
      ids.add(Num.get(values, v));
      if(token) {
        v += Num.length(values, v);
        poss.add(Num.get(values, v));
      } else {
        poss.add(0);
      }
    }
  }

  @Override
  protected void check() throws IOException {
    super.check();
    // check if main memory is exhausted
    if(splitKeys > 0 ? index.size() >= splitKeys : splitRequired(index.memory())) {
      writeIndex();
      index = new IndexTree(type);
    }
  }

  /**
   * Writes the current index tree as a partial structure.
   * @throws IOException I/O exception
   */
  private void writeIndex() throws IOException {
    // write ID arrays and references: number of entries, absolute values
    final String name = partial(type, splits);
    try(DataOutput outL = new DataOutput(data.meta.dbFile(name + 'l'));
        DataOutput outR = new DataOutput(data.meta.dbFile(name + 'r'))) {
      outL.write4(index.size());
      index.init();
      while(index.more()) {
        final byte[] values = index.ids.get(index.next());
        outR.write5(outL.size());
        outL.write(values, 0, Num.size(values));
      }
    }
    // write texts
    try(DataOutput outT = new DataOutput(data.meta.dbFile(name + 't'))) {
      index.init();
      while(index.more()) outT.writeToken(index.keys.get(index.next()));
    }
    splits++;
  }
}
