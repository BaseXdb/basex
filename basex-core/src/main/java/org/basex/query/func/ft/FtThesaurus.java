package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FtThesaurus extends FtAccess {
  /** Most recently used thesaurus. */
  private Thesaurus thesaurus;
  /** Most recently supplied root node. */
  private ANode nd;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode node = toNode(exprs[0], qc);
    final byte[] term = toToken(exprs[1], qc);
    final FtThesaurusOptions options = toOptions(2, new FtThesaurusOptions(), true, qc);

    if(node != nd) {
      thesaurus = new Thesaurus(node);
      nd = node;
    }
    final byte[] relation = Token.token(options.get(FtThesaurusOptions.RELATIONSHIP));
    final long levels = options.get(FtThesaurusOptions.LEVELS);

    return StrSeq.get(new ThesAccessor(thesaurus, relation, levels, info).find(term));
  }
}
