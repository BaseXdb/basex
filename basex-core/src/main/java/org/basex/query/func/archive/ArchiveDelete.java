package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ArchiveDelete extends ArchiveFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final B64 archive = toB64(exprs[0], qc, false);
    // entries to be deleted
    final TokenObjMap<Item[]> hm = new TokenObjMap<>();
    final Iter names = exprs[1].iter(qc);
    for(Item en; (en = qc.next(names)) != null;) {
      hm.put(checkElemToken(en).string(info), null);
    }

    try(ArchiveIn in = ArchiveIn.get(archive.input(info), info);
        ArchiveOut out = ArchiveOut.get(in.format(), info)) {
      if(in instanceof GZIPIn)
        throw ARCHIVE_MODIFY_X.get(info, in.format().toUpperCase(Locale.ENGLISH));
      while(in.more()) {
        if(!hm.contains(token(in.entry().getName()))) out.write(in);
      }
      return B64.get(out.finish());
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
  }
}
