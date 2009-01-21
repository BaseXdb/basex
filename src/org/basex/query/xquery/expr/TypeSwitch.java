package org.basex.query.xquery.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Var;

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
  public Expr comp(final XQContext ctx) throws XQException {
    ts = ts.comp(ctx);
    return this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
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
  public boolean usesPos(final XQContext ctx) {
    for(final Case c : cs) if(c.usesPos(ctx)) return true;
    return ts.usesPos(ctx);
  }

  @Override
  public boolean usesVar(final Var v) {
    if(v == null) return true;
    if(!v.visible(var)) return false;
    for(final Case c : cs) if(c.usesVar(v)) return true;
    return ts.usesVar(v);
  }

  @Override
  public Expr removeVar(final Var v) {
    if(!v.visible(var)) return this;
    for(int c = 0; c < cs.length; c++) cs[c] = cs[c].removeVar(v);
    ts = ts.removeVar(v);
    return this;
  }
  
  @Override
  public Type returned(final XQContext ctx) {
    final Type t = cs[0].returned(ctx);
    for(int l = 1; l < cs.length; l++) if(t != cs[l].returned(ctx)) return null;
    return t == ts.returned(ctx) ? t : null;
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
