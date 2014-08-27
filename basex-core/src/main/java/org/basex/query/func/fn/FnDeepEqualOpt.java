package org.basex.query.func.fn;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.Compare.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnDeepEqualOpt extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Compare cmp = new Compare(info);
    final Mode[] modes = Mode.values();
    if(exprs.length == 3) {
      for(final Item it : exprs[2].atomValue(qc, info)) {
        final byte[] key = uc(toToken(it));
        boolean found = false;
        for(final Mode m : modes) {
          found = eq(key, token(m.name()));
          if(found) {
            cmp.flag(m);
            break;
          }
        }
        if(!found) throw INVALIDOPTION_X.get(info, key);
      }
    }
    return Bln.get(cmp.equal(qc.iter(exprs[0]), qc.iter(exprs[1])));
  }
}
