package org.basex.query.func.client;

import static org.basex.query.QueryError.*;

import org.basex.api.client.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Functions to connect remote database instances.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class ClientFn extends StandardFunc {
  /**
   * Returns a connection and removes it from list with opened connections if requested.
   * @param qc query context
   * @param del flag indicating if connection has to be removed
   * @return connection
   * @throws QueryException query exception
   */
  final ClientSession session(final QueryContext qc, final boolean del) throws QueryException {
    final ClientSessions sessions = sessions(qc);
    final Uri id = (Uri) checkType(exprs[0], qc, AtomType.ANY_URI);
    final ClientSession cs = sessions.get(id);
    if(cs == null) throw CLIENT_ID_X.get(info, id);
    if(del) sessions.remove(id);
    return cs;
  }

  /**
   * Returns the sessions handler.
   * @param qc query context
   * @return connection handler
   */
  static ClientSessions sessions(final QueryContext qc) {
    return qc.resources.index(ClientSessions.class);
  }
}
