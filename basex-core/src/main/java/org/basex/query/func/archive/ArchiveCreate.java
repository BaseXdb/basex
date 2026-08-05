package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreate extends ArchiveFn {
  @Override
  protected B64 item(final QueryContext qc) throws QueryException {
    try(SpillOutput so = new SpillOutput(qc)) {
      create(so, null, qc);
      return so.finish(ARCHIVE_ERROR_X);
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }

  /**
   * Creates the archive.
   * @param os output stream
   * @param target target file to be excluded from the archive contents (can be {@code null})
   * @param qc query context
   * @throws QueryException query exception
   */
  public void create(final OutputStream os, @SuppressWarnings("unused") final IOFile target,
      final QueryContext qc) throws QueryException {
    final Map<String, Entry<Item, Item>> files = toFiles(arg(0), arg(1), qc);
    final CreateOptions options = toOptions(arg(2), new CreateOptions(), qc);
    create(files, options, os, qc);
  }
}
