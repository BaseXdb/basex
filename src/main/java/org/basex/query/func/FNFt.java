package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.ft.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Full-text functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNFt extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_FTOPTIONS = new QNm("options");
  /** Marker element. */
  private static final byte[] MARK = token("mark");
  /** Fuzzy option. */
  private static final byte[] FUZZY = token("fuzzy");
  /** Wildcards option. */
  private static final byte[] WILDCARDS = token("wildcards");
  /** Search mode. */
  private static final byte[] MODE = token("mode");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFt(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _FT_COUNT: return count(ctx);
      default:        return super.item(ctx, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _FT_SEARCH:   return search(ctx);
      case _FT_SCORE:    return score(ctx);
      case _FT_MARK:     return mark(ctx, false);
      case _FT_EXTRACT:  return mark(ctx, true);
      case _FT_TOKENS:   return tokens(ctx);
      case _FT_TOKENIZE: return tokenize(ctx);
      default:           return super.iter(ctx);
    }
  }

  /**
   * Performs the count function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item count(final QueryContext ctx) throws QueryException {
    final FTPosData tmp = ctx.ftpos;
    ctx.ftpos = new FTPosData();
    final Iter ir = ctx.iter(expr[0]);
    for(Item it; (it = ir.next()) != null;) checkDBNode(it);
    final int s = ctx.ftpos.size();
    ctx.ftpos = tmp;
    return Int.get(s);
  }

  /**
   * Performs the mark function.
   * @param ctx query context
   * @param ex extract flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter mark(final QueryContext ctx, final boolean ex) throws QueryException {
    byte[] m = MARK;
    int l = ex ? 150 : Integer.MAX_VALUE;

    if(expr.length > 1) {
      // name of the marker element; default is <mark/>
      m = checkStr(expr[1], ctx);
      if(!XMLToken.isQName(m)) Err.value(info, AtomType.QNM, m);
    }
    if(expr.length > 2) {
      l = (int) checkItr(expr[2], ctx);
    }
    final byte[] mark = m;
    final int len = l;

    return new Iter() {
      final FTPosData ftd = new FTPosData();
      ValueIter vi;
      Iter ir;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(vi != null) {
            final Item it = vi.next();
            if(it != null) return it;
            vi = null;
          }
          final FTPosData tmp = ctx.ftpos;
          ctx.ftpos = ftd;
          if(ir == null) ir = ctx.iter(expr[0]);
          final Item it = ir.next();
          if(it != null) {
            // copy node to main memory data instance
            final MemData md = new MemData(ctx.context.prop);
            final DataBuilder db = new DataBuilder(md);
            db.ftpos(mark, ctx.ftpos, len).build(checkDBNode(it));

            final IntList il = new IntList();
            for(int p = 0; p < md.meta.size; p += md.size(p, md.kind(p))) {
              il.add(p);
            }
            vi = DBNodeSeq.get(il, md, false, false).iter();
          }
          ctx.ftpos = tmp;
          if(it == null) return null;
        }
      }
    };
  }

  /**
   * Performs the score function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter score(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final Iter iter = expr[0].iter(ctx);

      @Override
      public Dbl next() throws QueryException {
        final Item item = iter.next();
        return item == null ? null : Dbl.get(item.score());
      }
    };
  }

  /**
   * Performs the search function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter search(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    final Value terms = ctx.value(expr[1]);
    final Item opt = expr.length > 2 ? expr[2].item(ctx, info) : null;
    final TokenMap tm = new FuncParams(Q_FTOPTIONS, info).parse(opt);
    return search(data, terms, tm, this, ctx);
  }

  /**
   * Performs an index-based search.
   * @param data data reference
   * @param terms query terms
   * @param map map with full-text options
   * @param fun calling function
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  static Iter search(final Data data, final Value terms, final TokenMap map,
      final StandardFunc fun, final QueryContext ctx) throws QueryException {

    final InputInfo info = fun.info;
    final IndexContext ic = new IndexContext(ctx, data, null, true);
    if(!data.meta.ftxtindex) BXDB_INDEX.thrw(info, data.meta.name,
        IndexType.FULLTEXT.toString().toLowerCase(Locale.ENGLISH));

    final FTOpt tmp = ctx.ftOpt();
    final FTOpt opt = new FTOpt().copy(data.meta);
    FTMode m = FTMode.ANY;
    if(map != null) {
      for(final byte[] k : map) {
        final byte[] v = map.get(k);
        if(eq(k, FUZZY)) {
          final boolean t = v.length == 0 || Util.yes(string(v));
          opt.set(FZ, t);
        } else if(eq(k, WILDCARDS)) {
          final boolean t = v.length == 0 || Util.yes(string(v));
          opt.set(WC, t);
        } else if(eq(k, MODE)) {
          m = FTMode.get(v);
          if(m == null) ELMOPTION.thrw(info, v);
        } else {
          ELMOPTION.thrw(info, k);
        }
      }
    }

    ctx.ftOpt(opt);
    final FTWords words = new FTWords(info, ic.data, terms, m, ctx);
    ctx.ftOpt(tmp);
    return new FTIndexAccess(info, words, ic.data.meta.name, ic.iterable).iter(ctx);
  }

  /**
   * Performs the tokens function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tokens(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    byte[] entry = expr.length < 2 ? Token.EMPTY : checkStr(expr[1], ctx);
    if(entry.length != 0) {
      final FTLexer ftl = new FTLexer(new FTOpt().copy(data.meta));
      ftl.init(entry);
      entry = ftl.nextToken();
    }
    return FNIndex.entries(data, new IndexEntries(entry, IndexType.FULLTEXT), this);
  }

  /**
   * Performs the tokenize function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tokenize(final QueryContext ctx) throws QueryException {
    final FTOpt opt = new FTOpt().copy(ctx.ftOpt());
    final FTLexer ftl = new FTLexer(opt).init(checkStr(expr[0], ctx));
    return new Iter() {
      @Override
      public Str next() {
        return ftl.hasNext() ? Str.get(ftl.nextToken()) : null;
      }
    };
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return sig == Function._FT_SEARCH || super.iterable();
  }

  @Override
  public boolean uses(final Use u) {
    // skip evaluation at compile time
    return u == Use.CTX && oneOf(sig, _FT_SEARCH, _FT_TOKENS) || super.uses(u);
  }

  @Override
  public boolean databases(final StringList db, final boolean rootContext) {
    if(oneOf(sig, _FT_SEARCH, _FT_TOKENS)) {
      if(!(expr[0] instanceof Str)) return false;
      db.add(string(((Str) expr[0]).string()));
      return true;
    }
    return super.databases(db, rootContext);
  }
}
