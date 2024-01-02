package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DbGetBinary extends DbGetValue {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(ResourceType.BINARY, qc);
  }

  @Override
  Value resource(final IOFile path, final QueryContext qc) {
    return new B64Lazy(path, IOERR_X);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    if(defined(1)) {
      exprType.assign(SeqType.BASE64_BINARY_O);
    } else {
      super.opt(cc);
    }
    return this;
  }
}
