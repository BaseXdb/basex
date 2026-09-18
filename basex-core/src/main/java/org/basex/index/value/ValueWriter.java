package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.util.list.*;

/**
 * Writes the files of a value index segment or of a base structure, in ascending key order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ValueWriter implements SegmentWriter {
  /** Entries. */
  private final DataOutput outL;
  /** References to the entries. */
  private final DataOutput outR;
  /** File with the entries. */
  private final IOFile file;
  /** Indicates if keys are written. */
  private final boolean keys;
  /** Indicates if references have positions. */
  private final boolean token;
  /** Number of written keys. */
  private int size;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   * @param prefix file prefix
   * @param keys write keys
   * @throws IOException I/O exception
   */
  ValueWriter(final Data data, final IndexType type, final String prefix, final boolean keys)
      throws IOException {
    this.keys = keys;
    token = type == IndexType.TOKEN;
    file = data.meta.dbFile(prefix + 'l');
    outL = new DataOutput(file);
    try {
      outR = new DataOutput(data.meta.dbFile(prefix + 'r'));
    } catch(final IOException ex) {
      outL.close();
      throw ex;
    }
    outL.write4(0);
  }

  @Override
  public void write(final byte[] key, final IntList ids, final IntList poss) throws IOException {
    // references of the index builder are unsorted if node IDs differ from PRE values
    final int is = ids.size();
    for(int i = 1; i < is; i++) {
      if(ids.get(i - 1) > ids.get(i)) {
        SegmentedIndex.sort(ids, poss);
        break;
      }
    }
    outR.write5(outL.size());
    if(keys) outL.writeToken(key);
    outL.writeNum(is);
    for(int i = 0, old = 0; i < is; i++) {
      final int id = ids.get(i);
      outL.writeNum(id - old);
      if(token) outL.writeNum(poss.get(i));
      old = id;
    }
    size++;
  }

  @Override
  public void close() throws IOException {
    try {
      outL.close();
    } finally {
      outR.close();
    }
    try(DataAccess da = new DataAccess(file)) {
      da.write4(0, size);
    }
  }
}
