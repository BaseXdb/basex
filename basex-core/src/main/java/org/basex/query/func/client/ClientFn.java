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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class ClientFn extends StandardFunc {
  /**
   * Returns the sessions handler.
   * @param qc query context
   * @return connection handler
   */
  static ClientSessions sessions(final QueryContext qc) {
    ClientSessions res = qc.resources.get(ClientSessions.class);
    if(res == null) {
      res = new ClientSessions();
      qc.resources.add(res);
    }
    return res;
  }

  /**
   * Returns a connection and removes it from list with opened connections if
   * requested.
   * @param qc query context
   * @param del flag indicating if connection has to be removed
   * @return connection
   * @throws QueryException query exception
   */
  final ClientSession session(final QueryContext qc, final boolean del) throws QueryException {
    final Uri id = (Uri) checkAtomic(exprs[0], qc, AtomType.URI);
    final ClientSession cs = sessions(qc).get(id);
    if(cs == null) throw BXCL_NOTAVL_X.get(info, id);
    if(del) sessions(qc).remove(id);
    return cs;
  }
}
