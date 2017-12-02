package org.basex.query.expr;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Context value.
 *
 * @author BaseX Team 2005-17, BSD License
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
    final Value v = cc.qc.focus.value;
    if(cc.nestedFocus()) {
      exprType.assign(Occ.ONE);
      if(v != null) adoptType(v);
    } else {
      if(v != null) return cc.replaceWith(this, v);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return ctxValue(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ctxValue(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return ctxValue(qc).item(qc, info);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CTX.in(flags);
  }

  @Override
  public boolean removable(final Var var) {
    return false;
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
  public String toString() {
    return ".";
  }
}
