package org.basex.query.var;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Reference to a static variable.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
final class StaticVarRef extends ParseExpr {
  /** Variable name. */
  private final QNm name;
  /** Referenced variable. */
  private StaticVar var;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param name variable name
   */
  StaticVarRef(final InputInfo info, final QNm name) {
    super(info, SeqType.ITEM_ZM);
    this.name = name;
  }

  @Override
  public void checkUp() {
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    var.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    final Value value = var.value;
    if(value != null) {
      cc.info(QueryText.OPTINLINE_X, this);
      return value;
    }
    exprType.assign(var.seqType());
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return var.value(qc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return var != null && var.has(flags);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.staticVar(var);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final StaticVarRef ref = new StaticVarRef(info, name);
    ref.var = var;
    return copyType(ref);
  }

  @Override
  public int exprSize() {
    // should always be inlined
    return 0;
  }

  @Override
  public boolean inlineable(final InlineContext v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Expr inline(final InlineContext ic) {
    return null;
  }

  /**
   * Initializes this reference with the given variable.
   * @param vr variable
   * @throws QueryException query exception
   */
  void init(final StaticVar vr) throws QueryException {
    if(vr.anns.contains(Annotation.PRIVATE) && !info.sc().baseURI().eq(vr.sc.baseURI()))
      throw VARPRIVATE_X.get(info, this);
    var = vr;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof StaticVarRef)) return false;
    final StaticVarRef s = (StaticVarRef) obj;
    return name.eq(s.name) && var == s.var;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.VAR, name));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.concat("$", name.string());
  }
}
