package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * String pattern functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNPat extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final byte[] val = checkStr(arg[0]);

    switch(func) {
      case MATCH:   return match(val, arg);
      case REPLACE: return replace(val, arg);
      case TOKEN:   return token(val, arg);
      default:      throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Evaluates the match function.
   * @param val input value
   * @param arg all items
   * @return function result
   * @throws XQException xquery exception
   */
  private Iter match(final byte[] val, final Iter[] arg) throws XQException {
    final Pattern p = pattern(arg[1], arg.length == 3 ? arg[2] : null);
    return Bln.get(p.matcher(Token.string(val)).find()).iter();
  }

  /**
   * Evaluates the replace function.
   * @param val input value
   * @param arg all items
   * @return function result
   * @throws XQException xquery exception
   */
  private Iter replace(final byte[] val, final Iter[] arg) throws XQException {
    Item repl = arg[2].atomic(this, false);

    final byte[] rep = checkStr(repl);
    for(int i = 0; i < rep.length; i++) {
      if(rep[i] == '\\') {
        if(i + 1 == rep.length || rep[i + 1] != '\\' && rep[i + 1] != '$')
          Err.or(FUNREGREP);
        i++;
      }
    }
    final Pattern p = pattern(arg[1], arg.length == 4 ? arg[3] : null);
    try {
      final String res = p.matcher(Token.string(val)).replaceAll(
          Token.string(rep));
      return Str.iter(Token.token(res));
    } catch(final Exception e) {
      final String m = e.getMessage();
      if(m.contains("No group")) Err.or(REGROUP);
      Err.or(REGERR, m);
      return null;
    }
  }

  /**
   * Evaluates the tokenize function.
   * @param val input value
   * @param arg all items
   * @return function result
   * @throws XQException xquery exception
   */
  private Iter token(final byte[] val, final Iter[] arg) throws XQException {
    final Pattern p = pattern(arg[1], arg.length == 3 ? arg[2] : null);

    final SeqIter sb = new SeqIter();
    final String str = Token.string(val);
    if(str.length() != 0) {
      final Matcher m = p.matcher(str);
      int s = 0;
      while(m.find()) {
        sb.add(Str.get(Token.token(str.substring(s, m.start()))));
        s = m.end();
      }
      sb.add(Str.get(Token.token(str.substring(s, str.length()))));
    }
    return sb;
  }

  /**
   * Checks the regular expression modifiers.
   * @param pattern pattern
   * @param mod modifier item
   * @return modified pattern
   * @throws XQException evaluation exception
   */
  private Pattern pattern(final Iter pattern, final Iter mod)
      throws XQException {

    // process modifiers
    Item pat = pattern.atomic(this, false);

    byte[] pt = checkStr(pat);
    int m = Pattern.UNIX_LINES;
    if(mod != null) {
      Item md = mod.atomic(this, false);
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

    try {
      final TokenBuilder tb = new TokenBuilder();
      for(int i = 0; i < pt.length; i++) {
        final byte b = pt[i];
        tb.add(b);
        if(b == '\\' && (i + 1 != pt.length && pt[i + 1] == ' ')) tb.add('\\');
      }
      return Pattern.compile(Token.string(tb.finish()), m);
    } catch(final Exception e) {
      Err.or(REGINV, pt);
      return null;
    }
  }
}
