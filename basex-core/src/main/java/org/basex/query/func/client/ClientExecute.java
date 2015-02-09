package org.basex.query.func.client;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ClientExecute extends ClientFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final ClientSession cs = session(qc, false);
    final String cmd = Token.string(toToken(exprs[1], qc));

    try {
      final ArrayOutput ao = new ArrayOutput();
      cs.setOutputStream(ao);
      cs.execute(cmd);
      cs.setOutputStream(null);
      return Str.get(ao.finish());
    } catch(final BaseXException ex) {
      throw BXCL_COMMAND_X.get(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM_X.get(info, ex);
    }
  }
}
