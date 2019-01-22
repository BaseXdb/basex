package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * HexBinary item ({@code xs:hexBinary}).
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param info input info
   * @throws QueryException query exception
   */
  public Hex(final byte[] value, final InputInfo info) throws QueryException {
    super(parse(Token.trim(value), info), AtomType.HEX);
  }

  /**
   * Constructor.
   * @param bin binary data
   * @param info input info
   * @throws QueryException query exception
   */
  public Hex(final Bin bin, final InputInfo info) throws QueryException {
    this(bin.binary(info));
  }

  @Override
  public byte[] string(final InputInfo info) throws QueryException {
    return Token.hex(binary(info), true);
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(info) : parse(item, info);
    return Token.eq(binary(info), bin);
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(info) : parse(item, info);
    return Token.diff(binary(info), bin);
  }

  /**
   * Converts the given item to a byte array.
   * @param item item to be converted
   * @param info input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final Item item, final InputInfo info) throws QueryException {
    final byte[] bytes = parse(item.string(info));
    if(bytes != null) return bytes;
    throw AtomType.HEX.castError(item, info);
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param info input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo info) throws QueryException {
    final byte[] bytes = parse(value);
    if(bytes != null) return bytes;
    throw AtomType.HEX.castError(value, info);
  }

  /**
   * Converts the input into a byte array.
   * @param data textual data
   * @return byte array, or {@code null} if input is invalid
   */
  private static byte[] parse(final byte[] data) {
    final int dl = data.length;
    if((dl & 1) != 0) return null;
    final byte[] value = new byte[dl >>> 1];
    for(int d = 0; d < dl; d += 2) {
      final int n = Token.dec(data[d], data[d + 1]);
      if(n < 0) return null;
      value[d >>> 1] = (byte) n;
    }
    return value;
  }

  @Override
  public String toString() {
    return Strings.concat('"', Token.hex(data, true), '"');
  }
}
