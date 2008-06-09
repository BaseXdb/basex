package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOptions extends Single implements Cloneable {
  /** Sensitive flag. */
  public Bln cs;
  /** Lowercase flag. */
  public Bln lc;
  /** Uppercase flag. */
  public Bln uc;
  /** Diacritics flag. */
  public Bln dc;
  /** Stemming flag (currently ignored). */
  public Bln st;
  /** Thesaurus flag (currently ignored). */
  public Bln ts;
  /** Wildcards flag. */
  public Bln wc;
  /** Stopwords. */
  public Set sw;
  /** Language (currently ignored). */
  public byte[] ln;

  /**
   * Constructor.
   * @param e expression
   */
  public FTOptions(final Expr e) {
    super(e);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    final FTOptions tmp = ctx.ftopt;
    
    final boolean emp = tmp == null;
    if(cs == null) cs = emp ? Bln.FALSE : tmp.cs;
    if(lc == null) lc = emp ? Bln.FALSE : tmp.lc;
    if(uc == null) uc = emp ? Bln.FALSE : tmp.uc;
    if(dc == null) dc = emp ? Bln.FALSE : tmp.dc;
    if(st == null) st = emp ? Bln.FALSE : tmp.st;
    if(ts == null) ts = emp ? Bln.FALSE : tmp.ts;
    if(wc == null) wc = emp ? Bln.FALSE : tmp.wc;
    if(sw == null) sw = emp ? null : tmp.sw;
    if(ln == null) ln = emp ? null : tmp.ln;
    
    if(expr != null) {
      ctx.ftopt = this;
      expr = expr.comp(ctx);
      ctx.ftopt = tmp;
    }
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final FTOptions tmp = ctx.ftopt;
    ctx.ftopt = this;
    final Iter it = ctx.iter(expr);
    ctx.ftopt = tmp;
    return it;
  }

  @Override
  public FTOptions clone() {
    try {
      return (FTOptions) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.startElement(this);
    if(st.bool()) ser.attribute(Token.token(STEMMING), Token.TRUE);
    if(wc.bool()) ser.attribute(Token.token(WILDCARDS), Token.TRUE);
    if(dc.bool()) ser.attribute(Token.token(DIACRITICS), Token.TRUE);
    ser.finishElement();
    expr.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr != null ? expr.toString() : "FTOptions");
    if(st.bool()) sb.append(" " + WITH + " " + STEMMING);
    if(wc.bool()) sb.append(" " + WITH + " " + WILDCARDS);
    if(dc.bool()) sb.append(" " + DIACRITICS + " " + SENSITIVE);
    if(uc.bool()) sb.append(" " + UPPERCASE);
    if(lc.bool()) sb.append(" " + LOWERCASE);
    return sb.toString();
  }
}
