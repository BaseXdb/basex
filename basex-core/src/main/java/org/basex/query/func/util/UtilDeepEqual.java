package org.basex.query.func.util;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.fn.DeepEqual.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilDeepEqual extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final DeepEqual cmp = new DeepEqual(info);
    final Mode[] modes = Mode.values();
    if(exprs.length == 3) {
      for(final Item item : exprs[2].atomValue(qc, info)) {
        final byte[] key = uc(toToken(item));
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
    return Bln.get(cmp.equal(exprs[0].iter(qc), exprs[1].iter(qc), qc));
  }
}
