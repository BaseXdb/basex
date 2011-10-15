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
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ByteList;

/**
 * String pattern functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNPat extends FuncCall {
  /** Function namespace. */
  private static final Uri U_FN = Uri.uri(FNURI);

  /** Root element for the analyze-string-result function. */
  private static final QNm ANALYZE =
    new QNm(token("fn:analyze-string-result"), U_FN);
  /** Element for the analyze-string-result function. */
  private static final QNm MATCH = new QNm(token("fn:match"), U_FN);
  /** Element for the analyze-string-result function. */
  private static final QNm NONMATCH = new QNm(token("fn:non-match"), U_FN);
  /** Element for the analyze-string-result function. */
  private static final QNm MGROUP = new QNm(token("fn:group"), U_FN);
  /** Attribute for the analyze-string-result function. */
  private static final QNm NR = new QNm(token("nr"));

  /** Classes pattern. */
  private static final Pattern CLASSES =
    Pattern.compile(".*?\\[([a-zA-Z])-([a-zA-Z]).*");
  /** Excluded classes pattern. */
  private static final Pattern EX =
    Pattern.compile(".*?\\[(.*?)-\\[(.*?)\\].*");

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
    switch(def) {
      case TOKEN:   return tokenize(checkEStr(expr[0], ctx), ctx);
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case MATCH:   return matches(checkEStr(expr[0], ctx), ctx);
      case REPLACE: return replace(checkEStr(expr[0], ctx), ctx);
      case ANALZYE: return analyzeString(checkEStr(expr[0], ctx), ctx);
      default:      return super.item(ctx, ii);
    }
  }

  /**
   * Evaluates the match function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws QueryException query exception
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
   * @throws QueryException query exception
   */
  private Item analyzeString(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    if(p.matcher("").matches()) REGROUP.thrw(input);
    final String str = string(val);
    final Matcher m = p.matcher(str);

    final FElem root = new FElem(ANALYZE, new Atts().add(FN, FNURI), EMPTY);
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
  private int[] match(final Matcher m, final String str, final FElem par,
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
  private void nonmatch(final String text, final FElem par) {
    final FElem sub = new FElem(NONMATCH);
    sub.add(new FTxt(token(text)));
    par.add(sub);
  }

  /**
   * Evaluates the replace function.
   * @param val input value
   * @param ctx query context
   * @return function result
   * @throws QueryException query exception
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
      r = r.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
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
   * @throws QueryException query exception
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
   * Checks the regular expression modifiers.
   * @param pattern pattern
   * @param mod modifier item
   * @param ctx query context
   * @return modified pattern
   * @throws QueryException query exception
   */
  private Pattern pattern(final Expr pattern, final Expr mod,
      final QueryContext ctx) throws QueryException {

    // process modifiers
    byte[] pt = checkStr(pattern, ctx);
    int m = Pattern.UNIX_LINES;
    if(mod != null) {
      for(final byte b : checkStr(mod, ctx)) {
        if(b == 'i') m |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        else if(b == 'm') m |= Pattern.MULTILINE;
        else if(b == 's') m |= Pattern.DOTALL;
        else if(b == 'q' && ctx.xquery3) m |= Pattern.LITERAL;
        else if(b == 'x') {
          boolean cc = false;
          final ByteList bl = new ByteList();
          for(final byte p : pt) {
            if(cc || p < 0 || p > ' ') bl.add(p);
            cc |= p == '[';
            cc &= p != ']';
          }
          pt = bl.toArray();
        } else {
          REGMOD.thrw(input, (char) b);
        }
      }
    }

    final ByteList bl = new ByteList();
    for(int i = 0; i < pt.length; ++i) {
      final byte b = pt[i];
      bl.add(b);
      if(b == '\\' && i + 1 != pt.length && pt[i + 1] == ' ') bl.add(b);
    }

    try {
      String str = bl.toString();
      if((m & Pattern.LITERAL) == 0 && str.indexOf('[') != -1 &&
          str.indexOf('-') != -1) {
        // replace classes by single characters to support Unicode matches
        while(true) {
          final Matcher mt = CLASSES.matcher(str);
          if(!mt.matches()) break;
          final char c1 = mt.group(1).charAt(0);
          final char c2 = mt.group(2).charAt(0);
          final TokenBuilder tb2 = new TokenBuilder("[");
          for(char c = c1; c <= c2; ++c) tb2.add(c);
          str = str.replaceAll("\\[" + c1 + "-" + c2, tb2.toString());
        }

        // remove excluded characters in classes
        String old = "";
        for(Matcher mt; (mt = EX.matcher(str)).matches() && !old.equals(str);) {
          old = str;
          final String in = mt.group(1);
          final String ex = mt.group(2);
          String out = in;
          for(int e = 0; e < ex.length(); ++e) {
            out = out.replaceAll(ex.substring(e, e + 1), "");
          }
          str = str.replaceAll("\\[" + in + "-\\[.*?\\]", "[" + out);
        }
      }
      return Pattern.compile(str, m);
    } catch(final Exception ex) {
      throw REGINV.thrw(input, pt);
    }
  }

  @Override
  public boolean uses(final Use u) {
    return def == Function.ANALZYE && (u == Use.X30 || u == Use.CNS) ||
        super.uses(u);
  }
}
