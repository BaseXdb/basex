package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This abstract class retrieves values from an index.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class IndexAccess extends Simple {
  /** Index context. */
  final IndexContext ictx;

  /**
   * Constructor.
   * @param ictx index context
   * @param info input info
   */
  IndexAccess(final IndexContext ictx, final InputInfo info) {
    super(info);
    this.ictx = ictx;
    type = SeqType.NOD_ZM;
  }

  @Override
  public abstract NodeIter iter(final QueryContext ctx) throws QueryException;

  @Override
  public final boolean iterable() {
    return ictx.iterable;
  }
}
