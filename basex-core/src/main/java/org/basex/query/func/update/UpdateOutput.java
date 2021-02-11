package org.basex.query.func.update;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class UpdateOutput extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(qc.updates().mod instanceof TransformModifier) throw BASEX_UPDATE.get(info);

    qc.updates().addOutput(exprs[0].value(qc), qc);
    return Empty.VALUE;
  }
}
