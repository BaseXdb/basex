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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class HtmlParse extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    return item != Empty.VALUE ? parse(new IOContent(toBytes(item)), qc) : Empty.VALUE;
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the input and creates an XML document.
   * @param io input data
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final Item parse(final IO io, final QueryContext qc) throws QueryException {
    final HtmlOptions opts = toOptions(1, new HtmlOptions(), qc);
    try {
      return new DBNode(new org.basex.build.html.HtmlParser(io, MainOptions.get(), opts));
    } catch(final IOException ex) {
      throw HTML_PARSE_X.get(info, ex);
    }
  }
}
