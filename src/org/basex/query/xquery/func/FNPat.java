package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
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
  /** From-To Pattern. */
  static final Pattern CLS = Pattern.compile(".*?\\[([a-zA-Z])-([a-zA-Z]).*");
  /** From-To Pattern. */
  static final Pattern CLSEX = Pattern.compile(".*?\\[(.*?)-\\[(.*?)\\]");

  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final byte[] val = checkStr(arg[0]);

    switch(func) {
      case MATCH:   return match(val, arg);
      case REPLACE: return replace(val, arg);
      case TOKEN:   return token(val, arg);
      default:      BaseX.notexpected(func); return null;
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
    final Item repl = arg[2].atomic(this, false);

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
      return Str.get(Token.token(res)).iter();
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
   * @return modified pattern
   * @throws XQException evaluation exception
   */
  private Pattern pattern(final Iter pattern, final Iter mod)
      throws XQException {

    // process modifiers
    final Item pat = pattern.atomic(this, false);

    byte[] pt = checkStr(pat);
    int m = Pattern.UNIX_LINES;
    if(mod != null) {
      final Item md = mod.atomic(this, false);
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
      if(b == '\\' && (i + 1 != pt.length && pt[i + 1] == ' ')) tb.add(b);
    }

    String str = tb.toString();
    if(str.indexOf('[') != -1 && str.indexOf('-') != -1) {
      // Replace classes by single characters to support Unicode matches
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

      // Remove excluded characters in classes
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
