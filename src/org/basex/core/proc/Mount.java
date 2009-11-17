package org.basex.core.proc;

import org.basex.core.Main;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.deepfs.fs.DeepFS;
import org.deepfs.util.LibraryLoader;

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
    final String mp = args[1];

    new Close().execute(context, out);
    new Open(db).execute(context, out);

    final Data data = context.data;
    if(data.fs == null) {
      Main.err("No DeepFS instance");
      return false;
    }

    final DeepFS fs = new DeepFS(context);

    if(LibraryLoader.load(LibraryLoader.JFUSELIBNAME)) {
      new Thread() {
        @Override
        public void run() {
          try {
            // DeepFSImpl.mount(mp, fs);
            Class<?> cls = Class.forName("org.deepfs.DeepFSImpl");
            Class<?>[] signature = new Class[2];
            signature[0] = String.class;
            signature[1] = DeepFS.class;
            cls.getMethod("mount", signature).invoke(null, mp, fs);
          } catch (Exception e) {
            Main.err("Can not mount: ", e);
          }
        }
      }.start();
    } else {
      Main.err("Missing native fuse support.");
      return false;
    }

    return true;
  }
}
