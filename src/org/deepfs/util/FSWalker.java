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

  /**
   * Creates a new FSWalker and registers all visitors.
   * @param v visitors implementing #{@link FSTraversal} to be registered
   */
  public FSWalker(final FSTraversal... v) {
    for(final FSTraversal vi : v) visitors.add(vi);
  }

  /**
   * Entry point for a directory traversal.
   * @param d directory traversal should start from
   */
  public void traverse(final File d) {
    preTraversalVisit(d);
    startTraversal(d);
    postTraversalVisit(d);
  }

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
   * Traverses a directory hierarchy.
   * @param d directory traversal starts from
   */
  private void startTraversal(final File d) {
    final File[] files = d.listFiles();
    if(files == null) return;

    for(final File f : files) {
      if(!valid(f)) {
        symLinkVisit(f);
      } else if(f.isDirectory()) {
        preDirectoryVisit(f);
        levelUpdate(++level);
        startTraversal(f);
        levelUpdate(--level);
        postDirectoryVisit(f);
      } else {
        regularFileVisit(f);
      }
    }
  }

  /**
   * Determines if the specified file is valid and no symbolic link.
   * @param f file to be tested
   * @return false for a symbolic link
   */
  private static boolean valid(final File f) {
    try {
      return f.getPath().equals(f.getCanonicalPath());
    } catch(final IOException ex) {
      return false;
    }
  }
}
