package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A named function literal.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Constructor.
   * @param anns annotations
   * @param name function name
   * @param params formal parameters
   * @param expr function body
   * @param seqType sequence type
   * @param vs variable scope
   * @param info input info
   */
  FuncLit(final AnnList anns, final QNm name, final Var[] params, final Expr expr,
      final SeqType seqType, final VarScope vs, final InputInfo info) {

    super(info, expr, seqType);
    this.anns = anns;
    this.name = name;
    this.params = params;
    this.vs = vs;
  }

  @Override
  public void comp(final CompileContext cc) {
    if(compiled) return;
    compiled = true;

    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
      expr.markTailCalls(null);
    } catch(final QueryException e) {
      expr = cc.error(e, this);
    } finally {
      cc.removeScope(this);
    }
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    comp(cc);
    return optimize(cc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    return new FuncItem(vs.sc, anns, name, params, funcType(), expr, qc.focus.copy(),
        vs.stackSize(), info);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final VarScope vsc = new VarScope(vs.sc);
    cc.pushScope(vsc);
    try {
      final int pl = params.length;
      final Var[] vars = new Var[pl];
      for(int p = 0; p < pl; p++) vars[p] = cc.copy(params[p], vm);
      final Expr ex = expr.copy(cc, vm);
      return copyType(new FuncLit(anns, name, vars, ex, seqType(), vsc, info));
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
  public boolean compiled() {
    return compiled;
  }

  @Override
  public boolean equals(final Object obj) {
    // [CG] could be enhanced
    return this == obj;
  }

  @Override
  public void plan(final QueryString qs) {
    qs.concat(name.prefixId(), "#", params.length);
  }
}
