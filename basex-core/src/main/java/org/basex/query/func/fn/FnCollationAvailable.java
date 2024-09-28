package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnCollationAvailable extends StandardFunc {
  /** Enumeration. */
  private enum Usage {
    /** Equality. */ EQUALITY,
    /** Sort. */ SORT,
    /** Substring. */ SUBSTRING;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
   final byte[] collation = toToken(arg(0), qc);
    final Value usage = arg(1).value(qc);

    try {
      toCollation(collation, qc);
      for(final Item use : usage) toEnum(use, Usage.class);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
    return true;
  }
}
