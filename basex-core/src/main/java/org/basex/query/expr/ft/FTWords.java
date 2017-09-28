package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.ft.FTFlag.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FTWords extends FTExpr {
  /** Search mode; default: {@link FTMode#ANY}. */
  final FTMode mode;
  /** Query expression. */
  Expr query;
  /** Minimum and maximum occurrences (can be {@code null}). */
  Expr[] occ;

  /** Simple evaluation. */
  boolean simple;
  /** Compilation flag. */
  private boolean compiled;
  /** Data reference (can be {@code null}). */
  private Data data;
  /** Pre-evaluated query tokens. */
  private TokenList tokens;
  /** Full-text options. */
  private FTOpt ftOpt;

  /** Thread-safe full-text tokenizer. */
  private final ThreadLocal<FTTokenizer> caches = new ThreadLocal<>();

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
   * @param data data reference
   * @param query query terms
   * @param mode search mode
   */
  public FTWords(final InputInfo info, final Data data, final Value query, final FTMode mode) {
    super(info);
    this.data = data;
    this.query = query;
    this.mode = mode;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(occ);
    checkNoUp(query);
  }

  @Override
  public FTWords compile(final CompileContext cc) throws QueryException {
    if(compiled) return this;
    compiled = true;

    if(occ != null) {
      final int ol = occ.length;
      for(int o = 0; o < ol; o++) occ[o] = occ[o].compile(cc);
    }
    query = query.compile(cc);

    return init(cc.qc, cc.qc.ftOpt());
  }

  /**
   * Prepares query evaluation.
   * @param qc query context
   * @param opt full-text options
   * @return self reference
   * @throws QueryException query exception
   */
  public FTWords init(final QueryContext qc, final FTOpt opt) throws QueryException {
    // pre-evaluate tokens, choose fast evaluation for default search options
    if(query.isValue()) {
      tokens = tokens(qc);
      simple = mode == FTMode.ANY && occ == null;
    }
    ftOpt = opt;
    return this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FTTokenizer ftt = get(qc);
    if(ftt.pos == 0) ftt.pos = ++qc.ftPos;
    ftt.matches.reset(ftt.pos);

    final int count = contains(qc, ftt);
    if(count == 0) ftt.matches.size(0);

    // scoring: include number of tokens for calculations
    return new FTNode(ftt.matches, count == 0 ? 0 : Scoring.word(count, qc.ftLexer.count()));
  }

  @Override
  public FTIter iter(final QueryContext qc) {
    return new FTIter() {
      FTIndexIterator ftiter;
      int len;

      @Override
      public FTNode next() throws QueryException {
        if(ftiter == null) {
          final FTTokenizer ftt = FTWords.this.get(qc);
          final FTLexer lexer = new FTLexer(ftOpt).
              lserror(qc.context.options.get(MainOptions.LSERROR));

          // number of distinct tokens
          int count = 0;
          // loop through unique tokens
          for(final byte[] k : unique(tokens != null ? tokens : tokens(qc))) {
            lexer.init(k);
            if(!lexer.hasNext()) return null;

            int d = 0;
            FTIndexIterator ii = null;
            do {
              final byte[] tok = lexer.nextToken();
              count += tok.length;
              if(ftOpt.sw != null && ftOpt.sw.contains(tok)) {
                ++d;
              } else {
                final FTIndexIterator ir = lexer.get().length > data.meta.maxlen ?
                  scan(lexer, ftt) : (FTIndexIterator) data.iter(lexer);
                ir.pos(++qc.ftPos);
                if(ii == null) {
                  ii = ir;
                } else {
                  ii = FTIndexIterator.intersect(ii, ir, ++d);
                  d = 0;
                }
              }
            } while(lexer.hasNext());

            if(ii != null) {
              // create or combine iterator
              if(ftiter == null) {
                len = count;
                ftiter = ii;
              } else if(mode == FTMode.ALL || mode == FTMode.ALL_WORDS) {
                if(ii.size() == 0) return null;
                len += count;
                ftiter = FTIndexIterator.intersect(ftiter, ii, 0);
              } else {
                if(ii.size() == 0) continue;
                len = Math.max(count, len);
                ftiter = FTIndexIterator.union(ftiter, ii);
              }
            }
          }
        }
        return ftiter == null || !ftiter.more() ? null :
          new FTNode(ftiter.matches(), data, ftiter.pre(), len, ftiter.size(), -1);
      }
    };
  }

  /**
   * Returns a scan-based index iterator.
   * @param lexer lexer, including the queried value
   * @param ftt full-text tokenizer
   * @return node iterator
   * @throws QueryException query exception
   */
  private FTIndexIterator scan(final FTLexer lexer, final FTTokenizer ftt) throws QueryException {
    final FTLexer input = new FTLexer(ftOpt);
    final FTTokens fttokens = ftt.cache(lexer.get());

    return new FTIndexIterator() {
      final int sz = data.meta.size;
      int pre = -1, ps;

      @Override
      public int pre() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < sz) {
          if(data.kind(pre) != Data.TEXT) continue;
          input.init(data.text(pre, true));
          ftt.matches.reset(ps);
          try {
            if(contains(fttokens, input, ftt) != 0) return true;
          } catch(final QueryException ignore) {
            // ignore exceptions
          }
        }
        return false;
      }
      @Override
      public FTMatches matches() {
        return ftt.matches;
      }
      @Override
      public void pos(final int p) {
        ps = p;
      }
      @Override
      public int size() {
        // worst case
        return Math.max(1, sz >>> 1);
      }
    };
  }

  /**
   * Returns all tokens of the query.
   * @param qc query context
   * @return token list
   * @throws QueryException query exception
   */
  private TokenList tokens(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    final Iter iter = qc.iter(query);
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      // skip empty tokens if not all results are needed
      final byte[] qu = toToken(it);
      if(qu.length != 0 || mode == FTMode.ALL || mode == FTMode.ALL_WORDS) tl.add(qu);
    }
    return tl;
  }

  /**
   * Evaluates the full-text match.
   * @param qc query context
   * @param ftt full-text tokenizer
   * @return number of tokens, used for scoring
   * @throws QueryException query exception
   */
  private int contains(final QueryContext qc, final FTTokenizer ftt) throws QueryException {
    ftt.first = true;
    final FTLexer lexer = qc.ftLexer.copy(ftOpt);

    // use faster evaluation for default options
    int num = 0;
    if(simple) {
      for(final byte[] t : tokens) {
        final FTTokens qtok = ftt.cache(t);
        num = Math.max(num, contains(qtok, lexer, ftt) * qtok.firstSize());
      }
      return num;
    }

    // find and count all occurrences
    final boolean all = mode == FTMode.ALL || mode == FTMode.ALL_WORDS;
    int oc = 0;
    for(final byte[] w : unique(tokens(qc))) {
      final FTTokens qtok = ftt.cache(w);
      final int o = contains(qtok, lexer, ftt);
      if(all && o == 0) return 0;
      num = Math.max(num, o * qtok.firstSize());
      oc += o;
    }

    // check if occurrences are in valid range. if yes, return number of tokens
    final long mn = occ != null ? toLong(occ[0], qc) : 1;
    final long mx = occ != null ? toLong(occ[1], qc) : Long.MAX_VALUE;
    if(mn == 0 && oc == 0) ftt.matches = FTNot.not(ftt.matches);
    return oc >= mn && oc <= mx ? Math.max(1, num) : 0;
  }

  /**
   * Checks if the first token contains the second full-text term.
   * @param tok cached query tokens
   * @param input input text
   * @param ftt full-text tokenizer
   * @return number of occurrences
   * @throws QueryException query exception
   */
  private int contains(final FTTokens tok, final FTLexer input, final FTTokenizer ftt)
      throws QueryException {

    int count = 0;
    final FTMatches matches = ftt.matches;
    final boolean and = !ftt.first && (mode == FTMode.ALL || mode == FTMode.ALL_WORDS);
    final FTBitapSearch bs = new FTBitapSearch(input.init(), tok, ftt.cmp);
    while(bs.hasNext()) {
      final int s = bs.next(), e = s + tok.firstSize() - 1;
      if(and) matches.and(s, e);
      else matches.or(s, e);
      count++;
    }

    matches.pos++;
    ftt.first = false;
    return count;
  }

  /**
   * Caches and returns all unique tokens specified in a query.
   * @param tl token list
   * @return token set
   */
  private TokenSet unique(final TokenList tl) {
    // cache all query tokens in a set (duplicates are removed)
    final TokenSet ts = new TokenSet();
    switch(mode) {
      case ALL:
      case ANY:
        for(final byte[] token : tl) ts.add(token);
        break;
      case ALL_WORDS:
      case ANY_WORD:
        final FTLexer lexer = new FTLexer(ftOpt);
        for(final byte[] token : tl) {
          lexer.init(token);
          while(lexer.hasNext()) ts.add(lexer.nextToken());
        }
        break;
      case PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        for(final byte[] token : tl) tb.add(token).add(' ');
        ts.add(tb.trim().finish());
    }
    return ts;
  }

  /**
   * Returns a full-text tokenizer instance.
   * @param qc query context
   * @return tokenizer
   */
  private FTTokenizer get(final QueryContext qc) {
    FTTokenizer ftt = caches.get();
    if(ftt == null) {
      ftt = new FTTokenizer(ftOpt, qc, info);
      caches.set(ftt);
    }
    return ftt;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    /* if the following conditions yield true, the index is accessed:
     * - all query terms are statically available
     * - no FTTimes option is specified
     * - explicitly set case, diacritics and stemming match options do not
     *   conflict with index options. */
    data = ii.ic.data;
    final MetaData md = data.meta;

    /* index will be applied if no explicit match options have been set
     * that conflict with the index options. As a consequence, though, index-
     * based querying might yield other results than sequential scanning. */
    if(occ != null ||
      ftOpt.cs != null && md.casesens == (ftOpt.cs == FTCase.INSENSITIVE) ||
      ftOpt.isSet(DC) && md.diacritics != ftOpt.is(DC) ||
      ftOpt.isSet(ST) && md.stemming != ftOpt.is(ST) ||
      ftOpt.ln != null && !ftOpt.ln.equals(md.language)) return false;

    // assign database options
    ftOpt.assign(md);

    // estimate costs if text is not known at compile time
    if(tokens == null) {
      ii.costs = Math.max(2, data.meta.size / 30);
      return true;
    }

    // summarize number of hits; break loop if no hits are expected
    final FTLexer ft = new FTLexer(ftOpt);
    ii.costs = 0;
    for(byte[] t : tokens) {
      ft.init(t);
      while(ft.hasNext()) {
        final byte[] tok = ft.nextToken();
        if(ftOpt.sw != null && ftOpt.sw.contains(tok)) continue;

        if(ftOpt.is(WC)) {
          // don't use index if one of the terms starts with a wildcard
          t = ft.get();
          if(t[0] == '.') return false;
          // don't use index if certain characters or more than 1 dot are found
          int d = 0;
          for(final byte w : t) {
            if(w == '{' || w == '\\' || w == '.' && ++d > 1) return false;
          }
        }
        // favor full-text index requests over exact queries
        final int costs = data.costs(ft);
        if(costs < 0) return false;
        if(costs != 0) ii.costs += Math.max(2, costs / 100);
      }
    }
    return true;
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
  public boolean removable(final Var var) {
    if(occ != null) for(final Expr o : occ) if(!o.removable(var)) return false;
    return query.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return occ != null ? VarUsage.sum(var, occ).plus(query.count(var)) : query.count(var);
  }

  @Override
  public FTExpr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {

    boolean change = occ != null && inlineAll(occ, var, ex, cc);
    final Expr q = query.inline(var, ex, cc);
    if(q != null) {
      query = q;
      change = true;
    }
    return change ? optimize(cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final FTWords ftw = new FTWords(info, query.copy(cc, vm), mode,
        occ == null ? null : Arr.copyAll(cc, vm, occ));

    ftw.simple = simple;
    ftw.compiled = compiled;
    ftw.data = data;
    ftw.tokens = tokens;
    ftw.ftOpt = ftOpt;
    return ftw;
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
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz + query.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTWords)) return false;
    final FTWords f = (FTWords) obj;
    return query.equals(f.query) && mode == f.mode && data == f.data && Array.equals(occ, f.occ) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), occ, query);
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
    if(occ != null) sb.append(OCCURS + ' ').append(occ[0]).append(' ').append(TO).append(' ').
      append(occ[1]).append(' ').append(TIMES);
    if(ftOpt != null) sb.append(ftOpt);
    return sb.toString();
  }
}
