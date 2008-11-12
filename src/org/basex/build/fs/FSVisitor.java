package org.basex.build.fs;

import java.io.File;
import java.io.IOException;

/**
 * This interface defines events that may occur during a file hierarchy
 * traversal.
 *
 * To get notified about an event during a file hierarchy traversal implement
 * this interface and register to (@see FSWalker).
 * <ul>
 * <li>preTraversal (right before the traversal will start)</li>
 * <li>preEvent (before a new directory is entered)</li>
 * <li>postEvent (after a directory is left)</li>
 * <li>regfileEvent (a file is being visited)</li>
 * <li>symlinkEvent (a symbolic link is being visited)</li>
 * <li>postTraversal (right after the traversal)</li>
 * </ul>
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Alexander Holupirek
 */
public interface FSVisitor {

  /** Invoked before traversal starts.
   * If more than one file hierarchy is to be imported (for example
   * 'Import all...' on Windows gets C:, D: ...), you have to indicate
   * the very first traversal to construct a document node.
   * @param path the traversal starts from.
   * @param docOpen indicates if this is also the beginning of the document.
   * @throws IOException I/O exception
   */
  void preTraversal(final String path, boolean docOpen) throws IOException;

  /** Invoked before directory is entered. 
   * @param dir name
   * @throws IOException I/O exception
   */
  void preEvent(final File dir) throws IOException;

  /** Invoked when directory is left.
   * @throws IOException I/O exception
   */
  void postEvent() throws IOException;

  /** Invoked when a regular file is visited.
   * @param file name
   * @throws IOException I/O exception
   */
  void regfileEvent(final File file) throws IOException;

  /** Invoked when a symbolic link is visited.
   * @param link to be visited.
   */
  void symlinkEvent(File link);

  /** Invoked after the traversal.
   * @param docClose indicates if the document is to be closed.
   * @throws IOException I/O exception
   */
  void postTraversal(boolean docClose) throws IOException;
}
