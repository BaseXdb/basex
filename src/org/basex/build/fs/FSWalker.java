package org.basex.build.fs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.io.IO;

/**
 * Performs a file hierarchy traversal.
 *
 * An object implementing the interface (@see FSVisitor) may register
 * to the traversal and is called (back) to react on events happening during
 * the traversal (such as entering a directory etc.).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Alexander Holupirek
 */
public final class FSWalker {
  /** Current file to be processed. */
  public File curr;
  /** Reference to the file system visitor. */
  private List<FSVisitor> visitors;
  /** Root Directory in UNIX, Partitions in Windows or given directory. */
  private File[] roots;

  /**
   * Registers another visitor to the file hierarchy traversal.
   * @see FSVisitor
   * @param v the visitor to be added.
   * @return true on success, false on failure.
   */
  public boolean register(final FSVisitor v) {
    if(visitors == null) visitors = new LinkedList<FSVisitor>();
    return visitors.add(v);
  }

  /**
   * Starts the traversal from a given path.
   * An empty path or '/' denotes that the complete FS should be parsed.
   * For Windows systems that means (C:/, D:/ ..), for Unix systems '/'.
   *
   * @param path the traversal starts from.
   * @throws IOException I/O exception
   */
  public void fileHierarchyTraversal(final IO path) throws IOException {
    File r = path.file().getCanonicalFile();
    if(Prop.UNIX && isSymlink(r)) {
      r = r.getCanonicalFile();
      roots = new File[] { r };
    } else if(path.equals("/") || path.path().length() == 0) {
      roots = Prop.UNIX ? new File[] { new File("/") } : File.listRoots();
    } else {
      roots = new File[] { r };
    }
    for(int r1 = 0; r1 < roots.length; r1++) {
      for(final FSVisitor v : visitors)
        v.preTraversal(roots[r1].toString(), r1 == 0);
      visitOrDescend(roots[r1]);
      for(final FSVisitor v : visitors)
        v.postTraversal(r1 == roots.length - 1);
    }
  }

  /**
   * Determines a symbolic link.
   * @param f file to be tested.
   * @return true for a symbolic link
   * @throws IOException I/O exception
   */
  private boolean isSymlink(final File f) throws IOException {
    return !f.getPath().equals(f.getCanonicalPath());
  }

  /**
   * Visit files in directory or step further down.
   * @param d the directory to be visited.
   */
  private void visitOrDescend(final File d) {

    final File[] files = d.listFiles();
    if(files != null)

    for(final File f : files) {
      try {
        curr = f;
        //if(Prop.debug) BaseX.debug(curr.getAbsolutePath());
        if(isSymlink(f)) for(final FSVisitor v : visitors)
          v.symlinkEvent(f);
        else if(f.isDirectory()) descend(f);
        else for(final FSVisitor v : visitors)
          v.regfileEvent(f);
      } catch(IOException e) {
        BaseX.debug(f + FSText.IOEXSKIP + e.getMessage());
      }
    }
  }

  /**
   * Descend into directory.
   * @param d the directory to descend into.
   * @throws IOException I/O exception
   */
  private void descend(final File d) throws IOException {
    // do not descend into symlinked directories.
    if(isSymlink(d)) {
      for(final FSVisitor v : visitors)
        v.symlinkEvent(d);
      return;
    }
    for(final FSVisitor v : visitors)
      v.preEvent(d);
    visitOrDescend(d);
    for(final FSVisitor v : visitors)
      v.postEvent();
  }
}
