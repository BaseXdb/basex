package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinFromBase64url extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    // strip trailing padding, skip whitespace, convert to standard alphabet
    int end = value.length;
    while(end > 0 && (value[end - 1] == '=' || ws(value[end - 1]))) end--;
    final TokenBuilder tb = new TokenBuilder(end + 3);
    for(int i = 0; i < end; i++) {
      final byte b = value[i];
      if(b == '-') tb.add('+');
      else if(b == '_') tb.add('/');
      else if(letterOrDigit(b)) tb.addByte(b);
      else if(!ws(b)) throw error(value, null);
    }
    while((tb.size() & 3) != 0) tb.add('=');

    try {
      return B64.get(B64.parse(tb.finish(), info));
    } catch(final QueryException ex) {
      throw error(value, ex);
    }
  }

  /**
   * Returns a conversion error.
   * @param value input string
   * @param cause cause (can be {@code null})
   * @return query exception
   */
  private QueryException error(final byte[] value, final QueryException cause) {
    return BIN_CE_X.get(info, "Invalid Base64url string: " + string(value)).cause(cause);
  }
}
