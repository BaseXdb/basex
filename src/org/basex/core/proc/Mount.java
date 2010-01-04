package org.basex.core.proc;

import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.deepfs.fs.DeepFS;
import org.deepfs.util.LibraryLoader;

/**
 * Evaluates the 'mount' command and mounts a DeepFS database as FUSE.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Alexander Holupirek
 */
public final class Mount extends Proc {
  /**
   * Default constructor.
   * @param name name of database
   * @param mountpoint filesystem path
   */
  public Mount(final String name, final String mountpoint) {
    super(User.ADMIN, name, mountpoint);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final String db = args[0];
    final String mp = args[1];

    new Close().execute(context, out);
    new Open(db).execute(context, out);

    final Data data = context.data;
    if(data.fs == null) return error("No DeepFS instance");

    if(!LibraryLoader.load(LibraryLoader.JFUSELIBNAME))
      return error("Missing native fuse support.");

    final DeepFS fs = new DeepFS(context);
    new Thread() {
      @Override
      public void run() {
        try {
          // DeepFSImpl.mount(mp, fs);
          final Class<?> cls = Class.forName("org.deepfs.DeepFSImpl");
          cls.getMethod("mount", new Class[] { String.class, DeepFS.class }).
            invoke(null, mp, fs);
        } catch (final Exception e) {
          Main.err("Cannot mount: ", e);
        }
      }
    }.start();
    return true;
  }
}
