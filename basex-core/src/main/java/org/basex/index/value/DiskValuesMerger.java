package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.in.DataInput;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides data for merging temporary value indexes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DiskValuesMerger implements SegmentReader {
  /** ID lists. */
  private final DataAccess idxl;
  /** References to the ID lists. */
  private final DataAccess idxr;
  /** Index keys. */
  private final DataInput idxt;
  /** File prefix. */
  private final String prefix;
  /** Data reference. */
  private final Data data;
  /** Indicates if references have positions. */
  private final boolean token;
  /** IDs of the current key. */
  private final IntList ids = new IntList();
  /** Positions of the current key. */
  private final IntList poss = new IntList();

  /** Current key ({@code null} if all keys have been read). */
  private byte[] key;
  /** Indicates if the files have been closed. */
  private boolean closed;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   * @param id merge ID
   * @throws IOException I/O exception
   */
  DiskValuesMerger(final Data data, final IndexType type, final int id) throws IOException {
    prefix = DiskValuesBuilder.partial(type, id);
    idxt = new DataInput(data.meta.dbFile(prefix + 't'));
    idxl = new DataAccess(data.meta.dbFile(prefix + 'l'));
    idxr = new DataAccess(data.meta.dbFile(prefix + 'r'));
    // skip the number of keys
    idxl.read4();
    token = type == IndexType.TOKEN;
    this.data = data;
    next();
  }

  @Override
  public byte[] key() {
    return key;
  }

  @Override
  public int[] ids() {
    return ids.toArray();
  }

  @Override
  public int[] poss() {
    return poss.toArray();
  }

  @Override
  public void next() throws IOException {
    ids.reset();
    poss.reset();
    if(idxr.cursor() >= idxr.length()) {
      key = null;
      close();
    } else {
      final byte[] values = idxl.readBytes(idxr.read5(), idxl.read4());
      DiskValuesBuilder.decode(values, values.length, token, ids, poss);
      key = idxt.readToken();
    }
  }

  @Override
  public void close() {
    if(closed) return;
    closed = true;
    idxl.close();
    idxr.close();
    try {
      idxt.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    data.meta.drop(prefix + '.');
  }
}
