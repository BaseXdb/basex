package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class CryptoGenerateSignature extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XNode node = toNode(arg(0), qc);
    final byte[] can = toToken(arg(1), qc);
    final byte[] dig = toToken(arg(2), qc);
    final byte[] sig = toToken(arg(3), qc);
    final byte[] ns = toToken(arg(4), qc);
    final byte[] tp = toToken(arg(5), qc);
    final Item ext1 = toNodeOrAtomItem(arg(6), true, qc);
    final XNode ext2 = toNodeOrNull(arg(7), qc);

    final byte[] path = ext1 == null || ext1 instanceof XNode ? Token.EMPTY : toToken(ext1);
    final XNode cert = ext2 != null ? ext2 : ext1 instanceof final XNode xnode ? xnode : null;
    return new DigitalSignature(info).generate(node, can, dig, sig, ns, tp, path, cert, qc);
  }
}
