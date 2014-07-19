package org.basex.query.expr;

import org.basex.core.*;
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
 * Context item (or value).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Context extends Simple {
  /**
   * Constructor.
   * @param info input info
   */
  public Context(final InputInfo info) {
    super(info);
    seqType = SeqType.ITEM_ZM;
  }

  @Override
  public Context compile(final QueryContext qc, final VarScope scp) {
    if(qc.value != null) seqType = qc.value.seqType();
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return checkCtx(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return checkCtx(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return checkCtx(qc).item(qc, info);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX;
  }

  @Override
  public boolean removable(final Var var) {
    return false;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new Context(info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.CTX) && super.accept(visitor);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Context;
  }

  @Override
  public String toString() {
    return ".";
  }
}
