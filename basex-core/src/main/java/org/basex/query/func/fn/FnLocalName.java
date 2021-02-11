package org.basex.query.func.fn;

import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnLocalName extends FnName {
  @Override
  byte[] name(final ANode node) {
    return Token.local(node.name());
  }
}
