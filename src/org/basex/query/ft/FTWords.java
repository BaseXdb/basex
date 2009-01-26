package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.ft.FTOpt.FTMode;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.Scoring;
import org.basex.util.TokenBuilder;

/**
 * FTWords expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    final int len = contains(ctx);
    return score(len == 0 ? 0 : Scoring.word(len, ctx.ftitem.size()));
  }

  /**
   * Evaluates the fulltext match.
   * @param ctx query context
   * @return length value, used for scoring
   * @throws QueryException xquery exception
   */
  private int contains(final QueryContext ctx) throws QueryException {
    int len = 0;
    int o = 0;

    if(mode == FTMode.ANY && word != null) {
      // speed up default case...
      final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, word);
      len = word.length;
      o += oc / ctx.ftopt.sb.count();
    } else {
      final Iter iter = ctx.iter(query);
      Item i;
  
      switch(mode) {
        case ALL:
          while((i = iter.next()) != null) {
            final byte[] txt = i.str();
            final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
            if(oc == 0) return 0;
            len += txt.length;
            o += oc / ctx.ftopt.sb.count();
          }
          break;
        case ALLWORDS:
          while((i = iter.next()) != null) {
            for(final byte[] txt : split(i.str(), ' ')) {
              final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
              if(oc == 0) return 0;
              len += txt.length;
              o += oc;
            }
          }
          break;
        case ANY:
          while((i = iter.next()) != null) {
            final byte[] txt = i.str();
            final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
            len += txt.length;
            o += oc / ctx.ftopt.sb.count();
          }
          break;
        case ANYWORD:
          while((i = iter.next()) != null) {
            for(final byte[] txt : split(i.str(), ' ')) {
              final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
              len += txt.length;
              o += oc;
            }
          }
          break;
        case PHRASE:
          final TokenBuilder tb = new TokenBuilder();
          while((i = iter.next()) != null) {
            tb.add(i.str());
            tb.add(' ');
          }
          final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, tb.finish());
          len += tb.size;
          o += oc / ctx.ftopt.sb.count();
          break;
      }
    }

    long mn = 1;
    long mx = Long.MAX_VALUE;
    if(occ != null) {
      mn = checkItr(ctx.iter(occ[0]));
      mx = checkItr(ctx.iter(occ[1]));
    }
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }

  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic) {
    /*
     * If the following conditions yield true, the index is accessed:
     * - no FTTimes option is specified and query is a simple String item
     * - case sensitivity, diacritics and stemming flags comply with index
     * - no stop words are specified
     * - if wildcards are specified, the fulltext index is a trie
     */
    final MetaData md = ic.data.meta;
    final FTOpt fto = ctx.ftopt;
    ic.io &= occ == null && word != null &&
      md.ftcs == fto.is(FTOpt.CS) && md.ftdc == fto.is(FTOpt.DC) &&
      md.ftst == fto.is(FTOpt.ST) && fto.sw == null &&
      (!fto.is(FTOpt.WC) || !md.ftfz);
    if(!ic.io) return;
    
    // index size is incorrect for phrases
    final FTTokenizer ft = new FTTokenizer(word, fto);
    while(ic.is != 0 && ft.more()) ic.is = Math.min(ic.is, ic.data.nrIDs(ft));
    ic.iu = true;
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
