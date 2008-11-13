package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import org.basex.index.FTTokenizer;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.FTOpt;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.FTIndexItem;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.Step;
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
  public Expr query;
  /** Minimum and maximum occurrences. */
  final Expr[] occ;
  /** Search mode. */
  final FTMode mode;
  /** FTNodeIter collecting the index results. */
  private FTNodeIter ftni = null;
  /** Flag for loading text from disk. */
  private boolean lt;

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
    occ[0] = ctx.comp(occ[0]);
    occ[1] = ctx.comp(occ[1]);
    query = ctx.comp(query);
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    int len; // = contains(ctx);
    if (query instanceof FTIndex) {
      if (ftni == null)
        ftni = (FTNodeIter) ctx.iter(query);
      if (ftni == Iter.EMPTY) return Iter.EMPTY;
      FTIndexItem ftii = ftni.next();
      final DNode dn = (DNode) ctx.coll(null).next();
      ctx.item = new DNode(dn.data, ftii.ftnode.getPre(), null, Type.TXT);
      ctx.ftitem = lt ? new FTTokenizer(dn.data.text(ftii.ftnode.getPre())) 
        : new FTTokenizer();
      ctx.ftpos.setPos(ftii.ftnode.convertPos(), ftii.ftnode.size);
      len = ftii.ftnode.size;
    } else {
      len = contains(ctx);
    }
    //final int len = contains(ctx);
    return Dbl.iter(len == 0 ? 0 : Scoring.word(ctx.ftitem.size(), len));
  }

  /**
   * Evaluates the fulltext match.
   * @param ctx query context
   * @return result of matching
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
          len += txt.length * oc;
          o += oc / ctx.ftopt.sb.count();
        }
        break;
      case ALLWORDS:
        while((i = iter.next()) != null) {
          for(final byte[] txt : split(i.str(), ' ')) {
            final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
            if(oc == 0) return 0;
            len += txt.length * oc;
            o += oc;
          }
        }
        break;
      case ANY:
        while((i = iter.next()) != null) {
          final byte[] txt = i.str();
          final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
          len += txt.length * oc;
          o += oc / ctx.ftopt.sb.count();
        }
        break;
      case ANYWORD:
        while((i = iter.next()) != null) {
          for(final byte[] txt : split(i.str(), ' ')) {
            final int oc = ctx.ftopt.contains(ctx.ftitem, ctx.ftpos, txt);
            len += txt.length * oc;
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
        len += txt.size * oc;
        o += oc / ctx.ftopt.sb.count();
        break;
    }
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexInfo ii, 
      final Step curr) throws XQException {
    DNode dn = (DNode) ctx.coll(null).next();
    ii.ui = dn.data.meta.ftxindex && 
    dn.data.meta.ftcs == ctx.ftopt.is(FTOpt.CS) && 
    dn.data.meta.ftdc == ctx.ftopt.is(FTOpt.DC) && 
    dn.data.meta.ftst == ctx.ftopt.is(FTOpt.ST) &&
    ctx.ftopt.sw == null && (!ctx.ftopt.is(FTOpt.WC) 
    || !dn.data.meta.ftfz) && checkItr(ctx.iter(occ[0])) == 1 && 
    checkItr(ctx.iter(occ[1])) == Long.MAX_VALUE && 
    mode == FTMode.ANY && query instanceof Str;
    if (!ii.ui) {
      ii.seq = true;
      return this;
    }
    
    ii.seq = false;
    lt = ctx.ftpos != null && (ctx.ftpos.content || ctx.ftpos.different 
        || ctx.ftpos.end || ctx.ftpos.ordered || ctx.ftpos.same 
        || ctx.ftpos.start || ctx.ftpos.dunit != null 
        || ctx.ftpos.wunit != null || ii.lt);
    //int i = 0;
    //while(fto.sb.more()) {
    /*
    while(ctx.ftitem.more()) {
     final int n = dn.data.nrIDs(ctx.ftitem);
     // final int n = ctx.item.data.nrIDs(fto.sb);
     if(n == 0) {
       ii.indexSize = 0;
       return Bln.FALSE;
     }
     i = Math.max(i, n);
    }
    ii.indexSize = i;
    */
    if (query instanceof Str) {
      Str s = (Str) query;
      return s.indexEquivalent(ctx, ii, curr);
    }
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NS, timer());
    query.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return query.toString();
  }
}
