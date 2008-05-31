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
  /** All option. */
  public static final int ALL = 0;
  /** All words option. */
  public static final int ALLWORDS = 1;
  /** Any option. */
  public static final int ANY = 2;
  /** Any words option. */
  public static final int ANYWORD = 3;
  /** Phrase search option. */
  public static final int PHRASE = 4;

  /** Minimum and maximum occurrences. */
  Expr[] occ;
  /** Search mode. */
  int mode;

  /**
   * Constructor.
   * @param e expression
   * @param m search mode
   * @param o occurrences
   */
  public FTWords(final Expr e, final int m, final Expr[] o) {
    super(e);
    mode = m;
    occ = o;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final int len = contains(ctx);
    return Dbl.iter(len == 0 ? 0 : Scoring.word(ctx.ftitem.length, len));
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    occ[0] = occ[0].comp(ctx);
    occ[1] = occ[1].comp(ctx);
    return super.comp(ctx);
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

    byte[] tok = ctx.ftitem;
    byte[] sb = opt.wildcards ? sub :
      FTCont.norm(new TokenBuilder(), sub).finish();

    if(!opt.diacritics) {
      sb = dc(sb);
      tok = dc(tok);
    }

    if(opt.uppercase) {
      sb = uc(sb);
    } else if(!opt.sensitive || opt.lowercase) {
      sb = lc(sb);
      if(!opt.lowercase) tok = lc(tok);
    }

    final int sl = sb.length;
    final int tl = tok.length;

    IntList il = null;
    int p = -1;

    if(opt.wildcards) {
      // performs a wildcard search
      final String cmp = string(sb);
      for(final String s : string(tok).split("[^A-Za-z0-9_]")) {
        p++;
        if(s.matches(cmp)) {
          if(il == null) il = new IntList();
          il.add(p);
        }
      }
    } else {
      // compare tokens character wise
      for(int i = 0; i <= tl - sl; i++) {
        if(i > 0 && tok[i] > ' ') continue;
        while(i < tl - sl && !letterOrDigit(tok[i])) i++;
        p++;
        int s = -1;
        int t = i - 1;
        if(opt.stopwords == null) {
          while((++t < tl & ++s < sl) && sb[s] == tok[t]);
        } else {
          // parsing stop words (inefficient)
          while(s < sl) {
            final TokenBuilder st1 = new TokenBuilder();
            final TokenBuilder st2 = new TokenBuilder();
            while(++s < sl && letterOrDigit(sb[s])) st1.add(sb[s]);
            while(++t < tl && letterOrDigit(tok[t])) st2.add(tok[t]);
            final byte[] s1 = st1.finish();
            if(!opt.stopwords.contains(s1) && !eq(s1, st2.finish())) {
              s = 0;
              break;
            }
          }
        }
        if(s == sl && (t == tl || !letterOrDigit(tok[t])) ||
            opt.stemming) {
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
