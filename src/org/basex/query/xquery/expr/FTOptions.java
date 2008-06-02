package org.basex.query.xquery.expr;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOptions extends Single implements Cloneable {
  /** Sensitive flag. */
  public Bln sens;
  /** Lowercase flag. */
  public Bln lc;
  /** Uppercase flag. */
  public Bln uc;
  /** Diacritics flag. */
  public Bln diacr;
  /** Stemming flag (currently ignored). */
  public Bln stem;
  /** Thesaurus flag (currently ignored). */
  public Bln thes;
  /** Wildcards flag. */
  public Bln wc;
  /** Stopwords flag. */
  public TokenList sw;
  /** Language (currently ignored). */
  public byte[] lng;

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
    
    if(tmp == null) {
      sens = Bln.FALSE;
      lc = Bln.FALSE;
      uc = Bln.FALSE;
      diacr = Bln.FALSE;
      stem = Bln.FALSE;
      thes = Bln.FALSE;
      wc = Bln.FALSE;
    } else {
      if(sens == null) sens = tmp.sens;
      if(lc == null) lc = tmp.lc;
      if(uc == null) uc = tmp.uc;
      if(diacr == null) diacr = tmp.diacr;
      if(stem == null) stem = tmp.stem;
      if(thes == null) thes = tmp.thes;
      if(wc == null) wc = tmp.wc;
      if(sw == null) sw = tmp.sw;
      if(lng == null) lng = tmp.lng;
    }
    
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
    if(stem.bool()) ser.attribute(Token.token("stemming"), Token.TRUE);
    if(wc.bool()) ser.attribute(Token.token("wildcards"), Token.TRUE);
    ser.finishElement();
    expr.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr != null ? expr.toString() : "FTOptions");
    if(stem.bool()) sb.append(" with stemming");
    if(wc.bool()) sb.append(" with wildcards");
    return sb.toString();
  }
}
