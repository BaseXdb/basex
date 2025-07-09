package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileListRoots extends FileFn {
  @Override
  public Value eval(final QueryContext qc) {
    final TokenList tl = new TokenList();
    for(final Path path : FileSystems.getDefault().getRootDirectories()) {
      tl.add(get(path, true).string());
    }
    return StrSeq.get(tl);
  }
}
