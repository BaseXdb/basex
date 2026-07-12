package org.basex.query.func.ft;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtTokens extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final FtTokensOptions options = toOptions(arg(2), new FtTokensOptions(), qc);

    byte[] token = toZeroToken(arg(1), qc);
    if(token.length != 0) {
      final FTLexer lexer = new FTLexer(new FTOpt().assign(data.meta));
      lexer.init(token);
      token = lexer.nextToken();
    }

    final IndexEntries entries;
    if(token.length != 0 && options.get(FtTokensOptions.FUZZY)) {
      final int errors = options.contains(FtTokensOptions.ERRORS) ?
        options.get(FtTokensOptions.ERRORS) : qc.context.options.get(MainOptions.LSERROR);
      // negative values are treated like 0: the number of errors is computed dynamically
      entries = new IndexEntries(token, Math.max(0, errors), IndexType.FULLTEXT);
    } else {
      entries = new IndexEntries(token, IndexType.FULLTEXT);
    }
    return IndexFn.entries(data, entries, this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), false, visitor) && super.accept(visitor);
  }
}
