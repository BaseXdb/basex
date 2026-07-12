package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnPadString extends StandardFunc {
  /** Padding side. */
  public enum Side {
    /** Start. */ START,
    /** End.   */ END,
    /** Both.  */ BOTH;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /** Options. */
  public static final class PadOptions extends Options {
    /** Option. */
    public static final StringOption FILL = new StringOption("fill", " ");
    /** Option. */
    public static final EnumOption<Side> SIDE = new EnumOption<>("side", Side.END);
  }

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    final long length = toLong(arg(1), qc);
    final PadOptions options = toOptions(arg(2), new PadOptions(), qc);

    final byte[] token = value.isEmpty() ? Token.EMPTY : value.string(info);
    final long missing = length - Token.length(token);
    if(missing <= 0) return Str.get(token);

    final byte[] fill = Token.token(options.get(PadOptions.FILL));
    if(fill.length == 0) throw INVALIDOPTION_X.get(info,
        Util.info("Option '%': fill string must not be empty.", PadOptions.FILL.name()));
    if(missing > Integer.MAX_VALUE) throw RANGE_X.get(info, length);

    final int miss = (int) missing;
    final Side side = options.get(PadOptions.SIDE);
    final int start = side == Side.START ? miss : side == Side.BOTH ? miss / 2 : 0;
    final TokenBuilder tb = new TokenBuilder();
    pad(tb, fill, start);
    tb.add(token);
    pad(tb, fill, miss - start);
    return Str.get(tb.finish());
  }

  /**
   * Appends the specified number of characters, repeating the fill string as often as required.
   * @param tb token builder
   * @param fill fill string
   * @param count number of characters to append
   */
  private static void pad(final TokenBuilder tb, final byte[] fill, final int count) {
    for(int c = 0; c < count;) {
      for(int f = 0; f < fill.length && c < count; f += Token.cl(fill, f), c++) {
        tb.add(Token.cp(fill, f));
      }
    }
  }
}
