package org.basex.query.value.node;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import javax.xml.*;
import javax.xml.parsers.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.w3c.dom.*;

/**
 * Tests for constructed nodes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FNodeTest extends SandboxTest {
  /** Identity of text children. */
  @Test public void identity() {
    query("<a>A</a> ! (text() is text())", true);
    query("<a b='c'>A</a> ! (text() is text())", true);
    query("<a>A</a> ! count(text() | text())", 1);
    query("let $a := element a { 'A' } return $a/text() is $a/text()", true);
    query("let $e := <b>x</b>, $a := element a { $e } return $a/b/text() is $e/text()", false);
  }

  /** Document order within a tree. */
  @Test public void documentOrder() {
    query("<a>A</a> ! (. << text())", true);
    query("let $a := <a><b>A</b><c/></a> return $a/b/text() << $a/c", true);
    query("let $a := <a><b>x</b><c>y</c></a> return $a/b/text() << $a/c/text()", true);
    query("let $a := <a><b>x</b><c>y</c></a> return string-join(reverse($a//text()))", "yx");
  }

  /** Document order of attributes and text children. */
  @Test public void attributeOrder() {
    query("let $a := <a b='c'>A</a> return $a/@b << $a/text()", true);
    query("let $a := <a b='1' c='2'>A</a> return $a/@c << $a/text()", true);
    query("let $x := <x><a b='c'>A</a><c/></x> return $x/a/@b << $x/c", true);
    query("let $a := <a b='c'>A</a> return count($a/@b | $a/text())", 2);
    query("let $a := <a b='c'>A</a> return count(($a/@b | $a/text()) except $a/text())", 1);
  }

  /** Nodes of distinct trees are not interleaved. */
  @Test public void treeOrder() {
    query("let $x := <a>A</a>, $y := <b/> return ($x << $y) eq ($x/text() << $y)", true);
  }

  /** String values. */
  @Test public void stringValue() {
    query("string(<a>A</a>)", "A");
    query("string(<a b='c'>A</a>)", "A");
    query("data(<a>A</a>)", "A");
    query("string(<a>{ 'A' }{ 'B' }</a>)", "AB");
    query("count(<a>{ 'A' }{ 'B' }</a>/text())", 1);
    query("count(<a>A</a>/node())", 1);
  }

  /** Serialization. */
  @Test public void serialization() {
    query("<a>A</a>", "<a>A</a>");
    query("<a b='c'>A</a>", "<a b=\"c\">A</a>");
    query("<a>{ '' }</a>", "<a/>");
    query("<a><b>A</b></a>", "<a><b>A</b></a>");
    query("serialize(<a>A</a>, { 'method': 'adaptive' })", "<a>A</a>");
  }

  /** Copies of elements with a single text child. */
  @Test public void copies() {
    query("let $e := <b>x</b> return element a { $e }", "<a><b>x</b></a>");
    query("let $e := <b c='d'>x</b> return element a { $e }", "<a><b c=\"d\">x</b></a>");
    query("copy $c := <a>A</a> modify insert node <b/> into $c return $c", "<a>A<b/></a>");
    query("deep-equal(<a>A</a>, <a>A</a>)", true);
    query("deep-equal(<a>A</a>, <a>B</a>)", false);
  }

  /** Navigation from a text child. */
  @Test public void navigation() {
    query("<a>A</a>/text()/.. ! name()", "a");
    query("string(<a><b>A</b></a>//text())", "A");
    query("string-join(<a><b>x</b></a>/b/text()/ancestor::*/name())", "ab");
    query("let $a := <a><b>x</b><c>y</c></a> return string($a/c/preceding-sibling::b/text())", "x");
  }

  /** A single text child is stored as a value and materialized on demand. */
  @Test public void collapsed() {
    final FElem elem = element("a", new FTxt("A"));
    assertEquals("A", Token.string(elem.textValue()));

    final GNode text = elem.childIter().next();
    assertEquals(elem.id + 1, text.id, "text child was not materialized on demand");
    assertSame(text, elem.childIter().next(), "materialized text child was not cached");
    assertEquals("A", Token.string(text.string()));
  }

  /** Attributes do not prevent the collapse; other children do. */
  @Test public void collapsedContent() {
    assertTrue(collapses(element("a", new FAttr(new QNm("b"), Token.token("c")), new FTxt("A"))),
        "attributes must not prevent the collapse");
    assertFalse(collapses(element("a", new FTxt("A"), new FTxt("B"))),
        "two children must not be collapsed");
    assertFalse(collapses(element("a", element("b", new FTxt("A")))),
        "element child must not be collapsed");
    assertFalse(collapses(element("a")), "empty element must not be collapsed");
  }

  /** An empty text child survives materialization.
   * @throws QueryException query exception */
  @Test public void emptyTextChild() throws QueryException {
    final FElem elem = element("a", new FTxt(Token.EMPTY));
    assertEquals(1, elem.materialize(data -> false, null, null).childIter().size(),
        "empty text child was dropped");
    assertTrue(collapses(elem), "empty text child was not collapsed");
  }

  /** Concurrent navigation yields one and the same text node.
   * @throws Exception exception */
  @Test public void contention() throws Exception {
    final int elems = 10000, threads = 4;
    final FElem[] fragments = new FElem[elems];
    for(int e = 0; e < elems; e++) fragments[e] = element("a", new FTxt("A"));

    final CyclicBarrier barrier = new CyclicBarrier(threads);
    final GNode[][] seen = new GNode[threads][elems];
    final List<Callable<?>> tasks = new ArrayList<>(threads);
    for(int t = 0; t < threads; t++) {
      final GNode[] nodes = seen[t];
      tasks.add(() -> {
        barrier.await();
        for(int e = 0; e < elems; e++) nodes[e] = fragments[e].childIter().next();
        return null;
      });
    }
    parallel(tasks);

    for(int e = 0; e < elems; e++) {
      for(int t = 1; t < threads; t++) {
        assertSame(seen[0][e], seen[t][e], "threads saw different text nodes");
      }
    }
  }

  /** Namespaces of elements imported from a DOM without namespace declarations.
   * @throws Exception exception */
  @Test public void domNamespaces() throws Exception {
    final Document doc = newDocument();
    final Element root = doc.createElementNS("A", "a:r");
    final Element x1 = doc.createElementNS("B", "b:x"), x2 = doc.createElementNS("B", "b:x");
    root.appendChild(x1);
    root.appendChild(x2);
    x1.appendChild(doc.createElementNS("C", "c:y"));

    final XNode elem = FElem.build(root, null).finish();
    assertEquals("A", Token.string(elem.uri(Token.token("a"))));
    for(final GNode child : elem.childIter()) {
      assertEquals("B", Token.string(((XNode) child).nsScope(null).value(Token.token("b"))));
    }
    final XNode y = (XNode) elem.childIter().next().childIter().next();
    assertEquals("C", Token.string(y.uri(Token.token("c"))));
    assertEquals("B", Token.string(y.uri(Token.token("b"))));
  }

  /** Namespaces of an element imported from a DOM with inherited and redeclared prefixes.
   * @throws Exception exception */
  @Test public void domAncestorNamespaces() throws Exception {
    final Document doc = newDocument();
    final String xmlns = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    final Element root = doc.createElementNS("A", "a:r");
    root.setAttributeNS(xmlns, "xmlns:p", "P1");
    root.setAttributeNS(xmlns, "xmlns:q", "Q");
    final Element x = doc.createElementNS("B", "b:x");
    x.setAttributeNS(xmlns, "xmlns:p", "P2");
    root.appendChild(x);
    x.appendChild(doc.createElementNS("A", "a:y"));

    final XNode elem = FElem.build(x, null).finish();
    final Atts scope = elem.nsScope(null);
    assertEquals("A", Token.string(scope.value(Token.token("a"))));
    assertEquals("B", Token.string(scope.value(Token.token("b"))));
    assertEquals("P2", Token.string(scope.value(Token.token("p"))));
    assertEquals("Q", Token.string(scope.value(Token.token("q"))));
    final XNode y = (XNode) elem.childIter().next();
    assertEquals("P2", Token.string(y.uri(Token.token("p"))));
    assertEquals("Q", Token.string(y.uri(Token.token("q"))));
  }

  /** Namespaces of a DOM element whose declarations conflict with its name or bind xml.
   * @throws Exception exception */
  @Test public void domConflictingNamespaces() throws Exception {
    final Document doc = newDocument();
    final String xmlns = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    final Element x = doc.createElementNS("B", "p:x");
    x.setAttributeNS(xmlns, "xmlns:p", "P2");
    x.setAttributeNS(xmlns, "xmlns:xml", XMLConstants.XML_NS_URI);
    final Element root = doc.createElementNS("A", "a:r");
    root.setAttributeNS(xmlns, "xmlns:xml", XMLConstants.XML_NS_URI);
    root.appendChild(x);

    final XNode elem = FElem.build(x, null).finish();
    // the binding of the element name takes precedence
    final Atts ns = elem.namespaces();
    assertEquals(2, ns.size());
    assertEquals("B", Token.string(ns.value(Token.token("p"))));
    assertEquals("A", Token.string(ns.value(Token.token("a"))));
    assertFalse(ns.contains(Token.XML));
    assertEquals("B", Token.string(elem.nsScope(null).value(Token.token("p"))));
    assertNull(elem.attributeIter().next());
  }

  /**
   * Creates an empty namespace-aware DOM document.
   * @return document
   * @throws Exception exception
   */
  private static Document newDocument() throws Exception {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    return dbf.newDocumentBuilder().newDocument();
  }

  /**
   * Creates an element with the specified content.
   * @param name element name
   * @param content attributes and children
   * @return element
   */
  private static FElem element(final String name, final GNode... content) {
    final FBuilder builder = FElem.build(new QNm(name));
    for(final GNode node : content) builder.node(node);
    return (FElem) builder.finish();
  }

  /**
   * Indicates if an element stores its single text child as a value.
   * @param elem element
   * @return result of check
   */
  private static boolean collapses(final FElem elem) {
    final GNode child = elem.childIter().next();
    return child != null && child.id == elem.id + 1;
  }
}
