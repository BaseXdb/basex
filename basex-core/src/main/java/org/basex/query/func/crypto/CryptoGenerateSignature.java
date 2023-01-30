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
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class CryptoGenerateSignature extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int el = exprs.length;
    final ANode node = toNode(exprs[0], qc);
    final byte[] can = toToken(exprs[1], qc);
    final byte[] dig = toToken(exprs[2], qc);
    final byte[] sig = toToken(exprs[3], qc);
    final byte[] ns = toToken(exprs[4], qc);
    final byte[] tp = toToken(exprs[5], qc);
    final Item arg6 = el > 6 ? toNodeOrAtomItem(6, qc) : Empty.VALUE;
    final ANode arg7 = el > 7 ? toNode(exprs[7], qc) : null;

    final byte[] path = arg6 == Empty.VALUE || arg6 instanceof ANode ? Token.EMPTY : toToken(arg6);
    final ANode cert = arg7 != null ? arg7 : arg6 instanceof ANode ? toNode(arg6) : null;
    return new DigitalSignature(info).generate(node, can, dig, sig, ns, tp, path, cert, qc);
  }
}
