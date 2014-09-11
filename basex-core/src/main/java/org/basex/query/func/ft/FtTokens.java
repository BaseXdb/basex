package org.basex.query.func.ft;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FtTokens extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    byte[] entry = exprs.length < 2 ? Token.EMPTY : toToken(exprs[1], qc);
    if(entry.length != 0) {
      final FTLexer ftl = new FTLexer(new FTOpt().copy(data.meta));
      ftl.init(entry);
      entry = ftl.nextToken();
    }
    return IndexFn.entries(data, new IndexEntries(entry, IndexType.FULLTEXT), this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 0) && super.accept(visitor);
  }
}
