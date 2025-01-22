package org.basex.query.func.html;

import org.basex.build.html.*;
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
    final HtmlOptions options = new HtmlOptions();
    final Parser parser = Parser.of(options);
    return Str.get(parser.available(options) ? parser.toString() : "");
  }
}
