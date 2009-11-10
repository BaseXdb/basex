package org.deepfs.util;

import java.io.File;

/**
 * Interface assembling the events fired during a filesystem traversal.
 * 
 * #{@link FSWalker} descends into a directory hierarchy and notifies
 * registered visitors about the events assembled in this interface.
 * 
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek
 */
public interface FSTraversal {

  /**
   * Event triggered once tree level has changed.
   * @param l relative level/depth of traversal in directory hierarchy
   */
  void levelUpdate(final int l);
  
  /**
   * Fired before the filesystem traversal starts.
   * @param d the directory node the traversal starts from
   */
  void preTraversalVisit(final File d);

  /**
   * Fired once the filesystem traversal finished.
   * @param d the directory node the traversal started from
   */
  void postTraversalVisit(final File d);
  
  /**
   * Visits a directory node in preorder (enter directory). 
   * @param d the directory node
   */
  void preDirectoryVisit(final File d);
  
  /**
   * Visits a directory node in postorder (leaving directory).
   * @param d the directory node  
   */
  void postDirectoryVisit(final File d);

  /** 
   * Visits a regular file.
   * @param f the file  
   */
  void regularFileVisit(final File f);

  /** 
   * Visits a symbolic link.
   * @param f the file  
   */
  void symLinkVisit(final File f);
}
