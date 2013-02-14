package org.basex.query.expr;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This abstract class retrieves values from an index.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class IndexAccess extends Simple {
  /** Data reference. */
  final Data data;
  /** Flag for iterative evaluation. */
  final boolean iterable;

  /**
   * Constructor.
   * @param d data reference
   * @param iter flag for iterative evaluation
   * @param ii input info
   */
  protected IndexAccess(final Data d, final boolean iter, final InputInfo ii) {
    super(ii);
    data = d;
    iterable = iter;
    type = SeqType.NOD_ZM;
  }

  @Override
  public abstract NodeIter iter(final QueryContext ctx) throws QueryException;

  @Override
  public final boolean iterable() {
    return iterable;
  }
}
