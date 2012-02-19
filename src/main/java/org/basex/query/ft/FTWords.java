package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.data.MetaData;
import org.basex.index.ft.FTIndexIterator;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.FTNode;
import org.basex.query.item.Item;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.Iter;
import org.basex.query.util.IndexContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;
import org.basex.util.ft.Scoring;
import org.basex.util.hash.TokenSet;
import org.basex.util.list.TokenList;

/**
 * FTWords expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTWords extends FTExpr {
  /** Words mode. */
  public enum FTMode {
    /** All option. */       M_ALL,
    /** All words option. */ M_ALLWORDS,
    /** Any option. */       M_ANY,
    /** Any words option. */ M_ANYWORD,
    /** Phrase search. */    M_PHRASE
  }

  /** Full-text tokenizer. */
  FTTokenizer ftt;
  /** Data reference. */
  Data data;
  /** Single string. */
  TokenList txt;

  /** All matches. */
  FTMatches matches = new FTMatches((byte) 0);
  /** Flag for first evaluation. */
  boolean first;
  /** Search mode; default: {@link FTMode#M_ANY}. */
  FTMode mode = FTMode.M_ANY;

  /** Query expression. */
  private Expr query;
  /** Minimum and maximum occurrences. */
  private Expr[] occ;
  /** Current token number. */
  private int tokNum;
  /** Fast evaluation. */
  private boolean fast;

  /**
   * Constructor for scan-based evaluation.
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
   * Constructor for index-based evaluation.
   * @param ii input info
   * @param d data reference
   * @param str string
   * @param ctx query context
   * @throws QueryException query exception
   */
  public FTWords(final InputInfo ii, final Data d, final Item str,
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
      if(query.isValue()) txt = tokens(ctx);

      // choose fast evaluation for default settings
      fast = mode == FTMode.M_ANY && txt != null && occ == null;
      ftt = new FTTokenizer(this, ctx.ftOpt(), ctx.context.prop);
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    if(tokNum == 0) tokNum = ++ctx.ftoknum;
    matches.reset(tokNum);

    final int c = contains(ctx);
    if(c == 0) matches.size = 0;

    // scoring: include number of tokens for calculations
    return new FTNode(matches, c == 0 ? 0 :
      Scoring.word(c, ctx.fttoken.count()));
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      /** Index iterator. */
      FTIndexIterator iat;
      /** Text length. */
      int len;

      @Override
      public FTNode next() throws QueryException {
        if(iat == null) {
          final FTLexer lex = new FTLexer(ftt.opt);

          // index iterator tree
          FTIndexIterator ia;
          // number of distinct tokens
          int t  = 0;
          // loop through all tokens
          final TokenSet ts = tokens(txt != null ? txt : tokens(ctx), ftt.opt);
          for(final byte[] k : ts) {
            lex.init(k);
            ia = null;
            int d = 0;
            if(!lex.hasNext()) return null;
            do {
              final byte[] tok = lex.nextToken();
              t += tok.length;
              if(ftt.opt.sw != null && ftt.opt.sw.id(tok) != 0) {
                ++d;
              } else {
                final FTIndexIterator ir = lex.get().length > data.meta.maxlen ?
                    scan(lex) : (FTIndexIterator) data.iter(lex);
                if(ia == null) {
                  ia = ir;
                } else {
                  ia = FTIndexIterator.intersect(ia, ir, ++d);
                  d = 0;
                }
              }
            } while(lex.hasNext());
            // create or combine iterator
            if(iat == null) {
              len = t;
              iat = ia;
            } else if(mode == FTMode.M_ALL || mode == FTMode.M_ALLWORDS) {
              if(ia.size() == 0) return null;
              len += t;
              iat = FTIndexIterator.intersect(ia, iat, 0);
            } else {
              if(ia.size() == 0) continue;
              len = Math.max(t, len);
              iat = FTIndexIterator.union(ia, iat);
            }
            iat.tokenNum(++ctx.ftoknum);
          }
        }
        return iat == null || !iat.more() ? null : new FTNode(iat.matches(),
            data, iat.next(), len, iat.size(), iat.score());
      }
    };
  }

  /**
   * Returns scan-based iterator.
   * @param lex lexer, including the queried value
   * @return node iterator
   * @throws QueryException query exception
   */
  FTIndexIterator scan(final FTLexer lex) throws QueryException {
    final FTLexer intok = new FTLexer(ftt.opt);
    final FTTokens qtok = ftt.cache(lex.get());
    return new FTIndexIterator() {
      int pre = -1;

      @Override
      public int next() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < data.meta.size) {
          if(data.kind(pre) != Data.TEXT) continue;
          intok.init(data.text(pre, true));
          matches.reset(0);
          try {
            if(ftt.contains(qtok, intok) != 0) return true;
          } catch(final QueryException ex) {
            // ignore exceptions
          }
        }
        return false;
      }
      @Override
      public double score() {
        return -1;
      }
      @Override
      public FTMatches matches() {
        return matches;
      }
      @Override
      public int size() {
        // worst case
        return data.meta.size >>> 1;
      }
    };
  }

  /**
   * Returns all tokens of the query.
   * @param ctx query context
   * @return token list
   * @throws QueryException query exception
   */
  TokenList tokens(final QueryContext ctx) throws QueryException {
    final TokenList tl = new TokenList();
    final Iter ir = ctx.iter(query);
    for(byte[] qu; (qu = nextToken(ir)) != null;) {
      // skip empty tokens if not all results are needed
      if(qu.length != 0 || mode == FTMode.M_ALL || mode == FTMode.M_ALLWORDS)
      tl.add(qu);
    }
    return tl;
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

    // use shortcut for default options
    int num = 0;
    if(fast) {
      for(final byte[] t : txt) {
        final FTTokens qtok = ftt.cache(t);
        num = Math.max(num, ftt.contains(qtok, intok) * qtok.length());
      }
      return num;
    }

    // find and count all occurrences
    final TokenList tl = tokens(ctx);
    final TokenSet ts = tokens(tl, intok.ftOpt());
    final boolean all = mode == FTMode.M_ALL || mode == FTMode.M_ALLWORDS;
    int oc = 0;
    for(final byte[] k : ts) {
      final FTTokens qtok = ftt.cache(k);
      final int o = ftt.contains(qtok, intok);
      if(all && o == 0) return 0;
      num = Math.max(num, o * qtok.length());
      oc += o;
    }

    // check if occurrences are in valid range. if yes, return number of tokens
    final long mn = occ != null ? checkItr(occ[0], ctx) : 1;
    final long mx = occ != null ? checkItr(occ[1], ctx) : Long.MAX_VALUE;
    if(mn == 0 && oc == 0) matches = FTNot.not(matches);
    return oc >= mn && oc <= mx ? Math.max(1, num) : 0;
  }

  /**
   * Caches and returns all unique tokens specified in a query.
   * @param list token list
   * @param ftopt full-text options
   * @return token set
   */
  TokenSet tokens(final TokenList list, final FTOpt ftopt) {
    // cache all query tokens (remove duplicates)
    final TokenSet ts = new TokenSet();
    switch(mode) {
      case M_ALL:
      case M_ANY:
        for(final byte[] t : list) ts.add(t);
        break;
      case M_ALLWORDS:
      case M_ANYWORD:
        final FTLexer l = new FTLexer(ftopt);
        for(final byte[] t : list) {
          l.init(t);
          while(l.hasNext()) ts.add(l.nextToken());
        }
        break;
      case M_PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        for(final byte[] t : list) tb.add(t).add(' ');
        ts.add(tb.trim().finish());
    }
    return ts;
  }

  /**
   * Returns the next token of the specified iterator, or {@code null}.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException query exception
   */
  byte[] nextToken(final Iter iter) throws QueryException {
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
      matches.and(s, e);
    else matches.or(s, e);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    /* If the following conditions yield true, the index is accessed:
     * - all query terms are statically available
     * - no FTTimes option is specified
     * - explicitly set case, diacritics and stemming match options do not
     *   conflict with index options. */
    final MetaData md = ic.data.meta;
    final FTOpt fto = ftt.opt;

    // skip index access if index does not support wildcards
    final boolean wc = fto.is(WC);
    if(wc && !md.wildcards) return false;

    /* Index will be applied if no explicit match options have been set
     * that conflict with the index options. As a consequence, though, index-
     * based querying might yield other results than sequential scanning. */
    if(occ != null ||
       fto.isSet(CS) && md.casesens != fto.is(CS) ||
       fto.isSet(DC) && md.diacritics != fto.is(DC) ||
       fto.isSet(ST) && md.stemming != fto.is(ST) ||
       fto.ln != null && !fto.ln.equals(md.language)) return false;

    // estimate costs if text is not statically known
    if(txt == null) {
      ic.costs(Math.max(1, ic.data.meta.size >> 10));
      return true;
    }

    // adopt database options to tokenizer
    fto.copy(md);

    // summarize number of hits; break loop if no hits are expected
    final FTLexer ft = new FTLexer(fto);
    ic.costs(0);
    for(byte[] t : txt) {
      ft.init(t);
      while(ft.hasNext()) {
        final byte[] tok = ft.nextToken();
        if(fto.sw != null && fto.sw.id(tok) != 0) continue;

        if(wc) {
          // don't use index if one of the terms starts with a wildcard
          t = ft.get();
          if(t[0] == '.') return false;
          // don't use index if certain characters or more than 1 dot are found
          int d = 0;
          for(final byte w : t) {
            if(w == '{' || w == '\\' || w == '.' && ++d > 1) return false;
          }
        }
        // reduce number of expected results to favor full-text index requests
        ic.addCosts(Math.max(1, ic.data.count(ft) >> 10));
      }
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
  public boolean uses(final Use u) {
    if(occ != null) for(final Expr o : occ) if(o.uses(u)) return true;
    return query.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    if(occ != null) for(final Expr o : occ) c += o.count(v);
    return c + query.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    if(occ != null) for(final Expr o : occ) if(!o.removable(v)) return false;
    return query.removable(v);
  }

  @Override
  public FTExpr remove(final Var v) {
    if(occ != null) {
      for(int o = 0; o < occ.length; ++o)
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
    query.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final boolean str = query instanceof Item && ((Item) query).type.isString();
    if(!str) sb.append("{ ");
    sb.append(query);
    if(!str) sb.append(" }");
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
      sb.append(OCCURS + ' ' + occ[0] + ' ' + TO + ' ' + occ[1] + ' ' + TIMES);
    }
    return sb.toString();
  }
}
