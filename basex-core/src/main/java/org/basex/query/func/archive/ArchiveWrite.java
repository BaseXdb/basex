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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArchiveWrite extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Path path = toPath(arg(0), qc);
    final Map<String, Item[]> map = toMap(arg(1), arg(2), qc);
    final CreateOptions options = toOptions(arg(3), new CreateOptions(), true, qc);

    try {
      try(BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile()))) {
        create(map, options, out, qc);
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
