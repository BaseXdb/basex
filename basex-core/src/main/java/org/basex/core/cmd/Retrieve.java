package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;

/**
 * Evaluates the 'retrieve' command and retrieves binary content.
 *
 * @author BaseX Team 2005-21, BSD License
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
    if(path == null) return error(PATH_INVALID_X, args[0]);

    final Data data = context.data();
    if(data.inMemory()) return error(NO_MAINMEM);

    final IOFile bin = data.meta.binary(path);
    if(bin == null || !bin.exists() || bin.isDir()) return error(RES_NOT_FOUND_X, path);

    try(BufferInput bi = BufferInput.get(bin)) {
      for(int b; (b = bi.read()) != -1;) out.write(b);
    } catch(final IOException ex) {
      return error(ex.toString());
    }
    return info(QUERY_EXECUTED_X_X, "", jc().performance);
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }
}
