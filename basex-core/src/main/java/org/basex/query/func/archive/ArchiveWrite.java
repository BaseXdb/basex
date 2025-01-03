package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveWrite extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Path path = toPath(arg(0), qc);
    final Map<String, Entry<Item, Item>> files = toFiles(arg(1), arg(2), qc);
    final CreateOptions options = toOptions(arg(3), new CreateOptions(), qc);

    try {
      try(BufferOutput out = new BufferOutput(new IOFile(path))) {
        create(files, options, out, qc);
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
