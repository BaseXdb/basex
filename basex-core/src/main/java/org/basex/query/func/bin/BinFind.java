package org.basex.query.func.bin;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class BinFind extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin binary = toBinOrNull(arg(0), qc);
    final Item offset = arg(1).atomItem(qc, info);
    final Bin search = toBin(arg(2), qc);
    if(binary == null) return Empty.VALUE;

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int pos = indexOf(bytes, search.binary(info), bounds(offset, Empty.VALUE, bl)[0]);
    return pos == -1 ? Empty.VALUE : Int.get(pos);
  }
}
