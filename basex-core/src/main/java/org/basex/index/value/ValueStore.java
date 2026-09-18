package org.basex.index.value;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.random.*;
import org.basex.util.list.*;

/**
 * A structure of an updatable value index that is stored on disk.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ValueStore extends IndexSegment implements ValueSource {
  /** Index type. */
  protected final IndexType type;
  /** Entries. */
  protected final DataAccess idxl;
  /** References to the entries. */
  protected final DataAccess idxr;
  /** Number of keys. */
  private final int size;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   * @param number segment number ({@code -1} for the base structure)
   * @param prefix file prefix
   * @throws IOException I/O exception
   */
  ValueStore(final Data data, final IndexType type, final int number, final String prefix)
      throws IOException {
    super(data, number, prefix);
    this.type = type;
    idxl = new DataAccess(data.meta.dbFile(prefix + 'l'));
    idxr = new DataAccess(data.meta.dbFile(prefix + 'r'));
    size = idxl.read4(0);
  }

  @Override
  public final int size() {
    return size;
  }

  @Override
  public final void refs(final int i, final IntList ids, final IntList poss) {
    ValueSource.refs(idxl, count(i), type == IndexType.TOKEN, ids, poss);
  }

  @Override
  public final long length() {
    return idxl.length() + idxr.length();
  }

  @Override
  public final void close() {
    idxl.close();
    idxr.close();
  }
}
