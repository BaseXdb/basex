package org.basex.data;

import org.basex.core.Prop;
import org.basex.index.Names;
import org.deepfs.util.LibraryLoader;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed memory structure and redundantly passes
 * the data on to a native application. The table mapping is documented in
 * {@link DiskData}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Alexander Holupirek
 */
public final class NativeData extends MemData {
  static {
    LibraryLoader.load(LibraryLoader.JSDBFSLIBNAME);
  }

  /** Initialization code invoked by constructor.
   * @param cap initial capacity of pre/dist/size table.
   */
  private native void jniInit(final int cap);

  /** Pass strings to native environment.
   * @param a string to be passed
   * @param b string to be passed
   */
  private native void passString(final String a, final String b);

  /**
   * Constructor.
   * @param cap initial array capacity
   * @param tag tag index
   * @param att attribute name index
   * @param n namespaces
   * @param s path summary
   * @param pr database properties
   */
  public NativeData(final int cap, final Names tag, final Names att,
      final Namespaces n, final PathSummary s, final Prop pr) {
    super(cap, tag, att, n, s, pr);
    jniInit(cap);
    passString("abäßcd", "ab€£ﬁ#cd");
  }
}
