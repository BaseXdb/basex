package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * HexBinary item ({@code xs:hexBinary}).
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Hex extends Bin {
  /**
   * Constructor.
   * @param value bytes
   */
  public Hex(final byte[] value) {
    super(value, AtomType.HEX);
  }

  /**
   * Constructor.
   * @param value textual representation
   * @param ii input info
   * @throws QueryException query exception
   */
  public Hex(final byte[] value, final InputInfo ii) throws QueryException {
    super(parse(Token.trim(value), ii), AtomType.HEX);
  }

  /**
   * Constructor.
   * @param bin binary data
   * @param ii input info
   * @throws QueryException query exception
   */
  public Hex(final Bin bin, final InputInfo ii) throws QueryException {
    this(bin.binary(ii));
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Token.hex(binary(ii), true);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return Token.eq(binary(ii), it instanceof Bin ? ((Bin) it).binary(ii) : parse(it, ii));
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return Token.diff(binary(ii), it instanceof Bin ? ((Bin) it).binary(ii) : parse(it, ii));
  }

  /**
   * Converts the given item to a byte array.
   * @param item item to be converted
   * @param info input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final Item item, final InputInfo info) throws QueryException {
    try {
      return parse(item.string(info));
    } catch(final IllegalArgumentException ex) {
      throw AtomType.HEX.castError(item, info);
    }
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param info input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo info) throws QueryException {
    try {
      return parse(value);
    } catch(final IllegalArgumentException ex) {
      throw AtomType.HEX.castError(value, info);
    }
  }

  /**
   * Converts the input into a byte array.
   * @param data textual data
   * @return decoded string
   * @throws IllegalArgumentException illegal argument exception
   */
  private static byte[] parse(final byte[] data) throws IllegalArgumentException {
    if((data.length & 1) != 0) throw new IllegalArgumentException();
    final int l = data.length >>> 1;
    final byte[] v = new byte[l];
    for(int i = 0; i < l; ++i) {
      v[i] = (byte) ((dec(data[i << 1]) << 4) + dec(data[(i << 1) + 1]));
    }
    return v;
  }

  /**
   * Converts a single character into a byte value.
   * @param ch character
   * @return byte value
   * @throws IllegalArgumentException illegal argument exception
   */
  private static int dec(final byte ch) throws IllegalArgumentException {
    if(ch >= '0' && ch <= '9') return ch - '0';
    if(ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F') return (ch & 0x0F) + 9;
    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return new TokenBuilder().add('"').add(Token.hex(data, true)).add('"').toString();
  }
}
