package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnAtomicTypeAnnotation extends FnSchemaType {

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    return annotate(qc, info, value.type.atomic());
  }
}
