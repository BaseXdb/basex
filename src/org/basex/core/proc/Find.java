package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.BoolList;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.XMLToken;
import static org.basex.util.Token.*;

/**
 * Evaluates the 'find' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

    // treat input as XQuery
    if(query.startsWith("/")) return query;

    final boolean r = root || ctx.root();
    if(query.length() == 0) return r ? "/" : ".";

    // file system instance
    final Data data = ctx.data();
    if(data.fs != null) return findFS(query, ctx, root);

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
        preds += "[@* ftcontains \"" + term.substring(1) + "\"]";
        term = term.substring(1);
        // add valid name tests
        if(XMLToken.isName(token(term))) {
          pre += (r ? "" : ".") + "//@" + term + " | ";
        }
      } else {
        preds += "[text() ftcontains \"" + term + "\"]";
        // add valid name tests
        if(XMLToken.isName(token(term))) {
          pre += (r ? "/" : "") + "descendant::*:" + term + " | ";
        }
        // add name test...
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

    final TokenBuilder xquery = new TokenBuilder();
    final boolean r = root || context.root();

    if(r) xquery.add("/");
    xquery.add("descendant-or-self::*");

    do {
      boolean size = false;
      boolean exact = true;
      String pred = "";

      // check prefix
      char op = qu.charAt(0);
      if(op == '>') {
        pred = DataText.S_SIZE;
        size = true;
      } else if(op == '<') {
        pred = DataText.S_SIZE;
        size = true;
      } else if(op == '.') {
        pred = DataText.S_SUFFIX;
        op = '=';
      } else {
        pred = DataText.S_NAME;
        exact = op == '=';
      }

      int off = exact ? 1 : 0;
      while(off < qu.length() && qu.charAt(off) == ' ') off++;
      qu = qu.substring(off);
      if(qu.length() == 0) continue;

      final int i = qu.indexOf(' ');
      String t = qu.substring(0, i);

      if(size) {
        t = Long.toString(calcNum(token(t)));
      } else {
        // if dot is found inside the current term, add suffix check
        final int d = t.lastIndexOf(".");
        if(d != -1) {
          xquery.add("[@" + DataText.S_SUFFIX + " = \"" +
              t.substring(d + 1) + "\"]");
        }
        t = "\"" + t + "\"";
      }
      // add predicate
      xquery.add("[@" + pred + (exact ? op : " ftcontains ") + t + "]");

      qu = qu.substring(i + 1);
    } while(qu.indexOf(' ') > -1);

    boolean f = true;
    for(final String t : split(query)) {
      if(Character.isLetterOrDigit(t.charAt(0))) {
        if(f) xquery.add(" | " + (r ? "/" : "") + "descendant-or-self::file");
        xquery.add("[descendant::text() ftcontains \"" + t + "\"]");
        f = false;
      }
    }
    return xquery.toString();
  }

  /**
   * Creates an XQuery representation for the specified table query.
   * @param filter filter terms
   * @param cols filter columns
   * @param elem element flag
   * @param tag root tag
   * @param root root flag
   * @return query
   */
  public static String findTable(final StringList filter, final TokenList cols,
      final BoolList elem, final byte[] tag, final boolean root) {

    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0, is = filter.size(); i < is; i++) {
      final String[] spl = split(filter.get(i));
      for(final String s : spl) {
        byte[] term = token(s);
        if(contains(term, '"')) term = replace(term, '\"', ' ');
        term = trim(term);
        if(term.length == 0) continue;
        tb.add("[");

        final boolean elm = elem.get(i);
        tb.add(elm ? ".//" : "@");
        tb.add(cols.list[i]);

        if(term[0] == '<' || term[0] == '>') {
          tb.add(term[0]);
          tb.add(calcNum(substring(term, 1)));
        } else {
          tb.add(" ftcontains \"");
          tb.add(term);
          tb.add("\"");
        }
        tb.add("]");
      }
    }
    return tb.size == 0 ? "." : (root ? "/" : "") +
      "descendant-or-self::" + string(tag) + tb;
  }

  /**
   * Returns an long value for the specified token. The suffixes "kb", "mb"
   * and "gb" are considered in the calculation.
   * @param tok token to be converted
   * @return long
   */
  private static long calcNum(final byte[] tok) {
    int tl = tok.length;
    final int s1 = tok.length < 1 ? 0 : lc(tok[tl - 1]);
    final int s2 = tok.length < 2 ? 0 : lc(tok[tl - 2]);
    int f = 0;

    // evaluate suffixes
    if(s1 == 'k') { tl -= 1; f = 10; }
    if(s1 == 'm') { tl -= 1; f = 20; }
    if(s1 == 'g') { tl -= 1; f = 30; }
    if(s1 == 'b' && s2 == 'k') { tl -= 2; f = 10; }
    if(s1 == 'b' && s2 == 'm') { tl -= 2; f = 20; }
    if(s1 == 'b' && s2 == 'g') { tl -= 2; f = 30; }
    final long i = toLong(tok, 0, tl) << f;
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
