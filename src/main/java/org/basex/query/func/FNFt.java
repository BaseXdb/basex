package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.ft.FTIndexAccess;
import org.basex.query.ft.FTWords;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.AtomType;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.DataBuilder;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.XMLToken;
import org.basex.util.ft.FTOpt;

/**
 * Full-text functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class FNFt extends Fun {
  /** Marker element. */
  private static final byte[] MARK = token("mark");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNFt(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case SEARCH:  return search(ctx);
      case SCORE:   return score(ctx);
      case MARK:    return mark(ctx, false);
      case EXTRACT: return mark(ctx, true);
      default:      return super.iter(ctx);
    }
  }

  /**
   * Performs the mark function.
   * @param ctx query context
   * @param ex extract flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter mark(final QueryContext ctx, final boolean ex)
      throws QueryException {

    byte[] m = MARK;
    int l = ex ? 150 : Integer.MAX_VALUE;

    if(expr.length > 1) {
      // name of the marker element; default is <mark/>
      m = checkStr(expr[1], ctx);
      if(!XMLToken.isQName(m)) Err.value(input, AtomType.QNM, m);
    }
    if(expr.length > 2) {
      l = (int) checkItr(expr[2], ctx);
    }
    final byte[] mark = m;
    final int len = l;

    return new Iter() {
      final FTPosData ftd = new FTPosData();
      Iter ir;
      ItemIter ii;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ii != null) {
            final Item it = ii.next();
            if(it != null) return it;
            ii = null;
          }
          final FTPosData tmp = ctx.ftpos;
          ctx.ftpos = ftd;
          if(ir == null) ir = ctx.iter(expr[0]);
          final Item it = ir.next();
          if(it != null) {
            ii = DataBuilder.mark(checkDBNode(it), mark, len, ctx);
          }
          ctx.ftpos = tmp;
          if(it == null) return null;
        }
      }
    };
  }

  /**
   * Performs the score function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter score(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final Iter iter = expr[0].iter(ctx);

      @Override
      public Dbl next() throws QueryException {
        final Item item = iter.next();
        return item == null ? null : Dbl.get(item.score());
      }
    };
  }

  /**
   * Performs the search function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  Iter search(final QueryContext ctx) throws QueryException {
    return search(checkDBNode(checkItem(expr[0], ctx)).data,
        checkStr(expr[1], ctx), this, ctx);
  }

  /**
   * Performs an index-based search.
   * @param data data reference
   * @param str search string
   * @param fun calling function
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  static Iter search(final Data data, final byte[] str, final Fun fun,
      final QueryContext ctx) throws QueryException {

    final IndexContext ic = new IndexContext(ctx, data, null, true);
    if(!data.meta.ftindex) NOIDX.thrw(fun.input, fun);

    final FTOpt tmp = ctx.ftopt;
    ctx.ftopt = new FTOpt();
    ctx.ftopt.set(CS, data.meta.casesens);
    ctx.ftopt.set(DC, data.meta.diacritics);
    ctx.ftopt.set(ST, data.meta.stemming);
    ctx.ftopt.ln = data.meta.language;
    final FTWords words = new FTWords(fun.input, ic.data, Str.get(str), ctx);
    ctx.ftopt = tmp;
    return new FTIndexAccess(fun.input, words, ic).iter(ctx);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && def == FunDef.SEARCH || super.uses(u);
  }
}
