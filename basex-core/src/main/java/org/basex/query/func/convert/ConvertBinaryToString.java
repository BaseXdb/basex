package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ConvertBinaryToString extends ConvertFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin bin = toBin(exprs[0], qc);
    final String enc = toEncoding(1, BXCO_ENCODING_X, qc);
    final boolean val = exprs.length < 3 || !toBoolean(exprs[2], qc);
    try {
      return Str.get(toString(bin.input(info), enc, val));
    } catch(final IOException ex) {
      throw BXCO_STRING_X.get(info, ex);
    }
  }
}
