package org.deepfs.jfuse;

import static org.catacombae.jfuse.types.system.StatConstant.*;
import org.catacombae.jfuse.types.system.StatConstant;

/** (from stat.h)
 * [XSI] The following macros shall be provided to test whether a file is
 * of the specified type.  The value m supplied to the macros is the value
 * of st_mode from a stat structure.  The macro shall evaluate to a non-zero
 * value if the test is true; 0 if the test is false.

 #define S_ISBLK(m)      (((m) & 0170000) == 0060000)    /* block special
 #define S_ISCHR(m)      (((m) & 0170000) == 0020000)    /* char special
 #define S_ISDIR(m)      (((m) & 0170000) == 0040000)    /* directory
 #define S_ISFIFO(m)     (((m) & 0170000) == 0010000)    /* fifo or socket
 #define S_ISREG(m)      (((m) & 0170000) == 0100000)    /* regular file
 #define S_ISLNK(m)      (((m) & 0170000) == 0120000)    /* symbolic link
 #define S_ISSOCK(m)     (((m) & 0170000) == 0140000)    /* socket
 #if !defined(_POSIX_C_SOURCE) || defined(_DARWIN_C_SOURCE)
 #define S_ISWHT(m)      (((m) & 0170000) == 0160000)    /* whiteout
 #define S_ISXATTR(m)    (((m) & 0200000) == 0200000)    /* extended attribute
 #endif
 */
public final class FileTestMacros {

  /** Private utility constructor. */
  private FileTestMacros() {
    throw new UnsupportedOperationException();
  }

  /** Test file mode whether it is a directory.
   * @param m file mode
   * @return true if directory mode
   */
  public static boolean S_ISDIR(final StatConstant m) {
    return S_ISDIR(m.getNativeValue());
  }

  /** Test file mode whether it is a directory.
   * @param m file mode
   * @return true if directory mode
   */
  public static boolean S_ISDIR(final int m) {
    return (m & S_IFMT.getNativeValue()) == S_IFDIR.getNativeValue();
  }

  /** Test file mode whether it is a regular file.
   * @param m file mode
   * @return true if directory mode
   */
  public static boolean S_ISREG(final StatConstant m) {
    return S_ISREG(m.getNativeValue());
  }

  /** Test file mode whether it is a regular file.
   * @param m file mode
   * @return true if directory mode
   */
  public static boolean S_ISREG(final int m) {
    return (m & S_IFMT.getNativeValue()) == S_IFREG.getNativeValue();
  }
}
