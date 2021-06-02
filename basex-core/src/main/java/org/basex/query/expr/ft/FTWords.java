package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
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
 * @author BaseX Team 2005-21, BSD License
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
  /** Input database (can be {@code null}). */
  private IndexDb db;
  /** Pre-evaluated query tokens. */
  private TokenList inputs;
  /** Full-text options. */
  private FTOpt ftOpt;

  /**
   * Constructor for sequential evaluation.
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
   * @param db index database
   * @param query query terms
   * @param mode search mode
   */
  public FTWords(final InputInfo info, final IndexDb db, final Value query, final FTMode mode) {
    super(info);
    this.db = db;
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
    ftOpt = cc.qc.ftOpt().copy();

    return optimize(cc);
  }

  @Override
  public FTWords optimize(final CompileContext cc) throws QueryException {
    optimize(cc.qc);
    if(occ != null) {
      final int ol = occ.length;
      for(int o = 0; o < ol; o++) occ[o] = occ[o].simplifyFor(Simplify.NUMBER, cc);
    }
    return this;
  }

  /**
   * Prepares query evaluation.
   * @param qc query context
   * @return self reference
   * @throws QueryException query exception
   */
  public FTWords optimize(final QueryContext qc) throws QueryException {
    // pre-evaluate tokens, choose fast evaluation for default search options
    if(query instanceof Value) {
      inputs = inputs(qc);
      simple = mode == FTMode.ANY && occ == null;
    }
    return this;
  }

  /**
   * Assigns full-text options.
   * @param opt full-text options
   * @return self reference
   */
  public FTWords ftOpt(final FTOpt opt) {
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
  public FTIter iter(final QueryContext qc) throws QueryException {
    final Data data = db.data(qc, IndexType.FULLTEXT);
    return new FTIter() {
      FTIndexIterator ftiter;
      int length;

      @Override
      public FTNode next() throws QueryException {
        if(ftiter == null) {
          final FTTokenizer ftt = FTWords.this.get(qc);
          final FTLexer lexer = new FTLexer(ftOpt).
              errors(qc.context.options.get(MainOptions.LSERROR));

          // length distinct tokens
          int len = 0;
          // loop through unique tokens
          for(final byte[] input : unique(inputs != null ? inputs : inputs(qc))) {
            lexer.init(input);
            if(!lexer.hasNext()) return null;

            int d = 0;
            FTIndexIterator ii = null;
            final StopWords sw = ftOpt.sw;
            do {
              final byte[] token = lexer.nextToken();
              len += token.length;
              if(sw != null && sw.contains(token)) {
                ++d;
              } else {
                final FTIndexIterator iter = lexer.token().length > data.meta.maxlen ?
                  scan(lexer, ftt, data) : (FTIndexIterator) data.iter(lexer);
                iter.pos(++qc.ftPos);
                if(ii == null) {
                  ii = iter;
                } else {
                  ii = FTIndexIterator.intersect(ii, iter, ++d);
                  d = 0;
                }
              }
            } while(lexer.hasNext());

            if(ii != null) {
              // create or combine iterator
              if(ftiter == null) {
                length = len;
                ftiter = ii;
              } else if(mode == FTMode.ALL || mode == FTMode.ALL_WORDS) {
                if(ii.size() == 0) return null;
                length += len;
                ftiter = FTIndexIterator.intersect(ftiter, ii, 0);
              } else {
                if(ii.size() == 0) continue;
                length = Math.max(len, length);
                ftiter = FTIndexIterator.union(ftiter, ii);
              }
            }
          }
        }
        return ftiter == null || !ftiter.more() ? null :
          new FTNode(ftiter.matches(), data, ftiter.pre(), length, ftiter.size());
      }
    };
  }

  /**
   * Returns a scan-based index iterator.
   * @param lexer lexer, including the queried value
   * @param ftt full-text tokenizer
   * @param data data reference
   * @return node iterator
   * @throws QueryException query exception
   */
  private FTIndexIterator scan(final FTLexer lexer, final FTTokenizer ftt, final Data data)
      throws QueryException {

    final FTLexer input = new FTLexer(ftOpt);
    final FTTokens fttokens = ftt.cache(lexer.token());
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
   * Returns all tokens of the query input.
   * @param qc query context
   * @return token list
   * @throws QueryException query exception
   */
  private TokenList inputs(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    final Iter iter = query.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      // skip empty tokens if not all results are needed
      final byte[] token = toToken(item);
      if(token.length != 0 || mode == FTMode.ALL || mode == FTMode.ALL_WORDS) tl.add(token);
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
      for(final byte[] input : inputs) {
        final FTTokens tokens = ftt.cache(input);
        num = Math.max(num, contains(tokens, lexer, ftt) * tokens.firstSize());
      }
      return num;
    }

    // find and count all occurrences
    final boolean all = mode == FTMode.ALL || mode == FTMode.ALL_WORDS;
    int oc = 0;
    for(final byte[] input : unique(inputs(qc))) {
      final FTTokens tokens = ftt.cache(input);
      final int o = contains(tokens, lexer, ftt);
      if(all && o == 0) return 0;
      num = Math.max(num, o * tokens.firstSize());
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
   * @param tokens cached query tokens
   * @param input input text
   * @param ftt full-text tokenizer
   * @return number of occurrences
   * @throws QueryException query exception
   */
  private int contains(final FTTokens tokens, final FTLexer input, final FTTokenizer ftt)
      throws QueryException {

    int count = 0;
    final FTMatches matches = ftt.matches;
    final boolean and = !ftt.first && (mode == FTMode.ALL || mode == FTMode.ALL_WORDS);
    final FTBitapSearch bs = new FTBitapSearch(input.init(), tokens, ftt.cmp);
    while(bs.hasNext()) {
      final int s = bs.next(), e = s + tokens.firstSize() - 1;
      if(and) matches.and(s, e);
      else matches.or(s, e);
      count++;
    }

    matches.pos++;
    ftt.first = false;
    return count;
  }

  /**
   * Caches and returns the unique query tokens for the given search mode.
   * @param tokens token list
   * @return token set
   */
  private TokenSet unique(final TokenList tokens) {
    // cache all query tokens in a set (duplicates are removed)
    final TokenSet ts = new TokenSet();
    switch(mode) {
      case ALL:
      case ANY:
        for(final byte[] token : tokens) ts.add(token);
        break;
      case ALL_WORDS:
      case ANY_WORD:
        final FTLexer lexer = new FTLexer(ftOpt);
        for(final byte[] token : tokens) {
          lexer.init(token);
          while(lexer.hasNext()) ts.add(lexer.nextToken());
        }
        break;
      case PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        for(final byte[] token : tokens) tb.add(token).add(' ');
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
    final ThreadLocal<FTTokenizer> tl = qc.threads.get(this);
    FTTokenizer ftt = tl.get();
    if(ftt == null) {
      ftt = new FTTokenizer(ftOpt, qc.context.options.get(MainOptions.LSERROR), info);
      tl.set(ftt);
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
    final Data data = ii.db.data();
    if(data == null && !ii.enforce() || occ != null) return false;

    if(data != null) {
      /* index will be applied if no explicit match options have been set
       * that conflict with the index options. As a consequence, though, index-
       * based querying might yield other results than sequential scanning. */
      final MetaData md = data.meta;
      if(ftOpt.cs != null && md.casesens == (ftOpt.cs == FTCase.INSENSITIVE) ||
          ftOpt.isSet(DC) && md.diacritics != ftOpt.is(DC) ||
          ftOpt.isSet(ST) && md.stemming != ftOpt.is(ST) ||
          ftOpt.ln != null && !ftOpt.ln.equals(md.language)) return false;
      // assign database options
      ftOpt.assign(md);
    }

    // estimate costs if text is not known at compile time
    if(inputs == null) {
      ii.costs = ii.enforce() ? IndexCosts.ENFORCE_DYNAMIC :
        IndexCosts.get(Math.max(2, data.meta.size / 30));
    } else {
      // summarize number of hits; break loop if no hits are expected
      ii.costs = IndexCosts.ZERO;
      final FTLexer lexer = new FTLexer(ftOpt);
      final TokenSet ts = new TokenSet();
      final StopWords sw = ftOpt.sw;
      for(final byte[] input : inputs) {
        lexer.init(input);
        while(lexer.hasNext()) {
          final byte[] token = lexer.nextToken();
          if(!ts.add(token) || sw != null && sw.contains(token)) continue;

          // don't use index if token starts with a wildcard
          if(ftOpt.is(WC) && token[0] == '.') return false;
          // favor full-text index requests over exact queries
          final IndexCosts ic = ii.costs(data, lexer);
          if(ic == null) return false;
          final int r = ic.results();
          if(r != 0) ii.costs = IndexCosts.add(ii.costs, IndexCosts.get(Math.max(2, r / 100)));
        }
      }
    }
    db = ii.db;
    return true;
  }

  @Override
  public boolean usesExclude() {
    return occ != null;
  }

  @Override
  public boolean has(final Flag... flags) {
    if(occ != null) for(final Expr o : occ) {
      if(o.has(flags)) return true;
    }
    return query.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    if(occ != null) {
      for(final Expr o : occ) {
        if(!o.inlineable(ic)) return false;
      }
    }
    return query.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return occ != null ? VarUsage.sum(var, occ).plus(query.count(var)) : query.count(var);
  }

  @Override
  public FTExpr inline(final InlineContext ic) throws QueryException {
    boolean changed = occ != null && ic.inline(occ);
    final Expr inlined = query.inline(ic);
    if(inlined != null) {
      query = inlined;
      changed = true;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final FTWords ftw = new FTWords(info, query.copy(cc, vm), mode,
        occ == null ? null : Arr.copyAll(cc, vm, occ));
    ftw.simple = simple;
    ftw.compiled = compiled;
    ftw.inputs = inputs;
    ftw.ftOpt = ftOpt;
    if(db != null) ftw.db = db.copy(cc, vm);
    return copyType(ftw);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && query.accept(visitor) &&
        (occ == null || visitAll(visitor, occ));
  }

  @Override
  public int exprSize() {
    int size = 1;
    if(occ != null) for(final Expr o : occ) size += o.exprSize();
    for(final Expr expr : exprs) size += expr.exprSize();
    return size + query.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTWords)) return false;
    final FTWords f = (FTWords) obj;
    return query.equals(f.query) && mode == f.mode && Objects.equals(db, f.db) &&
        Objects.equals(ftOpt, f.ftOpt) && Array.equals(occ, f.occ) && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), ftOpt, occ, query);
  }

  @Override
  public void plan(final QueryString qs) {
    if(query instanceof AStr) {
      qs.token(query);
    } else {
      qs.brace(query);
    }
    switch(mode) {
      case ALL:
        qs.token(ALL);
        break;
      case ALL_WORDS:
        qs.token(ALL).token(WORDS);
        break;
      case ANY_WORD:
        qs.token(ANY).token(WORD);
        break;
      case PHRASE:
        qs.token(PHRASE);
        break;
      default:
    }
    if(occ != null) qs.token(OCCURS).token(occ[0]).token(TO).token(occ[1]).token(TIMES);
    if(ftOpt != null) qs.token(ftOpt);
  }
}
