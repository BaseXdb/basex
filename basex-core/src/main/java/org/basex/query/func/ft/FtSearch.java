package org.basex.query.func.ft;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FtSearch extends FtAccess {
  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final Value query = exprs[1].value(qc);
    final FtIndexOptions options = toOptions(2, new FtIndexOptions(), true, qc);

    final IndexDb db = new IndexStaticDb(data, info);
    final FTMode mode = options.get(FtIndexOptions.MODE);
    final FTOpt opt = ftOpt(options, qc).assign(data.meta);

    final FTWords ftw = new FTWords(info, db, query, mode).ftOpt(opt).optimize(qc);
    return new FTIndexAccess(info, ftExpr(ftw, options), db).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, false, 0) && super.accept(visitor);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return compileData(cc);
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    exprs[1] = exprs[1].simplifyFor(Simplify.STRING, cc);
    super.simplifyArgs(cc);
  }
}
