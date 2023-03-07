package org.basex.query.func.ft;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.index.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FtTokens extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    byte[] prefix = defined(1) ? toToken(exprs[1], qc) : Token.EMPTY;
    if(prefix.length != 0) {
      final FTLexer lexer = new FTLexer(new FTOpt().assign(data.meta));
      lexer.init(prefix);
      prefix = lexer.nextToken();
    }
    return IndexFn.entries(data, new IndexEntries(prefix, IndexType.FULLTEXT), this);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, false, 0) && super.accept(visitor);
  }
}
