package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnFormatInteger extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<FormatParser> formats = new TokenObjMap<>();

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] pic = toToken(exprs[1], qc);
    final byte[] lng = exprs.length == 2 ? EMPTY : toToken(exprs[2], qc);

    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return Str.ZERO;
    final long num = toLong(it);

    FormatParser fp = formats.get(pic);
    if(fp == null) {
      fp = new IntFormat(pic, info);
      formats.put(pic, fp);
    }
    return Str.get(Formatter.get(lng).formatInt(num, fp));
  }
}
