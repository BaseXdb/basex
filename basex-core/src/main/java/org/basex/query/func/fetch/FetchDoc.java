package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FetchDoc extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return fetch(toIO(arg(0), qc), qc);
  }

  /**
   * Parses the input and creates an XML document.
   * @param source source
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected DBNode fetch(final IO source, final QueryContext qc) throws QueryException {
    final HashMap<String, String> options = toOptions(arg(1), qc);
    final DBOptions dbopts = new DBOptions(options, MainOptions.PARSING, info);
    final MainOptions mopts = dbopts.assignTo(new MainOptions());
    try {
      return new DBNode(Parser.singleParser(source, mopts, ""));
    } catch(final IOException ex) {
      throw FETCH_OPEN_X.get(info, ex);
    }
  }
}
