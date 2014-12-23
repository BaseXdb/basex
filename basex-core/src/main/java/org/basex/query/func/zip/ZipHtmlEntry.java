package org.basex.query.func.zip;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ZipHtmlEntry extends ZipXmlEntry {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return xmlEntry(qc, true);
  }
}
