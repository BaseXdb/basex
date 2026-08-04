package org.basex.query.func.validate;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValidateXsdVersion extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) {
    return Str.get(ValidateXsd.version());
  }
}
