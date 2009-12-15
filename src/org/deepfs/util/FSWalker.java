package org.deepfs.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Perform a preorder traversal of a directory hierarchy and notify visitors.
 *
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek
 */
public final class FSWalker implements FSTraversal {

  /** Registered visitors to be notified of events. */
  private final List<FSTraversal> visitors = new LinkedList<FSTraversal>();

  /** Depth of traversal. */
  private int level;

  @Override
  public void levelUpdate(final int l) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().levelUpdate(l);  }

  @Override
  public void postDirectoryVisit(final File d) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().postDirectoryVisit(d);
  }

  @Override
  public void postTraversalVisit(final File d) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().postTraversalVisit(d);
  }

  @Override
  public void preDirectoryVisit(final File d) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().preDirectoryVisit(d);
  }

  @Override
  public void preTraversalVisit(final File d) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().preTraversalVisit(d);
  }

  @Override
  public void regularFileVisit(final File f) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().regularFileVisit(f);
  }

  @Override
  public void symLinkVisit(final File f) {
    final ListIterator<FSTraversal> i = visitors.listIterator();
    while(i.hasNext()) i.next().symLinkVisit(f);
  }

  /**
   * Determines if the specified file is valid and no symbolic link.
   * @param f file to be tested
   * @return false for a symbolic link
   */
  public static boolean valid(final File f) {
    try {
      return f.getPath().equals(f.getCanonicalPath());
    } catch(final IOException ex) {
      return false;
    }
  }

  /**
   * Invoked when a directory is visited.
   * @param f file name
   */
  private void visitDirectory(final File f) {
    preDirectoryVisit(f);
    levelUpdate(++level);
    startTraversal(f);
    levelUpdate(--level);
    postDirectoryVisit(f);
  }

  /**
   * Invoked when a regular file is visited.
   * @param f file name
   */
  private void visitFile(final File f) {
    regularFileVisit(f);
  }

  /**
   * Invoked when a smbolic link is visited.
   * @param f file name
   */
  private void visitSymLink(final File f) {
    symLinkVisit(f);
  }

  /**
   * Invoked before the traversal starts.
   * @param d directory the traversal starts from
   */
  private void preTraversal(final File d) {
    preTraversalVisit(d);
  }

  /**
   * Invoked after the traversal has taken place.
   * @param d directory the traversal started from
   */
  private void postTraversal(final File d) {
    postTraversalVisit(d);
  }

  /**
   * Traverses a directory hierarchy.
   * @param d directory traversal starts from
   */
  private void startTraversal(final File d) {
    final File[] files = d.listFiles();
    if(files == null) return;

    for(final File f : files) {
      if(!valid(f)) {
        visitSymLink(f);
      } else if(f.isDirectory()) {
        visitDirectory(f);
      } else {
        visitFile(f);
      }
    }
  }

  /**
   * Creates a new FSWalker that has to be populated with #{@link FSTraversal}.
   */
  public FSWalker() { /* DEFAULT */ }

  /**
   * Creates a new FSWalker and {@link #register(FSTraversal)} a visitor.
   * @param v visitor implementing #{@link FSTraversal} to be registered
   */
  public FSWalker(final FSTraversal... v) {
    for(final FSTraversal vi : v)
      register(vi);
  }

  /**
   * Registers a new visitor to be informed during the traversal.
   * @param v visitor implementing #{@link FSTraversal}
   */
  public void register(final FSTraversal v) {
    visitors.add(v);
  }

  /**
   * Entry point for a directory traversal.
   * @param d directory traversal should start from
   */
  public void traverse(final File d) {
    preTraversal(d);
    startTraversal(d);
    postTraversal(d);
  }
}
