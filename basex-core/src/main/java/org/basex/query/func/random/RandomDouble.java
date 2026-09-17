package org.basex.query.func.random;

import java.util.concurrent.*;

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
  @Override
  public Dbl value(final QueryContext qc) {
    return Dbl.get(ThreadLocalRandom.current().nextDouble());
  }
}
