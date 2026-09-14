package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinDecodeString extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), BIN_UE_X, qc);
    final Long offset = toLongOrNull(arg(2), qc);
    final Long size = toLongOrNull(arg(3), qc);
    if(value == null) return Empty.VALUE;

    final boolean all = offset == null && size == null;
    try(BufferInput bi = all ? value.input(info) : input(value, offset, size)) {
      return Str.get(ConvertFn.toString(bi, encoding, false));
    } catch(final IOException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }

  /**
   * Returns an input stream over the specified part of a binary value.
   * @param value binary value
   * @param offset offset (can be {@code null})
   * @param size size (can be {@code null})
   * @return input stream
   * @throws QueryException query exception
   */
  private BufferInput input(final Bin value, final Long offset, final Long size)
      throws QueryException {
    final byte[] bytes = value.binary(info);
    final int[] bounds = bounds(offset, offset != null ? size : null, bytes.length);
    return new ArrayInput(Arrays.copyOfRange(bytes, bounds[0], bounds[0] + bounds[1]));
  }
}
