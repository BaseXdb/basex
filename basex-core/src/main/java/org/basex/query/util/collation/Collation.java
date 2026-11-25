package org.basex.query.util.collation;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Collation for comparing strings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Collation {
  /** Collation URI. */
  private final byte[] uri;

  /**
   * Constructor.
   * @param uri collation URI
   */
  Collation(final byte[] uri) {
    this.uri = uri;
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
    return elements(string(string), info).indexOf(elements(string(sub), info), false) != -1;
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
    return elements(string(string), info).matches(elements(string(sub), info), 0);
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
    final CollationElements ces = elements(string(string), info);
    final CollationElements subs = elements(string(sub), info);
    return ces.matches(subs, ces.size() - subs.size());
  }

  /**
   * Returns the substring before or after the first or last occurrence of a specified string.
   * @param string string
   * @param sub substring to be found
   * @param before return substring before the occurrence
   * @param last search last occurrence
   * @param info input info (can be {@code null})
   * @return substring
   * @throws QueryException query exception
   */
  public final byte[] extract(final byte[] string, final byte[] sub, final boolean before,
      final boolean last, final InputInfo info) throws QueryException {

    final String st = string(string);
    final CollationElements ces = elements(st, info), subs = elements(string(sub), info);
    final int pos = ces.indexOf(subs, last), ss = subs.size();
    if(pos == -1) return EMPTY;
    // substrings without elements occur at the start or end
    final int i = ss == 0 ? last ? st.length() : 0 :
      before ? ces.start(pos) : ces.end(pos + ss - 1);
    return token(before ? st.substring(0, i) : st.substring(i));
  }

  /**
   * Returns the collation URI.
   * @return URI
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
   * Indicates if collation keys can be generated.
   * @return result of check
   */
  boolean keys() {
    return true;
  }

  /**
   * Returns the collation key of an item that is compared as string.
   * @param item item
   * @param coll collation (can be {@code null})
   * @param info input info (can be {@code null})
   * @return key, or {@code null} if no collation is given, the item is no string or keys
   *   cannot be generated
   * @throws QueryException query exception
   */
  public static byte[] key(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    return coll != null && coll.keys() && item.type.isStringOrUntyped() ?
      coll.key(item.string(info), info) : null;
  }

  /**
   * Compares two items, using their collation keys if both are available.
   * @param item1 first item
   * @param key1 collation key of the first item (can be {@code null})
   * @param item2 second item
   * @param key2 collation key of the second item (can be {@code null})
   * @param coll collation (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return result of comparison (-1, 0, 1)
   * @throws QueryException query exception
   */
  public static int compare(final Item item1, final byte[] key1, final Item item2,
      final byte[] key2, final Collation coll, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return key1 != null && key2 != null ? Token.compare(key1, key2) :
      item1.compare(item2, coll, true, qc, info);
  }

  /**
   * Returns the collation elements of a string.
   * @param string string
   * @param info input info (can be {@code null})
   * @return collation elements
   * @throws QueryException query exception
   */
  abstract CollationElements elements(String string, InputInfo info) throws QueryException;

  /**
   * Returns a standard collation key.
   * @param string string
   * @return key
   */
  public static byte[] key(final byte[] string) {
    final ByteList bl = new ByteList(string.length * 3L);
    Token.forEachCp(string, cp -> bl.add(cp >>> 16).add(cp >>> 8).add(cp));
    return bl.finish();
  }
}
