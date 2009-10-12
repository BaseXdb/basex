package org.basex.core.proc;

import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.data.Data;
import org.deepfs.fs.DeepFS;
import org.deepfs.fs.DeepFSImpl;
import org.deepfs.jfuse.JFUSEAdapter;

/**
 * Evaluates the 'mount' command and mounts a DeepFS database as FUSE.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 */
public final class Mount extends Process {
  /**
   * Default constructor.
   * @param name name of database
   * @param mountpoint filesystem path
   */
  public Mount(final String name, final String mountpoint) {
    super(STANDARD, name, mountpoint);
  }

  @Override
  protected boolean exec() {
    final String db = args[0];
    final String mp = args[1];

    new Close().execute(context);
    new Open(db).execute(context);

    final Data data = context.data();
    if(data.fs == null) {
      Main.err("No DeepFS instance");
      return false;
    }

    final DeepFS fs = new DeepFS(context);

    if(JFUSEAdapter.loadJFUSELibrary()) {
      new Thread() {
        @Override
        public void run() {
          DeepFSImpl.mount(mp, fs);
        }
      }.start();
    } else {
      Main.err("Missing native jfuse library.");
      return false;
    }

    return true;
  }
}
