package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
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
  /** Fulltext token. */
  private byte[] tok;

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
    final Iter iter = ctx.iter(query);
    int len = 0;
    int o = 0;
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
          final int count = ctx.ftopt.sb.count(); 
          o += count == 0 ? 0 : oc / count;
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
        final TokenBuilder txt = new TokenBuilder();
        while((i = iter.next()) != null) {
          if(txt.size != 0) txt.add(' ');
          txt.add(i.str());
        }
        final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt.finish());
        len += txt.size;
        o += oc / ctx.ftopt.sb.count();
        break;
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
    // if the following conditions yield true, the index is accessed:
    // - no FTTimes option is specified and query is a simple String item
    ic.io &= occ == null && query instanceof Str;
    if(!ic.io) return;
    
    tok = ((Str) query).str();
    final FTTokenizer sb = ctx.ftopt.sb;
    sb.text = tok;
    sb.lc = ctx.ftopt.is(FTOpt.LC);
    sb.uc = ctx.ftopt.is(FTOpt.UC);
    sb.fz = ctx.ftopt.is(FTOpt.FZ);
    sb.wc = ctx.ftopt.is(FTOpt.WC);
    sb.cs = ctx.ftopt.is(FTOpt.CS);
    
    // index size is incorrect for phrases
    while(ic.is != 0 && sb.more()) ic.is = Math.min(ic.is, ic.data.nrIDs(sb));
    ic.iu = true;
  }

  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic) {
    return new FTIndex(ic.data, tok);
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
