package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnStaticBaseUri extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) {
    final Uri uri = sc().baseURI();
    return uri == Uri.EMPTY ? Empty.VALUE : uri;
  }
}
