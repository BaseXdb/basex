package org.basex.query.func.validate;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ValidateXsd extends ValidateXsdInfo {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Item it = item(qc, info);
    return it != null ? it.iter() : Empty.ITER;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return item(qc, null);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value seq = super.value(qc);
    if(seq.isEmpty()) return null;
    throw BXVA_FAIL_X.get(info, seq.iter().next());
  }
}
