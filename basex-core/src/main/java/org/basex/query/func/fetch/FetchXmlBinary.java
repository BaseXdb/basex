package org.basex.query.func.fetch;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FetchXmlBinary extends FetchXml {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return fetch(new IOContent(toBin(exprs[0], qc).binary(info)), qc);
  }
}
