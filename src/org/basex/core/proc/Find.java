package org.basex.core.proc;

import java.util.GregorianCalendar;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xpath.func.ContainsLC;
import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * Evaluates the 'find' command. Creates an XPath string for the specified
 * query terms and evaluates it.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Find extends XPath {
  @Override
  protected boolean exec() {
    return query(XPathProcessor.class, find(cmd.args(), context, false));
  }

  /**
   * Creates an XPath representation for the specified query.
   * @param query query
   * @param context context reference
   * @param root root flag
   * @return XPath query
   */
  public static String find(final String query, final Context context,
      final boolean root) {
    
    if(query.startsWith("/")) return query;

    final Nodes current = context.current();
    final boolean r = root || current.size == 1 && current.pre[0] < 1;
    if(query.length() == 0) return r ? "/" : ".";

    // deepfs instance
    final Data data = context.data();
    if(data.deepfs) return findFS(query, context, root);

    // parse user input
    final String qu = query.replaceAll(" \\+", " ");
    final String[] terms = split(qu);

    String pre = "";
    String preds = "";
    final String tag = "*";
    for(String term : terms) {
      if(term.startsWith("@=")) {
        preds += "[@* = \"" + term.substring(2) + "\"]";
      } else if(term.startsWith("=")) {
        preds += "[text() = \"" + term.substring(1) + "\"]";
      } else if(term.startsWith("~")) {
        preds += "[text() ~> \"" + term.substring(1) + "\"]";
      } else if(term.startsWith("@")) {
        if(term.length() == 1) continue;
        preds += "[" + ContainsLC.NAME + "(@*, \"" + term.substring(1) + "\")]";
        term = term.substring(1);
        final byte[] t = Token.token(term);
        if(Token.letter(t[0]) && Token.letterOrDigit(t)) {
          // attribute exists.. add location path
          pre += (r ? "" : ".") + "//@" + term + " | ";
        }
      } else {
        if(data.meta.ftxindex) {
          preds += "[text() ftcontains \"" + term + "\"]";
        } else {
          preds += "[" + ContainsLC.NAME + "(text(), \"" + term + "\")]";
        }
        final byte[] t = Token.token(term);
        if(Token.letter(t[0]) && Token.letterOrDigit(t)) {
          // tag exists.. add location path
          pre += (r ? "/" : "") + "descendant::" + term + " | ";
        }
        // name attribute exists...
        pre += "//*[" + ContainsLC.NAME + "(@name, \"" + term + "\")] | ";
      }
    }
    if(pre.length() == 0 && preds.length() == 0) return root ? "/" : ".";
    
    return pre + (r ? "/" : "") + "descendant-or-self::" + tag + preds;
  }

  /**
   * Creates an XPath representation for the specified file system query.
   * @param term query terms
   * @param context context
   * @param root root flag
   * @return XPath
   */
  private static String findFS(final String term, final Context context,
      final boolean root) {
    
    final String query = term.replaceAll("\\*|\\?|\\&|\"", " ") + ' ';
    String qu = query;

    final Nodes current = context.current();
    final TokenBuilder xpath = new TokenBuilder();
    final boolean r = root || current.size == 1 && current.pre[0] < 2;

    if(r) xpath.add("/");
    xpath.add("descendant-or-self::*");

    do {
      boolean size = false;
      boolean date = false;
      String pred = "";
      char operator = qu.charAt(0);
      boolean exact = true;
      if(operator == '>') {
        pred = "@size";
        size = true;
      } else if(operator == '<') {
        pred = "@size";
        size = true;
      } else if(operator == '}') {
        operator = '>';
        pred = "@mtime";
        date = true;
      } else if(operator == '{') {
        operator = '<';
        pred = "@mtime";
        date = true;
      } else if(operator == '.') {
        pred = "@suffix";
        operator = '=';
      } else if(operator == '~') {
        pred = "@name";
      } else if(operator == '=') {
        pred = "@name";
      } else {
        int i = qu.indexOf("=", 1);
        final int s = qu.indexOf(" ", 1);
        if(i == -1) i = qu.indexOf("<", 1);
        if(i == -1) i = qu.indexOf(">", 1);
        if(i != -1 && (s == -1 || s > i)) {
          pred = "self::file][descendant-or-self::node()/" + qu.substring(0, i);
          qu = qu.substring(i);
          operator = qu.charAt(0);
          size = operator != '=';
        } else {
          pred = "@name";
          exact = false;
        }
      }
      int off = exact ? 1 : 0;
      while(off < qu.length() && qu.charAt(off) == ' ') off++;
      qu = qu.substring(off);
      if(qu.length() == 0) return r ? "/" : ".";

      final int i = qu.indexOf(' ');
      String t = qu.substring(0, i);
      if(size) {
        t = Long.toString(calcNum(Token.token(t)));
      } else if(date) {
        final String[] dat = t.split("\\.");
        final int y = dat.length > 0 ? Integer.parseInt(dat[0]) : 1970;
        final int m = dat.length > 1 ? Integer.parseInt(dat[1]) - 1 : 0;
        final int d = dat.length > 2 ? Integer.parseInt(dat[2]) : 1;
        final long time = new GregorianCalendar(y, m, d).getTime().getTime();
        t = Long.toString(time / 60000);
      } else {
        final int d = t.lastIndexOf(".");
        // dot found... add suffix check
        if(d != -1) xpath.add("[@suffix = \"" + t.substring(d + 1) + "\"]");
        t = "\"" + t + "\"";
      }
      // add predicate
      xpath.add('[');
      if(exact) {
        xpath.add(pred + operator + t);
      } else {
        xpath.add(ContainsLC.NAME + "(" + pred + ", " + t + ")");
      }
      xpath.add(']');

      qu = qu.substring(i + 1);
    } while(qu.indexOf(' ') > -1);

    final Data data = context.data();
    if(data.meta.ftxindex) {
      xpath.add(" | ");
      if(!r) xpath.add(".");
      xpath.add("//file");
      for(final String t : split(query)) {
        xpath.add("[");
        final String pred = ".//text()";
        if(data.meta.ftxindex) {
          xpath.add(pred + " ftcontains \"" + t + "\"");
        }
        xpath.add("]");
      }
    }
    return xpath.toString();
  }

  /**
   * Creates an XPath representation for the specified table query.
   * @param filter filter terms
   * @param cols filter columns
   * @param tag root tag
   * @param data data reference
   * @param root root flag
   * @return XPath xpath expression
   */
  public static String findTable(final String[] filter, final TokenList cols,
      final byte[] tag, final Data data, final boolean root) {

    final TokenBuilder tb = new TokenBuilder();
    final boolean fs = data.deepfs;
    for(int i = 0; i < filter.length; i++) {
      if(filter[i].length() < 3) continue;

      final String[] spl = split(filter[i]);
      for(final String s : spl) {
        byte[] term = Token.token(s);
        if(Token.contains(term, '"')) term = Token.replace(term, '\"', ' ');
        term = Token.trim(term);
        if(term.length == 0) continue;
        tb.add("[");
        if(fs && i == 1) {
          tb.add(ContainsLC.NAME);
          tb.add("(@name, \"");
          tb.add(term);
          tb.add("\")");
        } else {
          final boolean att = fs && i < 4;
          if(fs) tb.add(att ? "@" : "*//");
          tb.add(cols.list[i]);
          String quote = "\"";

          if(term[0] == '<' || term[0] == '>') {
            tb.add(term[0]);
            quote = "";
            term = Token.token(calcNum(Token.substring(term, 1)));
          } else if(att) {
            tb.add(" = ");
          } else {
            if(data.meta.ftxindex) {
              tb.add(" ftcontains ");
            } else if(spl.length > 1 || !data.meta.txtindex && !att) {
              tb.add(" contains ");
            } else {
              tb.add(" = ");
            }
          }
          tb.add(quote);
          tb.add(term);
          tb.add(quote);
        }
        tb.add("]");
      }
    }
    String xpath = tb.toString();
    if(xpath.length() != 0) xpath = (root ? "/" : "") +
      "descendant-or-self::" + Token.string(tag) + xpath;

    return xpath;
  }

  /**
   * Returns an long value for the specified token. The suffixes "kb", "mb"
   * and "gb" are considered in the calculation.
   * @param tok token to be converted
   * @return long
   */
  private static long calcNum(final byte[] tok) {
    int tl = tok.length;
    final int s1 = tok.length < 1 ? 0 : Token.lc(tok[tl - 1]);
    final int s2 = tok.length < 2 ? 0 : Token.lc(tok[tl - 2]);
    int f = 0;

    // evaluate suffixes
    if(s1 == 'k') { tl -= 1; f = 10; }
    if(s1 == 'm') { tl -= 1; f = 20; }
    if(s1 == 'g') { tl -= 1; f = 30; }
    if(s1 == 'b' && s2 == 'k') { tl -= 2; f = 10; }
    if(s1 == 'b' && s2 == 'm') { tl -= 2; f = 20; }
    if(s1 == 'b' && s2 == 'g') { tl -= 2; f = 30; }
    final long i = Token.toLong(tok, 0, tl) << f;
    return i == Long.MIN_VALUE ? 0 : i;
  }

  /**
   * Splits the string and returns an array.
   * @param str string to be split
   * @return array
   */
  public static String[] split(final String str) {
    final int l = str.length();
    final String[] split = new String[l];

    int s = 0;
    char delim = 0;
    final StringBuilder tb = new StringBuilder();
    for(int i = 0; i < l; i++) {
      final char c = str.charAt(i);
      if(delim == 0) {
        if(c == '\'' || c == '"') {
          delim = c;
        } else if(!Character.isLetterOrDigit(c) && c != '@' && c != '='
            && c != '<' && c != '>' && c != '~') {
          if(tb.length() != 0) {
            split[s++] = tb.toString();
            tb.setLength(0);
          }
        } else {
          tb.append(c);
        }
      } else {
        if(c == delim) {
          delim = 0;
          if(tb.length() != 0) {
            split[s++] = tb.toString();
            tb.setLength(0);
          }
        } else {
          if(c != '\'' && c != '"') tb.append(c);
        }
      }
    }
    if(tb.length() != 0) split[s++] = tb.toString();
    return Array.finish(split, s);
  }
}
