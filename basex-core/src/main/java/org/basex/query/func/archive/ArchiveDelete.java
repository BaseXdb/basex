package org.basex.query.func.archive;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveDelete extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value entries = arg(1).atomValue(qc, info);

    // entries to be deleted
    final TokenSet names = new TokenSet(entries.size());
    for(final Item item : entries) names.add(toString(item, qc));
    return rewrite(arg(0), qc,
      (entry, out) -> !names.contains(token(entry.getName())),
      out -> { });
  }
}
