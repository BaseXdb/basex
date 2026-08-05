package org.basex.gui.view.editor;

import java.util.*;

import org.basex.io.*;

/**
 * Navigation history of the editor: the locations from which jumps were performed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Navigation {
  /** Maximum number of remembered locations. */
  private static final int MAX = 100;

  /** Previous locations. */
  private final ArrayList<Location> previous = new ArrayList<>();
  /** Next locations. */
  private final ArrayList<Location> next = new ArrayList<>();

  /**
   * Remembers a location that was left by a jump.
   * @param location location
   */
  void add(final Location location) {
    if(!previous.isEmpty() && previous.getLast().equals(location)) return;
    previous.add(location);
    if(previous.size() > MAX) previous.removeFirst();
    next.clear();
  }

  /**
   * Returns the previous or next location and remembers the current one.
   * @param forward forward flag
   * @param current current location (can be {@code null})
   * @return location, or {@code null} if the history has no further entry
   */
  Location go(final boolean forward, final Location current) {
    final ArrayList<Location> from = forward ? next : previous;
    if(from.isEmpty()) return null;
    final Location location = from.removeLast();
    if(current != null) (forward ? previous : next).add(current);
    return location;
  }

  /**
   * Indicates if the history has a previous or next location.
   * @param forward forward flag
   * @return result of check
   */
  boolean available(final boolean forward) {
    return !(forward ? next : previous).isEmpty();
  }

  /**
   * Location in an edited file.
   *
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   * @param file file
   * @param pos caret position
   */
  record Location(IOFile file, int pos) { }
}
