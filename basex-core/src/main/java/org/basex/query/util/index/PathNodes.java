package org.basex.query.util.index;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Path summary nodes and statistics of path expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PathNodes {
  /** Private constructor. */
  private PathNodes() { }

  /**
   * Returns the path nodes of a root expression.
   * @param rt root at compile time (can be {@code null})
   * @param data data reference (can be {@code null})
   * @param stats assess database statistics
   * @return path nodes, or {@code null} if nodes cannot be evaluated
   */
  public static ArrayList<PathNode> root(final Expr rt, final Data data, final boolean stats) {
    if(rt == null || data == null || !(stats ? data.meta.uptodate : data.meta.complete))
      return null;
    // document nodes: start with the root of the path summary
    if(rt.seqType().type.instanceOf(NodeType.DOCUMENT)) return data.paths().root();
    // single database node: resolve its path node (statistics are only exact for documents)
    if(stats || !(rt instanceof final DBNode node) || node.data() != data) return null;
    final PathNode pn = data.paths().node(node.pre());
    if(pn == null) return null;
    final ArrayList<PathNode> nodes = new ArrayList<>(1);
    nodes.add(pn);
    return nodes;
  }

  /**
   * Returns the path nodes that will result from a path.
   * @param path path expression
   * @param nodes current path nodes
   * @param stats assess database statistics
   * @return path nodes, or {@code null} if nodes cannot be collected
   * @throws QueryException query exception
   */
  public static ArrayList<PathNode> get(final Path path, final ArrayList<PathNode> nodes,
      final boolean stats) throws QueryException {

    ArrayList<PathNode> pn = nodes;
    for(final Expr expr : path.steps) {
      if(expr instanceof UtilRoot) {
        pn = UtilRoot.nodes(pn);
      } else if(expr instanceof final Step step) {
        pn = step.nodes(pn, stats);
        // check if predicates will never match
        if(!stats && pn != null) {
          for(final Expr ex : step.exprs) {
            if(ex.noMatches(pn, step.data())) return new ArrayList<>();
          }
        }
      } else {
        pn = null;
      }
      if(pn == null) break;
    }
    return pn;
  }

  /**
   * Checks if all path nodes are located on the same level.
   * @param nodes path nodes
   * @return result of check
   */
  public static boolean sameLevel(final ArrayList<PathNode> nodes) {
    final int level = nodes.isEmpty() ? 0 : nodes.getFirst().level();
    return Checks.all(nodes, node -> node.level() == level);
  }

  /**
   * Returns database statistics for the path nodes that will result from a path.
   * @param path path expression
   * @return statistics, or {@code null} if they are not available
   * @throws QueryException query exception
   */
  public static ArrayList<Stats> stats(final Path path) throws QueryException {
    return stats(path, null);
  }

  /**
   * Returns database statistics for the path nodes that will result from a path.
   * @param path path expression
   * @param nodes path nodes of the context, or {@code null} to start from the root
   * @return statistics, or {@code null} if they are not available
   * @throws QueryException query exception
   */
  private static ArrayList<Stats> stats(final Path path, final ArrayList<PathNode> nodes)
      throws QueryException {

    final Data data = path.data();
    if(data == null) return null;
    final ArrayList<PathNode> start = nodes != null ? nodes : root(path.root, data, true);
    final ArrayList<PathNode> pn = start != null ? get(path, start, true) : null;
    return pn != null ? data.paths().stats(pn) : null;
  }

  /**
   * Returns database statistics for the results of an expression.
   * @param expr expression (path or context value)
   * @param nodes path nodes of the context (can be {@code null})
   * @param data data reference (can be {@code null})
   * @return statistics, or {@code null} if they are not available
   * @throws QueryException query exception
   */
  public static ArrayList<Stats> stats(final Expr expr, final ArrayList<PathNode> nodes,
      final Data data) throws QueryException {

    if(data == null) return null;
    if(expr instanceof final AxisPath path) {
      return (nodes == null || path.root == null) && path.data() == data ?
        stats(path, nodes) : null;
    }
    return nodes != null && expr instanceof ContextValue ? data.paths().stats(nodes) : null;
  }
}
