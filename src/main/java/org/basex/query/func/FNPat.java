package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.RegEx;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

/**
 * String pattern functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNPat extends StandardFunc {
  /** Slash pattern. */
  private static final Pattern SLASH = Pattern.compile("\\$");
  /** Slash pattern. */
  private static final Pattern BSLASH = Pattern.compile("\\\\");

  /** Root element for the analyze-string-result function. */
  private static final QNm ANALYZE = new QNm("fn:analyze-string-result", FNURI);
  /** Element for the analyze-string-result function. */
  private static final QNm MATCH = new QNm("fn:match", FNURI);
  /** Element for the analyze-string-result function. */
  private static final QNm NONMATCH = new QNm("fn:non-match", FNURI);
  /** Element for the analyze-string-result function. */
  private static final QNm MGROUP = new QNm("fn:group", FNURI);
  /** Attribute for the analyze-string-result function. */
  private static final QNm NR = new QNm("nr");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNPat(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case TOKENIZE:   return tokenize(checkEStr(expr[0], ctx), ctx);
      default:         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
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
   * @throws org.basex.query.QueryException query exception
   */
  private Item matches(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    return Bln.get(p.matcher(string(val)).find());
  }

  /**
   * Evaluates the analyze-string function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws org.basex.query.QueryException query exception
   */
  private Item analyzeString(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    if(p.matcher("").matches()) REGROUP.thrw(input);
    final String str = string(val);
    final Matcher m = p.matcher(str);

    final FElem root = new FElem(ANALYZE, new Atts(FN, FNURI));
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

    final FElem nd = new FElem(g == 0 ? MATCH : MGROUP);
    if(g > 0) nd.add(new FAttr(NR, token(g)));

    final int start = m.start(g), end = m.end(g), gc = m.groupCount();
    int[] pos = { g + 1, start }; // group and position in string
    while(pos[0] <= gc && m.end(pos[0]) <= end) {
      final int st = m.start(pos[0]);
      if(st >= 0) { // group matched
        if(pos[1] < st) nd.add(new FTxt(token(str.substring(pos[1], st))));
        pos = match(m, str, nd, pos[0]);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      nd.add(new FTxt(token(str.substring(pos[1], end))));
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
    final FElem sub = new FElem(NONMATCH);
    sub.add(new FTxt(token(text)));
    par.add(sub);
  }

  /**
   * Evaluates the replace function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws org.basex.query.QueryException query exception
   */
  private Item replace(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final byte[] rep = checkStr(expr[2], ctx);
    for(int i = 0; i < rep.length; ++i) {
      if(rep[i] == '\\') {
        if(i + 1 == rep.length || rep[i + 1] != '\\' && rep[i + 1] != '$')
          FUNREGREP.thrw(input);
        ++i;
      }
    }

    final Pattern p = pattern(expr[1], expr.length == 4 ? expr[3] : null, ctx);
    String r = string(rep);
    if((p.flags() & Pattern.LITERAL) != 0) {
      r = SLASH.matcher(BSLASH.matcher(r).replaceAll("\\\\\\\\")).
        replaceAll("\\\\\\$");
    }

    try {
      return Str.get(p.matcher(string(val)).replaceAll(r));
    } catch(final Exception ex) {
      final String m = ex.getMessage();
      if(m.contains("No group")) REGROUP.thrw(input);
      throw REGERR.thrw(input, m);
    }
  }

  /**
   * Evaluates the tokenize function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws org.basex.query.QueryException query exception
   */
  private Iter tokenize(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    if(p.matcher("").matches()) REGROUP.thrw(input);

    final ItemCache ic = new ItemCache();
    final String str = string(val);
    if(!str.isEmpty()) {
      final Matcher m = p.matcher(str);
      int s = 0;
      while(m.find()) {
        ic.add(Str.get(str.substring(s, m.start())));
        s = m.end();
      }
      ic.add(Str.get(str.substring(s, str.length())));
    }
    return ic;
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern input pattern
   * @param mod modifier item
   * @param ctx query context
   * @return modified pattern
   * @throws org.basex.query.QueryException query exception
   */
  private Pattern pattern(final Expr pattern, final Expr mod,
      final QueryContext ctx) throws QueryException {

    return new RegEx(string(checkStr(pattern, ctx)), input).pattern(
        mod != null ? checkStr(mod, ctx) : null, ctx.xquery3);
  }

  @Override
  public boolean uses(final Use u) {
    return sig == Function.ANALYZE_STRING && (u == Use.X30 || u == Use.CNS) ||
        super.uses(u);
  }
}
