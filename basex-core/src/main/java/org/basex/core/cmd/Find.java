package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.path.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'find' command and processes a simplified request as XQuery.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Find extends AQuery {
  /** Start from root node. */
  private final boolean root;

  /**
   * Default constructor.
   * @param query simplified query
   */
  public Find(final String query) {
    this(query, false);
  }

  /**
   * Default constructor.
   * @param query simplified query
   * @param rt start from root node
   */
  public Find(final String query, final boolean rt) {
    super(Perm.NONE, true, query);
    root = rt;
  }

  @Override
  protected boolean run() {
    final String query = find(args[0], context, root);
    final boolean ok = query(query);
    final StringBuilder sb = new StringBuilder();
    if(options.get(MainOptions.QUERYINFO)) {
      sb.append(NL).append(QUERY_CC).append(NL).append(query).append(NL);
    }
    sb.append(info());
    error(sb.toString());
    return ok;
  }

  @Override
  public boolean updating(final Context ctx) {
    return updating(ctx, find(args[0], ctx, root));
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
  }

  /**
   * Creates an XQuery representation for the specified query.
   * @param query query
   * @param ctx database context
   * @param root start from root node
   * @return query
   */
  public static String find(final String query, final Context ctx, final boolean root) {
    // treat input as XQuery
    if(query.startsWith("/")) return query;

    final boolean r = root || ctx.root();
    if(query.isEmpty()) return r ? "/" : ".";

    // parse user input
    final String qu = query.replaceAll(" \\+", " ");
    final String[] terms = split(qu);

    String pre = "";
    String preds = "";
    for(String term : terms) {
      if(term.startsWith("@=")) {
        preds += "[@* = \"" + term.substring(2) + "\"]";
      } else if(term.startsWith("=")) {
        preds += "[text() = \"" + term.substring(1) + "\"]";
      } else if(term.startsWith("~")) {
        preds += "[text() contains text \"" + term.substring(1) +
          "\" using fuzzy]";
      } else if(term.startsWith("@")) {
        if(term.length() == 1) continue;
        preds += "[@* contains text \"" + term.substring(1) + "\"]";
        term = term.substring(1);
        // add valid name tests
        if(XMLToken.isName(token(term))) {
          pre += (r ? "" : ".") + "//@" + term + " | ";
        }
      } else {
        preds += "[text() contains text \"" + term + "\"]";
        // add valid name tests
        if(XMLToken.isName(token(term))) {
          pre += (r ? "/" : "") + Axis.DESC + "::*:" + term + " | ";
        }
      }
    }
    if(pre.isEmpty() && preds.isEmpty()) return root ? "/" : ".";

    // create final string
    final TokenBuilder tb = new TokenBuilder();
    final String name = "*";
    tb.add(pre + (r ? "/" : "") + Axis.DESCORSELF + "::" + name + preds);
    return tb.toString();
  }

  /**
   * Creates an XQuery representation for the specified table query.
   * @param filter filter terms
   * @param cols filter columns
   * @param elem element flag
   * @param name name of root element
   * @param root root flag
   * @return query
   */
  public static String findTable(final StringList filter, final TokenList cols, final BoolList elem,
      final byte[] name, final boolean root) {

    final TokenBuilder tb = new TokenBuilder();
    final int is = filter.size();
    for(int i = 0; i < is; ++i) {
      final String[] spl = split(filter.get(i));
      for(final String s : spl) {
        final byte[] term = trim(replace(token(s), '"', ' '));
        if(term.length == 0) continue;
        tb.add('[');

        final boolean elm = elem.get(i);
        tb.add(elm ? ".//" : "@");
        tb.add("*:");
        tb.add(cols.get(i));

        if(term[0] == '<' || term[0] == '>') {
          tb.add(term[0]);
          tb.addLong(calcNum(substring(term, 1)));
        } else {
          tb.add(" contains text \"");
          tb.add(term);
          tb.add('"');
        }
        tb.add(']');
      }
    }
    return tb.isEmpty() ? "/" : (root ? "/" : "") +
        Axis.DESCORSELF + "::*:" + string(name) + tb;
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
    for(int i = 0; i < l; ++i) {
      final char c = str.charAt(i);
      if(delim == 0) {
        if(c == '\'' || c == '"') {
          delim = c;
        } else if(!XMLToken.isChar(c) && c != '@' && c != '='
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
    return Array.copyOf(split, s);
  }
}
