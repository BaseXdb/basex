package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtThesaurus extends StandardFunc {
  /** Most recently used thesaurus accessor (can be {@code null}). */
  private volatile ThesAccessor recent;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XNode node = toNode(arg(0), qc);
    final byte[] term = toToken(arg(1), qc);
    final FtThesaurusOptions options = options(2, FtThesaurusOptions::new, qc);
    final byte[] relation = Token.token(options.get(FtThesaurusOptions.RELATIONSHIP));
    final int[] levels = options.get(FtThesaurusOptions.LEVELS);
    final int ll = levels.length;
    boolean range = ll != 0;
    for(int l = 1; range && l < ll; l++) range = levels[l] == levels[0] + l;
    if(!range) throw INVALIDOPTION_X.get(info, "Option 'levels': range of integers expected.");
    final long min = levels[0], max = levels[ll - 1];

    ThesAccessor accessor = recent;
    if(accessor == null || !accessor.matches(node, relation, min, max)) {
      accessor = new ThesAccessor(node, relation, min, max, info);
      recent = accessor;
    }
    return StrSeq.get(accessor.find(term, new FTOpt().assign(qc.ftOpt()), qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(2, FtThesaurusOptions::new, cc);
    return this;
  }
}
