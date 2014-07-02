package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.ft.FTFlag.*;

import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * FTWords expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTWords extends FTExpr {
  /** All matches. */
  FTMatches matches = new FTMatches();
  /** Flag for first evaluation. */
  boolean first;
  /** Search mode; default: {@link FTMode#ANY}. */
  FTMode mode = FTMode.ANY;
  /** Query expression. */
  Expr query;
  /** Minimum and maximum occurrences. */
  Expr[] occ;

  /** Full-text tokenizer. */
  private FTTokenizer ftt;
  /** Data reference. */
  private IndexContext ictx;
  /** Single string. */
  private TokenList txt;

  /** Query position. */
  private int pos;
  /** Fast evaluation. */
  private boolean fast;

  /**
   * Constructor for scan-based evaluation.
   * @param info input info
   * @param query query expression
   * @param mode search mode
   * @param occ occurrences
   */
  public FTWords(final InputInfo info, final Expr query, final FTMode mode, final Expr[] occ) {
    super(info);
    this.query = query;
    this.mode = mode;
    this.occ = occ;
  }

  /**
   * Constructor for index-based evaluation.
   * @param info input info
   * @param ictx index context
   * @param query query terms
   * @param mode search mode
   */
  public FTWords(final InputInfo info, final IndexContext ictx, final Value query,
      final FTMode mode) {
    super(info);
    this.query = query;
    this.mode = mode;
    this.ictx = ictx;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(occ);
    checkNoUp(query);
  }

  @Override
  public FTWords compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(occ != null) for(int o = 0; o < occ.length; ++o) occ[o] = occ[o].compile(ctx, scp);

    // compile only once
    if(txt == null) {
      query = query.compile(ctx, scp);
      if(query.isValue()) txt = tokens(ctx);
      // choose fast evaluation for default settings
      fast = mode == FTMode.ANY && txt != null && occ == null;
      if(ftt == null) ftt = new FTTokenizer(this, ctx);
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    if(pos == 0) pos = ++ctx.ftPos;
    matches.reset(pos);

    final int c = contains(ctx);
    if(c == 0) matches.size(0);

    // scoring: include number of tokens for calculations
    return new FTNode(matches, c == 0 ? 0 : Scoring.word(c, ctx.ftToken.count()));
  }

  @Override
  public FTIter iter(final QueryContext ctx) {
    final Data data = ictx.data;
    return new FTIter() {
      FTIndexIterator ftiter;
      int len;

      @Override
      public FTNode next() throws QueryException {
        if(ftiter == null) {
          final FTLexer lex = new FTLexer(ftt.opt);

          // index iterator tree
          // number of distinct tokens
          int t = 0;
          // loop through unique tokens
          for(final byte[] k : unique(txt != null ? txt : tokens(ctx))) {
            lex.init(k);
            if(!lex.hasNext()) return null;

            int d = 0;
            FTIndexIterator ii = null;
            do {
              final byte[] tok = lex.nextToken();
              t += tok.length;
              if(ftt.opt.sw != null && ftt.opt.sw.contains(tok)) {
                ++d;
              } else {
                final FTIndexIterator ir = lex.get().length > data.meta.maxlen ? scan(lex) :
                  (FTIndexIterator) data.iter(lex);
                ir.pos(++ctx.ftPos);
                if(ii == null) {
                  ii = ir;
                } else {
                  ii = FTIndexIterator.intersect(ii, ir, ++d);
                  d = 0;
                }
              }
            } while(lex.hasNext());

            // create or combine iterator
            if(ftiter == null) {
              len = t;
              ftiter = ii;
            } else if(mode == FTMode.ALL || mode == FTMode.ALL_WORDS) {
              if(ii.size() == 0) return null;
              len += t;
              ftiter = FTIndexIterator.intersect(ftiter, ii, 0);
            } else {
              if(ii.size() == 0) continue;
              len = Math.max(t, len);
              ftiter = FTIndexIterator.union(ftiter, ii);
            }
          }
        }

        // [CG] XQuery, Full-Text: check scoring in index-based model
        return ftiter == null || !ftiter.more() ? null :
          new FTNode(ftiter.matches(), data, ftiter.pre(), len, ftiter.size(), -1);
      }
    };
  }

  /**
   * Returns a scan-based iterator.
   * @param lex lexer, including the queried value
   * @return node iterator
   * @throws QueryException query exception
   */
  private FTIndexIterator scan(final FTLexer lex) throws QueryException {
    final Data data = ictx.data;
    final FTLexer input = new FTLexer(ftt.opt);
    final FTTokens tokens = ftt.cache(lex.get());

    return new FTIndexIterator() {
      int pre = -1, ps;

      @Override
      public int pre() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < data.meta.size) {
          if(data.kind(pre) != Data.TEXT) continue;
          input.init(data.text(pre, true));
          matches.reset(ps);
          try {
            if(ftt.contains(tokens, input) != 0) return true;
          } catch(final QueryException ignore) {
            // ignore exceptions
          }
        }
        return false;
      }
      @Override
      public FTMatches matches() {
        return matches;
      }
      @Override
      public void pos(final int p) {
        ps = p;
      }
      @Override
      public int size() {
        // worst case
        return Math.max(1, data.meta.size >>> 1);
      }
    };
  }

  /**
   * Returns all tokens of the query.
   * @param ctx query context
   * @return token list
   * @throws QueryException query exception
   */
  private TokenList tokens(final QueryContext ctx) throws QueryException {
    final TokenList tl = new TokenList();
    final Iter ir = ctx.iter(query);
    for(byte[] qu; (qu = nextToken(ir)) != null;) {
      // skip empty tokens if not all results are needed
      if(qu.length != 0 || mode == FTMode.ALL || mode == FTMode.ALL_WORDS)
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
    final FTLexer lexer = ftt.lexer(ctx.ftToken);

    // use faster evaluation for default options
    int num = 0;
    if(fast) {
      for(final byte[] t : txt) {
        final FTTokens qtok = ftt.cache(t);
        num = Math.max(num, ftt.contains(qtok, lexer) * qtok.length());
      }
      return num;
    }

    // find and count all occurrences
    final boolean all = mode == FTMode.ALL || mode == FTMode.ALL_WORDS;
    int oc = 0;
    for(final byte[] w : unique(tokens(ctx))) {
      final FTTokens qtok = ftt.cache(w);
      final int o = ftt.contains(qtok, lexer);
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
   * @return token set
   */
  private TokenSet unique(final TokenList list) {
    // cache all query tokens in a set (duplicates are removed)
    final TokenSet ts = new TokenSet();
    switch(mode) {
      case ALL:
      case ANY:
        for(final byte[] t : list) ts.add(t);
        break;
      case ALL_WORDS:
      case ANY_WORD:
        final FTLexer l = new FTLexer(ftt.opt);
        for(final byte[] t : list) {
          l.init(t);
          while(l.hasNext()) ts.add(l.nextToken());
        }
        break;
      case PHRASE:
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
    if(!first && (mode == FTMode.ALL || mode == FTMode.ALL_WORDS)) matches.and(s, e);
    else matches.or(s, e);
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) {
    /* If the following conditions yield true, the index is accessed:
     * - all query terms are statically available
     * - no FTTimes option is specified
     * - explicitly set case, diacritics and stemming match options do not
     *   conflict with index options. */
    final Data dt = ic.ictx.data;
    final MetaData md = dt.meta;
    final FTOpt fto = ftt.opt;

    /* Index will be applied if no explicit match options have been set
     * that conflict with the index options. As a consequence, though, index-
     * based querying might yield other results than sequential scanning. */
    if(occ != null ||
       fto.cs != null && md.casesens == (fto.cs == FTCase.INSENSITIVE) ||
       fto.isSet(DC) && md.diacritics != fto.is(DC) ||
       fto.isSet(ST) && md.stemming != fto.is(ST) ||
       fto.ln != null && !fto.ln.equals(md.language)) return false;

    // estimate costs if text is not statically known
    if(txt == null) {
      ic.costs(Math.max(1, dt.meta.size >>> 3));
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
        if(fto.sw != null && fto.sw.contains(tok)) continue;

        if(fto.is(WC)) {
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
        ic.addCosts(Math.max(1, dt.costs(ft) >> 10));
      }
    }
    return true;
  }

  @Override
  public FTExpr indexEquivalent(final IndexCosts ic) {
    ictx = ic.ictx;
    return this;
  }

  @Override
  public boolean usesExclude() {
    return occ != null;
  }

  @Override
  public boolean has(final Flag flag) {
    if(occ != null) for(final Expr o : occ) if(o.has(flag)) return true;
    return query.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    if(occ != null) for(final Expr o : occ) if(!o.removable(v)) return false;
    return query.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return occ != null ? VarUsage.sum(v, occ).plus(query.count(v)) : query.count(v);
  }

  @Override
  public FTExpr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {

    boolean change = occ != null && inlineAll(ctx, scp, occ, v, e);
    final Expr q = query.inline(ctx, scp, v, e);
    if(q != null) {
      query = q;
      change = true;
    }
    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final FTWords ftw = new FTWords(info, query.copy(ctx, scp, vs), mode,
        occ == null ? null : Arr.copyAll(ctx, scp, vs, occ));
    if(ftt != null) ftw.ftt = ftt.copy(ftw);
    if(txt != null) ftw.txt = txt.copy();
    ftw.ictx = ictx;
    ftw.first = first;
    ftw.pos = pos;
    ftw.fast = fast;
    return ftw;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), occ, query);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && query.accept(visitor) &&
        (occ == null || visitAll(visitor, occ));
  }

  @Override
  public int exprSize() {
    int sz = 1;
    if(occ != null) for(final Expr o : occ) sz += o.exprSize();
    for(final Expr e : exprs) sz += e.exprSize();
    return sz + query.exprSize();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final boolean str = query instanceof AStr;
    if(!str) sb.append("{ ");
    sb.append(query);
    if(!str) sb.append(" }");
    switch(mode) {
      case ALL:
        sb.append(' ' + ALL);
        break;
      case ALL_WORDS:
        sb.append(' ' + ALL + ' ' + WORDS);
        break;
      case ANY_WORD:
        sb.append(' ' + ANY + ' ' + WORD);
        break;
      case PHRASE:
        sb.append(' ' + PHRASE);
        break;
      default:
    }
    if(occ != null) sb.append(OCCURS + ' ' + occ[0] + ' ' + TO + ' ' + occ[1] + ' ' + TIMES);
    return sb.append(ftt.opt).toString();
  }
}
