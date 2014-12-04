package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnNumber extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = arg(0, qc).atomItem(qc, info);
    if(it == null) return Dbl.NAN;
    if(it.type == AtomType.DBL) return it;
    try {
      if(info != null) info.check(true);
      return AtomType.DBL.cast(it, qc, sc, info);
    } catch(final QueryException ex) {
      return Dbl.NAN;
    } finally {
      if(info != null) info.check(false);
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
