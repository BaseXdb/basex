package org.basex.fuse;

import java.io.File;

/**
 * Assembles helper functions for filesystem operations.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 */
public final class FSUtils {

  /** Hide constructor. */
  private FSUtils() { }

  /** Delete non-empty directory. 
   * @param dir to be deleted.
   * @return boolean true for success, false for failure.
   * */   
  public static boolean deleteDir(final File dir) {
    if (dir.isDirectory()) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) 
            if (!deleteDir(new File(dir, children[i]))) return false;
    }      
    return dir.delete();
  } 
}
