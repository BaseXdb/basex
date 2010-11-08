package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.FTIndexIterator;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTItem;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;

/**
 * FTWords expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTWords extends FTExpr {
  /** Words mode. */
  public enum FTMode {
    /** All option. */
    M_ALL,
    /** All words option. */
    M_ALLWORDS,
    /** Any option. */
    M_ANY,
    /** Any words option. */
    M_ANYWORD,
    /** Phrase search. */
    M_PHRASE
  }

  /** Full-text tokenizer. */
  FTTokenizer ftt;
  /** Data reference. */
  Data data;
  /** Single word. */
  byte[] txt;
  /** Fast evaluation. */
  boolean fast;

  /** All matches. */
  FTMatches all = new FTMatches((byte) 0);
  /** Flag for first evaluation. */
  boolean first;

  /** Expression list. */
  private Expr query;
  /** Minimum and maximum occurrences. */
  private Expr[] occ;
  /** Search mode. */
  private FTMode mode;
  /** Token number. */
  private byte tokNum;
  /** Standard evaluation. */
  private boolean simple;

  /**
   * Sequential constructor.
   * @param ii input info
   * @param e expression
   * @param m search mode
   * @param o occurrences
   */
  public FTWords(final InputInfo ii, final Expr e, final FTMode m,
      final Expr[] o) {
    super(ii);
    query = e;
    mode = m;
    occ = o;
  }

  /**
   * Index constructor.
   * @param ii input info
   * @param d data reference
   * @param t text
   * @param f fast evaluation
   */
  public FTWords(final InputInfo ii, final Data d, final byte[] t,
      final boolean f) {
    super(ii);
    data = d;
    txt = t;
    fast = f;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    if(occ != null) {
      for(int o = 0; o < occ.length; ++o)
        occ[o] = occ[o].comp(ctx);
    }
    query = query.comp(ctx);
    if(query instanceof Str) txt = ((Str) query).atom();
    simple = mode == FTMode.M_ANY && txt != null && occ == null;
    fast = ctx.ftfast && occ == null;
    ftt = new FTTokenizer(ctx.context.prop, this);
    return this;
  }

  @Override
  public FTItem item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(tokNum == 0) tokNum = ++ctx.ftoknum;
    all.reset(tokNum);

    final int c = contains(ctx);
    if(c == 0) all.size = 0;
    // scoring: pass on number of tokens
    return new FTItem(all, fast || c == 0 ? 0 : ctx.score.word(c,
        ctx.fttoken.count()));
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      /** Index iterator. */
      FTIndexIterator iat;

      @Override
      public FTItem next() {
        if(iat == null) {
          final FTLexer lex = new FTLexer(txt, ctx.context.prop, ctx.ftopt);
          int d = 0;
          while(lex.hasNext()) {
            final byte[] token = lex.nextToken();
            if(ctx.ftopt.sw != null && ctx.ftopt.sw.id(token) != 0) {
              ++d;
            } else {
              final FTIndexIterator i = (FTIndexIterator) data.ids(lex);
              iat = iat == null ? i : FTIndexIterator.phrase(iat, i, ++d);
              d = 0;
            }
          }
          iat.setTokenNum(++ctx.ftoknum);
        }
        return iat.more() ? new FTItem(iat.matches(), data, iat.next(),
            txt.length, iat.indexSize(), iat.score()) : null;
      }
    };
  }

  /**
   * Evaluates the full-text match.
   * @param ctx query context
   * @return length value, used for scoring
   * @throws QueryException query exception
   */
  private int contains(final QueryContext ctx) throws QueryException {
    // speed up default case
    first = true;
    if(simple) return ftt.contains(txt, ctx.fttoken, ctx.ftopt);

    // process special cases
    final Iter iter = ctx.iter(query);
    int len = 0;
    int o = 0;
    byte[] it;

    switch(mode) {
      case M_ALL:
        while((it = nextStr(iter)) != null) {
          final int oc = ftt.contains(it, ctx.fttoken, ctx.ftopt);
          if(oc == 0) return 0;
          len += it.length;
          o += oc;
        }
        break;
      case M_ALLWORDS:
        while((it = nextStr(iter)) != null) {
          for(final byte[] t : split(it, ' ')) {
            final int oc = ftt.contains(t, ctx.fttoken, ctx.ftopt);
            if(oc == 0) return 0;
            len += t.length;
            o += oc;
          }
        }
        break;
      case M_ANY:
        while((it = nextStr(iter)) != null) {
          o += ftt.contains(it, ctx.fttoken, ctx.ftopt);
          len += it.length;
        }
        break;
      case M_ANYWORD:
        while((it = nextStr(iter)) != null) {
          for(final byte[] t : split(it, ' ')) {
            o += ftt.contains(t, ctx.fttoken, ctx.ftopt);
            len += t.length;
          }
        }
        break;
      case M_PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        while((it = nextStr(iter)) != null) {
          tb.add(it);
          tb.add(' ');
        }
        o += ftt.contains(tb.finish(), ctx.fttoken, ctx.ftopt);
        len += tb.size();
        break;
    }

    final long mn = occ != null ? checkItr(occ[0], ctx) : 1;
    final long mx = occ != null ? checkItr(occ[1], ctx) : Long.MAX_VALUE;
    if(mn == 0 && o == 0) all = FTNot.not(all);
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }

  /**
   * Adds a match.
   * @param s start position
   * @param e end position
   * @return fast fast evaluation
   */
  boolean add(final int s, final int e) {
    // [CG] XQFT: check if this is needed and correct
    if(!first && (mode == FTMode.M_ALL || mode == FTMode.M_ALLWORDS)) all.and(
        s, e);
    else all.or(s, e);
    return fast;
  }

  /**
   * Checks if the next item is a string. Returns a token representation or an
   * exception.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException query exception
   */
  private byte[] nextStr(final Iter iter) throws QueryException {
    final Item it = iter.next();
    return it == null ? null : checkEStr(it);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    /*
     * If the following conditions yield true, the index is accessed: - the
     * query is a simple String item - no FTTimes option is specified - FTMode
     * is different to ANY, ALL and PHRASE - case sensitivity, diacritics and
     * stemming flags comply with index
     */
    final MetaData md = ic.data.meta;
    final FTOpt fto = ic.ctx.ftopt;

    if(txt == null || occ != null || mode != FTMode.M_ANY
        && mode != FTMode.M_ALL && mode != FTMode.M_PHRASE
        || md.casesens != fto.is(CS) || md.diacritics != fto.is(DC)
        || md.stemming != fto.is(ST) || md.language != fto.ln) return false;

    // no index results
    if(txt.length == 0) {
      ic.costs = 0;
      return true;
    }

    // limit index access to trie version and simple wildcard patterns
    boolean wc = fto.is(WC);
    if(wc) {
      wc = md.wildcards;
      // index does not support wildcards
      if(!wc) return false;
    }

    // summarize number of hits; break loop if no hits are expected
    final FTLexer ft = new FTLexer(txt, ic.ctx.context.prop, fto);
    ic.costs = 0;
    while(ft.hasNext()) {
      final byte[] tok = ft.nextToken();
      if(tok.length > MAXLEN) return false;
      if(fto.sw != null && fto.sw.id(tok) != 0) continue;

      if(wc) {
        // don't use index if one of the terms starts with a wildcard
        final byte[] t = ft.get();
        if(t[0] == '.') return false;
        // don't use index if certain characters, or more than one dot are found
        int d = 0;
        for(final byte w : t) {
          if(w == '{' || w == '\\' || w == '.' && ++d > 1) return false;
        }
      }

      // reduce number of expected results to favor full-text index requests
      final int s = ic.data.nrIDs(ft) + 3 >> 2;
      if(ic.costs > s || ic.costs == 0) ic.costs = s;
      if(s == 0) break;
    }
    return true;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) {
    data = ic.data;
    return this;
  }

  @Override
  public boolean usesExclude() {
    return occ != null;
  }

  @Override
  public boolean removable(final Var v) {
    if(occ != null) {
      for(int o = 0; o != occ.length; ++o)
        if(!occ[o].removable(v)) return false;
    }
    return query.removable(v);
  }

  @Override
  public FTExpr remove(final Var v) {
    if(occ != null) {
      for(int o = 0; o != occ.length; ++o)
        occ[o] = occ[o].remove(v);
    }
    query = query.remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    if(occ != null) {
      occ[0].plan(ser);
      occ[1].plan(ser);
    }
    if(txt != null) ser.text(txt);
    else query.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(!(query instanceof Str)) sb.append("{ ");
    sb.append(query);
    if(!(query instanceof Str)) sb.append(" }");
    switch(mode) {
      case M_ALL:
        sb.append(' ' + ALL);
        break;
      case M_ALLWORDS:
        sb.append(' ' + ALL + ' ' + WORDS);
        break;
      case M_ANYWORD:
        sb.append(' ' + ANY + ' ' + WORD);
        break;
      case M_PHRASE:
        sb.append(' ' + PHRASE);
        break;
      default:
    }

    if(occ != null) {
      sb.append(OCCURS + " " + occ[0] + " " + TO + " " + occ[1] + " " + TIMES);
    }
    return sb.toString();
  }
}
