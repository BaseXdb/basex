package org.basex.query.parse;

import java.util.*;

/**
 * Policies for shaping the syntax tree, which are applied by its builder. Symbols are given by
 * their grammar names; terminals by their text, without quotes.
 * <ul>
 * <li>{@link #NAMES}: transparent nonterminals. If such a node has exactly one retained child,
 *   the node is replaced by its child. The list is derived from the grammar (see
 *   {@code GrammarXml} in the tests) and reviewed by hand.</li>
 * <li>{@link #LISTS}: left-recursive lists, which are flattened.</li>
 * <li>{@link #DELIMITERS} and {@link #PARENT_DELIMITERS}: terminals that are implied by the kind
 *   of the enclosing node and are removed. All other terminals are kept: they are operators,
 *   keywords or values, or they tell alternatives of a node apart.</li>
 * <li>{@link #VANISH}: nonterminals that consist of removed delimiters only, and are removed
 *   entirely.</li>
 * </ul>
 * {@code FoldPolicyTest} compares the grammar productions of the nonterminals above with the
 * signatures recorded when the policy was last reviewed.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FoldPolicy {
  /** Transparent nonterminals. */
  public static final Set<String> NAMES = Set.of(
    // expression precedence ladder
    "Expr", "ExprSingle", "OrExpr", "AndExpr", "ComparisonExpr", "OtherwiseExpr", "FTContainsExpr",
    "StringConcatExpr", "RangeExpr", "AdditiveExpr", "MultiplicativeExpr", "UnionExpr",
    "IntersectExceptExpr", "ButWithExpr", "InstanceofExpr", "TreatExpr", "CoerceExpr",
    "CastableExpr", "CastExpr", "PipelineExpr", "ArrowExpr", "TransformWithExpr", "UnaryExpr",
    "ValueExpr", "SimpleMapExpr", "PathExpr", "RelativePathExpr", "StepExpr", "PostfixExpr",
    "PrimaryExpr",
    // full-text selections
    "FTSelection", "FTOr", "FTAnd", "FTMildNot", "FTUnaryNot", "FTPrimaryWithOptions",
    // literals
    "Literal", "NumericLiteral",
    // content of direct constructors
    "DirElemContent", "CommonContent", "QuotAttrValueContent", "AposAttrValueContent",
    // FLWOR clauses and bindings, setters and imports
    "InitialClause", "IntermediateClause", "ForBinding", "LetBinding", "Setter", "Import");

  /**
   * Left-recursive list nonterminals. The grammar defines these as {@code L ::= E | L ',' E},
   * which would nest the elements of a list. They are flattened into a single node.
   * {@code FoldPolicyTest} checks that all left-recursive productions are listed.
   */
  public static final Set<String> LISTS = Set.of("PositionalArguments");

  /**
   * Delimiters that are removed wherever they occur. They are brackets, separators and the
   * opening and closing tokens of constructors. In all productions in which they appear, they
   * are implied by the kind of the node, or by the children that are present.
   */
  public static final Set<String> DELIMITERS = Set.of(
    "(", ")", "{", "}", "[", "]", ",", ";", "$", ":=", "::", ":", "#", "%", ".",
    "=>", "=!>", "=?>",
    // direct constructors, string constructors and templates, pragmas
    "</", "/>", "\"", "'", "<?", "?>", "<!--", "-->", "<![CDATA[", "]]>", "(#", "#)", "``[", "]``",
    "`");

  /**
   * Terminals that are removed below some nonterminals only, as they are operators or values
   * elsewhere: {@code =}, {@code <} and {@code >} in comparisons, and {@code ?} as an occurrence
   * indicator. Keys are nonterminals.
   */
  public static final Map<String, Set<String>> PARENT_DELIMITERS = Map.ofEntries(
    Map.entry("ModuleDecl", Set.of("=")),
    Map.entry("NamespaceDecl", Set.of("=")),
    Map.entry("ModuleImport", Set.of("=")),
    Map.entry("SchemaPrefix", Set.of("=")),
    Map.entry("DecimalFormatDecl", Set.of("=")),
    Map.entry("DirAttributeList", Set.of("=")),
    Map.entry("DirElemConstructor", Set.of("<", ">")),
    Map.entry("Lookup", Set.of("?")),
    Map.entry("ArgumentPlaceholder", Set.of("?")));

  /** Nonterminals that are removed if only removed delimiters are left. */
  public static final Set<String> VANISH = Set.of("Separator");

  /** Private constructor. */
  private FoldPolicy() { }
}
