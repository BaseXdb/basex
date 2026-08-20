package org.basex.query.value.node;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

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
