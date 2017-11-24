package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnString extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = ctxArg(0, qc).item(qc, info);
    if(it instanceof FItem) throw FISTRING_X.get(info, it.type);
    return it == null ? Str.ZERO : it.type == AtomType.STR ? it : Str.get(it.string(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final boolean arg = exprs.length != 0;
    final Expr ex = arg ? exprs[0] : cc.qc.focus.value;
    // string('x') -> 'x'
    // $string[string() = 'a'] -> $string[. = 'a']
    return ex == null || !ex.seqType().eq(SeqType.STR_O) ? this :
      arg ? ex : new ContextValue(info).optimize(cc);
  }
}
