package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveDelete extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Bin archive = toArchive(arg(0), qc);
    final Value entries = arg(1).atomValue(qc, info);

    // entries to be deleted
    final TokenSet names = new TokenSet(entries.size());
    for(final Item item : entries) names.add(toString(item, qc));

    try(BufferInput bi = archive.input(info); ArchiveIn in = ArchiveIn.get(bi, info)) {
      final String format = in.format();
      if(in instanceof GZIPIn) throw ARCHIVE_MODIFY_X.get(info, format);

      final ArrayOutput ao = new ArrayOutput();
      try(ArchiveOut out = ArchiveOut.get(format, info, ao)) {
        while(in.more()) {
          if(!names.contains(token(in.entry().getName()))) out.write(in);
        }
      }
      return B64.get(ao.finish());
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
