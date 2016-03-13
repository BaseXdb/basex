package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FetchXml extends StandardFunc {
  /** Element: options. */
  private static final QNm Q_OPTIONS = QNm.get("options");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] in = toToken(exprs[0], qc);
    final Options opts = toOptions(1, Q_OPTIONS, new Options(), qc);
    if(!Uri.uri(in).isValid()) throw INVDOC_X.get(info, in);

    final IO input = IO.get(string(in));
    final MainOptions mo = MainOptions.get();
    new DBOptions(opts, DBOptions.PARSING, info).assignTo(mo);

    try {
      return new DBNode(Parser.singleParser(input, mo, ""));
    } catch(final IOException ex) {
      throw BXFE_IO_X.get(info, ex);
    }
  }
}
