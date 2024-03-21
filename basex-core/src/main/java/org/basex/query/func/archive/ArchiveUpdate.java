package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArchiveUpdate extends ArchiveCreate {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // entries to be updated
    final Bin archive = toArchive(arg(0), qc);
    final Map<String, Entry<Item, Item>> files = toFiles(arg(1), arg(2), qc);

    try(BufferInput bi = archive.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
      final String format = in.format();
      if(in instanceof GZIPIn) throw ARCHIVE_MODIFY_X.get(info, format);

      final ArrayOutput ao = new ArrayOutput();
      try(ArchiveOut out = ArchiveOut.get(format, info, ao)) {
        // write existing or updated entries
        while(in.more()) {
          final Entry<Item, Item> file = files.remove(in.entry().getName());
          if(file != null) {
            add(file, out, ZipEntry.DEFLATED, "", qc);
          } else {
            out.write(in);
          }
        }
        // add remaining entries
        for(final Entry<Item, Item> entry : files.values()) {
          add(entry, out, ZipEntry.DEFLATED, "", qc);
        }
      }
      return B64.get(ao.finish());
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
