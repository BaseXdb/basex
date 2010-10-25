package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.io.DataInput;
import org.basex.io.IO;

/**
 * This class provides data for merging temporary value indexes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class ValueMerge {
  /** Data input reference. */
  private final DataInput di;
  /** File prefix. */
  private final String pref;
  /** Data reference. */
  private final Data data;
  /** Index instance. */
  private final Values v;

  /** Current token. */
  byte[] token;
  /** Current pre values. */
  byte[] pre;

  /**
   * Constructor.
   * @param d data reference
   * @param txt text flag
   * @param i merge id
   * @throws IOException I/O exception
   */
  ValueMerge(final Data d, final boolean txt, final int i) throws IOException {
    pref = (txt ? DATATXT : DATAATV) + i;
    di = new DataInput(d.meta.file(pref + 't'));
    v = new Values(d, txt, pref);
    data = d;
    next();
  }

  /**
   * Jumps to the next text.
   * @throws IOException I/O exception
   */
  void next() throws IOException {
    pre = v.nextPres();
    if(pre.length != 0) {
      token = di.readBytes();
    } else {
      v.close();
      di.close();
      DropDB.drop(data.meta.name, pref + '.' + IO.BASEXSUFFIX, data.meta.prop);
    }
  }
}
