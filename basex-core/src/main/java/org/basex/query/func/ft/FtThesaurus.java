package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FtThesaurus extends FtAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode node = toNode(exprs[0], qc);
    final byte[] term = toToken(exprs[1], qc);
    final FtThesaurusOptions opts = toOptions(2, new FtThesaurusOptions(), qc);

    final byte[] rs = Token.token(opts.get(FtThesaurusOptions.RELATIONSHIP));
    final long levels = opts.get(FtThesaurusOptions.LEVELS);

    final TokenList list = new TokenList();
    new Thesaurus(node, rs, 1, levels, info).find(list, term, qc.context);
    return StrSeq.get(list);
  }
}
