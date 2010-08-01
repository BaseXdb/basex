package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Expr;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.util.format.DateFormatter;
import org.basex.query.util.format.IntFormatter;
import org.basex.query.util.format.NumFormatter;

/**
 * Formatting functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNFormat extends Fun {
  /**
   * Constructor.
   * @param i query info
   * @param f function definition
   * @param e arguments
   */
  protected FNFormat(final QueryInfo i, final FunDef f, final Expr... e) {
    super(i, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FORMINT: return formatInteger(ctx);
      case FORMNUM: return formatNumber(ctx);
      case FORMDTM: return formatDate(Type.DTM, ctx);
      case FORMDAT: return formatDate(Type.DAT, ctx);
      case FORMTIM: return formatDate(Type.TIM, ctx);
      default:      return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FORMINT: return expr[0].empty() ? atomic(ctx) : this;
      default:      return this;
    }
  }

  /**
   * Returns a formatted integer.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatInteger(final QueryContext ctx) throws QueryException {
    final String pic = string(checkStr(expr[1], ctx));
    if(expr[0].empty()) return Str.ZERO;

    final byte[] lang = expr.length == 2 ? EMPTY : checkStr(expr[2], ctx);
    final long num = checkItr(expr[0], ctx);

    return Str.get(IntFormatter.format(num, pic, string(lang)));
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatNumber(final QueryContext ctx) throws QueryException {
    // evaluate arguments
    Item it = expr[0].atomic(ctx);
    if(it == null) it = Dbl.NAN;
    else if(!it.unt() && !it.num()) numError(info(), it);

    final String pic = string(checkStr(expr[1], ctx));
    if(expr.length == 3) error(FORMNUM, expr[2]);

    return Str.get(NumFormatter.format(this, it, pic));
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @param type input type
   * @return string
   * @throws QueryException query exception
   */
  private Item formatDate(final Type type, final QueryContext ctx)
      throws QueryException {

    final Item it = expr[0].atomic(ctx);
    if(it == null) return Seq.EMPTY;
    final Date date = (Date) checkType(it, type);
    final String pic = string(checkStr(expr[1], ctx));
    final byte[] lng = expr.length == 5 ? checkStr(expr[2], ctx) : EMPTY;
    final byte[] cal = expr.length == 5 ? checkStr(expr[3], ctx) : EMPTY;
    final byte[] plc = expr.length == 5 ? checkStr(expr[4], ctx) : EMPTY;
    return Str.get(DateFormatter.format(this, date, pic, lng, cal, plc));
  }
}
