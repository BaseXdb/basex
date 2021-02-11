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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreateFrom extends ArchiveCreate {
  @Override
  public B64 item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final IOFile root = new IOFile(toPath(0, qc).toString());
    final CreateFromOptions opts = toOptions(1, new CreateFromOptions(), qc);
    final boolean recursive = opts.get(CreateFromOptions.RECURSIVE);
    final boolean rootDir = opts.get(CreateFromOptions.ROOT_DIR);
    if(!root.isDir()) throw FILE_NO_DIR_X.get(info, root);

    final Iter entries;
    if(exprs.length > 2) {
      entries = exprs[2].iter(qc);
    } else {
      final TokenList tl = new TokenList();
      if(recursive) {
        for(final String file : root.descendants()) tl.add(file);
      } else {
        for(final IOFile file : root.children()) {
          if(!file.isDir()) tl.add(file.name());
        }
      }
      entries = StrSeq.get(tl).iter();
    }

    final int level = level(opts);
    final String format = opts.get(CreateOptions.FORMAT);
    final String dir = rootDir && root.parent() != null ? root.name() + '/' : "";

    try(ArchiveOut out = ArchiveOut.get(format.toLowerCase(Locale.ENGLISH), info)) {
      out.level(level);
      try {
        while(true) {
          final Item en = qc.next(entries);
          if(en == null) break;
          final IOFile file = new IOFile(root, string(checkElemToken(en).string(info)));
          if(!file.exists()) throw FILE_NOT_FOUND_X.get(info, file);
          if(file.isDir()) throw FILE_IS_DIR_X.get(info, file);
          add(new Item[] { en, B64.get(file.read()) }, out, level, dir, qc);
        }
      } catch(final IOException ex) {
        throw ARCHIVE_ERROR_X.get(info, ex);
      }
      return B64.get(out.finish());
    }
  }
}
