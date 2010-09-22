package org.deepfs.fs;

import java.io.PrintStream;

/**
 * Internal representation of file status.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Alexander Holupirek
 */
public final class DeepStat {
  /** Device inode resides on. Type: dev_t (4 bytes) */
  public final long stdev = 0;
  /** Inode's number. Type: ino_t (4 bytes) */
  public long stino;
  /** Inode protection mode. Type: mode_t (2 or 4 bytes) */
  public long stmode;
  /** Number of hard links to the file. Type: nlink_t (2 bytes) */
  public long stnlink;
  /** User-id of owner. Type: uid_t (4 bytes) */
  public long stuid;
  /** Group-id of owner. Type: gid_t (4 bytes) */
  public long stgid;
  /** Device type, for special file inode. Type: dev_t (4 bytes) */
  public final long strdev = 0;
  /** Time of last access. */
  public long statimespec;
  /** File size, in bytes. Type: off_t (8 bytes) */
  public long stsize;
  /** Blocks allocated for file. Type: quad_t (8 bytes) */
  public final long stblocks = 0;
  /** Optimal file sys I/O ops blocksize. Type: u_long (4 bytes) */
  public final long stblocksize = 1;
  /** User defined flags for file. Type: u_long (4 bytes) */
  public final long stflags = 1;
  /** File generation number. Type: u_long (4 bytes) */
  public final long stgen = 1;
  /* Time of last data modification.
  public final Timespec st_mtimespec = new Timespec();
  /** Time of last file status change.
  public final Timespec st_ctimespec = new Timespec(); */

  /**
   * Prints stat fields to the provided stream.
   * @param prefix to output
   * @param ps stream to be printed to
   */
  public void printFields(final String prefix, final PrintStream ps) {
    ps.println(prefix + "st_dev = " + stdev);
    ps.println(prefix + "st_ino = " + stino);
    ps.println(prefix + "st_mode = 0x" + Long.toHexString(stmode));
    ps.println(prefix + "st_nlink = " + stnlink);
    ps.println(prefix + "st_uid = " + stuid);
    ps.println(prefix + "st_gid = " + stgid);
    ps.println(prefix + "st_rdev = " + strdev);
    ps.println(prefix + "st_atimespec = " + statimespec);
    ps.println(prefix + "st_mtimespec = " + statimespec);
    ps.println(prefix + "st_ctimespec = " + statimespec);
    ps.println(prefix + "st_size = " + stsize);
    ps.println(prefix + "st_blocks = " + stblocks);
    ps.println(prefix + "st_blocksize = " + stblocksize);
    ps.println(prefix + "st_flags = " + stflags);
    ps.println(prefix + "st_gen = " + stgen);
  }  
  
  /** Directory bit. */
  private static final int DFS_S_IFDIR = 0040000;
  /** Regular file bit. */
  private static final int DFS_S_IFREG = 0100000;
  /** File bit mask. */
  private static final int DFS_S_IFMT = 0170000;
  
  /**
   * Gets (native) user id or default value.
   * @return user id
   */
  public static long getUID() {
    return 0;
  }

  /**
   * Gets (native) group id or default value.
   * @return group id
   */
  public static long getGID() {
    return 0;
  }
  
  /**
   * Returns directory bit.
   *
   * @return bitmask indicating a directory
   */
  public static int getSIFDIR() {
    return DFS_S_IFDIR;
  }

  /**
   * Returns regular file bit.
   *
   * @return bitmask indicating a directory
   */
  public static int getSIFREG() {
    return DFS_S_IFREG;
  }
  
  /**
   * Test mode for regular file flag.
   * @param mode of file
   * @return true if mode is regular file
   */
  public static boolean isReg(final int mode) {
    return (mode & DFS_S_IFMT) == DFS_S_IFREG;
  }
  
  
}