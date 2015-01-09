package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FtTokenize extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FTOpt opt = new FTOpt().copy(qc.ftOpt());
    final FTLexer ftl = new FTLexer(opt).init(toToken(exprs[0], qc));
    return new Iter() {
      @Override
      public Str next() {
        return ftl.hasNext() ? Str.get(ftl.nextToken()) : null;
      }
    };
  }
}
