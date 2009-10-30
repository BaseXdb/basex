package org.basex.query.func;

import static org.basex.query.QueryText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * String pattern functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FNPat extends Fun {
  /** From-To Pattern. */
  static final Pattern CLS = Pattern.compile(".*?\\[([a-zA-Z])-([a-zA-Z]).*");
  /** From-To Pattern. */
  static final Pattern CLSEX = Pattern.compile(".*?\\[(.*?)-\\[(.*?)\\]");

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case TOKEN:   return token(checkStr(expr[0], ctx), ctx);
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case MATCH:   return match(checkStr(expr[0], ctx), ctx);
      case REPLACE: return replace(checkStr(expr[0], ctx), ctx);
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
  private Item match(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    return Bln.get(p.matcher(Token.string(val)).find());
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

    final Item repl = expr[2].atomic(ctx);
    if(repl == null) Err.empty(this);

    final byte[] rep = checkStr(repl);
    for(int i = 0; i < rep.length; i++) {
      if(rep[i] == '\\') {
        if(i + 1 == rep.length || rep[i + 1] != '\\' && rep[i + 1] != '$')
          Err.or(FUNREGREP);
        i++;
      }
    }
    final Pattern p = pattern(expr[1], expr.length == 4 ? expr[3] : null, ctx);
    try {
      return Str.get(Token.token(p.matcher(
          Token.string(val)).replaceAll(Token.string(rep))));
    } catch(final Exception ex) {
      final String m = ex.getMessage();
      if(m.contains("No group")) Err.or(REGROUP);
      Err.or(REGERR, m);
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
  private Iter token(final byte[] val, final QueryContext ctx)
      throws QueryException {

    final Pattern p = pattern(expr[1], expr.length == 3 ? expr[2] : null, ctx);
    final SeqIter sb = new SeqIter();
    final String str = Token.string(val);
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
    final Item pat = pattern.atomic(ctx);
    if(pat == null) Err.empty(this);

    byte[] pt = checkStr(pat);
    int m = Pattern.UNIX_LINES;
    if(mod != null) {
      final Item md = mod.atomic(ctx);
      if(md == null) Err.empty(this);
      for(final byte b : checkStr(md)) {
        if(b == 'i') m |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        else if(b == 'm') m |= Pattern.MULTILINE;
        else if(b == 's') m |= Pattern.DOTALL;
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
          Err.or(REGMOD, (char) b);
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
        final Matcher mt = CLS.matcher(str);
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
        final Matcher mt = CLSEX.matcher(str);
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
      Err.or(REGINV, pt);
      return null;
    }
  }
}
