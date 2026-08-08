package org.basex.query.func.ft;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
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
public final class FtTokens extends FtAccessFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final FtFuzzyOptions options = toOptions(arg(2), new FtFuzzyOptions(), qc);

    byte[] token = toZeroToken(arg(1), qc);
    if(token.length != 0) {
      final FTLexer lexer = new FTLexer(new FTOpt().assign(data.meta));
      lexer.init(token);
      token = lexer.nextToken();
    }

    final IndexEntries entries;
    if(token.length != 0 && options.get(FtFuzzyOptions.FUZZY) == Boolean.TRUE) {
      final int errors = options.contains(FtFuzzyOptions.ERRORS) ?
        options.get(FtFuzzyOptions.ERRORS) : errors(qc);
      // negative values are treated like 0: the number of errors is computed dynamically
      entries = new IndexEntries(token, Math.max(0, errors), IndexType.FULLTEXT);
    } else {
      entries = new IndexEntries(token, IndexType.FULLTEXT);
    }
    return IndexFn.entries(data, entries, this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(arg(0), false, false, visitor) && super.accept(visitor);
  }
}
