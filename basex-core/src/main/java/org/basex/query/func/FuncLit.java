package org.basex.query.func;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A named function literal.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class FuncLit extends Single implements Scope {
  /** Variable scope. */
  private final VarScope scope;
  /** Static context. */
  private final StaticContext sc;
  /** Annotations. */
  private Ann ann;
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
   * @param ann annotations
   * @param name function name
   * @param arg formal parameters
   * @param expr function body
   * @param ft function type
   * @param scope variable scope
   * @param sc static context
   * @param info input info
   */
  public FuncLit(final Ann ann, final QNm name, final Var[] arg, final Expr expr, final FuncType ft,
      final VarScope scope, final StaticContext sc, final InputInfo info) {
    super(info, expr);
    this.ann = ann;
    this.name = name;
    this.args = arg;
    this.scope = scope;
    this.sc = sc;
    check = ft == null;
    type = (ft == null ? FuncType.arity(args.length) : ft).seqType();
  }

  @Override
  public void compile(final QueryContext qc) throws QueryException {
    if(compiled) return;
    compiled = true;

    if(check) {
      final StaticFunc sf = qc.funcs.get(name, args.length, info, true);
      if(sf == null) throw FUNCUNKNOWN.get(info, name.string());
      ann = sf.ann;
      type = sf.funcType().seqType();
    }

    try {
      expr = expr.compile(qc, scope);
      expr.markTailCalls(null);
    } catch(final QueryException e) {
      expr = FNInfo.error(e, type);
    } finally {
      scope.cleanUp(this);
    }
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope o) throws QueryException {
    compile(qc);
    return expr.isValue() ? preEval(qc) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    return new FuncItem(sc, ann == null ? new Ann() : ann, name, args, (FuncType) type.type,
        expr, qc.value, qc.pos, qc.size, scope.stackSize());
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope o, final IntObjMap<Var> vs) {
    final Ann a = ann == null ? null : new Ann();
    if(a != null) for(int i = 0; i < ann.size(); i++) a.add(ann.names[i], ann.values[i], info);
    final VarScope scp = new VarScope(sc);
    final Var[] arg = new Var[args.length];
    for(int i = 0; i < arg.length; i++)
      vs.put(args[i].id, arg[i] = scp.newCopyOf(qc, args[i]));
    final Expr call = expr.copy(qc, scp, vs);
    return new FuncLit(a, name, arg, call, (FuncType) type.type, scp, sc, info);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX || flag == Flag.FCS;
  }

  /**
   * Creates a function literal for a function that was not yet encountered while parsing.
   * @param name function name
   * @param arity function arity
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return function literal
   * @throws QueryException query exception
   */
  public static FuncLit unknown(final QNm name, final long arity, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    final VarScope scp = new VarScope(sc);
    final Var[] arg = new Var[(int) arity];
    final Expr[] refs = new Expr[arg.length];
    for(int i = 0; i < arg.length; i++) {
      arg[i] = scp.newLocal(qc, new QNm(QueryText.ARG + (i + 1), ""), SeqType.ITEM_ZM, true);
      refs[i] = new VarRef(ii, arg[i]);
    }
    final TypedFunc call = qc.funcs.getFuncRef(name, refs, sc, ii);
    return new FuncLit(null, name, arg, call.fun, null, scp, sc, ii);
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
  public Ann annotations() {
    return ann;
  }
}
