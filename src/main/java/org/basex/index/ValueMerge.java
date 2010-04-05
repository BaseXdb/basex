package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.proc.DropDB;
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
  /** Data reference. */
  private final Data data;
  /** Text flag. */
  private final boolean text;

  /** Data input reference. */
  private final DataInput di;
  /** Index instance. */
  private final Values v;
  /** Index id. */
  private final int id;
  /** Current text. */
  byte[] t;
  /** Current pre values. */
  byte[] p;

  /**
   * Constructor.
   * @param d data reference
   * @param txt text flag
   * @param i merge id
   * @throws IOException I/O exception
   */
  ValueMerge(final Data d, final boolean txt, final int i) throws IOException {
    final String f = txt ? DATATXT : DATAATV;
    di = new DataInput(d.meta.file(f + i + 't'));
    v = new Values(d, txt, f + i);
    text = txt;
    data = d;
    id = i;
    next();
  }

  /**
   * Jumps to the next text.
   * @throws IOException I/O exception
   */
  void next() throws IOException {
    p = v.nextPres();
    if(p.length > 0) {
      //t = data.text(Num.read(p, 4), text);
      t = di.readBytes();
    } else {
      t = EMPTY;
      v.close();
      di.close();
      final String f = (text ? DATATXT : DATAATV) + id + '.' + IO.BASEXSUFFIX;
      DropDB.delete(data.meta.name, f, data.meta.prop);
    }
  }
}
