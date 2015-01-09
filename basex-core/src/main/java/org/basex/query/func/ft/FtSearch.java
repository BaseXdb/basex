package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FtSearch extends FtAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final Value terms = qc.value(exprs[1]);
    final FtOptions opts = toOptions(2, Q_OPTIONS, new FtOptions(), qc);

    final IndexContext ic = new IndexContext(data, false);
    if(!data.meta.ftxtindex) throw BXDB_INDEX_X.get(info, data.meta.name,
        IndexType.FULLTEXT.toString().toLowerCase(Locale.ENGLISH));

    final FTOpt opt = new FTOpt().copy(data.meta);
    final FTMode mode = opts.get(FtIndexOptions.MODE);
    opt.set(FZ, opts.get(FtIndexOptions.FUZZY));
    opt.set(WC, opts.get(FtIndexOptions.WILDCARDS));
    if(opt.is(FZ) && opt.is(WC)) throw BXFT_MATCH.get(info, this);

    final FTOpt tmp = qc.ftOpt();
    qc.ftOpt(opt);
    final FTExpr fte = new FTWords(info, data, terms, mode).compile(qc, null);
    qc.ftOpt(tmp);
    return new FTIndexAccess(info, options(fte, opts), ic).iter(qc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
  }
}
