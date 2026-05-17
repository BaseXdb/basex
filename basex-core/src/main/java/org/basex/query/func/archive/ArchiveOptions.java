package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveOptions extends ArchiveFn {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    String format = null;
    int level = -1;
    try {
      final Object archive = toInput(arg(0), qc);
      if(archive instanceof final Bin bin) {
        try(BufferInput bi = bin.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
          format = in.format();
          while(in.more()) {
            final ZipEntry ze = in.entry();
            if(ze.isDirectory()) continue;
            level = ze.getMethod();
            break;
          }
        }
      } else {
        format = ZIP;
        try(ZipFile zip = new ZipFile(new File(archive.toString()), Strings.CP437)) {
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
    final MapBuilder mb = new MapBuilder();
    if(format != null) mb.put(CreateOptions.FORMAT.name(), format);
    if(level >= 0) mb.put(CreateOptions.ALGORITHM.name(),
        level == 8 ? DEFLATE : level == 0 ? STORED : UNKNOWN);
    return mb.map();
  }
}
