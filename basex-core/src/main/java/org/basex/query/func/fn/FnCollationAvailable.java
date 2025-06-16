package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCollationAvailable extends StandardFunc {
  /** Enumeration. */
  private enum Usage {
    /** Compare. */ COMPARE,
    /** Key. */ KEY,
    /** Substring. */ SUBSTRING;

    @Override
    public String toString() {
      return Enums.string(this);
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

    final EnumSet<Usage> usages = EnumSet.noneOf(Usage.class);
    for(final Item use : usage) usages.add(toEnum(use, Usage.class));
    try {
      toCollation(collation, qc);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
    return true;
  }
}
