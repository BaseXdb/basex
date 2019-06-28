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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class ContextValue extends Simple {
  /**
   * Constructor.
   * @param info input info
   */
  public ContextValue(final InputInfo info) {
    super(info, SeqType.ITEM_ZM);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    if(cc.nestedFocus()) {
      exprType.assign(Occ.ONE);
      if(value != null) adoptType(value);
    } else {
      if(value != null) return cc.replaceWith(this, value);
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
  public boolean inlineable(final Var var) {
    return false;
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) {
    return var != null ? null : ex;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new ContextValue(info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT, false) && super.accept(visitor);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof ContextValue;
  }

  @Override
  public String toString() {
    return ".";
  }
}
