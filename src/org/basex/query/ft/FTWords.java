package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.ft.FTOpt.FTMode;
import org.basex.query.item.FTItem;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * FTWords expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTWords extends FTExpr {
  /** Minimum and maximum occurrences. */
  private final Expr[] occ;
  /** Search mode. */
  private final FTMode mode;
  /** Expression list. */
  private Expr query;
  /** Single word. */
  private byte[] word;

  /**
   * Constructor.
   * @param e expression
   * @param m search mode
   * @param o occurrences
   */
  public FTWords(final Expr e, final FTMode m, final Expr[] o) {
    query = e;
    mode = m;
    occ = o;
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    if(occ != null) {
      for(int o = 0; o < occ.length; o++) occ[o] = occ[o].comp(ctx);
    }
    query = query.comp(ctx);
    if(query instanceof Str) word = ((Str) query).str();
    return this;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem node = new FTItem(0);
    final int c = contains(ctx, node);
    double s = c == 0 ? 0 : ctx.score.word(c, ctx.fttoken.size());

    // evaluate weight
    final Expr w = ctx.ftopt.weight;
    if(w != null) {
      final double d = checkDbl(w, ctx);
      if(Math.abs(d) > 1000) Err.or(FTWEIGHT, d);
      s *= d;
    }
    node.score(s);
    return node;
  }

  /**
   * Evaluates the full-text match.
   * @param ctx query context
   * @param node resulting node
   * @return length value, used for scoring
   * @throws QueryException xquery exception
   */
  private int contains(final QueryContext ctx, final FTItem node)
      throws QueryException {

    // speed up default case
    final FTOpt opt = ctx.ftopt;
    if(mode == FTMode.ANY && word != null && occ == null)
      return opt.contains(word, ctx.fttoken, node) == 0 ? 0 : word.length;

    // process special cases
    final Iter iter = ctx.iter(query);
    int len = 0;
    int o = 0;
    byte[] it;

    switch(mode) {
      case ALL:
        while((it = nextStr(iter)) != null) {
          final int oc = opt.contains(it, ctx.fttoken, node);
          if(oc == 0) return 0;
          len += it.length;
          o += oc / ctx.ftopt.qu.count();
        }
        break;
      case ALLWORDS:
        while((it = nextStr(iter)) != null) {
          for(final byte[] txt : Token.split(it, ' ')) {
            final int oc = opt.contains(txt, ctx.fttoken, node);
            if(oc == 0) return 0;
            len += txt.length;
            o += oc;
          }
        }
        break;
      case ANY:
        while((it = nextStr(iter)) != null) {
          final int oc = opt.contains(it, ctx.fttoken, node);
          len += it.length;
          final int c = opt.qu.count();
          o += c > 0 ? oc / c : 0;
        }
        break;
      case ANYWORD:
        while((it = nextStr(iter)) != null) {
          for(final byte[] txt : Token.split(it, ' ')) {
            final int oc = opt.contains(txt, ctx.fttoken, node);
            len += txt.length;
            o += oc;
          }
        }
        break;
      case PHRASE:
        final TokenBuilder tb = new TokenBuilder();
        while((it = nextStr(iter)) != null) {
          tb.add(it);
          tb.add(' ');
        }
        final int oc = opt.contains(tb.finish(), ctx.fttoken, node);
        len += tb.size;
        final int c = opt.qu.count();
        o += c > 0 ? oc / c : 0;
        break;
    }

    long mn = 1;
    long mx = Long.MAX_VALUE;
    if(occ != null) {
      mn = checkItr(occ[0], ctx);
      mx = checkItr(occ[1], ctx);
    }
    return o < mn || o > mx ? 0 : Math.max(1, len);
  }

  /**
   * Checks if the next item is a string.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  private byte[] nextStr(final Iter iter) throws QueryException {
    final Item it = iter.next();
    if(it == null) return null;
    if(!it.s() && !it.u()) Err.type(info(), Type.STR, it);
    return it.str();
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    /*
     * If the following conditions yield true, the index is accessed:
     * - the query is a simple String item
     * - no FTTimes option and no weight is specified
     * - FTMode is different to ANY, ALL and PHRASE
     * - case sensitivity, diacritics and stemming flags comply with index
     * - no stop words are specified
     */
    final MetaData md = ic.data.meta;
    final FTOpt fto = ic.ctx.ftopt;
    if(word == null || occ != null || ic.ctx.ftopt.weight != null ||
        mode != FTMode.ANY && mode != FTMode.ALL && mode != FTMode.PHRASE ||
        md.ftcs != fto.is(FTOpt.CS) || md.ftdc != fto.is(FTOpt.DC) ||
        md.ftst != fto.is(FTOpt.ST) || fto.sw != null) return false;

    // limit index access to trie version and simple wildcard patterns
    if(fto.is(FTOpt.WC)) {
      if(md.ftfz || word[0] == '.') return false;
      int d = 0;
      for(final byte w : word) {
        if(w == '{' || w == '\\' || w == '.' && ++d > 1) return false;
      }
    }

    // summarize number of hits; break loop if no hits are expected
    final Tokenizer ft = new Tokenizer(word, fto);
    ic.is = 0;
    while(ft.more()) {
      final double s = ic.data.nrIDs(ft);
      if(s == 0) {
        ic.is = 0;
        break;
      }
      ic.is += s;
    }
    return true;
  }

  @Override
  public FTExpr indexEquivalent(final IndexContext ic) {
    return new FTIndex(ic.data, word);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    query.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return query.toString();
  }

  @Override
  public boolean usesExclude() {
    return occ != null;
  }
}
