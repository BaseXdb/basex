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
 * @author BaseX Team 2005-21, BSD License
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
    final Value value = exprs[0].atomValue(qc, info);

    // return internal byte array
    if(value instanceof BytSeq) return B64.get(((BytSeq) value).toJava());

    // single integer
    final long size = value.size();
    if(size == 1 && value instanceof Int) return B64.get((byte) ((Int) value).itr());

    final ByteList bl = new ByteList(Seq.initialCapacity(size));
    if(value instanceof IntSeq) {
      // integer sequence
      for(final long l : ((IntSeq) value).values()) bl.add((byte) l);
    } else {
      // other types
      final Iter iter = value.iter();
      for(Item item; (item = qc.next(iter)) != null;) bl.add((int) toLong(item));
    }
    return B64.get(bl.finish());
  }
}
