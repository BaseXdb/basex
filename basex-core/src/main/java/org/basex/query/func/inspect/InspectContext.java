package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectContext extends StandardFunc {
  /** Name of the result element. */
  public static final QNm Q_CONTEXT = new QNm("context");

  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    return new PlainDoc(qc, info).context();
  }
}
