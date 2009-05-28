package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.ft.FTOpt.FTMode;
import org.basex.query.item.FTNodeItem;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;

/**
 * FTWords expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTWords extends FTExpr {
  /** Expression list. */
  private Expr query;
  /** Minimum and maximum occurrences. */
  private final Expr[] occ;
  /** Search mode. */
  private final FTMode mode;
  /** Single word. */
  private byte[] word;

  /**
   * Constructor.
   * @param e expression
   * @param m search mode
   * @param o occurrences
   */
  public FTWords(final Expr e, final FTMode m, final Expr[] o) {
    query = e;
    mode = m;
    occ = o;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    if(occ != null) {
      for(int o = 0; o < occ.length; o++) occ[o] = occ[o].comp(ctx);
    }
    query = query.comp(ctx);
    if(query instanceof Str) word = ((Str) query).str();
    return this;
  }

  @Override
  public FTNodeItem atomic(final QueryContext ctx) throws QueryException {
    final int len = contains(ctx);
    double s = len == 0 ? 0 : ctx.score.word(len, ctx.ftitem.size());

    // evaluate weight
    final Expr w = ctx.ftopt.weight;
    if(w != null) {
      final double d = checkDbl(w, ctx);
      if(Math.abs(d) > 1000) Err.or(FTWEIGHT, d);
      s *= d;
    }
    return new FTNodeItem(s);
  }

  /**
   * Evaluates the full-text match.
   * @param ctx query context
   * @return length value, used for scoring
   * @throws QueryException xquery exception
   */
  private int contains(final QueryContext ctx) throws QueryException {
    int len = 0;
    int o = 0;

    if(mode == FTMode.ANY && word != null) {
      // speed up default case...
      final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftselect, word);
      len = word.length;
      final int c = ctx.ftopt.qu.count();
      o += c > 0 ? oc / c : 0;
    } else {
      final Iter iter = ctx.iter(query);
      byte[] it;
  
      switch(mode) {
        case ALL:
          while((it = nextStr(iter)) != null) {
            final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftselect, it);
            if(oc == 0) return 0;
            len += it.length;
            o += oc / ctx.ftopt.qu.count();
          }
          break;
        case ALLWORDS:
          while((it = nextStr(iter)) != null) {
            for(final byte[] txt : split(it, ' ')) {
              final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftselect, txt);
              if(oc == 0) return 0;
              len += txt.length;
              o += oc;
            }
          }
          break;
        case ANY:
          while((it = nextStr(iter)) != null) {
            final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftselect, it);
            len += it.length;
            final int c = ctx.ftopt.qu.count();
            o += c > 0 ? oc / c : 0;
          }
          break;
        case ANYWORD:
          while((it = nextStr(iter)) != null) {
            for(final byte[] txt : split(it, ' ')) {
              final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftselect, txt);
              len += txt.length;
              o += oc;
            }
          }
          break;
        case PHRASE:
          final TokenBuilder tb = new TokenBuilder();
          while((it = nextStr(iter)) != null) {
            tb.add(it);
            tb.add(' ');
          }
          final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftselect,
              tb.finish());
          len += tb.size;
          final int c = ctx.ftopt.qu.count();
          o += c > 0 ? oc / c : 0;
          break;
      }
    }

    long mn = 1;
    long mx = Long.MAX_VALUE;
    if(occ != null) {
      mn = checkItr(occ[0], ctx);
      mx = checkItr(occ[1], ctx);
    }
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }

  /**
   * Checks if the next item is a string.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  private byte[] nextStr(final Iter iter) throws QueryException {
    final Item it = iter.next();
    if(it == null) return null;
    if(!it.s() && !it.u()) Err.type(info(), Type.STR, it);
    return it.str();
  }
  
  @Override
  public boolean indexAccessible(final QueryContext ctx,
      final IndexContext ic) {
    /*
     * If the following conditions yield true, the index is accessed:
     * - no FTTimes option is specified and query is a simple String item
     * - case sensitivity, diacritics and stemming flags comply with index
     * - no stop words are specified
     * - if wildcards are specified, the full-text index is a trie
     */
    final MetaData md = ic.data.meta;
    final FTOpt fto = ctx.ftopt;
    if(occ != null || word == null || word.length == 0 ||
        md.ftcs != fto.is(FTOpt.CS) || md.ftdc != fto.is(FTOpt.DC) ||
        md.ftst != fto.is(FTOpt.ST) || fto.sw != null ||
        (fto.is(FTOpt.WC) && md.ftfz)) return false;
    
    // index size is incorrect for phrases
    final Tokenizer ft = new Tokenizer(word, fto);
    ic.is = Integer.MAX_VALUE;
    while(ic.is != 0 && ft.more()) ic.is = Math.min(ic.is, ic.data.nrIDs(ft));
    return true;
  }

  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic) {
    return new FTIndex(ic.data, word);
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    query.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return query.toString();
  }

  @Override
  public boolean usesExclude() {
    return occ != null;
  }
}
