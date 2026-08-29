package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class CryptoValidateSignature extends StandardFunc {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    final XNode node = toNode(arg(0), qc);
    return new DigitalSignature(info).validateSignature(node);
  }
}
