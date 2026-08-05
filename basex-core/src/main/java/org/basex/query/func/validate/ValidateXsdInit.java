package org.basex.query.func.validate;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValidateXsdInit extends ValidateFn {
  @Override
  protected Item item(final QueryContext qc) {
    MAP.clear();
    return Empty.VALUE;
  }

  @Override
  public ArrayList<ErrorInfo> errors(final QueryContext qc) {
    throw Util.notExpected();
  }
}
