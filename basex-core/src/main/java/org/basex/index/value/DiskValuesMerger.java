package org.basex.index.value;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.in.DataInput;

/**
 * This class provides data for merging temporary value indexes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DiskValuesMerger {
  /** Index instance. */
  private final DiskValues dv;
  /** Index keys. */
  private final DataInput dk;
  /** File prefix. */
  private final String pref;
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
   * @param i merge id
   * @throws IOException I/O exception
   */
  DiskValuesMerger(final Data data, final IndexType type, final int i) throws IOException {
    pref = DiskValues.fileSuffix(type) + i;
    dk = new DataInput(data.meta.dbFile(pref + 't'));
    dv = new DiskValues(data, type, pref);
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
      data.meta.drop(pref + '.');
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
