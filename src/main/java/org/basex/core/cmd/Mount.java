package org.basex.core.cmd;

import static org.basex.util.Reflect.*;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.Data;
import org.deepfs.fs.DeepFS;
import org.deepfs.util.LibraryLoader;

/**
 * Evaluates the 'mount' command and mounts a DeepFS database as FUSE.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Alexander Holupirek
 */
public final class Mount extends Command {
  /**
   * Default constructor.
   * @param name name of database
   * @param mountpoint filesystem path
   */
  public Mount(final String name, final String mountpoint) {
    super(User.ADMIN, name, mountpoint);
  }

  @Override
  protected boolean run() {
    final String db = args[0];
    final String mp = args[1];

    new Close().run(context);
    new Open(db).run(context);

    final Data data = context.data;
    if(data.fs == null) return error("No DeepFS instance");

    if(!LibraryLoader.load(LibraryLoader.JFUSELIBNAME))
      return error("Missing native fuse support.");

    final DeepFS fs = new DeepFS(context);
    new Thread() {
      @Override
      public void run() {
        // DeepFSImpl.mount(mp, fs);
        final Class<?> cls = find("org.deepfs.DeepFSImpl");
        invoke(find(cls, "mount", String.class, DeepFS.class), null, mp, fs);
      }
    }.start();
    return true;
  }
}
