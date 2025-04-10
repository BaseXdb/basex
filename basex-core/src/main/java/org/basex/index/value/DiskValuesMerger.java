package org.basex.index.value;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.in.DataInput;

/**
 * This class provides data for merging temporary value indexes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DiskValuesMerger {
  /** Index instance. */
  private final DiskValues dv;
  /** Index keys. */
  private final DataInput dk;
  /** File prefix. */
  private final String prefix;
  /** Data reference. */
  private final Data data;

  /** Current key. */
  byte[] key;
  /** Current values. */
  byte[] values;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   * @param id merge ID
   * @throws IOException I/O exception
   */
  DiskValuesMerger(final Data data, final IndexType type, final int id) throws IOException {
    prefix = DiskValues.fileSuffix(type) + id;
    dk = new DataInput(data.meta.dbFile(prefix + 't'));
    dv = new DiskValues(data, type, prefix);
    this.data = data;
    next();
  }

  /**
   * Jumps to the next value. {@link #values} will have 0 entries if the end of file is reached.
   * @throws IOException I/O exception
   */
  void next() throws IOException {
    values = nextValues();
    if(values.length == 0) {
      dv.close();
      dk.close();
      data.meta.drop(prefix + '.');
    } else {
      key = dk.readToken();
    }
  }

  /**
   * Returns next values. Called by the {@link DiskValuesBuilder}.
   * @return compressed values
   */
  private byte[] nextValues() {
    return dv.idxr.cursor() >= dv.idxr.length() ? EMPTY :
      dv.idxl.readBytes(dv.idxr.read5(), dv.idxl.read4());
  }
}
