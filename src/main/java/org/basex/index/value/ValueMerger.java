package org.basex.index.value;

import static org.basex.data.DataText.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.in.DataInput;

/**
 * This class provides data for merging temporary value indexes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class ValueMerger {
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
   * @param d data reference
   * @param txt text flag
   * @param i merge id
   * @throws IOException I/O exception
   */
  ValueMerger(final Data d, final boolean txt, final int i) throws IOException {
    pref = (txt ? DATATXT : DATAATV) + i;
    dk = new DataInput(d.meta.dbfile(pref + 't'));
    dv = new DiskValues(d, txt, pref);
    data = d;
    next();
  }

  /**
   * Jumps to the next value. {@link #values} will have 0 entries if the
   * end of file is reached.
   * @throws IOException I/O exception
   */
  void next() throws IOException {
    values = dv.nextValues();
    if(values.length != 0) {
      key = dk.readToken();
    } else {
      dv.close();
      dk.close();
      data.meta.drop(pref + '.');
    }
  }
}
