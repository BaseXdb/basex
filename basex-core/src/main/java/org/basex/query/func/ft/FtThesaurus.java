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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FtThesaurus extends FtAccess {
  /** Most recently used thesaurus. */
  private Thesaurus thesaurus;
  /** Most recently supplied root node. */
  private ANode node;

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode root = toNode(exprs[0], qc);
    final byte[] term = toToken(exprs[1], qc);
    final FtThesaurusOptions opts = toOptions(2, new FtThesaurusOptions(), qc);

    if(root != node) {
      thesaurus = new Thesaurus(root);
      node = root;
    }
    final byte[] rel = Token.token(opts.get(FtThesaurusOptions.RELATIONSHIP));
    final long levels = opts.get(FtThesaurusOptions.LEVELS);

    return StrSeq.get(new ThesAccessor(thesaurus, rel, levels, info).find(term));
  }
}
