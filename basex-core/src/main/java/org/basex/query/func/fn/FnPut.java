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
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnPut extends StandardFunc {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    final byte[] href = toZeroToken(arg(1), qc);
    final Item options = arg(2).item(qc, info);

    if(!node.type.oneOf(NodeType.DOCUMENT_NODE, NodeType.ELEMENT))
      throw UPFOTYPE_X.get(info, arg(0));

    final Uri uri = Uri.get(href);
    if(uri == Uri.EMPTY || !uri.isValid()) throw UPFOURI_X.get(info, href);
    final Updates updates = qc.updates();
    final DBNode target = updates.determineDataRef(node, qc);

    final String path = info.sc().resolve(string(uri.string())).path();
    // check if all target paths are unique
    if(!updates.putPaths.add(path)) throw UPURIDUP_X.get(info, path);

    final SerializerOptions sopts = FuncOptions.serializer(options, info);
    updates.add(new Put(target.pre(), target.data(), path, sopts, info), qc);
    return Empty.VALUE;
  }
}
