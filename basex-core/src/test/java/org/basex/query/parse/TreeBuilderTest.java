package org.basex.query.parse;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.query.parse.gen.*;
import org.basex.query.parse.syntax.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the construction of the syntax tree: merged content, lists, and the
 * boundaries between text and enclosed expressions. Timings are not asserted: see
 * {@code StressBench}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class TreeBuilderTest {
  /**
   * Parses a query and returns the body of the main module.
   * @param query query
   * @return query body
   * @throws Exception exception
   */
  private static Node body(final String query) throws Exception {
    return ((Nodes.Module) SyntaxParser.parse(query).root()).mainModule().queryBody();
  }

  /**
   * Returns the content of the element constructor of a query.
   * @param query query
   * @return content
   * @throws Exception exception
   */
  private static Node[] content(final String query) throws Exception {
    return ((Nodes.DirElemConstructor) body(query)).children();
  }

  /**
   * Returns the values of the attributes of the element constructor of a query.
   * @param query query
   * @return attribute values
   * @throws Exception exception
   */
  private static Node[] attributes(final String query) throws Exception {
    return ((Nodes.DirElemConstructor) body(query)).dirAttributeList().children();
  }

  /**
   * Checks the symbols of nodes.
   * @param nodes nodes
   * @param syms expected symbols
   */
  private static void syms(final Node[] nodes, final int... syms) {
    final int[] actual = new int[nodes.length];
    for(int n = 0; n < actual.length; n++) actual[n] = nodes[n].sym();
    assertArrayEquals(syms, actual);
  }

  /**
   * Returns the items of the positional arguments of a function call.
   * @param node function call
   * @return items
   */
  private static Node[] arguments(final Node node) {
    return ((Nodes.FunctionCall) node).argumentList().positionalArguments().children();
  }

  /**
   * A long run of content characters is a single terminal.
   * @throws Exception exception
   */
  @Test public void longContent() throws Exception {
    final String text = "x".repeat(200_000);
    for(final String query : new String[] { "<a>" + text + "</a>", "<a>" + text + "{1}</a>" }) {
      final Node[] content = content(query);
      assertEquals(query.contains("{1}") ? 2 : 1, content.length);
      assertEquals(Sym.ELEMENT_CONTENT_CHAR, content[0].sym());
      assertEquals(3, content[0].begin());
      assertEquals(3 + text.length(), content[0].end());
    }
    for(final String quote : new String[] { "'", "\"" }) {
      final Node[] parts = ((Nodes.DirAttributeValue) attributes("<a b=" + quote + text + quote +
          "/>")[1]).children();
      assertEquals(1, parts.length);
      assertEquals(text.length(), parts[0].end() - parts[0].begin());
    }
  }

  /**
   * Content characters are not merged across entities, escapes, enclosed expressions and
   * nested constructors.
   * @throws Exception exception
   */
  @Test public void contentBoundaries() throws Exception {
    syms(content("<a>x&amp;y</a>"), Sym.ELEMENT_CONTENT_CHAR, Sym.PREDEFINED_ENTITY_REF,
        Sym.ELEMENT_CONTENT_CHAR);
    syms(content("<a>x{1}y</a>"), Sym.ELEMENT_CONTENT_CHAR, Sym.EnclosedExpr,
        Sym.ELEMENT_CONTENT_CHAR);
    syms(content("<a>{1}{2}</a>"), Sym.EnclosedExpr, Sym.EnclosedExpr);
    syms(content("<a>x<b/>y</a>"), Sym.ELEMENT_CONTENT_CHAR, Sym.DirElemConstructor,
        Sym.ELEMENT_CONTENT_CHAR);
    syms(content("<a>x{{y}}z</a>"), Sym.ELEMENT_CONTENT_CHAR, Sym.LBRACE2,
        Sym.ELEMENT_CONTENT_CHAR, Sym.RBRACE2, Sym.ELEMENT_CONTENT_CHAR);
    syms(content("<a>x<![CDATA[y]]>z</a>"), Sym.ELEMENT_CONTENT_CHAR, Sym.CDataSection,
        Sym.ELEMENT_CONTENT_CHAR);

    final String query = "<a>ab{1}cd{{ef</a>";
    final Node[] content = content(query);
    syms(content, Sym.ELEMENT_CONTENT_CHAR, Sym.EnclosedExpr, Sym.ELEMENT_CONTENT_CHAR,
        Sym.LBRACE2, Sym.ELEMENT_CONTENT_CHAR);
    final int[] input = query.codePoints().toArray();
    assertEquals("ab", ((Terminal) content[0]).text(input));
    assertEquals("cd", ((Terminal) content[2]).text(input));
    assertEquals("ef", ((Terminal) content[4]).text(input));
  }

  /**
   * The boundaries of attribute values.
   * @throws Exception exception
   */
  @Test public void attributeValues() throws Exception {
    final Node[] attributes = attributes("<a b=\"x{1}y\"\"z&lt;w\" c='p''q'/>");
    assertEquals(4, attributes.length);
    syms(((Nodes.DirAttributeValue) attributes[1]).children(), Sym.QUOT_ATTR_CONTENT_CHAR,
        Sym.EnclosedExpr, Sym.QUOT_ATTR_CONTENT_CHAR, Sym.ESCAPE_QUOT,
        Sym.QUOT_ATTR_CONTENT_CHAR, Sym.PREDEFINED_ENTITY_REF, Sym.QUOT_ATTR_CONTENT_CHAR);
    syms(((Nodes.DirAttributeValue) attributes[3]).children(), Sym.APOS_ATTR_CONTENT_CHAR,
        Sym.ESCAPE_APOS, Sym.APOS_ATTR_CONTENT_CHAR);
  }

  /**
   * Nested argument lists keep their own items.
   * @throws Exception exception
   */
  @Test public void nestedArguments() throws Exception {
    final Node call = body("f(g(1, 2), h(3, (4, 5)), 6)");
    final Node[] arguments = arguments(call);
    syms(arguments, Sym.FunctionCall, Sym.FunctionCall, Sym.INTEGER_LITERAL);
    assertEquals(2, arguments(arguments[0]).length);
    syms(arguments(arguments[1]), Sym.INTEGER_LITERAL, Sym.ParenthesizedExpr);

    // deeply nested calls
    final int depth = 200;
    Node node = body("f(".repeat(depth) + "1" + ", 2)".repeat(depth));
    for(int d = 0; d < depth; d++) {
      final Node[] items = arguments(node);
      assertEquals(2, items.length);
      node = items[0];
    }
    assertEquals(Sym.INTEGER_LITERAL, node.sym());
  }

  /**
   * Long argument lists, and calls that have positional and keyword arguments.
   * @throws Exception exception
   */
  @Test public void longArguments() throws Exception {
    final int size = 20_000;
    final Node[] arguments = arguments(body("f(" + "1,".repeat(size - 1) + "1)"));
    assertEquals(size, arguments.length);
    for(int a = 1; a < size; a++) assertTrue(arguments[a - 1].end() < arguments[a].begin());

    final Nodes.ArgumentList list = ((Nodes.FunctionCall) body("f(1, 2, a := 3, b := 4)")).
        argumentList();
    assertEquals(2, list.positionalArguments().children().length);
    assertEquals(2, list.keywordArguments().children().length);

    // the list of a dynamic call is not shared with the list of the function call
    final Nodes.DynamicFunctionCall dynamic = (Nodes.DynamicFunctionCall) body("f(1, 2)(3, 4, 5)");
    assertEquals(3, dynamic.positionalArgumentList().positionalArguments().children().length);
    assertEquals(2, arguments(dynamic.postfixExpr()).length);
  }
}
