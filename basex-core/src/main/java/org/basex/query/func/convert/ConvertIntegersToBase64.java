package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class ConvertIntegersToBase64 extends ConvertFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return bytesToB64(qc);
  }

  /**
   * Converts the first argument from a byte sequence to a byte array.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final B64 bytesToB64(final QueryContext qc) throws QueryException {
    final Value values = arg(0).atomValue(qc, info);

    // return internal byte array
    if(values instanceof BytSeq) return B64.get(((BytSeq) values).toJava());

    // single integer
    final long size = values.size();
    if(size == 1 && values instanceof Int) return B64.get((byte) ((Int) values).itr());

    final ByteList bl = new ByteList(Seq.initialCapacity(size));
    if(values instanceof IntSeq) {
      // integer sequence
      for(final long l : ((IntSeq) values).values()) bl.add((byte) l);
    } else {
      // other types
      final Iter iter = values.iter();
      for(Item item; (item = qc.next(iter)) != null;) {
        bl.add((int) toLong(item));
      }
    }
    return B64.get(bl.finish());
  }
}
