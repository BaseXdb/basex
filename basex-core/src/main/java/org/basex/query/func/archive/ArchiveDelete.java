package org.basex.query.func.archive;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveDelete extends ArchiveFn {
  @Override
  public B64 value(final QueryContext qc) throws QueryException {
    // entries to be deleted
    final HashSet<String> names = toEntries(arg(1), qc);
    return rewrite(arg(0), qc,
      (entry, out) -> names == null || !names.contains(entry.getName()),
      out -> { });
  }
}
