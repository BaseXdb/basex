package org.deepfs.jfuse;

import java.io.PrintStream;

/**
 * Internal representation of file status.
 * @author Alexander Holupirek
 *
 */
public class DeepStat {
  /** Device inode resides on. Type: dev_t (4 bytes) */
  public long stdev = 0;
  /** Inode's number. Type: ino_t (4 bytes) */
  public long stino = 0;
  /** Inode protection mode. Type: mode_t (2 or 4 bytes) */
  public long stmode = 0;
  /** Number of hard links to the file. Type: nlink_t (2 bytes) */
  public long stnlink = 0;
  /** User-id of owner. Type: uid_t (4 bytes) */
  public long stuid = 0;
  /** Group-id of owner. Type: gid_t (4 bytes) */
  public long stgid = 0;
  /** Device type, for special file inode. Type: dev_t (4 bytes) */
  public long strdev = 0;
  /** Time of last access. */
//  public final Timespec st_atimespec = new Timespec();
  public long statimespec = 0;
  /** Time of last data modification. */
//  public final Timespec st_mtimespec = new Timespec();
  public long stmtimespec = 0;
  /** Time of last file status change. */
//  public final Timespec st_ctimespec = new Timespec();
  public long stctimespec = 0;
  /** File size, in bytes. Type: off_t (8 bytes) */
  public long stsize = 0;
  /** Blocks allocated for file. Type: quad_t (8 bytes) */
  public long stblocks = 0;
  /** Optimal file sys I/O ops blocksize. Type: u_long (4 bytes) */
  public long stblocksize = 0;
  /** User defined flags for file. Type: u_long (4 bytes) */
  public long stflags = 0;
  /** File generation number. Type: u_long (4 bytes) */
  public long stgen = 0;
  
  /**
   * Print stat fields to provided stream.
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

}
