package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.in.*;

/**
 * Evaluates the 'binary get' command and retrieves binary resources.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class BinaryGet extends ACreate {
  /**
   * Default constructor.
   * @param path source path
   */
  public BinaryGet(final String path) {
    super(Perm.NONE, true, path);
  }

  @Override
  protected boolean run() {
    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(PATH_INVALID_X, args[0]);

    final Data data = context.data();
    if(data.inMemory()) return error(NO_MAINMEM);

    final IOFile bin = data.meta.file(path, ResourceType.BINARY);
    if(!bin.exists() || bin.isDir()) return error(RES_NOT_FOUND_X, path);

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

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.BINARY + " " + CmdBinary.GET).add(0);
  }
}
