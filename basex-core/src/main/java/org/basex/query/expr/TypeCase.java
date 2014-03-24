package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.Iter;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Case expression for typeswitch.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class TypeCase extends Single {
  /** Variable. */
  final Var var;
  /** Matched sequence types. */
  private final SeqType[] types;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   * @param ts sequence types this case matches, the empty array means {@code default}
   * @param r return expression
   */
  public TypeCase(final InputInfo ii, final Var v, final SeqType[] ts, final Expr r) {
    super(ii, r);
    var = v;
    types = ts;
  }

  @Override
  public TypeCase compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    return compile(ctx, scp, null);
  }

  /**
   * Compiles the expression.
   * @param ctx query context
   * @param scp variable scope
   * @param v value to be bound
   * @return resulting item
   * @throws QueryException query exception
   */
  TypeCase compile(final QueryContext ctx, final VarScope scp, final Value v)
      throws QueryException {
    Value val = var != null && v != null ? var.checkType(v, ctx, info, true) : null;
    try {
      super.compile(ctx, scp);
      if(val != null) {
        Expr inlined = expr.inline(ctx, scp, var, val);
        if(inlined != null) expr = inlined;
      }
    } catch(final QueryException ex) {
      // replace original expression with error
      expr = FNInfo.error(ex, expr.type());
    }
    type = expr.type();
    return this;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e) {
    try {
      return super.inline(ctx, scp, v, e);
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe, expr.type());
      return this;
    }
  }

  @Override
  public TypeCase copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Var v = var == null ? null : scp.newCopyOf(ctx, var);
    if(var != null) vs.put(var.id, v);
    return new TypeCase(info, v, types.clone(), expr.copy(ctx, scp, vs));
  }

  /**
   * Checks if the given value matches this case.
   * @param val value to be matched
   * @return {@code true} if it matches, {@code false} otherwise
   */
  public boolean matches(final Value val) {
    if(types.length == 0) return true;
    for(final SeqType t : types) if(t.instance(val)) return true;
    return false;
  }

  /**
   * Evaluates the expression.
   * @param ctx query context
   * @param seq sequence to be checked
   * @return resulting item
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext ctx, final Value seq) throws QueryException {
    if(!matches(seq)) return null;

    if(var == null) return ctx.iter(expr);
    ctx.set(var, seq, info);
    return ctx.value(expr).iter();
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    if(types.length == 0) {
      e.add(planAttr(Token.token(DEFAULT), Token.TRUE));
    } else {
      final byte[] or = { ' ', '|', ' ' };
      final ByteList bl = new ByteList();
      for(final SeqType t : types) {
        if(!bl.isEmpty()) bl.add(or);
        bl.add(Token.token(t.toString()));
      }
      e.add(planAttr(Token.token(TYPE), bl.toArray()));
    }
    if(var != null) e.add(planAttr(VAR, Token.token(var.toString())));
    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(types.length == 0 ? DEFAULT : CASE);
    if(var != null) {
      tb.add(' ').add(var.toString());
      if(types.length != 0) tb.add(' ').add(AS);
    }
    if(types.length != 0) {
      for(int i = 0; i < types.length; i++) {
        if(i > 0) tb.add(" |");
        tb.add(' ').add(types[i].toString());
      }
    }
    return tb.add(' ' + RETURN + ' ' + expr).toString();
  }

  @Override
  public void markTailCalls(final QueryContext ctx) {
    expr.markTailCalls(ctx);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && (var == null || visitor.declared(var));
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }
}
