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
  private ThesAccessor accessor;
  /** Most recently supplied root node (can be {@code null}). */
  private XNode nd;
  /** Most recently requested relation (can be {@code null}). */
  private byte[] rel;
  /** Most recently requested maximum level. */
  private long lvl;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XNode node = toNode(arg(0), qc);
    final byte[] term = toToken(arg(1), qc);
    final FtThesaurusOptions options = options(2, FtThesaurusOptions::new, qc);
    final byte[] relation = Token.token(options.get(FtThesaurusOptions.RELATIONSHIP));
    final long levels = options.get(FtThesaurusOptions.LEVELS);

    if(nd == null || !nd.is(node) || !Token.eq(rel, relation) || lvl != levels) {
      accessor = new ThesAccessor(node, relation, levels, info);
      nd = node;
      rel = relation;
      lvl = levels;
    }
    return StrSeq.get(accessor.find(term, new FTOpt().assign(qc.ftOpt()), qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(2, FtThesaurusOptions::new, cc);
    return this;
  }
}
