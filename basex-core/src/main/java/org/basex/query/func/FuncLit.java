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
   * @param a annotations
   * @param nm function name
   * @param arg formal parameters
   * @param fn function body
   * @param ft function type
   * @param scp variable scope
   * @param sctx static context
   * @param ii input info
   */
  public FuncLit(final Ann a, final QNm nm, final Var[] arg, final Expr fn, final FuncType ft,
      final VarScope scp, final StaticContext sctx, final InputInfo ii) {
    super(ii, fn);
    ann = a;
    name = nm;
    args = arg;
    check = ft == null;
    type = (ft == null ? FuncType.arity(args.length) : ft).seqType();
    scope = scp;
    sc = sctx;
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    if(compiled) return;
    compiled = true;

    if(check) {
      final StaticFunc sf = ctx.funcs.get(name, args.length, info, true);
      if(sf == null) throw FUNCUNKNOWN.get(info, name.string());
      ann = sf.ann;
      type = sf.funcType().seqType();
    }

    final int fp = scope.enter(ctx);
    try {
      expr = expr.compile(ctx, scope);
      expr.markTailCalls(null);
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
    }
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope o) throws QueryException {
    compile(ctx);
    return expr.isValue() ? preEval(ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) {
    return new FuncItem(sc, ann == null ? new Ann() : ann, name, args, (FuncType) type.type,
        expr, false, ctx.value, ctx.pos, ctx.size, scope.stackSize());
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope o, final IntObjMap<Var> vs) {
    final Ann a = ann == null ? null : new Ann();
    if(a != null) for(int i = 0; i < ann.size(); i++) a.add(ann.names[i], ann.values[i], info);
    final VarScope scp = new VarScope(sc);
    final Var[] arg = new Var[args.length];
    for(int i = 0; i < arg.length; i++)
      vs.put(args[i].id, arg[i] = scp.newCopyOf(ctx, args[i]));
    final Expr call = expr.copy(ctx, scp, vs);
    return new FuncLit(a, name, arg, call, (FuncType) type.type, scp, sc, info);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX || flag == Flag.FCS;
  }

  /**
   * Creates a function literal for a function that was not yet encountered while parsing.
   * @param nm function name
   * @param ar function arity
   * @param ctx query context
   * @param sctx static context
   * @param ii input info
   * @return function literal
   * @throws QueryException query exception
   */
  public static FuncLit unknown(final QNm nm, final long ar, final QueryContext ctx,
      final StaticContext sctx, final InputInfo ii) throws QueryException {
    final VarScope scp = new VarScope(sctx);
    final Var[] arg = new Var[(int) ar];
    final Expr[] refs = new Expr[arg.length];
    for(int i = 0; i < arg.length; i++) {
      arg[i] = scp.newLocal(ctx, new QNm(QueryText.ARG + (i + 1), ""), SeqType.ITEM_ZM, true);
      refs[i] = new VarRef(ii, arg[i]);
    }
    final TypedFunc call = ctx.funcs.getFuncRef(nm, refs, sctx, ii);
    return new FuncLit(null, nm, arg, call.fun, null, scp, sctx, ii);
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
}
