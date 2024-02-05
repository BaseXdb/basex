package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FileListRoots extends FileFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    for(final Path path : FileSystems.getDefault().getRootDirectories()) {
      tl.add(get(path, true).string());
    }
    return StrSeq.get(tl);
  }
}
