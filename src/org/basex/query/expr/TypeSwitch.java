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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class TypeSwitch extends Expr {
  /** Default return expression. */
  private Expr ret;
  /** Typeswitch expression. */
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
    ret = r;
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
    final Iter seq = SeqIter.get(ctx.iter(ts));
    
    final int s = ctx.vars.size();
    for(final Case c : cs) {
      seq.reset();
      final Iter iter = c.iter(ctx, seq);
      if(iter != null) return iter;
    }
    if(var != null) ctx.vars.add(var.bind(seq.finish(), ctx).clone());
    final Iter si = SeqIter.get(ctx.iter(ret));
    ctx.vars.reset(s);
    return si;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    if(u == Use.VAR) return true;
    for(final Case c : cs) if(c.uses(u, ctx)) return true;
    return ts.uses(u, ctx) || ret.uses(u, ctx);
  }

  @Override
  public Expr remove(final Var v) {
    if(!v.visible(var)) return this;
    for(int c = 0; c < cs.length; c++) cs[c] = cs[c].remove(v);
    ts = ts.remove(v);
    return this;
  }
  
  @Override
  public Return returned(final QueryContext ctx) {
    final Return t = cs[0].returned(ctx);
    for(int l = 1; l < cs.length; l++) {
      if(t != cs[l].returned(ctx)) return Return.SEQ;
    }
    return t == ret.returned(ctx) ? t : Return.SEQ;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(TYPESWITCH + "(" + ts + ") ");
    for(int l = 0; l != cs.length; l++) {
      if(l != 0) sb.append(", ");
      sb.append(cs[l]);
    }
    return sb + " " + DEFAULT + " " + ret;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    if(var != null) ser.attribute(VAR, var.name.str());
    for(final Case c : cs) c.plan(ser);
    ts.plan(ser);
    ret.plan(ser);
    ser.closeElement();
  }
}
