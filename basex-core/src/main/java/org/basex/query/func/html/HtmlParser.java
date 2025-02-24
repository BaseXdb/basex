package org.basex.query.func.html;

import org.basex.build.html.HtmlParser.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HtmlParser extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    final Parser parser = Parser.DEFAULT;
    return Str.get(parser != null ? parser.toString() : "");
  }
}
