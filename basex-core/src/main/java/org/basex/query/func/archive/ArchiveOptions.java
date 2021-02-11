package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArchiveOptions extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    final String format;
    int level = -1;

    try(ArchiveIn arch = ArchiveIn.get(archive.input(info), info)) {
      format = arch.format();
      while(arch.more()) {
        final ZipEntry ze = arch.entry();
        if(ze.isDirectory()) continue;
        level = ze.getMethod();
        break;
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }

    // create result element
    final MapBuilder mb = new MapBuilder();
    if(format != null) mb.put(CreateOptions.FORMAT.name(), format);
    if(level >= 0) mb.put(CreateOptions.ALGORITHM.name(),
        level == 8 ? DEFLATE : level == 0 ? STORED : UNKNOWN);

    return mb.finish();
  }
}
