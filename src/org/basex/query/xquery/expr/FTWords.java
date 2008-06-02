package org.basex.query.xquery.expr;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * FTWords expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTWords extends Single {
  /** Words mode. */
  public enum Mode {
    /** All option. */       ALL,
    /** All words option. */ ALLWORDS,
    /** Any option. */       ANY,
    /** Any words option. */ ANYWORD,
    /** Phrase search. */    PHRASE
  };

  /** Stemming instance. */
  private static final FTStemming STEM = new FTStemming();
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
    return Dbl.iter(len == 0 ? 0 : Scoring.word(ctx.ftitem.length, len));
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
          final byte[] txt = norm(i.str());
          final int oc = cont(ctx, txt);
          len += txt.length * oc;
          o += oc;
        }
        break;
      case ALLWORDS:
        while((i = iter.next()) != null) {
          for(final byte[] txt : split(norm(i.str()), ' ')) {
            final int oc = cont(ctx, txt);
            len += txt.length * oc;
            o += oc;
          }
        }
        break;
      case ANY:
        while((i = iter.next()) != null) {
          final byte[] txt = norm(i.str());
          final int oc = cont(ctx, txt);
          len += txt.length * oc;
          o += oc;
        }
        break;
      case ANYWORD:
        while((i = iter.next()) != null) {
          for(final byte[] txt : split(norm(i.str()), ' ')) {
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
    return o < mn || o > mx ? 0 : len;
  }

  /**
   * Checks if the first token contains the second fulltext term.
   * @param ctx query context
   * @param sub second token
   * @return result of check
   */
  private static int cont(final XQContext ctx, final byte[] sub) {
    final FTOptions opt = ctx.ftopt;

    byte[] tk = ctx.ftitem;
    byte[] sb = opt.wc.bool() ? sub :
      FTCont.norm(new TokenBuilder(), sub).finish();

    if(opt.stem.bool()) {
      sb = STEM.stem(sb);
      tk = STEM.stem(tk);
    }

    if(!opt.diacr.bool()) {
      sb = dc(sb);
      tk = dc(tk);
    }

    if(opt.uc.bool()) {
      sb = uc(sb);
    } else if(!opt.sens.bool() || opt.lc.bool()) {
      sb = lc(sb);
      if(!opt.lc.bool()) tk = lc(tk);
    }
    
    final int sl = sb.length;
    final int tl = tk.length;
    final TokenBuilder st = new TokenBuilder();
    final TokenBuilder tt = new TokenBuilder();

    IntList il = null;
    int p = -1;

    if(opt.wc.bool()) {
      // [CG] performs a wildcard search - support wildcard phrases
      final String cmp = string(sb);
      for(final String s : string(tk).split("[\\p{Punct}\\s]")) {
        p++;
        if(s.matches(cmp)) {
          if(il == null) il = new IntList();
          il.add(p);
        }
      }
    } else {
      // compare tokens character wise
      for(int i = 0; i <= tl - sl; i++) {
        if(i > 0 && (tk[i] < 0 || tk[i] > ' ')) continue;
        while(i < tl - sl && !letterOrDigit(tk[i])) i++;
        p++;
        int s = -1;
        int t = i - 1;
        if(opt.sw == null) {
          while(++t < tl & ++s < sl) {
            byte sv = sb[s];
            byte tv = tk[t];
            if(sv != tv && (sv < 0 || sv > ' ' || tv < 0 || tv > ' ')) break;
          }
        } else {
          // parsing stop words (inefficient)
          while(s < sl) {
            st.reset();
            tt.reset();
            while(++s < sl && letterOrDigit(sb[s])) st.add(sb[s]);
            while(++t < tl && letterOrDigit(tk[t])) tt.add(tk[t]);
            final byte[] s1 = st.finish();
            if(!opt.sw.contains(s1) && !eq(s1, tt.finish())) {
              s = 0;
              break;
            }
          }
        }
        if(s == sl && (t == tl || !letterOrDigit(tk[t]))) {
          if(il == null) il = new IntList();
          il.add(p);
        }
      }
    }

    // "occurs 0 times" ..returning 1?
    if(il != null) ctx.ftselect.add(sub, il);
    return il == null ? 0 : il.size;
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
