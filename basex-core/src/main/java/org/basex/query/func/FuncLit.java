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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class FuncLit extends Single implements Scope {
  /** Variable scope. */
  private final VarScope vs;
  /** Function name. */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] args;
  /** Annotations. */
  private final AnnList anns;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Constructor.
   * @param anns annotations
   * @param name function name
   * @param args formal parameters
   * @param expr function body
   * @param seqType sequence type
   * @param vs variable scope
   * @param info input info
   */
  FuncLit(final AnnList anns, final QNm name, final Var[] args, final Expr expr,
      final SeqType seqType, final VarScope vs, final InputInfo info) {

    super(info, expr, seqType);
    this.anns = anns;
    this.name = name;
    this.args = args;
    this.vs = vs;
  }

  @Override
  public void comp(final CompileContext cc) throws QueryException {
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
    return new FuncItem(vs.sc, anns, name, args, (FuncType) seqType().type, expr, qc.focus.copy(),
        vs.stackSize());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final VarScope scp = new VarScope(vs.sc);
    cc.pushScope(scp);
    try {
      final int al = args.length;
      final Var[] arg = new Var[al];
      for(int a = 0; a < al; a++) arg[a] = cc.copy(args[a], vm);
      final Expr ex = expr.copy(cc, vm);
      return new FuncLit(anns, name, arg, ex, seqType(), scp, info);
    } finally {
      cc.removeScope();
    }
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var v : args) if(!visitor.declared(v)) return false;
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
  public String toString() {
    return new TokenBuilder(name.prefixId()).add('#').addExt(args.length).toString();
  }
}
