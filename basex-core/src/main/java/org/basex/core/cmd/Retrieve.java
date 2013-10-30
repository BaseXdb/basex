package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;

/**
 * Evaluates the 'retrieve' command and retrieves binary content.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Retrieve extends ACreate {
  /**
   * Default constructor.
   * @param path source path
   */
  public Retrieve(final String path) {
    super(Perm.NONE, true, path);
  }

  @Override
  protected boolean run() {
    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(NAME_INVALID_X, args[0]);

    final IOFile bin = context.data().meta.binary(path);
    if(bin == null || !bin.exists() || bin.isDir())
      return error(RES_NOT_FOUND_X, path);

    try {
      final BufferInput bi = new BufferInput(bin);
      try {
        for(int b; (b = bi.read()) != -1;) out.write(b);
      } finally {
        bi.close();
      }
      return info(QUERY_EXECUTED_X_X, "", perf);
    } catch(final IOException ex) {
      return error(FILE_NOT_STORED_X, ex);
    }
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
  }
}
