package org.basex.query;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.expr.path.*;
import org.basex.util.list.*;

/**
 * Collects the names that complete a path expression, by following its steps in the path summary.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class PathSuggestions {
  /** Data reference. */
  private final Data data;

  /** Stack of current path nodes. */
  private final ArrayDeque<ArrayList<PathNode>> stack = new ArrayDeque<>();
  /** All current path nodes. */
  private ArrayList<PathNode> all;
  /** Current path nodes. */
  private ArrayList<PathNode> current;
  /** Show or hide completions. */
  private boolean show;
  /** Last element name (can be {@code null}). */
  private byte[] name;

  /**
   * Constructor.
   * @param data data reference
   */
  PathSuggestions(final Data data) {
    this.data = data;
    init();
  }

  /**
   * Starts at the root nodes if no predicate is open.
   */
  void init() {
    if(stack.isEmpty()) {
      all = data.paths().root();
      current = all;
    }
  }

  /**
   * Follows an axis.
   * @param axis axis
   */
  void axis(final Axis axis) {
    all = axis != Axis.CHILD && axis != Axis.DESCENDANT ?
      new ArrayList<>() : PathIndex.desc(current, axis == Axis.DESCENDANT);
    current = all;
    show = true;
  }

  /**
   * Applies a name test.
   * @param nm name, with a leading {@code @} for attributes
   * @param exact exact name matching ({@code false} if the name is incomplete)
   */
  void test(final byte[] nm, final boolean exact) {
    name = nm;
    if(name == null) return;

    final ArrayList<PathNode> tmp = new ArrayList<>();
    boolean s = false;
    for(final PathNode p : all) {
      final byte[] nd = p.token(data);
      if(startsWith(nd, name)) {
        if(!exact || eq(nd, name)) tmp.add(p);
        s |= !eq(name, nd);
      }
    }
    show = name.length == 0 || s;
    current = tmp;
  }

  /**
   * Opens or closes a predicate.
   * @param open open flag
   */
  void predicate(final boolean open) {
    if(open) {
      test(name, true);
      stack.push(current);
      axis(Axis.CHILD);
    } else {
      current = stack.pop();
      show = false;
      all = current;
    }
  }

  /**
   * Returns the sorted names of the current path nodes.
   * @return names (empty if there is nothing to suggest)
   */
  StringList names() {
    final TreeSet<String> names = new TreeSet<>();
    if(show) {
      for(final PathNode node : current) {
        final String nm = string(node.token(data));
        if(!nm.isEmpty()) names.add(nm);
      }
    }
    return new StringList(names.toArray(String[]::new));
  }
}
