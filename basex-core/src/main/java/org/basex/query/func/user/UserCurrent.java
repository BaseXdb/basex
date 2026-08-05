package org.basex.query.func.user;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UserCurrent extends UserFn {
  @Override
  protected Str item(final QueryContext qc) {
    return Str.get(qc.user.name());
  }
}
