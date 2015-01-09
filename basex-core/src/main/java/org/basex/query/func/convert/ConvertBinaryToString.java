package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ConvertBinaryToString extends ConvertFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin bin = toBin(exprs[0], qc);
    final String enc = toEncoding(1, BXCO_ENCODING_X, qc);
    try {
      return Str.get(toString(bin.input(info), enc, qc));
    } catch(final IOException ex) {
      throw BXCO_STRING_X.get(info, ex);
    }
  }

  /**
   * Converts the specified input to a string in the specified encoding.
   * @param is input stream
   * @param enc encoding
   * @param qc query context
   * @return resulting value
   * @throws IOException I/O exception
   */
  public static byte[] toString(final InputStream is, final String enc, final QueryContext qc)
      throws IOException {
    return toString(is, enc, qc.context.options.get(MainOptions.CHECKSTRINGS));
  }
}
