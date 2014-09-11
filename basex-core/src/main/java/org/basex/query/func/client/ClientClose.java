package org.basex.query.func.client;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ClientClose extends ClientFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    try {
      session(qc, true).close();
      return null;
    } catch(final IOException ex) {
      throw BXCL_COMMAND_X.get(info, ex);
    }
  }
}
