package org.basex.query.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Unary lookup expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Lookup extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression and optional context
   */
  public Lookup(final InputInfo info, final Expr... expr) {
    super(info, expr);
  }

  @Override
  public ValueIter iter(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final Item ctx : exprs.length == 1 ? ctxValue(qc) : qc.value(exprs[1])) {
      final boolean map = ctx instanceof Map, array = ctx instanceof Array;
      if(!map && !array) throw CTXMAPARRAY.get(info, ctx);

      final FItem f = (FItem) ctx;
      if(exprs[0] == Str.WC) {
        for(final Value v : map ? ((Map) f).values() : ((Array) f).members()) vb.add(v);
      } else {
        final Iter ir = qc.iter(exprs[0]);
        for(Item it; (it = ir.next()) != null;) vb.add(f.invokeValue(qc, info, it));
      }
    }
    return vb;
  }

  @Override
  public boolean has(final Flag flag) {
    return exprs.length == 1 && flag == Flag.CTX || super.has(flag);
  }

  @Override
  public Lookup copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new Lookup(info, copyAll(qc, scp, vs, exprs)));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(exprs.length > 1) sb.append(exprs[1]).append(" ");
    return sb.append("? ").append(exprs[0]).toString();
  }
}
