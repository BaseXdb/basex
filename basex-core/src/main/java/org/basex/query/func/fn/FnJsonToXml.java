package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class FnJsonToXml extends FnParseJson {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    if(item == Empty.VALUE) return Empty.VALUE;
    return parse(toToken(item), true, qc);
  }
}
