package org.basex.core.proc;

import org.basex.core.Main;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.deepfs.jfuse.JFUSEAdapter;

/**
 * Evaluates the 'mount' command and mounts a DeepFS database as FUSE.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 */
public final class Mount extends AAdmin {
  /**
   * Default constructor.
   * @param name name of database
   * @param mountpoint filesystem path
   */
  public Mount(final String name, final String mountpoint) {
    super(name, mountpoint);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final String db = args[0];
//    final String mp = args[1];

    new Close().execute(context, out);
    new Open(db).execute(context, out);

    final Data data = context.data();
    if(data.fs == null) {
      Main.err("No DeepFS instance");
      return false;
    }

//    final DeepFS fs = new DeepFS(context);

    /* [AH] request Prop.JFUSE, ...
     * Class<?> cls = Class.forName("org.deepfs.DeepFS");
     * cls.getMethod("mount", ...).invoke(null, ...);
     */
    if(JFUSEAdapter.loadJFUSELibrary()) {
      new Thread() {
        @Override
        public void run() {
//          DeepFSImpl.mount(mp, fs);
        }
      }.start();
    } else {
      Main.err("Missing native jfuse library.");
      return false;
    }

    return true;
  }
}
