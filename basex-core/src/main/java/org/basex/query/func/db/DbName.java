package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbName extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Str.get(toDBNode(toNode(exprs[0], qc)).data().meta.name);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Data data = exprs[0].data();
    return data != null ? Str.get(data.meta.name) : this;
  }
}
