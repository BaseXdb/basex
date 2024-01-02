package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Lukas Kircher
 */
public final class CryptoGenerateSignature extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    final byte[] can = toToken(arg(1), qc);
    final byte[] dig = toToken(arg(2), qc);
    final byte[] sig = toToken(arg(3), qc);
    final byte[] ns = toToken(arg(4), qc);
    final byte[] tp = toToken(arg(5), qc);
    final Item arg6 = defined(6) ? toNodeOrAtomItem(arg(6), qc) : Empty.VALUE;
    final ANode arg7 = toNodeOrNull(arg(7), qc);

    final byte[] path = arg6.isEmpty() || arg6 instanceof ANode ? Token.EMPTY : toToken(arg6);
    final ANode cert = arg7 != null ? arg7 : arg6 instanceof ANode ? toNode(arg6) : null;
    return new DigitalSignature(info).generate(node, can, dig, sig, ns, tp, path, cert, qc);
  }
}
