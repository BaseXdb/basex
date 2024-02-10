package org.basex.query.func.validate;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ValidateXsdInit extends ValidateFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    MAP.clear();
    return Empty.VALUE;
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) throws QueryException {
    throw Util.notExpected();
  }
}
