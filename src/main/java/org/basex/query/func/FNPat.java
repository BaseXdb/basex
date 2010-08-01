package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.util.TokenBuilder;

/**
 * String pattern functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNPat extends Fun {
  /** Classes pattern. */
  private static final Pattern CLASSES =
    Pattern.compile(".*?\\[([a-zA-Z])-([a-zA-Z]).*");
  /** Excluded classes pattern. */
  private static final Pattern EXCLASSES =
    Pattern.compile(".*?\\[(.*?)-\\[(.*?)\\]");

  /**
   * Constructor.
   * @param i query info
   * @param f function definition
   * @param e arguments
   */
  protected FNPat(final QueryInfo i, final FunDef f, final Expr... e) {
    super(i, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case TOKEN:   return tokenize(checkStr(expr[0], ctx), ctx);
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case MATCH:   return matches(checkStr(expr[0], ctx), ctx);
      case REPLACE: return replace(checkStr(expr[0], ctx), ctx);
      case ANALZYE: return analyzeString(checkStr(expr[0], ctx), ctx);
      default:      return super.atomic(ctx);
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
    final String str = string(val);
    final Matcher m = p.matcher(str);
    final NodIter ch = new NodIter();
    final FElem root = new FElem(new QNm(ANALYZE), ch, null);
    int s = 0;
    while(m.find()) {
      if(s != m.start()) {
        ch.add(node(NONMATCH, str.substring(s, m.start()), root));
      }
      s = m.end();
      ch.add(node(MATCH, m.group(), root));
    }
    if(s != str.length()) ch.add(node(NONMATCH, str.substring(s), root));
    return root;
  }

  /**
   * Returns a new match node.
   * @param tag tag name
   * @param text text
   * @param root root node
   * @return node
   */
  private FElem node(final byte[] tag, final String text, final FElem root) {
    final NodIter txt = new NodIter();
    final FElem sub = new FElem(new QNm(tag), txt, root);
    txt.add(new FTxt(token(text), sub));
    return sub;
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

    final byte[] rep = checkEmptyStr(expr[2], ctx);
    for(int i = 0; i < rep.length; i++) {
      if(rep[i] == '\\') {
        if(i + 1 == rep.length || rep[i + 1] != '\\' && rep[i + 1] != '$')
          error(FUNREGREP);
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
      if(m.contains("No group")) error(REGROUP);
      error(REGERR, m);
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
    final SeqIter sb = new SeqIter();
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
    byte[] pt = checkEmptyStr(pattern, ctx);
    int m = Pattern.UNIX_LINES;
    if(mod != null) {
      for(final byte b : checkEmptyStr(mod, ctx)) {
        if(b == 'i') m |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        else if(b == 'm') m |= Pattern.MULTILINE;
        else if(b == 's') m |= Pattern.DOTALL;
        else if(b == 'q') m |= Pattern.LITERAL;
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
          error(REGMOD, (char) b);
        }
      }
    }

    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < pt.length; i++) {
      final byte b = pt[i];
      tb.add(b);
      if(b == '\\' && i + 1 != pt.length && pt[i + 1] == ' ') tb.add(b);
    }

    String str = tb.toString();
    if(str.indexOf('[') != -1 && str.indexOf('-') != -1) {
      // replace classes by single characters to support Unicode matches
      while(true) {
        final Matcher mt = CLASSES.matcher(str);
        if(!mt.matches()) break;
        final char c1 = mt.group(1).charAt(0);
        final char c2 = mt.group(2).charAt(0);
        if(c1 < c2) {
          final TokenBuilder sb = new TokenBuilder("[");
          for(char c = c1; c <= c2; c++) sb.add(c);
          str = str.replaceAll("\\[" + c1 + "-" + c2, sb.toString());
        }
      }

      // remove excluded characters in classes
      while(true) {
        final Matcher mt = EXCLASSES.matcher(str);
        if(!mt.matches()) break;
        final String in = mt.group(1);
        final String ex = mt.group(2);
        String out = in;
        for(int e = 0; e < ex.length(); e++) {
          out = out.replaceAll(ex.substring(e, e + 1), "");
        }
        str = str.replaceAll("\\[" + in + "-\\[.*?\\]", "[" + out);
      }
    }

    try {
      return Pattern.compile(str, m);
    } catch(final Exception ex) {
      error(REGINV, pt);
      return null;
    }
  }
}
