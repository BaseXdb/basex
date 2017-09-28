package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnString extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = ctxArg(0, qc).item(qc, info);
    if(it instanceof FItem) throw FISTRING_X.get(info, it.type);
    return it == null ? Str.ZERO : it.type == AtomType.STR ? it : Str.get(it.string(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // string('x') -> 'x'
    return exprs.length != 0 && exprs[0].seqType().eq(SeqType.STR) ? exprs[0] : this;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(Locking.CONTEXT)) && super.accept(visitor);
  }
}
