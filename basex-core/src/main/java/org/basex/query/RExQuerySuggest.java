package org.basex.query;

import java.lang.reflect.*;
import java.util.*;

import org.basex.data.*;
import org.basex.query.expr.path.*;
import org.basex.query.parse.gen.*;
import org.basex.query.parse.syntax.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Gives suggestions for completing a path: the query is parsed up to the name that is entered.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class RExQuerySuggest {
  /** Literals, which may be operands of a path. */
  private static final BitSet LITERALS = Sym.set(Sym.INTEGER_LITERAL, Sym.HEX_INTEGER_LITERAL,
      Sym.BINARY_INTEGER_LITERAL, Sym.DECIMAL_LITERAL, Sym.DOUBLE_LITERAL, Sym.STRING_LITERAL);
  /** Names, which are not reduced if whitespace follows. */
  private static final BitSet NAMES = Sym.set(Sym.NC_NAME, Sym.Q_NAME,
      Sym.URI_QUALIFIED_NAME);

  /** Code points of the query. */
  private final int[] input;
  /** Path suggestions. */
  private final PathSuggestions suggestions;
  /** Nodes of the syntax stack. */
  private Node[] nodes;
  /** Symbols of the nodes. */
  private int[] syms;

  /**
   * Constructor.
   * @param input code points of the query
   * @param suggestions path suggestions
   */
  private RExQuerySuggest(final int[] input, final PathSuggestions suggestions) {
    this.input = input;
    this.suggestions = suggestions;
  }

  /**
   * Collects suggestions for completing a path.
   * @param query query string
   * @param data data reference
   * @return list of suggestions, followed by valid input string
   */
  static StringList suggest(final String query, final Data data) {
    final int[] input = query.codePoints().toArray();
    final PathSuggestions suggestions = new PathSuggestions(data);
    final int valid = new RExQuerySuggest(input, suggestions).walk();
    return suggestions.names().add(new String(input, 0, valid));
  }

  /**
   * Follows the path at the end of the query.
   * @return start of the name that is entered, or the end of the query
   */
  private int walk() {
    final int length = input.length;
    // the name that is entered is not parsed
    int cut = length;
    while(cut > 0) {
      final int cp = input[cut - 1];
      // a colon belongs to a name, unless it is part of an axis
      if(!XMLToken.isNCChar(cp) && cp != '*' && !(cp == ':' && (cut == 1 || input[cut - 2] != ':')
          && (cut == length || input[cut] != ':'))) break;
      cut--;
    }
    final SyntaxStack stack = SyntaxParser.parseStart(input, cut);
    if(stack == null) return length;
    nodes = stack.nodes();
    syms = stack.syms();

    // a keyword and a parenthesis after a slash or an axis start a kind test
    int start = cut, last = nodes.length - 1;
    if(last > 1 && nodes[last] instanceof Marker && syms[last] == Sym.LPAREN &&
        nodes[last - 1] instanceof final Terminal t &&
        (slash(nodes[last - 2]) || nodes[last - 2] instanceof Nodes.Axis)) {
      if(cut == length) start = t.begin;
      last -= 2;
    }
    if(length - start == 2 && input[start] == '.' && input[start + 1] == '.') return length;
    final boolean name = start < length &&
        (XMLToken.isNCStartChar(input[start]) || input[start] == '*');
    final Node before = last < 0 ? null : nodes[last];
    final boolean attribute = before instanceof final Terminal t && t.sym == Sym.AT_SIGN ||
        before instanceof final Nodes.Axis axis && axis.isAttribute();
    // a name cannot follow a step or another operand
    if(before instanceof final Terminal t && NAMES.get(t.sym) || name && before != null &&
        !(before instanceof Marker || before instanceof Terminal ||
        before instanceof Nodes.Axis)) return length;

    path(last);
    if(!name && !attribute) return length;
    final TokenBuilder tb = new TokenBuilder();
    if(attribute) tb.add('@');
    if(name) tb.add(new String(input, start, length - start).replace("*:", ""));
    suggestions.test(tb.finish(), false);
    return attribute ? before.begin() : start;
  }

  /**
   * Follows the path that ends at an entry, after the paths of the predicates around it.
   * @param last index of the last entry of the path
   */
  private void path(final int last) {
    int first = last + 1;
    while(first > 0 && inPath(first - 1, first <= last && slash(nodes[first]))) first--;
    int open = first - 1;
    while(open >= 0 && !predicate(open)) open--;
    if(open >= 0) {
      path(open - 1);
      suggestions.predicate(true);
    }
    suggestions.init();
    for(int i = first; i <= last; i++) follow(nodes[i]);
  }

  /**
   * Checks if an entry belongs to a path.
   * @param index index of the entry
   * @param slash the entry is followed by a slash
   * @return result of check
   */
  private boolean inPath(final int index, final boolean slash) {
    final Node node = nodes[index];
    if(node instanceof Marker) return false;
    if(node instanceof final Terminal t) {
      return slash(t) || t.sym == Sym.AT_SIGN || slash && LITERALS.get(t.sym);
    }
    // an operand of a slash (the first entry is the prolog)
    return step(node) || slash && index > 0;
  }

  /**
   * Checks if an entry is the opening bracket of the predicate of a step.
   * @param index index of the entry
   * @return result of check
   */
  private boolean predicate(final int index) {
    if(!(nodes[index] instanceof Marker) || syms[index] != Sym.LBRACKET) return false;
    int i = index - 1;
    while(i >= 0 && nodes[i] instanceof Nodes.Predicate) i--;
    return i >= 0 && (nodes[i] instanceof Nodes.AbbreviatedStep ||
        nodes[i] instanceof Nodes.FullStep);
  }

  /**
   * Checks if a node is a slash.
   * @param node node
   * @return result of check
   */
  private static boolean slash(final Node node) {
    return node instanceof final Terminal t && (t.sym == Sym.SLASH || t.sym == Sym.SLASH2);
  }

  /**
   * Checks if a node is a step, a path or a predicate.
   * @param node node
   * @return result of check
   */
  private static boolean step(final Node node) {
    return node instanceof Nodes.AxisStep || node instanceof Nodes.AbbreviatedStep ||
        node instanceof Nodes.FullStep || node instanceof Nodes.Predicate ||
        node instanceof Nodes.RelativePathExpr || node instanceof Nodes.AbsolutePathExpr ||
        node instanceof Nodes.Axis || node instanceof Nodes.ContextValueRef;
  }

  /**
   * Follows a slash, a step, a path, a predicate or an operand.
   * @param node node
   */
  private void follow(final Node node) {
    if(node instanceof final Terminal t) {
      if(t.sym == Sym.SLASH) suggestions.axis(Axis.CHILD);
      else if(t.sym == Sym.SLASH2) suggestions.axis(Axis.DESCENDANT);
    } else if(node instanceof final Nodes.AbsolutePathExpr absolute) {
      suggestions.axis(absolute.isSlash2() ? Axis.DESCENDANT : Axis.CHILD);
      if(absolute.relativePathExpr() != null) follow(absolute.relativePathExpr());
    } else if(node instanceof final Nodes.RelativePathExpr relative) {
      for(final Node child : relative.children()) follow(child);
    } else if(node instanceof final Nodes.AxisStep axis) {
      follow(axis.fullStep() != null ? axis.fullStep() : axis.abbreviatedStep());
      for(final Node predicate : axis.children()) follow(predicate);
    } else if(node instanceof final Nodes.FullStep full) {
      tests(full.nodeTest(), !full.axis().isAttribute());
    } else if(node instanceof final Nodes.AbbreviatedStep abbreviated) {
      if(abbreviated.isDot2()) suggestions.test(Token.token("node()"), true);
      else tests(abbreviated.isAtSign() ? abbreviated.nodeTest() : abbreviated.simpleNodeTest(),
          !abbreviated.isAtSign());
    } else if(node instanceof Nodes.Predicate) {
      suggestions.predicate(true);
      suggestions.predicate(false);
    } else if(!step(node)) {
      // an operand may contain a path
      final Node inner = path(node);
      if(inner != null) {
        suggestions.init();
        follow(inner);
      }
    }
  }

  /**
   * Applies a node test: a name, or the names of a union or of an element test.
   * @param test node test
   * @param element element flag
   */
  private void tests(final Node test, final boolean element) {
    // dynamic node tests are no names
    if(test.sym() == Sym.EnclosedExpr) return;
    if(test instanceof final Nodes.UnionNodeTest union) {
      for(final Node simple : union.children()) tests(simple, element);
    } else if(test instanceof final Nodes.ElementNodeType el && el.nameTestUnion() != null) {
      for(final Node name : el.nameTestUnion().children()) tests(name, true);
    } else {
      final TokenBuilder tb = new TokenBuilder();
      if(!element) tb.add('@');
      tb.add(new String(input, test.begin(), test.end() - test.begin()).replace("*:", ""));
      suggestions.test(tb.finish(), true);
    }
  }

  /**
   * Finds the outermost path or step on the rightmost branch of a syntax tree.
   * @param root root node
   * @return path or step, or {@code null}
   */
  private static Node path(final Node root) {
    for(Node node = root; node != null; node = lastChild(node)) {
      if(node instanceof Nodes.AbsolutePathExpr || node instanceof Nodes.RelativePathExpr ||
          node instanceof Nodes.AxisStep) return node;
    }
    return null;
  }

  /**
   * Returns the child that ends last.
   * @param node node
   * @return child, or {@code null} if there is none
   */
  private static Node lastChild(final Node node) {
    if(!node.getClass().isRecord()) return null;
    Node last = null;
    for(final RecordComponent rc : node.getClass().getRecordComponents()) {
      final Object value = Reflect.invoke(rc.getAccessor(), node);
      final Node child = value instanceof final Node single ? single :
        value instanceof final Node[] many && many.length > 0 ? many[many.length - 1] : null;
      if(child != null && (last == null || child.end() >= last.end())) last = child;
    }
    return last;
  }
}
