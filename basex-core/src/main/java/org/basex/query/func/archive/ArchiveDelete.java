package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArchiveDelete extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final B64 archive = toB64(exprs[0], qc, false);
    // entries to be deleted
    final TokenSet names = new TokenSet();
    final Iter iter = exprs[1].iter(qc);
    for(Item en; (en = qc.next(iter)) != null;) {
      names.add(checkElemToken(en).string(info));
    }

    try(ArchiveIn in = ArchiveIn.get(archive.input(info), info)) {
      final String format = in.format();
      if(in instanceof GZIPIn) throw ARCHIVE_MODIFY_X.get(info, format);
      try(ArchiveOut out = ArchiveOut.get(format, info)) {
        while(in.more()) {
          if(!names.contains(token(in.entry().getName()))) out.write(in);
        }
        return B64.get(out.finish());
      }
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
