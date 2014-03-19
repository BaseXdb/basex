package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class organizes collations.
 * Some of the core functions have been inspired by the Saxon HE source code.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Collation {
  /** Implementation-defined collation URL. */
  private static final byte[] URL = token(Text.URL + "/collation");
  /** Available locales, indexed by language code. */
  private static final HashMap<String, Locale> LOCALES = new HashMap<>();
  /** Strengths. */
  private static final byte[][] STRENGTHS = tokens(
    "primary", "secondary", "tertiary", "identical");
  /** Decompositions. */
  private static final byte[][] DECOMPOSITIONS = tokens("none", "full", "standard");
  /** Language string. */
  private static final byte[] LANG = token("lang");
  /** Strength string. */
  private static final byte[] STRENGTH = token("strength");
  /** Decomposition string. */
  private static final byte[] DECOMPOSITION = token("decomposition");

  static {
    for(final Locale l : Locale.getAvailableLocales())
      LOCALES.put(l.toString().replace('_', '-'), l);
  }

  /** Collator. */
  private final Collator coll;
  /** Collation URI. */
  private final byte[] uri;

  /**
   * Private Constructor.
   * @param cl collator instance
   * @param u uri
   */
  private Collation(final Collator cl, final byte[] u) {
    coll = cl;
    uri = u;
  }

  /**
   * Returns a collation instance for the specified uri.
   * @param uri collation uri
   * @param ctx query context
   * @param sc static context
   * @param info input info
   * @param err error code for unknown collation uris
   * @return collator instance or {@code null} for unicode point collation
   * @throws QueryException query exception
   */
  public static Collation get(final byte[] uri, final QueryContext ctx, final StaticContext sc,
      final InputInfo info, final Err err) throws QueryException {

    // return default collation
    if(uri == null) return sc.collation;

    byte[] args = uri;
    final Uri u = Uri.uri(args);
    if(!u.isValid()) throw INVURI.get(info, args);
    if(!u.isAbsolute() && !Token.startsWith(args, '?')) {
      args = sc.baseURI().resolve(u, info).string();
    }
    // return unicode point collation
    if(eq(COLLATIONURI, args)) return null;

    // normalize arguments
    if(Token.startsWith(args, URL)) args = substring(args, URL.length);
    if(Token.startsWith(args, '?')) args = substring(args, 1);
    args = replace(args, '&', ';');

    // create new collation or return cached instance
    final byte[] full = new TokenBuilder(URL).add('?').add(args).finish();
    if(ctx.collations == null) ctx.collations = new TokenObjMap<>();
    Collation coll = ctx.collations.get(full);
    if(coll == null) {
      final Collator cl = get(args);
      if(cl == null) throw err.get(info, uri);
      coll = new Collation(cl, full);
      ctx.collations.put(full, coll);
    }
    return coll;
  }

  /**
   * Returns a collator instance for the specified uri.
   * @param args arguments
   * @return collator instance or {@code null} if uri is invalid.
   */
  private static Collator get(final byte[] args) {
    Locale locale = Locale.getDefault();
    int strngth = -1;
    int dcmpstn = -1;
    for(final byte[] param : split(args, ';')) {
      final byte[][] kv = split(param, '=');
      if(kv.length != 2) return null;
      final byte[] key = kv[0];
      final byte[] val = kv[1];
      if(eq(key, LANG)) {
        locale = LOCALES.get(string(val));
        if(locale == null) return null;
      } else if(eq(key, STRENGTH)) {
        final int ss = STRENGTHS.length;
        int s = -1;
        while(++s < ss && !eq(val, STRENGTHS[s]));
        if(s == ss) return null;
        strngth = s;
      } else if(eq(key, DECOMPOSITION)) {
        final int ss = DECOMPOSITIONS.length;
        int s = -1;
        while(++s < ss && !eq(val, DECOMPOSITIONS[s]));
        if(s == ss) return null;
        dcmpstn = s;
      } else {
        return null;
      }
    }
    final Collator coll = Collator.getInstance(locale);
    if(strngth != -1) coll.setStrength(strngth);
    if(dcmpstn != -1) coll.setDecomposition(dcmpstn);
    return coll;
  }

  /**
   * Compares two strings.
   * @param string string
   * @param compare string to be compared
   * @return result of check
   */
  public int compare(final byte[] string, final byte[] compare) {
    return coll.compare(string(string), string(compare));
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
    if(coll instanceof RuleBasedCollator) return (RuleBasedCollator) coll;
    throw CHARCOLL.get(info);
  }
}
