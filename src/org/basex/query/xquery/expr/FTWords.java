package org.basex.query.xquery.expr;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * FTWords expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTWords extends Single {
  /** Fulltext parser. */
  private final FTTokenizer sb = new FTTokenizer();
  /** Words mode. */
  public enum Mode {
    /** All option. */       ALL,
    /** All words option. */ ALLWORDS,
    /** Any option. */       ANY,
    /** Any words option. */ ANYWORD,
    /** Phrase search. */    PHRASE
  };

  /** Minimum and maximum occurrences. */
  Expr[] occ;
  /** Search mode. */
  Mode mode;

  /**
   * Constructor.
   * @param e expression
   * @param m search mode
   * @param o occurrences
   */
  public FTWords(final Expr e, final Mode m, final Expr[] o) {
    super(e);
    mode = m;
    occ = o;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    occ[0] = occ[0].comp(ctx);
    occ[1] = occ[1].comp(ctx);
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final int len = contains(ctx);
    return Dbl.iter(len == 0 ? 0 : Scoring.word(ctx.ftitem.size(), len));
  }

  /**
   * Evaluates the fulltext match.
   * @param ctx query context
   * @return result of matching
   * @throws XQException xquery exception
   */
  private int contains(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(expr);
    final long mn = checkItr(ctx.iter(occ[0]));
    final long mx = checkItr(ctx.iter(occ[1]));
    int len = 0;
    int o = 0;
    Item i;

    switch(mode) {
      case ALL:
        while((i = iter.next()) != null) {
          final byte[] txt = i.str();
          final int oc = cont(ctx, txt);
          if(oc == 0) return 0;
          len += txt.length * oc;
          o += oc;
        }
        break;
      case ALLWORDS:
        while((i = iter.next()) != null) {
          for(final byte[] txt : split(i.str(), ' ')) {
            final int oc = cont(ctx, txt);
            if(oc == 0) return 0;
            len += txt.length * oc;
            o += oc;
          }
        }
        break;
      case ANY:
        while((i = iter.next()) != null) {
          final byte[] txt = i.str();
          final int oc = cont(ctx, txt);
          len += txt.length * oc;
          o += oc;
        }
        break;
      case ANYWORD:
        while((i = iter.next()) != null) {
          for(final byte[] txt : split(i.str(), ' ')) {
            final int oc = cont(ctx, txt);
            len += txt.length * oc;
            o += oc;
          }
        }
        break;
      case PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        boolean more = false;
        while((i = iter.next()) != null) {
          if(more) tb.add(' ');
          tb.add(norm(i.str()));
          more = true;
        }
        final int oc = cont(ctx, tb.finish());
        len += tb.size * oc;
        o += oc;
        break;
    }
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }

  /**
   * Checks if the first token contains the second fulltext term.
   * @param ctx query context
   * @param sub second token
   * @return result of check
   */
  private int cont(final XQContext ctx, final byte[] sub) {
    final FTOptions opt = ctx.ftopt;

    final FTTokenizer tk = ctx.ftitem;
    tk.stem = opt.st.bool();
    tk.dc = opt.dc.bool();
    tk.sens = opt.cs.bool();
    tk.init();

    sb.init(sub);
    sb.stem = tk.stem;
    sb.dc = tk.dc;
    sb.sens = tk.sens;
    sb.uc = opt.uc.bool();
    sb.lc = opt.lc.bool();
    sb.wc = opt.wc.bool();

    IntList il = null;
    while(tk.more()) {
      final int tp = tk.p;
      byte[] t = tk.next();
      boolean f = true;
      boolean c = false;
      sb.init();
      final int tpos = tk.pos;
      while(f && sb.more()) {
        final byte[] s = sb.next();
        if(c) {
          tk.more();
          t = tk.next();
        } else {
          c = true;
        }

        if(opt.sw != null) {
          final boolean s1 = opt.sw.id(s) != 0;
          final boolean s2 = opt.sw.id(t) != 0;
          f = !(s1 ^ s2);
          if(s1 || s2) continue;
        }
        f = sb.wc ? string(t).matches(string(s)) : eq(t, s);
      }

      if(f) {
        if(il == null) il = new IntList();
        il.add(tpos);
      }
      tk.p = tp;
    }
    
    if(il != null) ctx.ftselect.add(sub, il);
    return il == null ? 0 : il.size;
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
