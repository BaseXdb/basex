package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreate extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try(SpillOutput so = new SpillOutput(qc)) {
      create(so, qc);
      return so.finish(ARCHIVE_ERROR_X);
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }

  /**
   * Creates the archive.
   * @param os output stream
   * @param qc query context
   * @throws QueryException query exception
   */
  public void create(final OutputStream os, final QueryContext qc) throws QueryException {
    final Map<String, Entry<Item, Item>> files = toFiles(arg(0), arg(1), qc);
    final CreateOptions options = toOptions(arg(2), new CreateOptions(), qc);
    create(files, options, os, qc);
  }
}
