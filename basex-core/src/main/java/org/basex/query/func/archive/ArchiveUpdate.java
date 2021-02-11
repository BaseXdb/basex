package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArchiveUpdate extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    // entries to be updated
    final B64 archive = toB64(exprs[0], qc, false);
    final Map<String, Item[]> map = toMap(1, qc);

    try(ArchiveIn in = ArchiveIn.get(archive.input(info), info)) {
      final String format = in.format();
      if(in instanceof GZIPIn) throw ARCHIVE_MODIFY_X.get(info, format);
      try(ArchiveOut out = ArchiveOut.get(format, info)) {
        // write existing or updated entries
        while(in.more()) {
          final Item[] entry = map.remove(in.entry().getName());
          if(entry != null) {
            add(entry, out, ZipEntry.DEFLATED, "", qc);
          } else {
            out.write(in);
          }
        }
        // add remaining entries
        for(final Item[] entry : map.values()) {
          add(entry, out, ZipEntry.DEFLATED, "", qc);
        }
        return B64.get(out.finish());
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
