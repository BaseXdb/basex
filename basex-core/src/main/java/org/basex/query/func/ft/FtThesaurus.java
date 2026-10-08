package org.basex.query.func.ft;

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
    final long levels = options.get(FtThesaurusOptions.LEVELS);

    ThesAccessor accessor = recent;
    if(accessor == null || !accessor.matches(node, relation, levels)) {
      accessor = new ThesAccessor(node, relation, levels, info);
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
