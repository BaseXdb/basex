package org.deepfs.fs;

import java.nio.ByteBuffer;
import org.basex.core.Main;
import org.catacombae.jfuse.FUSE;
import org.catacombae.jfuse.FUSEFileSystemAdapter;
import org.catacombae.jfuse.types.fuse26.FUSEFileInfo;
import org.catacombae.jfuse.types.fuse26.FUSEFillDir;
import org.catacombae.jfuse.types.system.Stat;
import org.catacombae.jfuse.util.FUSEUtil;
import static org.basex.util.Token.*;

/**
 * DeepFS: The XQuery Filesystem. Filesystem-side implementation of DeepFS.
 *
 * Uses LGPL jFUSE bindings v0.1 provided by
 * Copyright (C) 2008-2009  Erik Larsson <erik82@kth.se>
 * and is based on example code taken from the project.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 */
public class DeepFSImpl extends FUSEFileSystemAdapter {

  /** Actual directory token. */
  private static final byte[] DOT = token(".");
  /** Actual parent directory token. */
  private static final byte[] DOTDOT = token("..");

  /** Connection to database storage. */
  private DeepFS dbfs;

  /** Constructor. */
  public DeepFSImpl() {
    dbfs = new DeepFS("deepfs");
  }

  /**
   * Constructor (currently we always create a fresh instance).
   * @param db DeepFS database/filesystem instance to be created.
   */
  public DeepFSImpl(final String db) {
    dbfs = new DeepFS(db);
  }

  @Override
  public int mknod(final ByteBuffer path, final short fileMode,
      final long devNum) {
    String pathString = FUSEUtil.decodeUTF8(path);
    int rc = dbfs.create(pathString, fileMode);
    Main.debug("mknod: " + pathString + " (" + rc + ") device: " + devNum);
    return (rc == -1) ? -ENETRESET : 0;
  }

  @Override
  public int mkdir(final ByteBuffer path, final short createMode) {
    String pathString = FUSEUtil.decodeUTF8(path);
    int rc = 0;
    rc = dbfs.mkdir(pathString, createMode);
    Main.debug("mkdir: " + pathString + "(" + rc + ")");
    return (rc == -1) ? -ENETRESET : 0;
  }

  @Override
  public int getattr(final ByteBuffer path, final Stat stbuf) {
    String pathString = FUSEUtil.decodeUTF8(path);
    Main.debug("getattr: " + pathString);
    if(pathString == null) return -ENOENT;
    int rc = dbfs.stat(pathString, stbuf);
    if(rc == -1) return -ENOENT;
    return 0;
  }

  @Override
  public int readdir(final ByteBuffer path, final FUSEFillDir filler,
      final long offset, final FUSEFileInfo fi) {

    String pathString = FUSEUtil.decodeUTF8(path);
    if(pathString == null) return -ENOENT;

    byte[][] dents = dbfs.readdir(pathString);
    if(dents == null) return -ENOENT;

    filler.fill(DOT, null, 0);
    filler.fill(DOTDOT, null, 0);
    for(byte[] de : dents)
      filler.fill(de, null, 0); // name, stat, offset

    return 0;
  }

  @Override
  public int open(final ByteBuffer path, final FUSEFileInfo fi) {
    String pathString = FUSEUtil.decodeUTF8(path);
    if(pathString == null) return -ENOENT;

//    if(!pathString.equals(helloPath)) return -ENOENT;
//    if((fi.flags & 3) != O_RDONLY) return -EACCES;

    return -ENOENT;
  }

  @Override
  public int read(final ByteBuffer path, final ByteBuffer buf,
      final long offset, final FUSEFileInfo fi) {
    String pathString = FUSEUtil.decodeUTF8(path);
    if(pathString == null) return -ENOENT;
    if(offset < 0 || offset > Integer.MAX_VALUE) return -EINVAL;

//    int bytesLeftInFile = helloStr.length - (int) offset;
//    if(bytesLeftInFile > 0) {
//      int len = Math.min(bytesLeftInFile, buf.remaining());
//      buf.put(helloStr, (int) offset, len);
//      return len;
//    }

    return 0;
  }

  @Override
  public void destroy(final Object o) {
    Main.debug("destroy");
    dbfs.umount();
  }

  /**
   * Main entry point.
   *
   * @param args mount point is expected as first argument.
   */
  public static void main(final String[] args) {
    FUSE.main(args, new DeepFSImpl());
  }
}
