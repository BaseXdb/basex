package org.basex.core.proc;

import java.util.Arrays;
import org.basex.core.Context;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.query.path.Axis;
import org.basex.util.BoolList;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.XMLToken;
import org.deepfs.fs.DeepFS;
import org.deepfs.util.FSImporter;
import static org.basex.util.Token.*;

/**
 * Evaluates the 'find' command and processes a simplified request as XQuery.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Find extends AQuery {
  /** Contains text token. */
  private static final String CT = "contains text";

  /**
   * Default constructor.
   * @param query simplified query
   */
  public Find(final String query) {
    super(DATAREF, query);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    return query(find(args[0], context, false), out);
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
    if(query.isEmpty()) return r ? "/" : ".";

    // file system instance
    final Data data = ctx.data;
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
        preds += "[@* " + CT + " \"" + term.substring(1) + "\"]";
        term = term.substring(1);
        // add valid name tests
        if(XMLToken.isName(token(term))) {
          pre += (r ? "" : ".") + "//@" + term + " | ";
        }
      } else {
        preds += "[text() " + CT + " \"" + term + "\"]";
        // add valid name tests
        if(XMLToken.isName(token(term))) {
          pre += (r ? "/" : "") + Axis.DESC + "::*:" + term + " | ";
        }
        // add name test...
        pre += (r ? "/" : "") + Axis.DESCORSELF +
          "::*[@name " + CT + " \"" + term + "\"] | ";
      }
    }
    if(pre.isEmpty() && preds.isEmpty()) return root ? "/" : ".";

    return pre + (r ? "/" : "") + Axis.DESCORSELF + "::" + tag + preds;
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
    xquery.add(Axis.DESCORSELF + "::");
    String name = "*";

    do {
      boolean exact = true;
      String pred = "";

      // check prefix
      char op = qu.charAt(0);
      if(op == '>') {
        pred = DeepFS.S_SIZE;
      } else if(op == '<') {
        pred = DeepFS.S_SIZE;
      } else if(op == '.') {
        pred = DeepFS.S_SUFFIX;
        op = '=';
      } else {
        pred = DeepFS.S_NAME;
        exact = op == '=';
      }

      int off = exact ? 1 : 0;
      while(off < qu.length() && qu.charAt(off) == ' ') off++;
      qu = qu.substring(off);
      if(qu.isEmpty()) continue;

      final int i = qu.indexOf(' ');
      String t = qu.substring(0, i);

      if(pred == DeepFS.S_SIZE) {
        t = Long.toString(calcNum(token(t)));
        if(!name.isEmpty()) name = "file";
      } else {
        // if dot is found inside the current term, add suffix check
        final int d = t.lastIndexOf(".");
        if(d != -1) {
          xquery.add("[@" + DeepFS.S_SUFFIX + " = \"" +
              t.substring(d + 1) + "\"]");
        }
        t = "\"" + t + "\"";
      }
      // add predicate
      xquery.add(name + "[@" + pred + (exact ? op : " " + CT + " ") +
          t + "]");

      qu = qu.substring(i + 1);
      name = "";
    } while(qu.indexOf(' ') > -1);

    boolean f = true;
    for(final String t : split(query)) {
      if(Character.isLetterOrDigit(t.charAt(0))) {
        if(f) xquery.add(" | " + (r ? "/" : "") + Axis.DESCORSELF + "::file");
        xquery.add("[" + Axis.DESC + "::text() " + CT + " \"" + t + "\"]");
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
   * @param fs file system flag
   * @return query
   */
  public static String findTable(final StringList filter, final TokenList cols,
      final BoolList elem, final byte[] tag, final boolean root,
      final boolean fs) {

    final TokenBuilder tb = new TokenBuilder();
    final int is = filter.size();
    for(int i = 0; i < is; i++) {
      final String[] spl = fs ? new String[] { 
          FSImporter.escape(filter.get(i)) } : split(filter.get(i));
      for(final String s : spl) {
        byte[] term = token(s);
        if(contains(term, '"')) term = replace(term, '\"', ' ');
        term = trim(term);
        if(term.length == 0) continue;
        tb.add('[');

        final boolean elm = elem.get(i);
        tb.add(elm ? ".//" : "@");
        tb.add("*:");
        tb.add(cols.get(i));

        if(term[0] == '<' || term[0] == '>') {
          tb.add(term[0]);
          tb.add(calcNum(substring(term, 1)));
        } else {
          tb.add(" " + CT + " \"");
          tb.add(term);
          tb.add('"');
        }
        tb.add(']');
      }
    }
    return tb.size() == 0 ? "/" : (root ? "/" : "") +
        Axis.DESCORSELF + "::*:" + string(tag) + tb;
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
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < l; i++) {
      final char c = str.charAt(i);
      if(delim == 0) {
        if(c == '\'' || c == '"') {
          delim = c;
        } else if(!Character.isLetterOrDigit(c) && c != '@' && c != '='
            && c != '<' && c != '>' && c != '~') {
          if(sb.length() != 0) {
            split[s++] = sb.toString();
            sb.setLength(0);
          }
        } else {
          sb.append(c);
        }
      } else {
        if(c == delim) {
          delim = 0;
          if(sb.length() != 0) {
            split[s++] = sb.toString();
            sb.setLength(0);
          }
        } else {
          if(c != '\'' && c != '"') sb.append(c);
        }
      }
    }
    if(sb.length() != 0) split[s++] = sb.toString();
    return Arrays.copyOf(split, s);
  }

  @Override
  public String toString() {
    return Cmd.FIND + " " + args[0];
  }
}
