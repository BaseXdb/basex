package org.basex.query.func.ft;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.ft.*;
import org.basex.query.func.*;
import org.basex.query.func.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * Full-text functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNFt extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("options");
  /** Marker element. */
  private static final byte[] MARK = Token.token("mark");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _FT_CONTAINS: return contains(qc);
      case _FT_COUNT:    return count(qc);
      default:           return super.item(qc, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _FT_SEARCH:   return search(qc);
      case _FT_SCORE:    return score(qc);
      case _FT_MARK:     return mark(qc, false);
      case _FT_EXTRACT:  return mark(qc, true);
      case _FT_TOKENS:   return tokens(qc);
      case _FT_TOKENIZE: return tokenize(qc);
      default:           return super.iter(qc);
    }
  }

  /**
   * Performs the count function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item count(final QueryContext qc) throws QueryException {
    final FTPosData tmp = qc.ftPosData;
    qc.ftPosData = new FTPosData();
    final Iter ir = qc.iter(exprs[0]);
    for(Item it; (it = ir.next()) != null;) toDBNode(it);
    final int s = qc.ftPosData.size();
    qc.ftPosData = tmp;
    return Int.get(s);
  }

  /**
   * Performs the mark function.
   * @param qc query context
   * @param ex extract flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter mark(final QueryContext qc, final boolean ex) throws QueryException {
    byte[] m = MARK;
    int l = ex ? 150 : Integer.MAX_VALUE;

    if(exprs.length > 1) {
      // name of the marker element; default is <mark/>
      m = toToken(exprs[1], qc);
      if(!XMLToken.isQName(m)) throw valueError(info, AtomType.QNM, m);
    }
    if(exprs.length > 2) {
      l = (int) toLong(exprs[2], qc);
    }
    final byte[] mark = m;
    final int len = l;

    return new Iter() {
      final FTPosData ftd = new FTPosData();
      Iter ir;
      ValueIter vi;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(vi != null) {
            final Item it = vi.next();
            if(it != null) return it;
            vi = null;
          }
          final FTPosData tmp = qc.ftPosData;
          try {
            qc.ftPosData = ftd;
            if(ir == null) ir = qc.iter(exprs[0]);
            final Item it = ir.next();
            if(it == null) return null;

            // copy node to main memory data instance
            final MemData md = new MemData(qc.context.options);
            final DataBuilder db = new DataBuilder(md);
            db.ftpos(mark, qc.ftPosData, len).build(toDBNode(it));

            final IntList il = new IntList();
            for(int p = 0; p < md.meta.size; p += md.size(p, md.kind(p))) il.add(p);
            vi = DBNodeSeq.get(il, md, false, false).iter();
          } finally {
            qc.ftPosData = tmp;
          }
        }
      }
    };
  }

  /**
   * Performs the score function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter score(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter iter = exprs[0].iter(qc);
      @Override
      public Dbl next() throws QueryException {
        final Item item = iter.next();
        return item == null ? null : Dbl.get(item.score());
      }
    };
  }

  /**
   * Performs the contains function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Bln contains(final QueryContext qc) throws QueryException {
    final Value input = qc.value(exprs[0]);
    final Value query = qc.value(exprs[1]);
    final FTOptions opts = toOptions(2, Q_OPTIONS, new FTOptions(), qc);

    final FTOpt opt = new FTOpt();
    final FTMode mode = opts.get(FTIndexOptions.MODE);
    opt.set(FZ, opts.get(FTIndexOptions.FUZZY));
    opt.set(WC, opts.get(FTIndexOptions.WILDCARDS));
    opt.set(DC, opts.get(FTOptions.DIACRITICS) == FTDiacritics.SENSITIVE);
    opt.set(ST, opts.get(FTOptions.STEMMING));
    opt.ln = Language.get(opts.get(FTOptions.LANGUAGE));
    opt.cs = opts.get(FTOptions.CASE);
    if(opt.is(FZ) && opt.is(WC)) throw BXFT_MATCH.get(info, this);

    final FTOpt tmp = qc.ftOpt();
    qc.ftOpt(opt);
    final FTExpr fte = new FTWords(info, query, mode, null).compile(qc, null);
    qc.ftOpt(tmp);
    return new FTContainsExpr(input, options(fte, opts), info).item(qc, info);
  }

  /**
   * Performs the search function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter search(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final Value terms = qc.value(exprs[1]);
    final FTOptions opts = toOptions(2, Q_OPTIONS, new FTOptions(), qc);

    final IndexContext ic = new IndexContext(data, false);
    if(!data.meta.ftxtindex) throw BXDB_INDEX_X.get(info, data.meta.name,
        IndexType.FULLTEXT.toString().toLowerCase(Locale.ENGLISH));

    final FTOpt opt = new FTOpt().copy(data.meta);
    final FTMode mode = opts.get(FTIndexOptions.MODE);
    opt.set(FZ, opts.get(FTIndexOptions.FUZZY));
    opt.set(WC, opts.get(FTIndexOptions.WILDCARDS));
    if(opt.is(FZ) && opt.is(WC)) throw BXFT_MATCH.get(info, this);

    final FTOpt tmp = qc.ftOpt();
    qc.ftOpt(opt);
    final FTExpr fte = new FTWords(info, data, terms, mode).compile(qc, null);
    qc.ftOpt(tmp);
    return new FTIndexAccess(info, options(fte, opts), ic).iter(qc);
  }

  /**
   * Performs the search function.
   * @param ftexpr full-text expression
   * @param opts full-text options
   * @return expressions
   */
  private FTExpr options(final FTExpr ftexpr, final FTOptions opts) {
    FTExpr fte = ftexpr;
    if(opts != null) {
      if(opts.get(FTIndexOptions.ORDERED)) {
        fte = new FTOrder(info, fte);
      }
      if(opts.contains(FTIndexOptions.DISTANCE)) {
        final FTDistanceOptions fopts = opts.get(FTIndexOptions.DISTANCE);
        final Int min = Int.get(fopts.get(FTDistanceOptions.MIN));
        final Int max = Int.get(fopts.get(FTDistanceOptions.MAX));
        final FTUnit unit = fopts.get(FTDistanceOptions.UNIT);
        fte = new FTDistance(info, fte, min, max, unit);
      }
      if(opts.contains(FTIndexOptions.WINDOW)) {
        final FTWindowOptions fopts = opts.get(FTIndexOptions.WINDOW);
        final Int sz = Int.get(fopts.get(FTWindowOptions.SIZE));
        final FTUnit unit = fopts.get(FTWindowOptions.UNIT);
        fte = new FTWindow(info, fte, sz, unit);
      }
      if(opts.contains(FTIndexOptions.SCOPE)) {
        final FTScopeOptions fopts = opts.get(FTIndexOptions.SCOPE);
        final boolean same = fopts.get(FTScopeOptions.SAME);
        final FTUnit unit = fopts.get(FTScopeOptions.UNIT).unit();
        fte = new FTScope(info, fte, same, unit);
      }
      if(opts.contains(FTIndexOptions.CONTENT)) {
        final FTContents cont = opts.get(FTIndexOptions.CONTENT);
        fte = new FTContent(info, fte, cont);
      }
    }
    return fte;
  }

  /**
   * Performs the tokens function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tokens(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    byte[] entry = exprs.length < 2 ? Token.EMPTY : toToken(exprs[1], qc);
    if(entry.length != 0) {
      final FTLexer ftl = new FTLexer(new FTOpt().copy(data.meta));
      ftl.init(entry);
      entry = ftl.nextToken();
    }
    return FNIndex.entries(data, new IndexEntries(entry, IndexType.FULLTEXT), this);
  }

  /**
   * Performs the tokenize function.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tokenize(final QueryContext qc) throws QueryException {
    final FTOpt opt = new FTOpt().copy(qc.ftOpt());
    final FTLexer ftl = new FTLexer(opt).init(toToken(exprs[0], qc));
    return new Iter() {
      @Override
      public Str next() {
        return ftl.hasNext() ? Str.get(ftl.nextToken()) : null;
      }
    };
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(oneOf(func, _FT_SEARCH, _FT_TOKENS) && !dataLock(visitor, 1)) && super.accept(visitor);
  }
}
