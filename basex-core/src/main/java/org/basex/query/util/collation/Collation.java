package org.basex.query.util.collation;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class organizes collations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Collation {
  /** Case-insensitive collation. */
  private static final byte[] NOCASE =
      token("http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive");

  /** UCA collations. */
  private static final byte[] UCA = token("http://www.w3.org/2013/collation/UCA");
  /** Implementation-defined collation URI. */
  private static final byte[] URL = token(Prop.URL + "/collation");
  /** Collation URI. */
  private byte[] uri = EMPTY;

  /** Search modes. */
  protected static enum Mode {
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
   * @param info input info
   * @param err error code for unknown collation uris
   * @return collator instance or {@code null} for unicode point collation
   * @throws QueryException query exception
   */
  public static Collation get(final byte[] uri, final QueryContext qc, final StaticContext sc,
      final InputInfo info, final Err err) throws QueryException {

    // return default collation
    if(uri == null) return sc.collation;

    byte[] url = uri;
    final Uri u = Uri.uri(url);
    if(!u.isValid()) throw INVURI_X.get(info, url);
    if(!u.isAbsolute()) {
      if(Token.startsWith(url, '?')) {
        url = concat(URL, url);
      } else {
        url = sc.baseURI().resolve(u, info).string();
      }
    }
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
   * @param info input info
   * @param err error code for unknown collation URIs
   * @return collation instance or {@code null} if uri is invalid.
   * @throws QueryException query exception
   */
  private static Collation get(final byte[] uri, final InputInfo info, final Err err)
      throws QueryException {

    // case-insensitive collation
    if(eq(NOCASE, uri)) return new NoCaseCollation();

    final int q = Token.indexOf(uri, '?');
    final byte[] base = q == -1 ? uri : substring(uri, 0, q);
    final String args = q == -1 ? "" : string(replace(substring(uri, q + 1), '&', ';'));

    final CollationOptions opts;
    if(eq(UCA, base) && UCAOptions.ACTIVE) {
      opts = new UCAOptions();
    } else if(eq(URL, base)) {
      opts = new BaseXCollationOptions();
    } else {
      throw err.get(info, Util.inf("Unknown collation '%'", uri));
    }

    try {
      return opts.get(args).uri(uri);
    } catch(final IllegalArgumentException ex) {
      throw err.get(info, ex);
    }
  }

  /**
   * Assigns the specified URI.
   * @param u uri
   * @return self reference
   */
  public final Collation uri(final byte[] u) {
    uri = u;
    return this;
  }

  /**
   * Checks if a string is contained in another.
   * @param string string
   * @param sub substring to be found
   * @param info input info
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
   * @param info input info
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
   * @param info input info
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
   * @param info input info
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] after(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), Mode.INDEX_OF, info);
    return i == -1 ? EMPTY : token(st.substring(i));
  }

  /**
   * Returns the substring before a specified string.
   * @param string string
   * @param sub substring to be found
   * @param info input info
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] before(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), Mode.INDEX_AFTER, info);
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
  public abstract int compare(final byte[] string, final byte[] compare);

  /**
   * Returns the start or end position of the specified substring.
   * @param string string
   * @param sub substring to be found
   * @param mode search mode
   * @param info input info
   * @return string index
   * @throws QueryException query exception
   */
  protected abstract int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo info) throws QueryException;
}
