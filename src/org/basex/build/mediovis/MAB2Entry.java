package org.basex.build.mediovis;

import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This is a simple data structure for storing MAB2 entries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MAB2Entry {
  /** Top ID. */
  final byte[] id;
  /** Children offsets. */
  long[] children;
  /** File offset. */
  long pos;
  /** Number of children. */
  int size;

  /**
   * Constructor.
   * @param i id value
   */
  public MAB2Entry(final byte[] i) {
    id = i;
  }

  /**
   * Adds a child.
   * @param c child to be added
   */
  public void add(final long c) {
    if(children == null) children = new long[1];
    else if(children.length == size) children = Array.extend(children);
    children[size++] = c;
  }

  /**
   * Sets the file offset.
   * @param p file offset
   */
  public void pos(final long p) {
    pos = p;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("MAB2Entry:\n");
    sb.append("ID: " + Token.string(id) + "\n");
    sb.append("Pos: " + pos + "\n");
    sb.append("Size: " + size + "\n");
    sb.append("Children: \n");
    for(int s = 0; s < size; s++) {
      if(s != 0) sb.append(", ");
      sb.append(children[s]);
    }
    sb.append("\n");
    return sb.toString();
  }
}
