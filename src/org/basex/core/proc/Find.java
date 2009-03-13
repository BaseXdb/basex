package org.basex.core.proc;

import java.io.IOException;
import java.util.GregorianCalendar;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.BoolList;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.XMLToken;

/**
 * Evaluates the 'find' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Find extends AQuery {
  /**
   * Constructor.
   * @param q query
   */
  public Find(final String q) {
    super(DATAREF | PRINTING, q);
  }

  @Override
  protected boolean exec() {
    final String query = args[0] == null ? "" : args[0];
    return query(find(query, context, false));
  }

  @Override
  protected void out(final PrintOutput o) throws IOException {
    out(o, Prop.xqformat);
  }

  /**
   * Creates an XQuery representation for the specified query.
   * @param query query
   * @param ctx context reference
   * @param root root flag
   * @return query
   */
  public static String find(final String query, final Context ctx,
      final boolean root) {
    
    if(query.startsWith("/")) return query;

    final boolean r = root || ctx.root();
    if(query.length() == 0) return r ? "/" : ".";

    // deepfs instance
    final Data data = ctx.data();
    if(data.fs != null) return findFS(query, ctx, root);

    // parse user input
    final String qu = query.replaceAll(" \\+", " ");
    final String[] terms = split(qu);

    String pre = "";
    String preds = "";
    final String tag = "*";
    for(String term : terms) {
      final byte[] token = Token.token(term);
      if(term.startsWith("@=")) {
        preds += "[@* = \"" + term.substring(2) + "\"]";
      } else if(term.startsWith("=")) {
        preds += "[text() = \"" + term.substring(1) + "\"]";
      } else if(term.startsWith("~")) {
        preds += "[text() ~> \"" + term.substring(1) + "\"]";
      } else if(term.startsWith("@")) {
        if(term.length() == 1) continue;
        preds += "[@* ftcontains \"" + term.substring(1) + "\"]";
        term = term.substring(1);
        if(XMLToken.isName(token)) {
          // attribute exists.. add location path
          pre += (r ? "" : ".") + "//@" + term + " | ";
        }
      } else {
        preds += "[text() ftcontains \"" + term + "\"]";
        if(XMLToken.isName(token) && data.tagID(token) != 0) {
          // add location path for tag
          pre += (r ? "/" : "") + "descendant::*:" + term + " | ";
        }
        // name attribute exists...
        pre += "descendant-or-self::*[@name ftcontains \"" + term + "\"] | ";
      }
    }
    if(pre.length() == 0 && preds.length() == 0) return root ? "/" : ".";
    return pre + (r ? "/" : "") + "descendant-or-self::" + tag + preds;
  }

  /**
   * Creates an XQuery representation for the specified file system query.
   * @param term query terms
   * @param context context
   * @param root root flag
   * @return query
   */
  private static String findFS(final String term, final Context context,
      final boolean root) {
    
    final String query = term.replaceAll("\\*|\\?|\\&|\"", " ") + ' ';
    String qu = query;

    final Nodes current = context.current();
    final TokenBuilder xpath = new TokenBuilder();
    final boolean r = root || current.size() == 1 && current.nodes[0] < 2;

    if(r) xpath.add("/");
    xpath.add("descendant-or-self::*");

    do {
      boolean size = false;
      boolean date = false;
      String pred = "";
      char operator = qu.charAt(0);
      boolean exact = true;
      if(operator == '>') {
        pred = "@" + DataText.S_SIZE;
        size = true;
      } else if(operator == '<') {
        pred =  "@" + DataText.S_SIZE;
        size = true;
      } else if(operator == '}') {
        operator = '>';
        pred = "@" + DataText.S_MTIME;
        date = true;
      } else if(operator == '{') {
        operator = '<';
        pred = "@" + DataText.S_MTIME;
        date = true;
      } else if(operator == '.') {
        pred = "@" + DataText.S_SUFFIX;
        operator = '=';
      } else if(operator == '~') {
        pred = "@" + DataText.S_NAME;
      } else if(operator == '=') {
        pred = "@" + DataText.S_NAME;
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
          pred = "@" + DataText.S_NAME;
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
        if(d != -1) xpath.add("[@" + DataText.S_SUFFIX + " = \"" +
            t.substring(d + 1) + "\"]");
        t = "\"" + t + "\"";
      }
      // add predicate
      xpath.add('[');
      if(exact) {
        xpath.add(pred + operator + t);
      } else {
        xpath.add(pred + " ftcontains " + t);
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
        xpath.add("[.//text() ftcontains \"" + t + "\"]");
      }
    }
    return xpath.toString();
  }

  /**
   * Creates an XQuery representation for the specified table query.
   * @param filter filter terms
   * @param cols filter columns
   * @param elem element flag
   * @param tag root tag
   * @param data data reference
   * @param root root flag
   * @return query
   */
  public static String findTable(final StringList filter, final TokenList cols,
      final BoolList elem, final byte[] tag, final Data data,
      final boolean root) {

    final TokenBuilder tb = new TokenBuilder();
    final boolean fs = data.fs != null;
    for(int i = 0; i < filter.size; i++) {
      if(filter.list[i].length() < 3) continue;

      final String[] spl = split(filter.list[i]);
      for(final String s : spl) {
        byte[] term = Token.token(s);
        if(Token.contains(term, '"')) term = Token.replace(term, '\"', ' ');
        term = Token.trim(term);
        if(term.length == 0) continue;
        tb.add("[");
        if(fs && i == 1) {
          tb.add("@" + DataText.S_NAME + " ftcontains \"" + term + "\"");
        } else {
          final boolean elm = elem.list[i];
          tb.add(elm ? ".//" : "@");
          tb.add(cols.list[i]);
          String quote = "\"";

          if(term[0] == '<' || term[0] == '>') {
            tb.add(term[0]);
            quote = "";
            term = Token.token(calcNum(Token.substring(term, 1)));
          } else if(data.meta.ftxindex && elm) {
            tb.add(" ftcontains ");
          } else if(spl.length == 1 && (elm && data.meta.txtindex ||
              !elm && data.meta.atvindex)) {
            tb.add(" = ");
          } else {
            tb.add(" ftcontains ");
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
  private static String[] split(final String str) {
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
