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
 * This class resolves collation URIs.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Collations {
  /** ICU (UCA) collation URL. */
  public static final byte[] UCA = concat(W3_URL, "/2013/collation/UCA");
  /** BaseX collation URI. */
  private static final byte[] BASEX = token(BASEX_URL + "/collation");

  /** Private constructor. */
  private Collations() { }

  /**
   * Returns a collation instance for the specified URI.
   * @param uri collation URI (can be {@code null})
   * @param qc query context
   * @param info input info
   * @param err error code for unknown collation URIs
   * @return collation (can be {@code null})
   * @throws QueryException query exception
   */
  public static Collation get(final byte[] uri, final QueryContext qc, final InputInfo info,
      final QueryError err) throws QueryException {

    // return default collation
    if(uri == null) return info.sc().collation;
    // return Unicode point collation or cached instance (skips URI parsing)
    if(eq(COLLATION_URI, uri)) return null;

    if(qc.collations == null) qc.collations = new TokenObjectMap<>();
    Collation coll = qc.collations.get(uri);
    if(coll != null) return coll;

    final Uri u = Uri.get(uri);
    if(!u.isValid()) throw INVURI_X.get(info, uri);
    // URIs that do not depend on the base URI are cached by their original string
    byte[] url = uri, key = uri;
    if(!u.isAbsolute()) {
      if(startsWith(uri, '?')) {
        url = concat(BASEX, uri);
      } else {
        url = key = info.sc().baseURI().resolve(u).string();
        if(eq(COLLATION_URI, url)) return null;
        coll = qc.collations.get(url);
      }
    }
    if(coll == null) {
      coll = get(url, info, err);
      qc.collations.put(key, coll);
    }
    return coll;
  }

  /**
   * Returns the URI of a collation.
   * @param coll collation (can be {@code null})
   * @return URI
   */
  public static byte[] uri(final Collation coll) {
    return coll == null ? COLLATION_URI : coll.uri();
  }

  /**
   * Returns a collation instance for the specified URI.
   * @param uri collation URI
   * @param info input info (can be {@code null})
   * @param err error code for unknown collation URIs
   * @return collation instance
   * @throws QueryException query exception
   */
  private static Collation get(final byte[] uri, final InputInfo info, final QueryError err)
      throws QueryException {

    // static collations
    if(eq(HtmlNoCaseCollation.URI, uri)) return HtmlNoCaseCollation.INSTANCE;
    if(eq(UnicodeNoCaseCollation.URI, uri)) return UnicodeNoCaseCollation.INSTANCE;

    final int q = indexOf(uri, '?');
    final byte[] base = q == -1 ? uri : substring(uri, 0, q);
    final HashMap<String, String> args = args(
        q == -1 ? "" : string(replace(substring(uri, q + 1), '&', ';')));

    CollationOptions opts = null;
    if(eq(BASEX, base)) {
      opts = new BaseXCollationOptions(false);
    } else if(eq(UCA, base)) {
      final boolean fallback = !YesNo.NO.toString().equals(args.get(UCAOptions.FALLBACK.name()));
      if(ExternalLib.ICU.available()) opts = new UCAOptions(fallback);
      else if(fallback) opts = new BaseXCollationOptions(fallback);
    }
    if(opts == null) throw err.get(info, Util.inf("Unknown collation '%'.", uri));

    try {
      return opts.get(uri, args);
    } catch(final IllegalArgumentException | BaseXException ex) {
      throw err.get(info, ex.getMessage()).cause(ex);
    }
  }

  /**
   * Returns a map with all arguments.
   * @param args arguments
   * @return arguments
   */
  private static HashMap<String, String> args(final String args) {
    final HashMap<String, String> map = new HashMap<>();
    for(final String option : Strings.split(args, ';')) {
      final String[] kv = Strings.split(option, '=', 2);
      map.put(kv[0], kv.length == 2 ? kv[1] : "");
    }
    return map;
  }
}
