package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Default value of an omitted argument of a static function call.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DefaultValue extends Simple {
  /** Called function. */
  private final StaticFunc func;
  /** Parameter index. */
  private final int index;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param func called function
   * @param index parameter index
   */
  DefaultValue(final InputInfo info, final StaticFunc func, final int index) {
    super(info, Types.ITEM_ZM);
    this.func = func;
    this.index = index;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr expr = func.compileDefault(index, cc);
    // constant default values are inlined
    if(expr instanceof final Value value) return cc.replaceWith(this, value);
    return adoptType(expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return func.defaultValue(index, qc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return func.defaultHas(index, flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    // the focus of the caller cannot be inlined into the default value
    return ic.var != null || !has(Flag.CTX);
  }

  @Override
  public VarUsage count(final Var var) {
    return var == null && has(Flag.CTX) ? VarUsage.MORE_THAN_ONCE : VarUsage.NEVER;
  }

  @Override
  public Expr inline(final InlineContext ic) {
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new DefaultValue(info, func, index));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final DefaultValue dv && func == dv.func &&
        index == dv.index;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, func.paramName(index).prefixId()));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(func.paramName(index).varString()).token(":=").token(DEFAULT);
  }
}
