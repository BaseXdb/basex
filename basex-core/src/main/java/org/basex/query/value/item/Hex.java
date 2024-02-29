package org.basex.query.value.item;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * HexBinary item ({@code xs:hexBinary}).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Hex extends Bin {
  /**
   * Constructor.
   * @param value bytes
   */
  public Hex(final byte[] value) {
    super(value, AtomType.HEX_BINARY);
  }

  /**
   * Constructor.
   * @param value textual representation
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Hex(final byte[] value, final InputInfo info) throws QueryException {
    this(parse(Token.trim(value), info));
  }

  /**
   * Constructor.
   * @param bin binary data
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Hex(final Bin bin, final InputInfo info) throws QueryException {
    this(bin.binary(info));
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeToken(data);
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return Token.hex(data, true);
  }

  @Override
  public int hash() {
    return Token.hash(data);
  }

  @Override
  public byte[] parse(final Item item, final InputInfo info) throws QueryException {
    return parse(item.string(info), info);
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo info) throws QueryException {
    final byte[] bytes = parse(value);
    if(bytes != null) return bytes;
    throw AtomType.HEX_BINARY.castError(value, info);
  }

  /**
   * Converts the input into a byte array.
   * @param data textual data
   * @return byte array, or {@code null} if input is invalid
   */
  private static byte[] parse(final byte[] data) {
    final int dl = data.length;
    if((dl & 1) != 0) return null;
    final byte[] array = new byte[dl >>> 1];
    for(int d = 0; d < dl; d += 2) {
      final int n = Token.dec(data[d], data[d + 1]);
      if(n < 0) return null;
      array[d >>> 1] = (byte) n;
    }
    return array;
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add('"');
    if(data.length > 128) {
      tb.add(Token.hex(Arrays.copyOf(data, 128), true)).add(Text.DOTS);
    } else {
      tb.add(Token.hex(data, true));
    }
    qs.token(tb.add('"').finish());
  }
}
