package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests namespaces.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class NamespaceTest extends SandboxTest {
  /** Test documents. */
  private static final String[][] DOCS = {
    { "d1", "<x/>" },
    { "d2", "<x xmlns='xx'/>" },
    { "d3", "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>" },
    { "d4", "<a:x xmlns:a='aa'><a:y xmlns:b='bb'/></a:x>" },
    { "d5", "<a:x xmlns:a='aa'/>" },
    { "d6", "<a:x xmlns='xx' xmlns:a='aa'><a:y xmlns:b='bb'/></a:x>" },
    { "d7", "<x xmlns='xx'><y/></x>" },
    { "d8", "<a><b xmlns='B'/><c/></a>" },
    { "d9", "<a xmlns='A'><b><c/><d xmlns='D'/></b><e/></a>" },
    { "d10", "<a xmlns='A'><b><c/><d xmlns='D'><g xmlns='G'/></d></b><e/></a>"},
    { "d11", "<a xmlns='A'><b xmlns:ns1='AA'><d/></b><c xmlns:ns1='AA'>" +
    "<d/></c></a>" },
    { "d12", "<a><b/><c xmlns='B'/></a>" },
    { "d13", "<a><b xmlns='A'/></a>" },
    { "d14", "<a xmlns='A'><b xmlns='B'/><c xmlns='C'/></a>" },
    { "d15", "<a xmlns='A'><b xmlns='B'/><c xmlns='C'><d xmlns='D'/></c>" +
    "<e xmlns='E'/></a>" },
    { "d16", "<a><b/></a>" },
    { "d17", "<ns:a xmlns:ns='NS'><b/></ns:a>" },
    { "d18", "<n xmlns:ns='ns'><a/></n>"},
    { "d19", "<x:n xmlns:x='X'/>"},
    { "d20", "<x:a xmlns:x='A'><x:b xmlns:x='B'/><x:c/></x:a>"},
    { "d21", "<n><a xmlns:p1='u1'><b xmlns:p2='u2'/></a><c/></n>"}
  };

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   */
  @Test public void insertIntoShiftPreValues() {
    create(12);
    query("insert node <b xmlns:ns='A'/> into db:open('d12')/*:a/*:b");
    assertEquals(NL +
        "  Pre[3] xmlns:ns=\"A\"" + NL +
        "  Pre[4] xmlns=\"B\"",
        context.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   */
  @Test public void insertIntoShiftPreValues2() {
    create(13);
    query("insert node <c/> as first into db:open('d13')/a");
    assertEquals(NL +
        "  Pre[3] xmlns=\"A\"",
        context.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   */
  @Test public void insertIntoShiftPreValues3() {
    create(14);
    query("insert node <n xmlns='D'/> into db:open('d14')/*:a/*:b");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\"" + NL +
        "    Pre[2] xmlns=\"B\"" + NL +
        "      Pre[3] xmlns=\"D\"" + NL +
        "    Pre[4] xmlns=\"C\"",
        context.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test public void deleteShiftPreValues() {
    create(12);
    query("delete node db:open('d12')/a/b");
    assertEquals(NL +
        "  Pre[2] xmlns=\"B\"",
        context.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test public void deleteShiftPreValues2() {
    create(14);
    query("delete node db:open('d14')/*:a/*:b");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\"" + NL +
        "    Pre[2] xmlns=\"C\"",
        context.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test public void deleteShiftPreValues3() {
    create(15);
    query("delete node db:open('d15')/*:a/*:c");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\"" + NL +
        "    Pre[2] xmlns=\"B\"" + NL +
        "    Pre[3] xmlns=\"E\"",
        context.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test public void deleteShiftPreValues4() {
    create(16);
    query("delete node db:open('d16')/a/b");
    assertTrue(context.data().nspaces.toString().isEmpty());
  }

  /**
   * Inserts an attribute with namespace.
   */
  @Test public void insertAttributeWithNs() {
    create(1);
    query("insert node attribute { QName('ns', 'pref:local') } { } into /*");
    final Data data = context.data();
    assertFalse(data.nsFlag(0));
    assertTrue(data.nsFlag(1));
    assertFalse(data.nsFlag(2));
    assertEquals(0, data.uriId(1, data.kind(1)));
    assertEquals(1, data.uriId(2, data.kind(2)));
    assertEquals("ns", string(data.nspaces.uri(1)));
  }

  /**
   * Tests for correct namespace hierarchy, esp. if namespace nodes
   * on the following axis of an insert/delete operation are
   * updated correctly.
   */
  @Test public void delete1() {
    create(11);
    query("delete node db:open('d11')/*:a/*:b",
        "db:open('d11')/*:a",
        "<a xmlns='A'><c xmlns:ns1='AA'><d/></c></a>");
  }

  /**
   * Tests if a namespace node is deleted.
   */
  @Test public void delete2() {
    create(21);
    query("delete node //b");
    assertEquals(NL +
        "  Pre[2] xmlns:p1=\"u1\"",
        context.data().nspaces.toString());
  }

  /** Test query. */
  @Test public void copy1() {
    query(
        "copy $c := <x:a xmlns:x='xx'><b/></x:a>/b modify () return $c",
        "<b xmlns:x='xx'/>");
  }

  /**
   * Detects corrupt namespace hierarchy.
   */
  @Test public void copy2() {
    create(4);
    query(
        "declare namespace a='aa';" +
        "copy $c:=db:open('d4') modify () return $c//a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects missing prefix declaration.
   */
  @Test public void copy3() {
    create(4);
    query(
        "declare namespace a='aa';" +
        "copy $c:=db:open('d4')//a:y modify () return $c",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects duplicate namespace declaration in MemData instance.
   */
  @Test public void copy4() {
    query(
        "copy $c := <a xmlns='test'><b><c/></b><d/></a> modify () return $c",
        "<a xmlns='test'><b><c/></b><d/></a>");
  }

  /**
   * Detects bogus namespace after insert.
   */
  @Test public void bogusDetector() {
    create(1);
    query(
        "insert node <a xmlns='test'><b><c/></b><d/></a> into db:open('d1')/x",
        "declare namespace na = 'test';db:open('d1')/x/na:a",
        "<a xmlns='test'><b><c/></b><d/></a>");
  }

  /**
   * Detects empty default namespace in serializer.
   */
  @Test public void emptyDefaultNamespace() {
    query("<ns:x xmlns:ns='X'><y/></ns:x>",
        "<ns:x xmlns:ns='X'><y/></ns:x>");
  }

  /**
   * Detects duplicate default namespace in serializer.
   */
  @Test public void duplicateDefaultNamespace() {
    query("<ns:x xmlns:ns='X'><y/></ns:x>",
        "<ns:x xmlns:ns='X'><y/></ns:x>");
  }

  /**
   * Detects malformed namespace hierarchy.
   */
  @Test public void nsHierarchy() {
    create(9);
    query("insert node <f xmlns='F'/> into db:open('d9')//*:e");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\"" + NL +
        "    Pre[4] xmlns=\"D\"" + NL +
        "    Pre[6] xmlns=\"F\"",
        context.data().nspaces.toString());
  }

  /**
   * Detects malformed namespace hierarchy.
   */
  @Test public void nsHierarchy2() {
    create(10);
    query("insert node <f xmlns='F'/> into db:open('d10')//*:e");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\"" + NL +
        "    Pre[4] xmlns=\"D\"" + NL +
        "      Pre[5] xmlns=\"G\"" + NL +
        "    Pre[7] xmlns=\"F\"",
        context.data().nspaces.toString());
  }

  /**
   * Detects malformed namespace hierarchy inserting an element.
   */
  @Test public void nsHierarchy3() {
    query(transform(
        "<a xmlns='x'/>",
        "insert node <a xmlns='y'/> into $input"),
        "<a xmlns='x'><a xmlns='y'/></a>");

    // in-depth test
    create(2);
    query("insert node <a xmlns='y'/> into db:open('d2')//*:x");
    assertEquals(NL +
        "  Pre[1] xmlns=\"xx\"" + NL +
        "    Pre[2] xmlns=\"y\"",
        context.data().nspaces.toString());
  }

  /**
   * Detects malformed namespace hierarchy adding a document to an empty DB.
   */
  @Test public void nsHierarchy4() {
    execute(new CreateDB("d00x"));
    execute(new Add("x", "<A xmlns='A'><B/><C/></A>"));
    query("/", "<A xmlns='A'><B/><C/></A>");
  }

  /** Test query. */
  @Test public void copy5() {
    query(
        "copy $c := <n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n> " +
        "modify () return $c",
    "<n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n>");
  }

  /**
   * Test query.
   */
  @Test public void insertD2intoD1() {
    create(1, 2);
    query(
        "insert node db:open('d2') into db:open('d1')/x",
        "db:open('d1')",
        "<x><x xmlns='xx'/></x>");
  }

  /**
   * Test query.
   */
  @Test public void insertD3intoD1() {
    create(1, 3);
    query(
        "insert node db:open('d3') into db:open('d1')/x",
        "db:open('d1')/x/*",
        "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>");
  }

  /**
   * Test query.
   */
  @Test public void insertD3intoD1b() {
    create(1, 3);
    query(
        "insert node db:open('d3') into db:open('d1')/x",
        "db:open('d1')/x/*/*",
        "<b:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /**
   * Detects missing prefix declaration.
   */
  @Test public void insertD4intoD1() {
    create(1, 4);
    query(
        "declare namespace a='aa'; insert node db:open('d4')/a:x/a:y " +
        "into db:open('d1')/x",
        "db:open('d1')/x",
        "<x><a:y xmlns:a='aa' xmlns:b='bb'/></x>");
  }

  /**
   * Detects duplicate prefix declaration at pre=0 in MemData instance after insert.
   * Though result correct, prefix
   * a is declared twice. -> Solution?
   */
  @Test public void insertD4intoD5() {
    create(4, 5);
    query(
        "declare namespace a='aa';insert node db:open('d4')//a:y " +
        "into db:open('d5')/a:x",
        "declare namespace a='aa';db:open('d5')//a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects duplicate namespace declarations in MemData instance.
   */
  @Test public void insertD7intoD1() {
    create(1, 7);
    query(
        "declare namespace x='xx';insert node db:open('d7')/x:x into db:open('d1')/x",
        "db:open('d1')/x",
        "<x><x xmlns='xx'><y/></x></x>");
  }

  /**
   * Detects general problems with namespace references.
   */
  @Test public void insertD6intoD4() {
    create(4, 6);
    query(
        "declare namespace a='aa';insert node db:open('d6') into db:open('d4')/a:x",
        "declare namespace a='aa';db:open('d4')/a:x/a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects general problems with namespace references.
   */
  @Test public void insertTransform1() {
    query(
        "declare default element namespace 'xyz';" +
        "copy $foo := <foo/> modify insert nodes (<bar/>, <baz/>)" +
        "into $foo return $foo",
        "<foo xmlns='xyz'><bar/><baz/></foo>");
  }

  /**
   * Detects general problems with namespace references.
   */
  @Test public void insertTransform2() {
    query(
        "copy $foo := <foo/> modify insert nodes (<bar/>)" +
        "into $foo return $foo",
        "<foo><bar/></foo>");
  }

  /**
   * Tests, whether the PRE values of the namespace structure nodes are correctly adjusted after
   * inserts.
   */
  @Test public void insertTransform3() {
    query(transform("document { <X><C xmlns:c='NS'/></X> }",
        "insert node <B><B b:b='B' xmlns:b='B'/></B> before $input/X/*:C"),
        "<X><B><B xmlns:b='B' b:b='B'/></B><C xmlns:c='NS'/></X>");
  }

  /**
   * Detects wrong namespace references.
   */
  @Test public void uriStack() {
    create(8);
    query(
        "db:open('d8')",
        "<a><b xmlns='B'/><c/></a>");
  }

  /**
   * Deletes the document node and checks if namespace nodes of descendants
   * are deleted as well. F.i. adding a document via REST/PUT deletes a
   * document node if the given document/name is already stored in the target
   * collection. If the test fails, this may lead to superfluous namespace nodes.
   * @throws IOException I/O exception
   */
  @Test public void deleteDocumentNode() throws IOException {
    create(2);
    context.data().startUpdate(context.options);
    context.data().delete(0);
    context.data().finishUpdate(context.options);
    final byte[] ns = context.data().defaultNs();
    assertTrue(ns != null && ns.length == 0);
  }

  /**
   * Checks a path optimization fix.
   */
  @Test public void queryPathOpt() {
    create(17);
    query("db:open('d17')/descendant::*:b", "<b xmlns:ns='NS'/>");
  }

  /**
   * Checks a path optimization fix.
   */
  @Test public void queryPathOpt2() {
    create(17);
    query("db:open('d17')/*:a/*:b", "<b xmlns:ns='NS'/>");
  }

  /**
   * Inserts an element with a prefixed attribute and checks
   * if there are superfluous namespace declarations for the element.
   */
  @Test public void gh249() {
    create(18);
    query(
      "declare namespace ns='ns'; " +
      "insert node <b ns:id='0'/> into /n/a");
    query("//*:a", "<a xmlns:ns='ns'><b ns:id='0'/></a>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameNSCheck1() {
    query(
      "copy $copy := <a/> " +
      "modify rename node $copy as QName('uri', 'e') " +
      "return $copy",
      "<e xmlns=\"uri\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameNSCheck2() {
    query(
      "copy $n := <a><b/></a> " +
      "modify rename node $n/b as QName('uri', 'e') " +
      "return $n/*:e",
      "<e xmlns=\"uri\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameNSCheck3() {
    query(
      "copy $n := <a c='d'/> " +
      "modify rename node $n as QName('uri', 'e') " +
      "return $n",
      "<e xmlns=\"uri\" c=\"d\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameNSCheck4() {
    query(
      "copy $a := <a a='v'/> " +
      "modify rename node $a/@a as QName('uri', 'p:a') " +
      "return $a",
      "<a xmlns:p='uri' p:a='v'/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameNSCheck5() {
    error(
      "copy $a := <a xmlns:p='A' a='v'/> " +
      "modify rename node $a/@a as QName('uri', 'p:a') " +
      "return $a",
      UPNSCONFL_X_X);
  }

  /**
   * Checks if duplicate attributes are detected if a default namespace is declared.
   */
  @Test public void duplAttribute1() {
    error(
      "<e xmlns='URI' a=''/> update { insert node attribute a { } into . }",
      UPATTDUPL_X);
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameRemoveNS1() {
    error(
      "copy $a := <a xmlns='A'><b xmlns='B'/></a> " +
      "modify for $el in $a/descendant-or-self::element() return " +
      "rename node $el as QName('',local-name($el)) " +
      "return $a",
      UPNSCONFL_X_X);
  }

  /**
   * Checks namespace declarations.
   */
  @Test public void renameRemoveNS2() {
    query(
      "copy $a := <a:a xmlns:a='A'><b:a xmlns:b='B'/></a:a> " +
      "modify for $el in $a/descendant-or-self::element() return " +
      "rename node $el as QName('',local-name($el)) " +
      "return $a",
      "<a xmlns:a='A'><a xmlns:b='B'/></a>");
  }

  /**
   * Checks duplicate namespace declarations.
   */
  @Test public void avoidDuplicateNSDeclaration() {
    create(19);
    query(
      "let $b := <a xmlns:x='X' x:id='0'/> " +
      "return insert node $b//@*:id into /*:n");
    assertEquals(1, context.data().namespaces(1).size());
  }

  /** Handles duplicate prefixes. */
  @Test public void duplicatePrefixes1() {
    query(
      "<e xmlns:p='u'>{ <a xmlns:p='u' p:a='v'/>/@* }</e>",
      "<e xmlns:p='u' p:a='v'/>");
  }

  /** Handles duplicate prefixes. */
  @Test public void duplicatePrefixes2() {
    query(
      "<e xmlns:p='u1'>{ <a xmlns:p='u2' p:a='v'/>/@* }</e>",
      "<e xmlns:p_1='u2' xmlns:p='u1' p_1:a='v'/>");
  }

  /** Handles duplicate prefixes. */
  @Test public void duplicatePrefixes3() {
    query(
      "<e xmlns:p='u' xmlns:p1='u1'>{ <a xmlns:p='u1' p:a='v'/>/@* }</e>",
      "<e xmlns:p1='u1' xmlns:p='u' p1:a='v'/>");
  }

  /** Handles duplicate prefixes. */
  @Test public void duplicatePrefixes4() {
    query(
      "<e xmlns:p='u' xmlns:p1='u1'>{ <a xmlns:p='u2' p:a='v'/>/@* }</e>",
      "<e xmlns:p_1='u2' xmlns:p1='u1' xmlns:p='u' p_1:a='v'/>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test public void nsInAtt() {
    query("data(<a a='{namespace-uri-for-prefix('x', <x:a/>)}' xmlns:x='X'/>/@a)",
        "X");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test public void nsInBraces() {
    query("<a xmlns:x='X'>{namespace-uri-for-prefix('x', <x:b/>)}</a>/text()",
        "X");
  }

  /**
   * Test query.
   */
  @Test public void defaultElementNamespaceTest() {
    query("declare default element namespace 'a';" +
        "let $x as element(a) := <a/> return $x",
        "<a xmlns=\"a\"/>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test public void newPrefix() {
    query("<a>{ attribute {QName('U', 'a')} {} }</a>",
        "<a xmlns:ns0='U' ns0:a=''/>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test public void newPrefix2() {
    query("<a xmlns:ns1='ns1'><b xmlns='ns1'>" +
        "<c>{attribute {QName('ns1', 'att1')} {}," +
        "attribute {QName('ns2', 'att2')} {}}</c></b></a>",
        "<a xmlns:ns1='ns1'><b xmlns='ns1'>" +
        "<c xmlns:ns0_1='ns2' xmlns:ns0='ns1' ns0:att1='' ns0_1:att2=''/>" +
        "</b></a>");
  }

  /**
   * Test query for stripping existing namespaces.
   * @throws Exception exception
   */
  @Test public void stripNS() throws Exception {
    final IO io = IO.get("<a xmlns:a='a'><b><c/><c/><c/></b></a>");
    try(QueryProcessor qp = new QueryProcessor("/*:a/*:b", context).context(new DBNode(io))) {
      final ANode sub = (ANode) qp.iter().next();
      query(DataBuilder.stripNS(sub, token("a"), context).serialize().toString(),
          "<b><c/><c/><c/></b>");
    }
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test @Disabled
  public void xuty0004() {
    final String query = "declare variable $input-context external;" +
        "let $source as node()* := (" +
        "    <status>on leave</status>," +
        "    <!-- for 6 months -->" +
        "  )," +
        "  $target := $input-context/works[1]/employee[1]" +
        "return insert nodes $source into $target";
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.value();
    } catch(final QueryException ex) {
      assertEquals("XUTY0004", ex.error().toString());
    }
    fail("should throw XUTY0004");
  }

  /**
   * Tests preserve, no-inherit for copy expression. Related to XQUTS
   * id-insert-expr-081-no-inherit.xq. Tests if no-inherit has a persistent
   * effect. Is it actually supposed to?
   * The <new/> tag is inserted into a fragment f using no-inherit and copy.
   * The resulting fragment is inserted into a database. The
   * namespaces in scope with prefix 'ns' are finally checked for the
   * inserted <new/> tag. If the result is non-empty we may have a problem -
   * being not able propagate the no-inherit flag to our table.
   */
  @Test @Disabled
  public void copyPreserveNoInheritPersistent() {
    query("declare copy-namespaces preserve,no-inherit;" +
        "declare namespace my = 'ns';" +
        "let $v :=" +
        "(copy $c := <my:n><my:a/></my:n>" +
        "modify insert node <new/> into $c " +
        "return $c)" +
        "return insert node $v into db:open('d2')/n",
        "namespace-uri-for-prefix('my', db:open('d2')//*:new)",
        "");
  }

  /**
   * Checks if a query uses the outer default namespace.
   */
  @Test public void defaultNS() {
    create(1);
    query("<h xmlns='U'>{ db:open('d1')/x }</h>/*", "");
  }

  /**
   * Test query.
   */
  @Test public void precedingSiblingNsDecl() {
    create(20);
    query("//Q{A}a", "<x:a xmlns:x='A'><x:b xmlns:x='B'/><x:c/></x:a>");
    query("//Q{A}b", "");
    query("//Q{A}c", "<x:c xmlns:x='A'/>");
    query("//Q{B}a", "");
    query("//Q{B}b", "<x:b xmlns:x='B'/>");
    query("//Q{B}c", "");
  }

  /**
   * Test query.
   */
  @Test public void duplicateXMLNamespace() {
    create(1);
    query("insert node attribute xml:space { 'preserve' } into /x", "");
    query(".", "<x xml:space='preserve'/>");
    error("insert node attribute xml:space { 'preserve' } into /x", UPATTDUPL_X);
  }

  /**
   * Test query.
   */
  @Test public void duplicateNamespaces() {
    query("copy $c := <a xmlns='X'/> modify (" +
          "  rename node $c as QName('X','b')," +
          "  insert node attribute c{'a'} into $c" +
          ") return $c", "<b xmlns=\"X\" c=\"a\"/>");
    error("copy $c := <a xmlns='X'/> modify (" +
        "  rename node $c as QName('Y','b')," +
        "  insert node attribute c{'a'} into $c" +
        ") return $c", UPNSCONFL_X_X);
    query("copy $c := <a/> modify (" +
        "  rename node $c as QName('X','b')," +
        "  insert node attribute c{'a'} into $c" +
        ") return $c", "<b xmlns=\"X\" c=\"a\"/>");
  }

  /**
   * Test query (#780).
   */
  @Test public void xmlNS() {
    query("insert node (<w:a xmlns:w='X' xml:x=''><w:b/><w:c/><w:d/><w:e/><w:f/></w:a>," +
        "<w:g xmlns:w='X' xml:y=''/>) into <w:h xmlns:w='X' xml:z=''/>");
    query("insert node (<w:a xmlns:w='X' xmlns:a='a' a:x=''><w:b/><w:c/><w:d/><w:e/><w:f/></w:a>," +
        "<w:g xmlns:w='X' xmlns:a='a' a:y=''/>) into <w:h xmlns:w='X' xmlns:a='a' a:z=''/>");
  }

  /**
   * Creates the database context.
   */
  @BeforeAll public static void start() {
    // turn off pretty printing
    set(MainOptions.SERIALIZER, SerializerMode.NOINDENT.get());
  }

  /**
   * Creates the specified test databases.
   * @param db database numbers
   */
  private static void create(final int... db) {
    for(final int d : db) {
      final String[] doc = DOCS[d - 1];
      execute(new CreateDB(doc[0], doc[1]));
    }
  }

  /**
   * Runs a query and matches the result against the expected output.
   * @param query query
   * @param expected expected output
   */
  private static void query(final String query, final String expected) {
    query(null, query, expected);
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param first first query
   * @param second second query
   * @param expected expected output
   */
  private static void query(final String first, final String second, final String expected) {
    if(first != null) Sandbox.query(first);
    final String result = Sandbox.query(second).trim();

    // quotes are replaced by apostrophes to simplify comparison
    final String res = result.replaceAll("\"", "'");
    final String exp = expected.replaceAll("\"", "'");
    if(!exp.equals(res)) fail("\n[E] " + exp + "\n[F] " + res);
  }
}
