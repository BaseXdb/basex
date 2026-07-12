package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringTokenSortRatio extends StringFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value1 = toStr(arg(0), qc), value2 = toStr(arg(1), qc);
    final FTOpt opt = ftOpt(toOptions(arg(2), new StringOptions(), qc));

    checkLength(value1.codepoints(info));
    checkLength(value2.codepoints(info));
    return Dbl.get(TokenRatio.sort(tokens(value1, opt), tokens(value2, opt)));
  }
}
