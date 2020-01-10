package org.basex.query.func.html;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.html.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class HtmlParse extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final HtmlOptions hopts = toOptions(1, new HtmlOptions(), qc);
    if(item == Empty.VALUE) return Empty.VALUE;

    final MainOptions opts = MainOptions.get();
    try {
      final IO io = new IOContent(toBytes(item));
      return new DBNode(new org.basex.build.html.HtmlParser(io, opts, hopts));
    } catch(final IOException ex) {
      throw HTML_PARSE_X.get(info, ex);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optFirst(true);
  }
}
