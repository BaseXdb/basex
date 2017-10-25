package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnPut extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final ANode nd = toNode(exprs[0], qc);
    final byte[] file = toEmptyToken(exprs[1], qc);
    final Item so = exprs.length > 2 ? exprs[2].item(qc, info) : null;
    final SerializerOptions sopts = FuncOptions.serializer(so, info);

    if(nd.type != NodeType.DOC && nd.type != NodeType.ELM) throw UPFOTYPE_X.get(info, exprs[0]);

    final Uri uri = Uri.uri(file);
    if(uri == Uri.EMPTY || !uri.isValid()) throw UPFOURI_X.get(info, file);
    final Updates updates = qc.updates();
    final DBNode target = updates.determineDataRef(nd, qc);

    final String path = new QueryInput(string(uri.string()), sc).io.path();
    // check if all target paths are unique
    if(!updates.putPaths.add(path)) throw UPURIDUP_X.get(info, path);

    updates.add(new Put(target.pre(), target.data(), path, sopts, info), qc);
    return null;
  }
}
