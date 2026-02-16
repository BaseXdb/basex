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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnPut extends StandardFunc {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XNode node = toNode(arg(0), qc);
    final byte[] source = toZeroToken(arg(1), qc);
    final SerializerOptions options = toSerializerOptions(arg(2), qc);

    if(!node.kind().oneOf(Kind.DOCUMENT, Kind.ELEMENT)) throw UPFOTYPE_X.get(info, arg(0));

    final Uri uri = Uri.get(source);
    if(uri == Uri.EMPTY || !uri.isValid()) throw UPFOURI_X.get(info, source);
    final Updates updates = qc.updates();
    final DBNode target = updates.determineDataRef(node, qc);

    final String path = sc().resolve(string(uri.string())).path();
    // check if all target paths are unique
    if(!updates.putPaths.add(path)) throw UPURIDUP_X.get(info, path);

    updates.add(new Put(target.pre(), target.data(), path, options, info), qc);
    return Empty.VALUE;
  }
}
