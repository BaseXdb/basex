package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnPut extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final byte[] file = toEmptyToken(exprs[1], qc);
    final ANode nd = toNode(exprs[0], qc);
    if(nd.type != NodeType.DOC && nd.type != NodeType.ELM) throw UPFOTYPE_X.get(info, exprs[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.isValid()) throw UPFOURI_X.get(info, file);
    final Updates updates = qc.resources.updates();
    final DBNode target = updates.determineDataRef(nd, qc);

    final String uri = IO.get(string(u.string())).path();
    // check if all target paths are unique
    if(!updates.putPaths.add(uri)) throw UPURIDUP_X.get(info, uri);

    updates.add(new Put(target.pre, target.data, uri, info), qc);
    return null;
  }
}
