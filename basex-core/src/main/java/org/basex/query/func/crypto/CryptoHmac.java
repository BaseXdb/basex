package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public final class CryptoHmac extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new Encryption(ii).hmac(toToken(exprs[0], qc), toToken(exprs[1], qc),
        toToken(exprs[2], qc), exprs.length == 4 ? toToken(exprs[3], qc) : Token.EMPTY);
  }
}
