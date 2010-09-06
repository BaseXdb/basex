package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
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
import org.basex.query.iter.NodIter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * String pattern functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNPat extends Fun {
  /** Function namespace. */
  private static final Uri FNNS = Uri.uri(FNURI);
  /** Classes pattern. */
  private static final Pattern CLASSES =
    Pattern.compile(".*?\\[([a-zA-Z])-([a-zA-Z]).*");
  /** Excluded classes pattern. */
  private static final Pattern EXCLASSES =
    Pattern.compile(".*?\\[(.*?)-\\[(.*?)\\]");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNPat(final InputInfo ii, final FunDef f, final Expr... e) {
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
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case MATCH:   return matches(checkEStr(expr[0], ctx), ctx);
      case REPLACE: return replace(checkEStr(expr[0], ctx), ctx);
      case ANALZYE: return analyzeString(checkEStr(expr[0], ctx), ctx);
      default:      return super.atomic(ctx, ii);
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
    if(p.matcher("").matches()) Err.or(input, REGROUP);
    final String str = string(val);
    final Matcher m = p.matcher(str);

    final NodIter ch = new NodIter();
    final FElem root = new FElem(new QNm(ANALYZE, FNNS), ch, new NodIter(),
        EMPTY, new Atts().add(FN, FNURI));
    int s = 0;
    while(m.find()) {
      if(s != m.start()) nonmatch(str.substring(s, m.start()), root, ch);
      match(m, str, root, ch, 0);
      s = m.end();
    }
    if(s != str.length()) nonmatch(str.substring(s), root, ch);
    return root;
  }

  /**
   * Processes a match.
   * @param m matcher
   * @param str string
   * @param par parent
   * @param ch child iterator
   * @param g group number
   * @return next group number and position in string
   */
  private int[] match(final Matcher m, final String str, final FElem par,
      final NodIter ch, final int g) {

    final NodIter sub = new NodIter(), att = new NodIter();
    final FElem nd = new FElem(new QNm(g == 0 ? MATCH : MGROUP, FNNS),
        sub, att, EMPTY, new Atts(), par);
    if(g > 0) att.add(new FAttr(new QNm(NR), token(g), nd));

    final int start = m.start(g), end = m.end(g), gc = m.groupCount();
    int[] pos = { g + 1, start }; // group and position in string
    while(pos[0] <= gc && m.end(pos[0]) <= end) {
      final int st = m.start(pos[0]);
      if(st >= 0) { // group matched
        if(pos[1] < st) sub.add(new FTxt(token(str.substring(pos[1], st)), nd));
        pos = match(m, str, nd, sub, pos[0]);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      sub.add(new FTxt(token(str.substring(pos[1], end)), nd));
      pos[1] = end;
    }
    ch.add(nd);
    return pos;
  }

  /**
   * Processes a non-match.
   * @param text text
   * @param par root node
   * @param ch child iterator
   */
  private void nonmatch(final String text, final FElem par, final NodIter ch) {
    final NodIter txt = new NodIter();
    final FElem sub = new FElem(new QNm(NONMATCH, FNNS), txt, par);
    txt.add(new FTxt(token(text), sub));
    ch.add(sub);
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
          Err.or(input, FUNREGREP);
        i++;
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
      if(m.contains("No group")) Err.or(input, REGROUP);
      Err.or(input, REGERR, m);
      return null;
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
    if(p.matcher("").matches()) Err.or(input, REGROUP);

    final ItemIter sb = new ItemIter();
    final String str = string(val);
    if(!str.isEmpty()) {
      final Matcher m = p.matcher(str);
      int s = 0;
      while(m.find()) {
        sb.add(Str.get(str.substring(s, m.start())));
        s = m.end();
      }
      sb.add(Str.get(str.substring(s, str.length())));
    }
    return sb;
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
        else if(b == 'q' && ctx.xquery11) m |= Pattern.LITERAL;
        else if(b == 'x') {
          boolean cc = false;
          final TokenBuilder tb = new TokenBuilder();
          for(final byte p : pt) {
            if(cc || p < 0 || p > ' ') tb.add(p);
            cc |= p == '[';
            cc &= p != ']';
          }
          pt = tb.finish();
        } else {
          Err.or(input, REGMOD, (char) b);
        }
      }
    }

    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < pt.length; ++i) {
      final byte b = pt[i];
      tb.add(b);
      if(b == '\\' && i + 1 != pt.length && pt[i + 1] == ' ') tb.add(b);
    }

    String str = tb.toString();
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
      while(true) {
        final Matcher mt = EXCLASSES.matcher(str);
        if(!mt.matches()) break;
        final String in = mt.group(1);
        final String ex = mt.group(2);
        String out = in;
        for(int e = 0; e < ex.length(); ++e) {
          out = out.replaceAll(ex.substring(e, e + 1), "");
        }
        str = str.replaceAll("\\[" + in + "-\\[.*?\\]", "[" + out);
      }
    }

    try {
      return Pattern.compile(str, m);
    } catch(final Exception ex) {
      Err.or(input, REGINV, pt);
      return null;
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X11 && def == FunDef.ANALZYE || super.uses(u);
  }
}
