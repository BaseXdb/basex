package org.basex.query.xquery.expr;

import static org.basex.query.QueryTokens.*;
import org.basex.data.Serializer;
import org.basex.query.FTOpt;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOptions extends Single {
  /** FTOptions. */
  public FTOpt opt;

  /**
   * Constructor.
   * @param e expression
   * @param o ft options
   */
  public FTOptions(final Expr e, final FTOpt o) {
    super(e);
    opt = o;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    expr = expr.comp(ctx);
    ctx.ftopt = tmp;
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = opt;
    final Iter it = ctx.iter(expr);
    ctx.ftopt = tmp;
    return it;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.startElement(this);
    if(opt.st) ser.attribute(Token.token(STEMMING), Token.TRUE);
    if(opt.wc) ser.attribute(Token.token(WILDCARDS), Token.TRUE);
    if(opt.fz) ser.attribute(Token.token(FUZZY), Token.TRUE);
    if(opt.dc) ser.attribute(Token.token(DIACRITICS), Token.TRUE);
    if(opt.uc) ser.attribute(Token.token(UPPERCASE), Token.TRUE);
    if(opt.lc) ser.attribute(Token.token(LOWERCASE), Token.TRUE);
    ser.finishElement();
    expr.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr != null ? expr.toString() : "FTOptions");
    if(opt.st) sb.append(" " + WITH + " " + STEMMING);
    if(opt.wc) sb.append(" " + WITH + " " + WILDCARDS);
    if(opt.fz) sb.append(" " + WITH + " " + FUZZY);
    if(opt.dc) sb.append(" " + DIACRITICS + " " + SENSITIVE);
    if(opt.uc) sb.append(" " + UPPERCASE);
    if(opt.lc) sb.append(" " + LOWERCASE);
    return sb.toString();
  }
}
