package org.basex.query.expr.index;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines a dynamic database source for index operations.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class IndexDynDb extends IndexDb {
  /** Expression yielding a database. */
  private Expr expr;

  /**
   * Constructor.
   * @param info input info
   * @param iterable iterable flag
   * @param expr expression
   */
  public IndexDynDb(final InputInfo info, final boolean iterable, final Expr expr) {
    super(info, iterable);
    this.expr = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public boolean removable(final Var var) {
    return expr.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public IndexDb inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
    final Expr sub = expr.inline(var, ex, cc);
    if(sub == null) return null;
    expr = sub;
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + 1;
  }

  @Override
  public IndexDynDb copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new IndexDynDb(info, iterable, expr.copy(cc, vm));
  }

  @Override
  public Data data(final QueryContext qc, final IndexType type) throws QueryException {
    final Value v = qc.value(expr);
    final Data d = v.data();
    if(d == null) throw BXDB_NOINDEX_X.get(info, v);
    if(!v.seqType().instanceOf(SeqType.DOC_ZM)) throw BXDB_DOC_X.get(info, v);
    type.check(d, info);
    return d;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexDynDb && expr.equals(((IndexDynDb) obj).expr) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr);
  }

  @Override
  public Expr source() {
    return expr;
  }
}
