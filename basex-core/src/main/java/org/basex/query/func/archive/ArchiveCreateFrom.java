package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.AbstractMap.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ArchiveCreateFrom extends ArchiveCreate {
  @Override
  public B64 item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IOFile root = new IOFile(toPath(arg(0), qc));
    if(!root.isDir()) throw FILE_NO_DIR_X.get(info, root);

    final CreateFromOptions options = toOptions(arg(1), new CreateFromOptions(), qc);
    final boolean recursive = options.get(CreateFromOptions.RECURSIVE);
    final boolean rootDir = options.get(CreateFromOptions.ROOT_DIR);

    final Iter entries;
    if(defined(2)) {
      entries = arg(2).iter(qc);
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

    final int level = level(options);
    final String format = options.get(CreateOptions.FORMAT).toLowerCase(Locale.ENGLISH);
    final String dir = rootDir && root.parent() != null ? root.name() + '/' : "";
    final ArrayOutput ao = new ArrayOutput();
    try(ArchiveOut out = ArchiveOut.get(format, info, ao)) {
      out.level(level);
      try {
        while(true) {
          final Item item = qc.next(entries);
          if(item == null) break;
          final IOFile file = new IOFile(root, toString(item, qc));
          if(!file.exists()) throw FILE_NOT_FOUND_X.get(info, file);
          if(file.isDir()) throw FILE_IS_DIR_X.get(info, file);
          add(new SimpleEntry<>(item, B64.get(file.read())), out, level, dir, qc);
        }
      } catch(final IOException ex) {
        throw ARCHIVE_ERROR_X.get(info, ex);
      }
    }
    return B64.get(ao.finish());
  }
}
