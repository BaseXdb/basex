package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;

/**
 * Typeswitch expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TypeSwitch extends Single {
  /** Return expression. */
  private Expr ts;
  /** Expression list. */
  public Case[] cs;
  /** Variable. */
  private final Var var;
  
  /**
   * Constructor.
   * @param t typeswitch expression
   * @param c case expressions
   * @param v variable
   * @param r default return expression
   */
  public TypeSwitch(final Expr t, final Case[] c, final Var v, final Expr r) {
    super(r);
    ts = t;
    cs = c;
    var = v;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    ts = ts.comp(ctx);
    return this;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final SeqIter seq = SeqIter.get(ctx.iter(ts));
    
    final int s = ctx.vars.size();
    for(final Case c : cs) {
      seq.reset();
      final Iter iter = c.iter(ctx, seq);
      if(iter != null) return iter;
    }
    if(var != null) ctx.vars.add(var.bind(seq.finish(), ctx));
    final SeqIter sb = SeqIter.get(ctx.iter(expr));
    ctx.vars.reset(s);
    return sb;
  }

  @Override
  public boolean usesPos(final QueryContext ctx) {
    for(final Case c : cs) if(c.usesPos(ctx)) return true;
    return ts.usesPos(ctx);
  }

  @Override
  public int countVar(final Var v) {
    if(v == null) return 1;
    if(!v.visible(var)) return 0;
    int c = 0;
    for(final Case s : cs) c += s.countVar(v);
    return c + ts.countVar(v);
  }

  @Override
  public Expr removeVar(final Var v) {
    if(!v.visible(var)) return this;
    for(int c = 0; c < cs.length; c++) cs[c] = cs[c].removeVar(v);
    ts = ts.removeVar(v);
    return this;
  }
  
  @Override
  public Return returned(final QueryContext ctx) {
    final Return t = cs[0].returned(ctx);
    for(int l = 1; l < cs.length; l++) {
      if(t != cs[l].returned(ctx)) return Return.SEQ;
    }
    return t == ts.returned(ctx) ? t : Return.SEQ;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(TYPESWITCH + "(" + ts + ") ");
    for(int l = 0; l != cs.length; l++) {
      if(l != 0) sb.append(", ");
      sb.append(cs[l]);
    }
    return sb + " " + DEFAULT + " " + expr;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    if(var != null) ser.attribute(VAR, var.name.str());
    for(final Case c : cs) c.plan(ser);
    ts.plan(ser);
    expr.plan(ser);
    ser.closeElement();
  }
}
