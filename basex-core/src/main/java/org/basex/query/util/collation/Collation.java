package org.basex.query.util.collation;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * This class organizes collations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class Collation {
  /** UCA collations. */
  private static final byte[] UCA = token("http://www.w3.org/2013/collation/UCA");
  /** Implementation-defined collation URL. */
  private static final byte[] URL = token(Prop.URL + "/collation");

  /** Available locales, indexed by language code. */
  private static HashMap<String, Locale> locales;

  /** Collator. */
  @SuppressWarnings("rawtypes")
  private final Comparator comp;
  /** Collation URI. */
  private final byte[] uri;

  /**
   * Private Constructor.
   * @param cl comparator instance
   * @param u uri
   */
  @SuppressWarnings("rawtypes")
  Collation(final Comparator cl, final byte[] u) {
    comp = cl;
    uri = u;
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
    // return unicode point collation
    if(eq(NoCaseCollation.URL, url)) return NoCaseCollation.get();

    // create new collation or return cached instance
    if(qc.collations == null) qc.collations = new TokenObjMap<>();
    Collation coll = qc.collations.get(url);
    if(coll == null) {
      final Collator cl = get(url, info, err);
      if(cl == null) return null;
      coll = new Collation(get(url, info, err), url);
      qc.collations.put(url, coll);
    }
    return coll;
  }

  /**
   * Returns a collator instance for the specified uri.
   * @param uri uri
   * @param info input info
   * @param err error code for unknown collation uris
   * @return collator instance or {@code null} if uri is invalid.
   * @throws QueryException query exception
   */
  private static Collator get(final byte[] uri, final InputInfo info, final Err err)
      throws QueryException {

    final int q = Token.indexOf(uri, '?');
    final byte[] base = q == -1 ? uri : substring(uri, 0, q);
    final String args = q == -1 ? "" : string(replace(substring(uri, q + 1), '&', ';'));

    final Options opts;
    final boolean uca = eq(UCA, base);
    if(uca) {
      opts = new UCAOptions();
    } else if(eq(URL, base)) {
      opts = new CollationOptions();
    } else {
      throw err.get(info, uri);
    }

    final boolean nomercy = !uca || args.contains("fallback=no");
    for(final String param : args.split(";")) {
      final String[] kv = param.split("=");
      if(kv.length != 2) return null;
      final String key = kv[0], val = kv[1];
      try {
        opts.assign(key, val);
      } catch(final BaseXException ex) {
        if(nomercy || key.equals(UCAOptions.FALLBACK.name())) throw err.get(info, uri);;
      }
    }
    if(uca) {
      if(nomercy) throw err.get(info, uri);
      return null;
    }

    if(locales == null) {
      // initializes locales
      locales = new HashMap<>();
      for(final Locale l : Locale.getAvailableLocales())
        locales.put(l.toString().replace('_', '-'), l);
    }
    final Locale locale = locales.get(opts.get(CollationOptions.LANG));
    if(locale == null) throw err.get(info, uri);

    final Collator coll = Collator.getInstance(locale);
    if(opts.contains(CollationOptions.STRENGTH))
      coll.setStrength(opts.get(CollationOptions.STRENGTH).value);
    if(opts.contains(CollationOptions.DECOMPOSITION))
      coll.setDecomposition(opts.get(CollationOptions.DECOMPOSITION).value);
    return coll;
  }

  /**
   * Compares two strings.
   * @param string string
   * @param compare string to be compared
   * @return result of check
   */
  @SuppressWarnings("unchecked")
  public int compare(final byte[] string, final byte[] compare) {
    return comp.compare(string(string), string(compare));
  }

  /**
   * Checks if a string is contained in another.
   * @param string string
   * @param sub substring to be found
   * @param info input info
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {
    return indexOf(string(string), string(sub), false, false, info) != -1;
  }

  /**
   * Checks if a string starts with another.
   * @param string string
   * @param sub substring to be found
   * @param info input info
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean startsWith(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final RuleBasedCollator rbc = rbc(info);
    return startsWith(rbc.getCollationElementIterator(string(string)),
        rbc.getCollationElementIterator(string(sub)));
  }

  /**
   * Checks if a string ends with another.
   * @param string string
   * @param sub substring to be found
   * @param info input info
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean endsWith(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {
    return indexOf(string(string), string(sub), false, true, info) != -1;
  }

  /**
   * Returns the substring after a specified string.
   * @param string string
   * @param sub substring to be found
   * @param info input info
   * @return substring
   * @throws QueryException query exception
   */
  public byte[] after(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), false, false, info);
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
  public byte[] before(final byte[] string, final byte[] sub, final InputInfo info)
      throws QueryException {

    final String st = string(string);
    final int i = indexOf(st, string(sub), true, false, info);
    return i == -1 ? EMPTY : token(st.substring(0, i));
  }

  /**
   * Returns the collation URI.
   * @return uri
   */
  public byte[] uri() {
    return uri;
  }

  /**
   * Returns the start or end position of the specified substring.
   * @param string string
   * @param sub substring to be found
   * @param start return start or end position
   * @param ends checks if string ends with substring
   * @param info input info
   * @return result of check
   * @throws QueryException query exception
  */
  private int indexOf(final String string, final String sub, final boolean start,
      final boolean ends, final InputInfo info) throws QueryException {

    final RuleBasedCollator rbc = rbc(info);
    final CollationElementIterator i = rbc.getCollationElementIterator(string);
    final CollationElementIterator is = rbc.getCollationElementIterator(sub);
    do {
      final int cs = next(is);
      if(cs == -1) return 0;
      int c;
      do {
        c = next(i);
        if(c == -1) return -1;
      } while(c != cs);

      final int s = i.getOffset();
      if(startsWith(i, is) && (!ends || next(i) == -1))
        return start ? s - 1 : i.getOffset();
      i.setOffset(s);
      is.reset();
    } while(true);
  }

  /**
   * Determine whether one string starts with another.
   * @param i string iterator
   * @param is substring iterator
   * @return result of check
   */
  private static boolean startsWith(final CollationElementIterator i,
      final CollationElementIterator is) {
    do {
      final int cs = next(is);
      if(cs == -1) return true;
      if(cs != next(i)) return false;
    } while(true);
  }

  /**
   * Returns the next element from an iterator.
   * @param i iterator
   * @return next element, or {@code -1}
   */
  private static int next(final CollationElementIterator i) {
    do {
      final int c = i.next();
      if(c != 0) return c;
    } while(true);
  }

  /**
   * Converts the collator to a rule-based collator, or raises an error.
   * @param info input info
   * @return rule-based collator
   * @throws QueryException query exception
   */
  private RuleBasedCollator rbc(final InputInfo info) throws QueryException {
    if(comp instanceof RuleBasedCollator) return (RuleBasedCollator) comp;
    throw CHARCOLL.get(info);
  }
}
