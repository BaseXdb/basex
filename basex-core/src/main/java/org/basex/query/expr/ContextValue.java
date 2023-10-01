package org.basex.query.expr;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Context value.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ContextValue extends Simple {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public ContextValue(final InputInfo info) {
    super(info, SeqType.ITEM_ZM);
  }

  /**
   * Creates a new, optimized context value expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @return optimized expression
   */
  public static Expr get(final CompileContext cc, final InputInfo info) {
    return new ContextValue(info).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    if(value != null) {
      if(!cc.nestedFocus()) return cc.replaceWith(this, value);
      adoptType(value);
    }
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ctxValue(qc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CTX.in(flags);
  }

  @Override
  public VarUsage count(final Var var) {
    return var == null ? VarUsage.ONCE : VarUsage.NEVER;
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    return ic.var == null ? ic.copy() : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new ContextValue(info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT) && super.accept(visitor);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof ContextValue;
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(".");
  }
}
