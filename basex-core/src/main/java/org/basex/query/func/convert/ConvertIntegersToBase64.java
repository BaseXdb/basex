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
 * @author BaseX Team 2005-17, BSD License
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
    final Value v = exprs[0].atomValue(qc, info);

    // return internal byte array
    if(v instanceof BytSeq) return B64.get(((BytSeq) v).toJava());

    // single integer
    final int s = (int) v.size();
    if(s == 1 && v instanceof Int) return B64.get((byte) ((Int) v).itr());

    final ByteList bl = new ByteList(Math.max(Array.CAPACITY, s));
    if(v instanceof IntSeq) {
      // integer sequence
      for(final long l : ((IntSeq) v).values()) bl.add((byte) l);
    } else {
      // other types
      final Iter iter = v.iter();
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        bl.add((int) toLong(it));
      }
    }
    return B64.get(bl.finish());
  }
}
