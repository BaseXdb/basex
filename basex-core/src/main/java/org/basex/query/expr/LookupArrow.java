package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Lookup arrow expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LookupArrow extends Arr {
  /** Name of function. */
  private final Str name;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param name name of function
   * @param exprs map expression and function arguments
   */
  public LookupArrow(final InputInfo info, final Str name, final Expr[] exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
    this.name = name;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr map = exprs[0];
    return map.size() == 0 ? map : this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item item = exprs[0].item(qc, info);
    if(item == Empty.VALUE) return Empty.VALUE;

    final XQMap map = toMap(item, qc);
    final FItem func = toFunction(map.get(name), qc);
    final Expr[] args = exprs.clone();
    args[0] = map;
    return new DynFuncCall(info, func, args).value(qc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new LookupArrow(info, name, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof LookupArrow)) return false;
    final LookupArrow la = (LookupArrow) obj;
    return name.equals(la.name) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), name, exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token("=?>").token(name.string()).
      params(Arrays.copyOfRange(exprs, 1, exprs.length));
  }
}
