package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class JobsInvoke extends JobsEval {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final IO io = checkPath(0, qc);
    try {
      return eval(qc, string(io.read()), io.url());
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
