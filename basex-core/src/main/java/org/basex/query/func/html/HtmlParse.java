package org.basex.query.func.html;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.build.html.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class HtmlParse extends StandardFunc {
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get("options", HTML_URI);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] in = toBinary(exprs[0], qc);
    final HtmlOptions opts = toOptions(1, Q_OPTIONS, new HtmlOptions(), qc);
    try {
      return new DBNode(new org.basex.build.html.HtmlParser(new IOContent(in),
          qc.context.options, opts));
    } catch(final IOException ex) {
      throw BXHL_IO_X.get(info, ex);
    }
  }
}
