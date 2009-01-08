package org.basex.query.xquery.expr;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.FTOpt;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
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
  public FTExpr comp(final XQContext ctx) throws XQException {
    occ[0] = occ[0].comp(ctx);
    occ[1] = occ[1].comp(ctx);
    query = query.comp(ctx);
    return this;
  }

  @Override
  public FTNodeIter iter(final XQContext ctx) throws XQException {
    final int len = contains(ctx);
    return score(len == 0 ? 0 : Scoring.word(len, ctx.ftitem.size()));
  }

  /**
   * Evaluates the fulltext match.
   * @param ctx query context
   * @return length value, used for scoring
   * @throws XQException xquery exception
   */
  private int contains(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(query);
    final long mn = checkItr(ctx.iter(occ[0]));
    final long mx = checkItr(ctx.iter(occ[1]));
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
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }

  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic) 
    throws XQException {

    // if the following conditions yield true, the index is accessed:
    // - no FTTimes option is specified and query is a simple String item
    ic.io &= occ[0].i() && ((Item) occ[0]).itr() == 1 &&
             occ[1].i() && ((Item) occ[1]).itr() == Long.MAX_VALUE &&
             query instanceof Str;
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
  public FTExpr indexEquivalent(final XQContext ctx, final IndexContext ic) {
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
}
