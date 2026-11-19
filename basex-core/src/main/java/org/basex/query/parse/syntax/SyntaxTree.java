package org.basex.query.parse.syntax;

import java.lang.reflect.*;

import org.basex.query.parse.gen.*;

/**
 * Syntax tree.
 *
 * @param input input: code points, with normalized line endings (borrowed, not copied)
 * @param root root node, a {@code Module} or a sequence type
 * @param gaps gaps between tokens: pairs of begin and end offsets, which contain whitespace and
 *   comments
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public record SyntaxTree(int[] input, Node root, int[] gaps) {
  /**
   * Returns a readable dump of the tree: one line per node, indented by level, with the components
   * of the records and the text of the terminals. Offsets and components that are not set are
   * omitted.
   * @return dump
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    dump(root, "", sb);
    return sb.toString();
  }

  /**
   * Dumps a node.
   * @param node node
   * @param indent indentation
   * @param sb string builder
   */
  private void dump(final Node node, final String indent, final StringBuilder sb) {
    if(node instanceof final Terminal terminal) {
      sb.append(indent).append(Sym.symbol(terminal.sym)).append(' ').
        append(terminal.text(input)).
        append('\n');
      return;
    }
    sb.append(indent).append(node.getClass().getSimpleName()).append('\n');
    final String ind = indent + "  ";
    for(final RecordComponent rc : node.getClass().getRecordComponents()) {
      final String name = rc.getName();
      if(name.equals("begin") || name.equals("end")) continue;
      final Object value;
      try {
        value = rc.getAccessor().invoke(node);
      } catch(final ReflectiveOperationException ex) {
        throw new IllegalStateException(ex);
      }
      if(value == null || Boolean.FALSE.equals(value)) continue;
      if(value instanceof Boolean) {
        sb.append(ind).append(name).append('\n');
      } else if(value instanceof final Node[] nodes) {
        sb.append(ind).append(name).append('[').append(nodes.length).append("]\n");
        for(final Node n : nodes) dump(n, ind + "  ", sb);
      } else {
        sb.append(ind).append(name).append(":\n");
        dump((Node) value, ind + "  ", sb);
      }
    }
  }
}
