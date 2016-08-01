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
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class FuncLit extends Single implements Scope {
  /** Variable scope. */
  private final VarScope vs;
  /** Annotations. */
  private AnnList anns;
  /** Function name. */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] args;
  /** If the function's type should be checked at compile time. */
  private final boolean check;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Constructor.
   * @param anns annotations
   * @param name function name
   * @param args formal parameters
   * @param expr function body
   * @param ft function type
   * @param vs variable scope
   * @param info input info
   */
  FuncLit(final AnnList anns, final QNm name, final Var[] args, final Expr expr, final FuncType ft,
      final VarScope vs, final InputInfo info) {

    super(info, expr);
    this.anns = anns;
    this.name = name;
    this.args = args;
    this.vs = vs;
    check = ft == null;
    seqType = (ft == null ? FuncType.arity(args.length) : ft).seqType();
  }

  @Override
  public void comp(final CompileContext cc) throws QueryException {
    if(compiled) return;
    compiled = true;

    if(check) {
      final StaticFunc sf = cc.qc.funcs.get(name, args.length, info, true);
      anns = sf.anns;
      seqType = sf.funcType().seqType();
    }

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
    return expr.isValue() ? preEval(cc) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    return new FuncItem(vs.sc, anns, name, args, (FuncType) seqType.type, expr, qc.value, qc.pos,
        qc.size, vs.stackSize());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final VarScope scp = new VarScope(vs.sc);
    cc.pushScope(scp);
    try {
      final int al = args.length;
      final Var[] arg = new Var[al];
      for(int a = 0; a < al; a++) arg[a] = cc.copy(args[a], vm);

      final Expr call = expr.copy(cc, vm);
      return new FuncLit(anns, name, arg, call, (FuncType) seqType.type, scp, info);
    } finally {
      cc.removeScope();
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX || flag == Flag.POS;
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
  public String toString() {
    return new TokenBuilder(name.string()).add('#').addExt(args.length).toString();
  }

  /**
   * Returns annotations.
   * @return annotations
   */
  public AnnList annotations() {
    return anns;
  }
}
