package org.basex.query.parse.syntax;

/**
 * Stack of the syntax tree builder.
 *
 * @param nodes nodes
 * @param syms symbols of the nodes
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public record SyntaxStack(Node[] nodes, int[] syms) { }
