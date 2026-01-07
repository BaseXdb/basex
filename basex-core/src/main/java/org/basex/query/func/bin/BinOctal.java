package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinOctal extends BinFn {
  /** Octal/binary map. */
  private static final IntObjectMap<String> MAP = new IntObjectMap<>(8);

  static {
    MAP.put('0', "000");
    MAP.put('1', "001");
    MAP.put('2', "010");
    MAP.put('3', "011");
    MAP.put('4', "100");
    MAP.put('5', "101");
    MAP.put('6', "110");
    MAP.put('7', "111");
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toDigits(qc);
    if(value == null) return Empty.VALUE;
    final int tl = value.length;
    if(tl == 0) return B64.EMPTY;

    final TokenBuilder tb = new TokenBuilder(tl * 3L);
    for(final byte b : value) {
      final String bits = MAP.get(b);
      if(bits != null) tb.add(bits);
      else tb.addByte(b);
    }
    if(tb.get(0) == '0' && tb.get(1) == '0') tb.delete(0, 2);
    return B64.get(binary2bytes(tb.finish()));
  }
}
