package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnMatches extends RegEx {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toEmptyToken(exprs[0], qc);
    final Pattern p = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc, false);
    return Bln.get(p.matcher(string(value)).find());
  }
}
