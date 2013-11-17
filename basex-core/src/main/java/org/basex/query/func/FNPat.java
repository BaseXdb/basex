package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.regex.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * String pattern functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNPat extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<Pattern> patterns = new TokenObjMap<Pattern>();

  /** Slash pattern. */
  private static final Pattern SLASH = Pattern.compile("\\$");
  /** Slash pattern. */
  private static final Pattern BSLASH = Pattern.compile("\\\\");

  /** Module prefix. */
  private static final String PREFIX = "fn";
  /** QName. */
  private static final QNm Q_ANALYZE = QNm.get(PREFIX, "analyze-string-result", FNURI);
  /** QName. */
  private static final QNm Q_MATCH = QNm.get(PREFIX, "match", FNURI);
  /** QName. */
  private static final QNm Q_NONMATCH = QNm.get(PREFIX, "non-match", FNURI);
  /** QName. */
  private static final QNm Q_MGROUP = QNm.get(PREFIX, "group", FNURI);

  /** Attribute for the analyze-string-result function. */
  private static final String NR = "nr";

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNPat(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case TOKENIZE: return tokenize(ctx).iter();
      default:       return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case TOKENIZE: return tokenize(ctx);
      default:       return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case MATCHES:        return matches(checkEStr(expr[0], ctx), ctx);
      case REPLACE:        return replace(checkEStr(expr[0], ctx), ctx);
      case ANALYZE_STRING: return analyzeString(checkEStr(expr[0], ctx), ctx);
      default:             return super.item(ctx, ii);
    }
  }

  /**
   * Evaluates the match function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws QueryException query exception
   */
  private Item matches(final byte[] val, final QueryContext ctx) throws QueryException {
    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    return Bln.get(p.matcher(string(val)).find());
  }

  /**
   * Evaluates the analyze-string function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws QueryException query exception
   */
  private Item analyzeString(final byte[] val, final QueryContext ctx) throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    if(p.matcher("").matches()) throw REGROUP.get(info);
    final String str = string(val);
    final Matcher m = p.matcher(str);

    final FElem root = new FElem(Q_ANALYZE).declareNS();
    int s = 0;
    while(m.find()) {
      if(s != m.start()) nonmatch(str.substring(s, m.start()), root);
      match(m, str, root, 0);
      s = m.end();
    }
    if(s != str.length()) nonmatch(str.substring(s), root);
    return root;
  }

  /**
   * Processes a match.
   * @param m matcher
   * @param str string
   * @param par parent
   * @param g group number
   * @return next group number and position in string
   */
  private static int[] match(final Matcher m, final String str, final FElem par,
      final int g) {

    final FElem nd = new FElem(g == 0 ? Q_MATCH : Q_MGROUP);
    if(g > 0) nd.add(NR, token(g));

    final int start = m.start(g), end = m.end(g), gc = m.groupCount();
    int[] pos = { g + 1, start }; // group and position in string
    while(pos[0] <= gc && m.end(pos[0]) <= end) {
      final int st = m.start(pos[0]);
      if(st >= 0) { // group matched
        if(pos[1] < st) nd.add(str.substring(pos[1], st));
        pos = match(m, str, nd, pos[0]);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      nd.add(str.substring(pos[1], end));
      pos[1] = end;
    }
    par.add(nd);
    return pos;
  }

  /**
   * Processes a non-match.
   * @param text text
   * @param par root node
   */
  private static void nonmatch(final String text, final FElem par) {
    par.add(new FElem(Q_NONMATCH).add(text));
  }

  /**
   * Evaluates the replace function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws QueryException query exception
   */
  private Item replace(final byte[] val, final QueryContext ctx) throws QueryException {
    final byte[] rep = checkStr(expr[2], ctx);
    for(int i = 0; i < rep.length; ++i) {
      if(rep[i] == '\\') {
        if(i + 1 == rep.length || rep[i + 1] != '\\' && rep[i + 1] != '$')
          throw FUNREPBS.get(info);
        ++i;
      }
      if(rep[i] == '$' && (i == 0 || rep[i - 1] != '\\') &&
        (i + 1 == rep.length || !digit(rep[i + 1]))) throw FUNREPDOL.get(info);
    }

    final Pattern p = pattern(expr[1], expr.length == 4 ? expr[3] : null, ctx);
    if(p.pattern().isEmpty()) throw REGROUP.get(info);

    String r = string(rep);
    if((p.flags() & Pattern.LITERAL) != 0) {
      r = SLASH.matcher(BSLASH.matcher(r).replaceAll("\\\\\\\\")).replaceAll("\\\\\\$");
    }

    try {
      return Str.get(p.matcher(string(val)).replaceAll(r));
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) throw REGROUP.get(info);
      throw REGPAT.get(info, ex);
    }
  }

  /**
   * Evaluates the tokenize function.
   * @param ctx query context
   * @return function result
   * @throws QueryException query exception
   */
  private Value tokenize(final QueryContext ctx) throws QueryException {
    final byte[] val = checkEStr(expr[0], ctx);
    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    if(p.matcher("").matches()) throw REGROUP.get(info);

    final TokenList tl = new TokenList();
    final String str = string(val);
    if(!str.isEmpty()) {
      final Matcher m = p.matcher(str);
      int s = 0;
      while(m.find()) {
        tl.add(str.substring(s, m.start()));
        s = m.end();
      }
      tl.add(str.substring(s, str.length()));
    }
    return StrSeq.get(tl);
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern input pattern
   * @param modifier modifier item
   * @param ctx query context
   * @return pattern modifier
   * @throws QueryException query exception
   */
  private Pattern pattern(final Expr pattern, final Expr modifier,
      final QueryContext ctx) throws QueryException {

    final byte[] pat = checkStr(pattern, ctx);
    final byte[] mod = modifier != null ? checkStr(modifier, ctx) : null;
    final TokenBuilder tb = new TokenBuilder(pat);
    if(mod != null) tb.add(0).add(mod);
    final byte[] key = tb.finish();
    Pattern p = patterns.get(key);
    if(p == null) {
      p = RegExParser.parse(pat, mod, sc.xquery3(), info);
      patterns.put(key, p);
    }
    return p;
  }
}
