package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.index.FTIndexIterator;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTNode;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenObjMap;
import org.basex.util.TokenSet;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;

/**
 * FTWords expression.
 *
 * @author BaseX Team 2005-11, ISC License
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

  /** Cache. */
  final TokenObjMap<ArrayList<byte[][]>> cache =
    new TokenObjMap<ArrayList<byte[][]>>();

  /** Full-text tokenizer. */
  FTTokenizer ftt;
  /** Data reference. */
  Data data;
  /** Single string. */
  byte[] txt;

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
  private int tokNum;
  /** Standard evaluation. */
  private boolean fast;

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
   * @param str string
   * @param ctx query context
   * @throws QueryException query exception
   */
  public FTWords(final InputInfo ii, final Data d, final Str str,
      final QueryContext ctx) throws QueryException {
    super(ii);
    query = str;
    data = d;
    comp(ctx);
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    // compile only once
    if(ftt == null) {
      if(occ != null) {
        for(int o = 0; o < occ.length; ++o) occ[o] = occ[o].comp(ctx);
      }
      query = query.comp(ctx);
      if(query instanceof Str) txt = ((Str) query).atom();

      // choose fast evaluation for default settings
      fast = mode == FTMode.M_ANY && txt != null && occ == null;

      ftt = new FTTokenizer(this, ctx.ftopt, ctx.context.prop);
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    if(tokNum == 0) tokNum = ++ctx.ftoknum;
    all.reset(tokNum);

    final int c = contains(ctx);
    if(c == 0) all.size = 0;

    // scoring: include number of tokens for calculations
    return new FTNode(all, c == 0 ? 0 : ctx.score.word(c, ctx.fttoken.count()));
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      /** Index iterator. */
      FTIndexIterator iat;

      @Override
      public FTNode next() {
        if(iat == null) {
          final FTLexer lex = new FTLexer(ftt.opt).init(txt);
          int d = 0;
          while(lex.hasNext()) {
            final byte[] token = lex.nextToken();
            if(ftt.opt.sw != null && ftt.opt.sw.id(token) != 0) {
              ++d;
            } else {
              final FTIndexIterator i = (FTIndexIterator) data.ids(lex);
              iat = iat == null ? i : FTIndexIterator.phrase(iat, i, ++d);
              d = 0;
            }
          }
          iat.tokenNum(++ctx.ftoknum);
        }
        return iat.more() ? new FTNode(iat.matches(), data, iat.next(),
            txt.length, iat.indexSize(), iat.score()) : null;
      }
    };
  }

  /**
   * Evaluates the full-text match.
   * @param ctx query context
   * @return number of tokens, used for scoring
   * @throws QueryException query exception
   */
  private int contains(final QueryContext ctx) throws QueryException {
    first = true;
    final FTLexer intok = ftt.copy(ctx.fttoken);

    // use shortcut for default processing
    if(fast) {
      final FTTokens qtok = ftt.cache(txt);
      return ftt.contains(qtok, intok) * qtok.length();
    }

    // cache all query tokens (remove duplicates)
    final TokenSet tm = new TokenSet(); 
    final Iter qu = ctx.iter(query);
    byte[] q;
    switch(mode) {
      case M_ALL:
      case M_ANY:
        while((q = nextToken(qu)) != null) tm.add(q);
        break;
      case M_ALLWORDS:
      case M_ANYWORD:
        final FTLexer l = new FTLexer(intok.ftOpt());
        while((q = nextToken(qu)) != null) {
          l.init(q);
          while(l.hasNext()) tm.add(l.nextToken());
        }
        break;
      case M_PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        while((q = nextToken(qu)) != null) tb.add(q).add(' ');
        tm.add(tb.trim().finish());
    }

    // find and count all occurrences
    final boolean a = mode == FTMode.M_ALL || mode == FTMode.M_ALLWORDS;
    int num = 0, oc = 0;
    for(int i = 1; i <= tm.size(); i++) {
      final FTTokens qtok = ftt.cache(tm.key(i));
      final int o = ftt.contains(qtok, intok);
      if(a && o == 0) return 0;
      num = Math.max(num, o * qtok.length());
      oc += o;
    }

    // check if occurrences are in valid range. if yes, return number of tokens 
    final long mn = occ != null ? checkItr(occ[0], ctx) : 1;
    final long mx = occ != null ? checkItr(occ[1], ctx) : Long.MAX_VALUE;
    if(mn == 0 && oc == 0) all = FTNot.not(all);
    return oc >= mn && oc <= mx ? Math.max(1, num) : 0;
  }

  /**
   * Returns the next token of the specified iterator, or {@code null}.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException query exception
   */
  private byte[] nextToken(final Iter iter) throws QueryException {
    final Item it = iter.next();
    return it == null ? null : checkEStr(it);
  }
  
  /**
   * Adds a match.
   * @param s start position
   * @param e end position
   */
  void add(final int s, final int e) {
    if(!first && (mode == FTMode.M_ALL || mode == FTMode.M_ALLWORDS))
      all.and(s, e);
    else all.or(s, e);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    /* If the following conditions yield true, the index is accessed:
     * - the query is a simple String item
     * - no FTTimes option is specified
     * - FTMode is different to ANY, ALL and PHRASE
     * - case sensitivity, diacritics and stemming flags comply with index. */
    final MetaData md = ic.data.meta;
    final FTOpt fto = ftt.opt;

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
    final FTLexer ft = new FTLexer(fto).init(txt);
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
