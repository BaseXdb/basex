package org.basex.query.util.collation;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.serial.SerializerOptions.YesNo;
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
public class Collation {
  /** UCA collations. */
  private static final byte[] UCA = token("http://www.w3.org/2013/collation/UCA");
  /** Implementation-defined collation URL. */
  private static final byte[] URL = token(Prop.URL + "/collation");
  /** Initialization of locales. */
  private static class Locales {
    /** Available locales, indexed by language code. */
    static HashMap<String, Locale> map = new HashMap<>();
    static {
      for(final Locale l : Locale.getAvailableLocales()) map.put(l.toString().replace('_', '-'), l);
    }
  }

  /** Collator. */
  @SuppressWarnings("rawtypes")
  private final Comparator comp;
  /** Collation URI. */
  private final byte[] uri;

  /**
   * Private Constructor.
   * @param comp comparator instance
   * @param uri uri
   */
  @SuppressWarnings("rawtypes")
  Collation(final Comparator comp, final byte[] uri) {
    this.comp = comp;
    this.uri = uri;
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

    final CollationOptions opts;
    final boolean uca = eq(UCA, base);
    if(uca) {
      opts = new UCAOptions();
    } else if(eq(URL, base)) {
      opts = new BaseXCollationOptions();
    } else {
      throw err.get(info, uri);
    }

    String fail = null;
    for(final String param : args.split(";")) {
      final String[] kv = param.split("=");
      if(kv.length != 2) return null;
      final String key = kv[0], val = kv[1];
      try {
        opts.assign(key, val);
      } catch(final BaseXException ex) {
        fail = key;
      }
    }
    if(fail != null && (!uca || fail.equals(UCAOptions.FALLBACK.name()) ||
        opts.get(UCAOptions.FALLBACK) == YesNo.NO)) throw err.get(info, uri);;

    final Locale locale = Locales.map.get(opts.get(BaseXCollationOptions.LANG));
    if(locale == null) throw err.get(info, uri);

    final Collator coll = Collator.getInstance(locale);
    if(!opts.assign(coll)) throw err.get(info, uri);
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

  /** Search modes. */
  protected static enum Mode {
    /** Default. */      INDEX_OF,
    /** End position. */ INDEX_AFTER,
    /** Starts-with. */  STARTS_WITH,
    /** Ends-with. */    ENDS_WITH
  }

  /**
   * Returns the start or end position of the specified substring.
   * @param string string
   * @param sub substring to be found
   * @param mode search mode
   * @param info input info
   * @return string index
   * @throws QueryException query exception
   */
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo info) throws QueryException {

    final RuleBasedCollator rbc = rbc(info);
    final CollationElementIterator i = rbc.getCollationElementIterator(string);
    final CollationElementIterator is = rbc.getCollationElementIterator(sub);
    do {
      final int cs = next(is);
      if(cs == -1) return 0;
      int c;
      // find first equal character
      do {
        c = next(i);
        if(c == -1) return -1;
      } while(c != cs);

      final int s = i.getOffset();
      if(startsWith(i, is)) {
        if(mode == Mode.INDEX_AFTER) {
          return i.getOffset();
        } else if(mode == Mode.ENDS_WITH) {
          if(next(i) == -1) return s - 1;
        } else {
          return s - 1;
        }
      }
      i.setOffset(s);
      is.reset();
    } while(mode != Mode.STARTS_WITH);

    return -1;
  }

  /**
   * Determines whether one string starts with another.
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
