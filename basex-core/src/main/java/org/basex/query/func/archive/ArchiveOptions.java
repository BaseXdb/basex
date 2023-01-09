package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArchiveOptions extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 archive = toB64(exprs[0], qc, false);
    final String format;
    int level = -1;

    try(BufferInput bi = archive.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
      format = in.format();
      while(in.more()) {
        final ZipEntry ze = in.entry();
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

    return mb.map();
  }
}
