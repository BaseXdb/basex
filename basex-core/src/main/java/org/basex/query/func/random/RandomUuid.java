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
public final class RandomUuid extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) {
    return Str.get(UUID.randomUUID().toString());
  }
}
