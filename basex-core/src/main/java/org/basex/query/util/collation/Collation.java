package org.basex.query.util.collation;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.Options.*;

/**
 * This class organizes collations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class Collation {
  /** Case-insensitive collation. */
  private static final byte[] NOCASE =
      token("http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive");

  /** ICU (UCA) collation URL. */
  public static final byte[] ICU = token("http://www.w3.org/2013/collation/UCA");
  /** BaseX collation URI. */
  public static final byte[] BASEX = token(BASEX_URL + "/collation");
  /** Collation URI. */
  private byte[] uri = EMPTY;

  /** Search modes. */
  protected enum Mode {
    /** Default. */      INDEX_OF,
    /** End position. */ INDEX_AFTER,
    /** Starts-with. */  STARTS_WITH,
    /** Ends-with. */    ENDS_WITH
  }

  /**
   * Returns a collation instance for the specified uri.
   * @param uri collation uri (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param err error code for unknown collation uris
   * @return collation instance or {@code null} for unicode point collation
   * @throws QueryException query exception
   */
  public static Collation get(final byte[] uri, final QueryContext qc, final InputInfo info,
      final QueryError err) throws QueryException {

    // return default collation
    if(uri == null) return info.sc().collation;

    final Uri u = Uri.get(uri);
    if(!u.isValid()) throw INVURI_X.get(info, uri);
    final byte[] url = u.isAbsolute() ? uri : Token.startsWith(uri, '?') ? concat(BASEX, uri) :
      info.sc().baseURI().resolve(u, info).string();

    // return unicode point collation
    if(eq(COLLATION_URI, url)) return null;

    // create new collation or return cached instance
    if(qc.collations == null) qc.collations = new TokenObjMap<>();
    Collation coll = qc.collations.get(url);
    if(coll == null) {
      coll = get(url, info, err);
      qc.collations.put(url, coll);
    }
    return coll;
  }

  /**
   * Returns a collation instance for the specified URI.
   * @param uri collation URI
   * @param info input info (can be {@code null})
   * @param err error code for unknown collation URIs
   * @return collation instance or {@code null} if uri is invalid
   * @throws QueryException query exception
   */
  private static Collation get(final byte[] uri, final InputInfo info, final QueryError err)
      throws QueryException {

    // case-insensitive collation
    if(eq(NOCASE, uri)) return NoCaseCollation.INSTANCE;

    final int q = Token.indexOf(uri, '?');
    final byte[] base = q == -1 ? uri : substring(uri, 0, q);
    final HashMap<String, String> args = args(
        q == -1 ? "" : string(replace(substring(uri, q + 1), '&', ';')));

    CollationOptions opts = null;
    if(eq(BASEX, base)) {
      opts = new BaseXCollationOptions(false);
    } else if(eq(ICU, base)) {
      final boolean fallback = !YesNo.NO.toString().equals(args.get(UCAOptions.FALLBACK.name()));
      opts = Prop.ICU ? new UCAOptions(fallback) : new BaseXCollationOptions(fallback);
    }
    if(opts == null) throw err.get(info, Util.inf("Unknown collation '%'", uri));

    try {
      final Collation coll = opts.get(args);
      coll.uri = uri;
      return coll;
    } catch(final IllegalArgumentException | BaseXException ex) {
      throw err.get(info, ex.getMessage());
    }
  }

  /**
   * Returns a collation for the specified collation and input info.
   * @param coll collation
   * @param info input info (can be {@code null})
   * @return collation instance, or {@code null} for unicode point collation
   */
  public static Collation get(final Collation coll, final InputInfo info) {
    return coll != null ? coll : info != null ? info.sc().collation : null;
  }

  /**
   * Returns a map with all arguments.
   * @param args arguments
   * @return error message
   */
  private static HashMap<String, String> args(final String args) {
    final HashMap<String, String> map = new HashMap<>();
    for(final String option : Strings.split(args, ';')) {
      final String[] kv = Strings.split(option, '=', 2);
      map.put(kv[0], kv.length == 2 ? kv[1] : "");
    }
    return map;
  }

  /**
   * Checks if a string is contained in another.
   * @param string string
   * @param sub substring to be found
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean contains(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {
    return indexOf(string(string), string(sub), Mode.INDEX_OF, info) != -1;
  }

  /**
   * Checks if a string starts with another.
   * @param string string
   * @param sub substring to be found
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean startsWith(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {
    return indexOf(string(string), string(sub), Mode.STARTS_WITH, info) != -1;
  }

  /**
   * Checks if a string ends with another.
   * @param string string
   * @param sub substring to be found
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean endsWith(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {
    return indexOf(string(string), string(sub), Mode.ENDS_WITH, info) != -1;
  }

  /**
   * Returns the substring after a specified string.
   * @param string string
   * @param sub substring to be found
   * @param info input info (can be {@code null})
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] after(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), Mode.INDEX_AFTER, info);
    return i == -1 ? EMPTY : token(st.substring(i));
  }

  /**
   * Returns the substring before a specified string.
   * @param string string
   * @param sub substring to be found
   * @param info input info (can be {@code null})
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] before(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), Mode.INDEX_OF, info);
    return i == -1 ? EMPTY : token(st.substring(0, i));
  }

  /**
   * Returns the collation URI.
   * @return uri
   */
  public final byte[] uri() {
    return uri;
  }

  /**
   * Compares two strings.
   * @param string string
   * @param compare string to be compared
   * @return result of comparison (-1, 0, 1)
   */
  public abstract int compare(byte[] string, byte[] compare);

  /**
   * Returns a collation key.
   * @param string string
   * @param info input info (can be {@code null})
   * @return key
   * @throws QueryException query exception
   */
  public abstract byte[] key(byte[] string, InputInfo info) throws QueryException;

  /**
   * Returns the start or end position of the specified substring.
   * @param string string
   * @param sub substring to be found
   * @param mode search mode
   * @param info input info (can be {@code null})
   * @return string index
   * @throws QueryException query exception
   */
  protected abstract int indexOf(String string, String sub, Mode mode, InputInfo info)
      throws QueryException;

  /**
   * Returns a standard collation key.
   * @param string string
   * @return key
   */
  public static byte[] key(final byte[] string) {
    final ByteList bl = new ByteList(string.length * 3L);
    for(final TokenParser tp = new TokenParser(string); tp.more();) {
      final int n = tp.next();
      bl.add(n >>> 16).add(n >>> 8).add(n);
    }
    return bl.finish();
  }
}
