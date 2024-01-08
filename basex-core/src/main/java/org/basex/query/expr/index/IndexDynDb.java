package org.basex.query.expr.index;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines a dynamic database source for index operations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class IndexDynDb extends IndexDb {
  /** Expression yielding a database. */
  private Expr expr;

  /**
   * Constructor.
   * @param expr expression
   * @param info input info (can be {@code null})
   */
  public IndexDynDb(final Expr expr, final InputInfo info) {
    super(info);
    this.expr = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return expr.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public IndexDb inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined == null) return null;
    expr = inlined;
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
    return copyType(new IndexDynDb(expr.copy(cc, vm), info));
  }

  @Override
  Data data(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    final Data data = value.data();
    if(data == null || !value.seqType().type.instanceOf(NodeType.DOCUMENT_NODE))
      throw DB_NODE_X.get(info, value);
    return data;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexDynDb && expr.equals(((IndexDynDb) obj).expr) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function._DB_NAME, expr);
  }
}
