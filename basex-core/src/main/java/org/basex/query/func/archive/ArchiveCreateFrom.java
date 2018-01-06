package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreateFrom extends ArchiveCreate {
  @Override
  public B64 item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final IOFile root = new IOFile(toPath(0, qc).toString());
    final ArchOptions opts = toOptions(1, new ArchOptions(), qc);
    final Iter entries;
    if(exprs.length > 2) {
      entries = exprs[2].iter(qc);
    } else {
      final TokenList tl = new TokenList();
      for(final String file : root.descendants()) tl.add(file);
      entries = StrSeq.get(tl).iter();
    }

    final String format = opts.get(ArchOptions.FORMAT);
    final int level = level(opts);
    if(!root.isDir()) throw FILE_NO_DIR_X.get(info, root);

    try(ArchiveOut out = ArchiveOut.get(format.toLowerCase(Locale.ENGLISH), info)) {
      out.level(level);
      try {
        while(true) {
          Item en = qc.next(entries);
          if(en == null) break;
          en = checkElemToken(en);
          final IOFile file = new IOFile(root, string(en.string(info)));
          if(!file.exists()) throw FILE_NOT_FOUND_X.get(info, file);
          if(file.isDir()) throw FILE_IS_DIR_X.get(info, file);
          add(en, B64.get(file.read()), out, level, qc);
        }
      } catch(final IOException ex) {
        throw ARCHIVE_ERROR_X.get(info, ex);
      }
      return B64.get(out.finish());
    }
  }
}
