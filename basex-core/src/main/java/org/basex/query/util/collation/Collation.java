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
import org.basex.util.options.Options.*;

/**
 * This class organizes collations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Collation {
  /** Case-insensitive collation. */
  private static final byte[] NOCASE =
      token("http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive");

  /** UCA collations. */
  private static final byte[] UCA = token("http://www.w3.org/2013/collation/UCA");
  /** Implementation-defined collation URI. */
  private static final byte[] URL = token(BASEX_URL + "/collation");
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
   * @param uri collation uri
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param err error code for unknown collation uris
   * @return collator instance or {@code null} for unicode point collation
   * @throws QueryException query exception
   */
  public static Collation get(final byte[] uri, final QueryContext qc, final StaticContext sc,
      final InputInfo ii, final QueryError err) throws QueryException {

    // return default collation
    if(uri == null) return sc.collation;

    final Uri u = Uri.uri(uri);
    if(!u.isValid()) throw INVURI_X.get(ii, uri);
    final byte[] url = u.isAbsolute() ? uri :
      Token.startsWith(uri, '?') ? concat(URL, uri) : sc.baseURI().resolve(u, ii).string();

    // return unicode point collation
    if(eq(COLLATION_URI, url)) return null;

    // create new collation or return cached instance
    if(qc.collations == null) qc.collations = new TokenObjMap<>();
    Collation coll = qc.collations.get(url);
    if(coll == null) {
      coll = get(url, ii, err);
      qc.collations.put(url, coll);
    }
    return coll;
  }

  /**
   * Returns a collation instance for the specified URI.
   * @param uri collation URI
   * @param ii input info
   * @param err error code for unknown collation URIs
   * @return collation instance or {@code null} if uri is invalid
   * @throws QueryException query exception
   */
  private static Collation get(final byte[] uri, final InputInfo ii, final QueryError err)
      throws QueryException {

    // case-insensitive collation
    if(eq(NOCASE, uri)) return new NoCaseCollation();

    final int q = Token.indexOf(uri, '?');
    final byte[] base = q == -1 ? uri : substring(uri, 0, q);
    final HashMap<String, String> args = args(
        q == -1 ? "" : string(replace(substring(uri, q + 1), '&', ';')));

    CollationOptions opts = null;
    if(eq(URL, base)) {
      opts = new BaseXCollationOptions(false);
    } else if(eq(UCA, base)) {
      if(UCAOptions.ACTIVE) {
        opts = new UCAOptions();
      } else if(!YesNo.NO.toString().equals(args.get(UCAOptions.FALLBACK.name()))) {
        opts = new BaseXCollationOptions(true);
      }
    }
    if(opts == null) throw err.get(ii, Util.inf("Unknown collation '%'", uri));

    try {
      final Collation coll = opts.get(args);
      coll.uri = uri;
      return coll;
    } catch(final IllegalArgumentException | BaseXException ex) {
      throw err.get(ii, ex.getMessage());
    }
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
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean contains(final byte[] string, final byte[] sub, final InputInfo ii)
      throws QueryException {
    return indexOf(string(string), string(sub), Mode.INDEX_OF, ii) != -1;
  }

  /**
   * Checks if a string starts with another.
   * @param string string
   * @param sub substring to be found
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean startsWith(final byte[] string, final byte[] sub, final InputInfo ii)
      throws QueryException {
    return indexOf(string(string), string(sub), Mode.STARTS_WITH, ii) != -1;
  }

  /**
   * Checks if a string ends with another.
   * @param string string
   * @param sub substring to be found
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean endsWith(final byte[] string, final byte[] sub, final InputInfo ii)
      throws QueryException {
    return indexOf(string(string), string(sub), Mode.ENDS_WITH, ii) != -1;
  }

  /**
   * Returns the substring after a specified string.
   * @param string string
   * @param sub substring to be found
   * @param ii input info
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] after(final byte[] string, final byte[] sub, final InputInfo ii)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), Mode.INDEX_AFTER, ii);
    return i == -1 ? EMPTY : token(st.substring(i));
  }

  /**
   * Returns the substring before a specified string.
   * @param string string
   * @param sub substring to be found
   * @param ii input info
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] before(final byte[] string, final byte[] sub, final InputInfo ii)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), Mode.INDEX_OF, ii);
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
   * @return result of check
   */
  public abstract int compare(byte[] string, byte[] compare);

  /**
   * Returns the start or end position of the specified substring.
   * @param string string
   * @param sub substring to be found
   * @param mode search mode
   * @param info input info
   * @return string index
   * @throws QueryException query exception
   */
  protected abstract int indexOf(String string, String sub, Mode mode, InputInfo info)
      throws QueryException;
}
