package org.basex.query.value.item;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class AStr extends Item {
  /** ASCII index flag. */
  private static final int[] ASCII = {};
  /** Number of codepoints that are represented by a single index entry. */
  private static final int BLOCK = 32;

  /** String data ({@code null} if not cached yet). */
  byte[] value;
  /** Sparse codepoint index ({@code null}: not computed; {@link #ASCII}: ASCII string). */
  private int[] index;

  /**
   * Constructor.
   */
  AStr() {
    super(BasicType.STRING);
  }

  /**
   * Constructor, specifying a type and value.
   * @param value value
   * @param type atomic type
   */
  AStr(final byte[] value, final Type type) {
    super(type);
    this.value = value;
    if(value.length < 2) index = ASCII;
  }

  @Override
  public final boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  /**
   * Checks if the string only consists of ASCII characters.
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean ascii(final InputInfo info) throws QueryException {
    return index(info) == ASCII;
  }

  /**
   * Returns the codepoint index of the string.
   * @param info input info (can be {@code null})
   * @return index, or {@link #ASCII} for ASCII strings
   * @throws QueryException query exception
   */
  private int[] index(final InputInfo info) throws QueryException {
    int[] idx = index;
    if(idx == null) {
      final byte[] token = string(info);
      if(Token.ascii(token)) {
        idx = ASCII;
      } else {
        // count codepoints first: the index can then be allocated with its final size
        final int count = Token.length(token, false), tl = token.length;
        idx = new int[(count - 1) / BLOCK + 1];
        idx[0] = count;
        for(int t = 0, c = 0; t < tl; t += Token.cl(token, t), c++) {
          if(c != 0 && c % BLOCK == 0) idx[c / BLOCK] = t;
        }
      }
      index = idx;
    }
    return idx;
  }

  /**
   * Returns the byte offset of the specified codepoint.
   * @param token token
   * @param idx codepoint index
   * @param pos codepoint position
   * @return byte offset
   */
  private static int offset(final byte[] token, final int[] idx, final int pos) {
    final int block = pos / BLOCK;
    int t = block == 0 ? 0 : idx[block];
    for(int c = pos % BLOCK; c > 0; c--) t += Token.cl(token, t);
    return t;
  }

  /**
   * Returns the string length.
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final int length(final InputInfo info) throws QueryException {
    final int[] idx = index(info);
    return idx == ASCII ? string(info).length : idx[0];
  }

  /**
   * Returns the single characters of the string.
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final byte[][] characters(final InputInfo info) throws QueryException {
    final TokenList list = new TokenList(length(info));
    Token.forEachCp(string(info), cp -> list.add(Token.cpToken(cp)));
    return list.finish();
  }

  /**
   * Returns the codepoints of the string.
   * @param info input info (can be {@code null})
   * @return codepoints
   * @throws QueryException query exception
   */
  public final int[] codepoints(final InputInfo info) throws QueryException {
    final IntList list = new IntList(length(info));
    Token.forEachCp(string(info), list::add);
    return list.finish();
  }

  /**
   * Returns a substring.
   * @param info input info (can be {@code null})
   * @param start start position
   * @param end end position
   * @return substring
   * @throws QueryException query exception
   */
  public final AStr substring(final InputInfo info, final int start, final int end)
      throws QueryException {
    final byte[] token = string(info);
    final int[] idx = index(info);
    final boolean ascii = idx == ASCII;
    final int length = ascii ? token.length : idx[0];
    if(start == 0 && end == length) return this;

    final int s = ascii ? start : offset(token, idx, start);
    final int e = ascii ? end : end < length ? offset(token, idx, end) : token.length;
    return Str.get(Arrays.copyOfRange(token, s, e));
  }

  @Override
  public Item shrink(final QueryContext qc) {
    // reuse an equal token; skip lazy items whose value has not been cached yet
    if(value != null) value = qc.shared.token(value);
    return this;
  }

  @Override
  public final boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    return comparable(item) && Token.eq(string(deep.info), item.string(deep.info), deep);
  }

  @Override
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final QueryContext qc, final InputInfo ii) throws QueryException {
    return Token.compare(string(ii), item.string(ii), Collation.get(coll, ii));
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof final AStr a)) return false;
    return type == a.type && Token.eq(value, a.value);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.quoted(value);
  }
}
