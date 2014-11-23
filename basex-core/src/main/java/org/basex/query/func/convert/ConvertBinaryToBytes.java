package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ConvertBinaryToBytes extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return BytSeq.get(toBin(exprs[0], qc).input(info).content());
    } catch(final IOException ex) {
      throw BXCO_STRING_X.get(info, ex);
    }
  }
}
