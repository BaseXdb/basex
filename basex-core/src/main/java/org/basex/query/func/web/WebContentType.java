package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebContentType extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return Str.get(MediaType.get(toString(arg(0), qc)).toString());
  }
}
