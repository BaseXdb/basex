package org.basex.query.func.random;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Dirk Kirsten
 */
public final class RandomDouble extends StandardFunc {
  /** Random instance. */
  private static final Random RND = new Random();

  @Override
  public Dbl value(final QueryContext qc) {
    return Dbl.get(RND.nextDouble());
  }
}
