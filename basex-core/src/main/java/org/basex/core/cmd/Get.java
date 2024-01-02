package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;

/**
 * Evaluates the 'get' command and retrieves an XML document.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Get extends ACreate {
  /**
   * Default constructor.
   * @param path source path
   */
  public Get(final String path) {
    super(Perm.NONE, true, path);
  }

  @Override
  protected boolean run() {
    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(PATH_INVALID_X, args[0]);

    final Data data = context.data();
    final int pre = data.resources.doc(path);
    if(pre == -1) return error(RES_NOT_FOUND_X, path);

    try(Serializer ser = Serializer.get(out)) {
      ser.serialize(new DBNode(data, pre));
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
