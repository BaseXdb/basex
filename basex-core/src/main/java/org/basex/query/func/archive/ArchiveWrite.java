package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArchiveWrite extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Path path = toPath(0, qc);
    final Map<String, Item[]> map = toMap(1, qc);
    final CreateOptions opts = toOptions(3, new CreateOptions(), qc);

    try {
      try(BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile()))) {
        create(map, opts, out, qc);
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
