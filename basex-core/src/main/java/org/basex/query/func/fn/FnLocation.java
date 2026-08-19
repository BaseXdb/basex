package org.basex.query.func.fn;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnLocation extends ContextFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final XNode node = toNodeOrNull(context(qc), qc);
    if(!(node instanceof final DBNode dbnode)) return Empty.VALUE;
    final Locations locations = dbnode.data().locations;
    if(locations == null) return Empty.VALUE;

    final long location = locations.location(dbnode.pre());
    final int line = (int) (location >>> 32), column = (int) location;
    if(line <= 0) return Empty.VALUE;

    final byte[] uri = node.root().baseURI();
    return new XQShapeMap(Records.LOCATION.get(),
      uri.length == 0 ? Empty.VALUE : Uri.get(uri, false), Empty.VALUE,
      Itr.get(line, BasicType.POSITIVE_INTEGER),
      column <= 0 ? Empty.VALUE : Itr.get(column, BasicType.POSITIVE_INTEGER));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
