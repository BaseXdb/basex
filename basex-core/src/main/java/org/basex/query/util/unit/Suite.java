package org.basex.query.util.unit;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.unit.Constants.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQuery Unit tests: Testing multiple modules.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Suite {
  /** Query context. */
  private final QueryContext ctx;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param ii input info
   * @param qc query context
   */
  public Suite(final QueryContext qc, final InputInfo ii) {
    info = ii;
    ctx = qc;
  }

  /**
   * Tests all specified libraries.
   * @param libs locations of library modules
   * @return resulting value
   * @throws QueryException query exception
   */
  public Item test(final ArrayList<IO> libs) throws QueryException {
    final FElem suites = new FElem(TESTSUITES);
    for(final IO io : libs) {
      try {
        final QueryContext qc = new QueryContext(ctx);
        try {
          final StaticScope mod = qc.parse(string(io.read()), io.path(), null);
          qc.compile();
          suites.add(new Unit(qc, info).test(mod.sc));
        } finally {
          qc.close();
        }
      } catch(final IOException ex) {
        throw IOERR.get(info, ex);
      }
    }
    return suites;
  }
}
