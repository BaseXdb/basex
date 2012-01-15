package org.basex.test.query;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.query.up.primitives.RenameNode;
import org.basex.query.util.Err;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests namespaces.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class NamespaceTest extends AdvancedQueryTest {
  /** Default database name. */
  private static final String DB = Util.name(NamespaceTest.class);

  /** Test documents. */
  private static String[][] docs = {
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
  };

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   * @throws Exception exception
   */
  @Test
  public void insertIntoShiftPreValues() throws Exception {
    create(12);
    query("insert node <b xmlns:ns='A'/> into doc('d12')/*:a/*:b");
    assertEquals(NL +
        "  Pre[3] xmlns:ns=\"A\" " + NL +
        "  Pre[4] xmlns=\"B\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   * @throws Exception exception
   */
  @Test
  public void insertIntoShiftPreValues2() throws Exception {
    create(13);
    query("insert node <c/> as first into doc('d13')/a");
    assertEquals(NL +
        "  Pre[3] xmlns=\"A\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   * @throws Exception exception
   */
  @Test
  public void insertIntoShiftPreValues3() throws Exception {
    create(14);
    query("insert node <n xmlns='D'/> into doc('d14')/*:a/*:b");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\" " + NL +
        "    Pre[2] xmlns=\"B\" " + NL +
        "      Pre[3] xmlns=\"D\" " + NL +
        "    Pre[4] xmlns=\"C\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   * @throws Exception exception
   */
  @Test
  public void deleteShiftPreValues() throws Exception {
    create(12);
    query("delete node doc('d12')/a/b");
    assertEquals(NL +
        "  Pre[2] xmlns=\"B\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   * @throws Exception exception
   */
  @Test
  public void deleteShiftPreValues2() throws Exception {
    create(14);
    query("delete node doc('d14')/*:a/*:b");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\" " + NL +
        "    Pre[2] xmlns=\"C\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   * @throws Exception exception
   */
  @Test
  public void deleteShiftPreValues3() throws Exception {
    create(15);
    query("delete node doc('d15')/*:a/*:c");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\" " + NL +
        "    Pre[2] xmlns=\"B\" " + NL +
        "    Pre[3] xmlns=\"E\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   * @throws Exception exception
   */
  @Test
  public void deleteShiftPreValues4() throws Exception {
    create(16);
    query("delete node doc('d16')/a/b");
    assertTrue(CONTEXT.data().nspaces.toString().isEmpty());
  }

  /**
   * Tests for correct namespace hierarchy, esp. if namespace nodes
   * on the following axis of an insert/delete operation are
   * updated correctly.
   * @throws Exception exception
   */
  @Test
  public void delete1() throws Exception {
    create(11);
    query("delete node doc('d11')/*:a/*:b",
        "doc('d11')/*:a",
        "<a xmlns='A'><c xmlns:ns1='AA'><d/></c></a>");
  }

  /** Test query. */
  @Test
  public void copy1() {
    query(
        "copy $c := <x:a xmlns:x='xx'><b/></x:a>/b modify () return $c",
        "<b xmlns:x='xx'/>");
  }

  /**
   * Detects corrupt namespace hierarchy.
   * @throws Exception exception
   */
  @Test
  public void copy2() throws Exception {
    create(4);
    query(
        "declare namespace a='aa';" +
        "copy $c:=doc('d4') modify () return $c//a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects missing prefix declaration.
   * @throws Exception exception
   */
  @Test
  public void copy3() throws Exception {
    create(4);
    query(
        "declare namespace a='aa';" +
        "copy $c:=doc('d4')//a:y modify () return $c",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects duplicate namespace declaration in MemData instance.
   */
  @Test
  public void copy4() {
    query(
        "copy $c := <a xmlns='test'><b><c/></b><d/></a> modify () return $c",
        "<a xmlns='test'><b><c/></b><d/></a>");
  }

  /**
   * Detects bogus namespace after insert.
   * @throws Exception exception
   */
  @Test
  public void bogusDetector() throws Exception {
    create(1);
    query(
        "insert node <a xmlns='test'><b><c/></b><d/></a> into doc('d1')/x",
        "declare namespace na = 'test';doc('d1')/x/na:a",
        "<a xmlns='test'><b><c/></b><d/></a>");
  }

  /**
   * Detects empty default namespace in serializer.
   */
  @Test
  public void emptyDefaultNamespace() {
    query("<ns:x xmlns:ns='X'><y/></ns:x>",
        "<ns:x xmlns:ns='X'><y/></ns:x>");
  }

  /**
   * Detects duplicate default namespace in serializer.
   */
  @Test
  public void duplicateDefaultNamespace() {
    query("<ns:x xmlns:ns='X'><y/></ns:x>",
        "<ns:x xmlns:ns='X'><y/></ns:x>");
  }

  /**
   * Detects malformed namespace hierarchy.
   * @throws Exception exception
   */
  @Test
  public void nsHierarchy() throws Exception {
    create(9);
    query("insert node <f xmlns='F'/> into doc('d9')//*:e");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\" " + NL +
        "    Pre[4] xmlns=\"D\" " + NL +
        "    Pre[6] xmlns=\"F\" ",
        CONTEXT.data().nspaces.toString());
  }

  /**
   * Detects malformed namespace hierarchy.
   * @throws Exception exception
   */
  @Test
  public void nsHierarchy2() throws Exception {
    create(10);
    query("insert node <f xmlns='F'/> into doc('d10')//*:e");
    assertEquals(NL +
        "  Pre[1] xmlns=\"A\" " + NL +
        "    Pre[4] xmlns=\"D\" " + NL +
        "      Pre[5] xmlns=\"G\" " + NL +
        "    Pre[7] xmlns=\"F\" ",
        CONTEXT.data().nspaces.toString());
  }

  /** Test query. */
  @Test
  public void copy5() {
    query(
        "copy $c := <n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n> " +
        "modify () return $c",
    "<n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n>");
  }

  /**
   * Test query.
   * @throws Exception exception
   */
  @Test
  public void insertD2intoD1() throws Exception {
    create(1, 2);
    query(
        "insert node doc('d2') into doc('d1')/x",
        "doc('d1')",
        "<x><x xmlns='xx'/></x>");
  }

  /**
   * Test query.
   * @throws Exception exception
   */
  @Test
  public void insertD3intoD1() throws Exception {
    create(1, 3);
    query(
        "insert node doc('d3') into doc('d1')/x",
        "doc('d1')/x/*",
        "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>");
  }

  /**
   * Test query.
   * @throws Exception exception
   */
  @Test
  public void insertD3intoD1b() throws Exception {
    create(1, 3);
    query(
        "insert node doc('d3') into doc('d1')/x",
        "doc('d1')/x/*/*",
        "<b:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /**
   * Detects missing prefix declaration.
   * @throws Exception exception
   */
  @Test
  public void insertD4intoD1() throws Exception {
    create(1, 4);
    query(
        "declare namespace a='aa'; insert node doc('d4')/a:x/a:y " +
        "into doc('d1')/x",
        "doc('d1')/x",
        "<x><a:y xmlns:a='aa' xmlns:b='bb'/></x>");
  }

  /**
   * Detects duplicate prefix declaration at pre=0 in MemData instance after
   * insert.
   * Though result correct, prefix
   * a is declared twice. -> Solution?
   * @throws Exception exception
   */
  @Test
  public void insertD4intoD5() throws Exception {
    create(4, 5);
    query(
        "declare namespace a='aa';insert node doc('d4')//a:y " +
        "into doc('d5')/a:x",
        "declare namespace a='aa';doc('d5')//a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects duplicate namespace declarations in MemData instance.
   * @throws Exception exception
   */
  @Test
  public void insertD7intoD1() throws Exception {
    create(1, 7);
    query(
        "declare namespace x='xx';insert node doc('d7')/x:x into doc('d1')/x",
        "doc('d1')/x",
        "<x><x xmlns='xx'><y/></x></x>");
  }

  /**
   * Detects general problems with namespace references.
   * @throws Exception exception
   */
  @Test
  public void insertD6intoD4() throws Exception {
    create(4, 6);
    query(
        "declare namespace a='aa';insert node doc('d6') into doc('d4')/a:x",
        "declare namespace a='aa';doc('d4')/a:x/a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }

  /**
   * Detects general problems with namespace references.
   */
  @Test
  public void insertTransform1() {
    query(
        "declare default element namespace 'xyz';" +
        "copy $foo := <foo/> modify insert nodes (<bar/>, <baz/>)" +
        "into $foo return $foo",
        "<foo xmlns='xyz'><bar/><baz/></foo>");
  }

  /**
   * Detects general problems with namespace references.
   */
  @Test
  public void insertTransformX() {
    query(
        "copy $foo := <foo/> modify insert nodes (<bar/>)" +
        "into $foo return $foo",
        "<foo><bar/></foo>");
  }

  /**
   * Detects wrong namespace references.
   * @throws Exception exception
   */
  @Test
  public void uriStack() throws Exception {
    create(8);
    query(
        "doc('d8')",
        "<a><b xmlns='B'/><c/></a>");
  }

  /**
   * Deletes the document node and checks if namespace nodes of descendants
   * are deleted as well. F.i. adding a document via REST/PUT deletes a
   * document node if the given document/name is already stored in the target
   * collection. If the test fails, this may lead to superfluous namespace
   * nodes.
   * @throws Exception exception
   */
  @Test
  public void deleteDocumentNode() throws Exception {
    create(2);
    CONTEXT.data().delete(0);
    final byte[] ns = CONTEXT.data().nspaces.globalNS();
    assertTrue(ns != null && ns.length == 0);
  }

  /**
   * Checks a path optimization fix.
   * @throws BaseXException database exception
   */
  @Test
  public void queryPathOpt() throws BaseXException {
    create(17);
    query("doc('d17')/descendant::*:b", "<b xmlns:ns='NS'/>");
  }

  /**
   * Checks a path optimization fix.
   * @throws BaseXException database exception
   */
  @Test
  public void queryPathOpt2() throws BaseXException {
    create(17);
    query("doc('d17')/*:a/*:b", "<b xmlns:ns='NS'/>");
  }

  /**
   * GH-249: Inserts an element with a prefixed attribute and checks
   * if there are superfluous namespace declarations for the element.
   * @throws BaseXException database exception
   */
  @Test
  public void superfluousPrefixDeclaration() throws BaseXException {
    create(18);
    query(
      "declare namespace ns='ns'; " +
      "insert node <b ns:id='0'/> into /n/a");
    query("//*:a", "<a xmlns:ns='ns'><b ns:id='0'/></a>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameNSCheck1() {
    query(
      "copy $copy := <a/> " +
      "modify rename node $copy as QName('uri', 'e') " +
      "return $copy",
      "<e xmlns=\"uri\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameNSCheck2() {
    query(
      "copy $n := <a><b/></a> " +
      "modify rename node $n/b as QName('uri', 'e') " +
      "return $n/*:e",
      "<e xmlns=\"uri\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameNSCheck3() {
    query(
      "copy $n := <a c='d'/> " +
      "modify rename node $n as QName('uri', 'e') " +
      "return $n",
      "<e xmlns=\"uri\" c=\"d\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameNSCheck4() {
    query(
      "copy $a := <a a='v'/> " +
      "modify rename node $a/@a as QName('uri', 'p:a') " +
      "return $a",
      "<a xmlns:p='uri' p:a='v'/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameNSCheck5() {
    error(
      "copy $a := <a xmlns:p='A' a='v'/> " +
      "modify rename node $a/@a as QName('uri', 'p:a') " +
      "return $a",
      Err.UPNSCONFL);
  }

  /**
   * Checks namespace declarations.
   * Note: xmlns='' is being erroneously added. Might have to to with
   * Data.update(); see {@link RenameNode#apply()}.
   */
  @Test
  public void renameDuplNSCheck() {
    query(
      "copy $a := <a a='v'/> " +
      "modify rename node $a/@a as fn:QName('uri', 'a') " +
      "return $a",
      "<a xmlns=\"uri\" a=\"v\"/>");
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameRemoveNS1() {
    error(
      "copy $a := <a xmlns='A'><b xmlns='B'/></a> " +
      "modify for $el in $a/descendant-or-self::element() return " +
      "rename node $el as QName('',local-name($el)) " +
      "return $a",
      Err.UPNSCONFL);
  }

  /**
   * Checks namespace declarations.
   */
  @Test
  public void renameRemoveNS2() {
    query(
      "copy $a := <a:a xmlns:a='A'><b:a xmlns:b='B'/></a:a> " +
      "modify for $el in $a/descendant-or-self::element() return " +
      "rename node $el as QName('',local-name($el)) " +
      "return $a",
      "<a xmlns:a='A'><a xmlns:b='B'/></a>");
  }

  /**
   * Checks duplicate namespace declarations.
   * @throws BaseXException exception
   */
  @Test
  public void avoidDuplicateNSDeclaration() throws BaseXException {
    create(19);
    query(
      "let $b := <a xmlns:x='X' x:id='0'/> " +
      "return insert node $b//@*:id into /*:n");
    assertEquals(1, CONTEXT.data().ns(1).size());
  }

  /** Handles duplicate prefixes. */
  @Test
  public void duplicatePrefixes1() {
    query(
      "<e xmlns:p='u'>{ <a xmlns:p='u' p:a='v'/>/@* }</e>",
      "<e xmlns:p='u' p:a='v'/>");
  }

  /** Handles duplicate prefixes. */
  @Test
  public void duplicatePrefixes2() {
    query(
      "<e xmlns:p='u1'>{ <a xmlns:p='u2' p:a='v'/>/@* }</e>",
      "<e xmlns:p_1='u2' xmlns:p='u1' p_1:a='v'/>");
  }

  /** Handles duplicate prefixes. */
  @Test
  public void duplicatePrefixes3() {
    query(
      "<e xmlns:p='u' xmlns:p1='u1'>{ <a xmlns:p='u1' p:a='v'/>/@* }</e>",
      "<e xmlns:p1='u1' xmlns:p='u' p1:a='v'/>");
  }

  /** Handles duplicate prefixes. */
  @Test
  public void duplicatePrefixes4() {
    query(
      "<e xmlns:p='u' xmlns:p1='u1'>{ <a xmlns:p='u2' p:a='v'/>/@* }</e>",
      "<e xmlns:p_1='u2' xmlns:p1='u1' xmlns:p='u' p_1:a='v'/>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void nsInAtt() {
    query("data(<a a='{namespace-uri-for-prefix('x', <b/>)}' xmlns:x='X'/>/@a)",
        "X");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void nsInBraces() {
    query("<a xmlns:x='X'>{namespace-uri-for-prefix('x', <b/>)}</a>/text()",
        "X");
  }

  /**
   * Test query.
   */
  @Test
  public void defaultElementNamespaceTest() {
    query("declare default element namespace 'a';" +
        "let $x as element(a) := <a/> return $x",
        "<a xmlns=\"a\"/>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void newPrefix() {
    query("<a>{ attribute {QName('U', 'a')} {} }</a>",
        "<a xmlns:ns0='U' ns0:a=''/>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void newPrefix2() {
    query("<a xmlns:ns1='ns1'><b xmlns='ns1'>" +
        "<c>{attribute {QName('ns1', 'att1')} {}," +
        "attribute {QName('ns2', 'att2')} {}}</c></b></a>",
        "<a xmlns:ns1='ns1'><b xmlns='ns1'>" +
        "<c xmlns:ns0_1='ns2' xmlns:ns0='ns1' ns0:att1='' ns0_1:att2=''/>" +
        "</b></a>");
  }

  /**
   * Creates the database context.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void start() throws BaseXException {
    // turn off pretty printing
    new Set(Prop.SERIALIZER, "indent=no").execute(CONTEXT);
  }

  /**
   * Creates the specified test databases.
   * @param db database numbers
   * @throws BaseXException database exception
   */
  public void create(final int... db) throws BaseXException {
    for(final int d : db) {
      final String[] doc = docs[d - 1];
      new CreateDB(doc[0], doc[1]).execute(CONTEXT);
    }
  }

  /**
   * Removes test databases and closes the database context.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    // drop all test databases
    for(final String[] db : docs) new DropDB(db[0]).execute(CONTEXT);
    new DropDB(DB).execute(CONTEXT);
    CONTEXT.close();
  }

  /**
   * Runs a query and matches the result against the expected output.
   * @param query query
   * @param expected expected output
   */
  private void query(final String query, final String expected) {
    query(null, query, expected);
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param first first query
   * @param second second query
   * @param expected expected output
   */
  private void query(final String first, final String second,
      final String expected) {

    try {
      if(first != null) new XQuery(first).execute(CONTEXT);
      final String result = new XQuery(second).execute(CONTEXT).trim();

      // quotes are replaced by apostrophes to simplify comparison
      final String res = result.replaceAll("\\\"", "'");
      final String exp = expected.replaceAll("\\\"", "'");
      if(!exp.equals(res)) fail("\nExpected: " + exp + "\nFound: " + res);
    } catch(final BaseXException ex) {
      fail(Util.message(ex));
    }
  }
}
