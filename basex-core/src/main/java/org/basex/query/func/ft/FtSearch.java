package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;
import static org.basex.util.ft.FTFlag.*;

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
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FtSearch extends FtAccess {
  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final Value terms = exprs[1].value(qc);
    final FtIndexOptions opts = toOptions(2, new FtIndexOptions(), qc);

    final IndexDb db = new IndexStaticDb(data, info);
    final FTOpt opt = new FTOpt().assign(data.meta);
    final FTMode mode = opts.get(FtIndexOptions.MODE);
    opt.set(FZ, opts.get(FtIndexOptions.FUZZY));
    opt.set(WC, opts.get(FtIndexOptions.WILDCARDS));
    if(opt.is(FZ) && opt.is(WC)) throw FT_OPTIONS.get(info, this);

    final FTWords ftw = new FTWords(info, db, terms, mode).ftOpt(opt).optimize(qc);
    return new FTIndexAccess(info, options(ftw, opts), db).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    exprs[1] = exprs[1].simplifyFor(Simplify.STRING, cc);
    return compileData(cc);
  }
}
