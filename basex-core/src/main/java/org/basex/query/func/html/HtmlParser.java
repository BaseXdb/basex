package org.basex.query.func.html;

import org.basex.build.html.HtmlParser.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HtmlParser extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) {
    final Parser parser = Parser.PARSER;
    return Str.get(parser != null ? parser.toString() : "");
  }
}
