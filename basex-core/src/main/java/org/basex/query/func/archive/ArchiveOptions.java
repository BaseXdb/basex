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
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveOptions extends ArchiveFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    String format = null;
    int level = -1;
    try {
      final Object archive = toInput(arg(0), qc);
      if(archive instanceof final Bin bin) {
        try(BufferInput bi = bin.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
          format = in.format();
          level = in.method();
          while(level == -1 && in.more()) {
            final ZipEntry ze = in.entry();
            if(!ze.isDirectory()) level = ze.getMethod();
          }
        }
      } else {
        format = ZIP;
        try(ZipFile zip = (ZipFile) archive) {
          for(final ZipEntry ze : entries(zip, null)) {
            if(ze.isDirectory()) continue;
            level = ze.getMethod();
            break;
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    return XQMap.get(Records.ARCHIVE_OPTIONS.get(), format != null ? Str.get(format) : Empty.VALUE,
        level >= 0 ? Str.get(algorithm(level)) : Empty.VALUE);
  }
}
