package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A named function literal.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class FuncLit extends Single implements Scope {
  /** Variable scope. */
  private final VarScope vs;
  /** Function name. */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] params;
  /** Annotations. */
  private final AnnList anns;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param params formal parameters
   * @param anns annotations
   * @param seqType sequence type
   * @param name function name
   * @param vs variable scope
   */
  FuncLit(final InputInfo info, final Expr expr, final Var[] params, final AnnList anns,
      final SeqType seqType, final QNm name, final VarScope vs) {

    super(info, expr, seqType);
    this.name = name;
    this.params = params;
    this.anns = anns;
    this.vs = vs;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
      expr.markTailCalls(null);
    } catch(final QueryException e) {
      expr = cc.error(e, this);
    } finally {
      cc.removeScope(this);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return expr instanceof Value || !expr.has(Flag.CTX) ? cc.preEval(this) : this;
  }

  @Override
  public boolean compiled() {
    return true;
  }

  @Override
  public FuncItem item(final QueryContext qc, final InputInfo ii) {
    return new FuncItem(info, expr, params, anns, funcType(), vs.stackSize(), name,
        qc.focus.copy());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final VarScope vsc = new VarScope();
    cc.pushScope(vsc);
    try {
      final int pl = params.length;
      final Var[] vars = new Var[pl];
      for(int p = 0; p < pl; p++) vars[p] = cc.copy(params[p], vm);
      final Expr ex = expr.copy(cc, vm);
      return copyType(new FuncLit(info, ex, vars, anns, seqType(), name, vsc));
    } finally {
      cc.removeScope();
    }
  }

    @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var var : params) {
      if(!visitor.declared(var)) return false;
    }
    return expr.accept(visitor);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.inlineFunc(this);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj;
  }

  @Override
  public String description() {
    return "function literal";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(anns).concat("(: ", name.prefixId(), "#", params.length, " :)");
    qs.token(FUNCTION).params(params).token(AS).token(funcType().declType).brace(expr);
  }
}
