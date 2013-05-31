package org.basex.query.util.inspect;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class contains functions for inspecting XQuery modules.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Inspect {
  /** Query context. */
  protected final QueryContext ctx;
  /** Input info. */
  protected final InputInfo info;

  /** Parsed main module. */
  protected StaticScope module;

  /**
   * Constructor.
   * @param qc query context
   * @param ii input info
   */
  protected Inspect(final QueryContext qc, final InputInfo ii) {
    ctx = qc;
    info = ii;
  }

  /**
   * Parses a module.
   * @param io input reference
   * @return query parser
   * @throws QueryException query exception
   */
  protected QueryParser parseQuery(final IO io) throws QueryException {
    if(!io.exists()) WHICHRES.thrw(info, io);

    final QueryContext qc = new QueryContext(ctx.context);
    try {
      final String input = string(io.read());
      // parse query
      final QueryParser qp = new QueryParser(input, io.path(), qc);
      module = QueryProcessor.isLibrary(input) ? qp.parseLibrary(true) : qp.parseMain();
      return qp;
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    } catch(final QueryException ex) {
      throw IOERR.thrw(info, ex);
    } finally {
      qc.close();
    }
  }

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent node
   * @return element node
   */
  protected abstract FElem elem(final String name, final FElem parent);
}
