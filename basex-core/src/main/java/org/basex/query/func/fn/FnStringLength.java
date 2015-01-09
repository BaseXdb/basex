package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] s;
    if(exprs.length == 0) {
      final Item it = ctxValue(qc).item(qc, info);
      if(it instanceof FItem) throw FISTRING_X.get(ii, it.type);
      s = it == null ? Token.EMPTY : it.string(ii);
    } else {
      s = toEmptyToken(arg(0, qc), qc);
    }
    return Int.get(Token.length(s));
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
