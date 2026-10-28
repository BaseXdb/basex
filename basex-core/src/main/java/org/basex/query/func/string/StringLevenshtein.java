package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshtein extends StringScore {
  @Override
  public Dbl value(final QueryContext qc) throws QueryException {
    return cpsScore(qc, Levenshtein::distance);
  }
}
