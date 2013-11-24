package org.basex.query.up;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.*;
import org.junit.*;

/**
 * General test of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class UpdateTest extends AdvancedQueryTest {
  /** Test document. */
  private static final String DOC = "src/test/resources/xmark.xml";

  /**
   * Creates a database.
   * @param input input database string, if null, then use default.
   * @throws BaseXException database exception
   */
  private static void createDB(final String input) throws BaseXException {
    new CreateDB(NAME, input == null ? DOC : input).execute(context);
  }

  // BASIC XQUF TESTS *********************************************
  /**
   * Basic delete.
   */
  @Test
  public void delete() {
    query(transform("<n><a><b/></a><c/><d><e><f/></e><g/></d></n>",
        "delete node $input//b, delete node $input//f, delete node $input//e"),
        "<n><a/><c/><d><g/></d></n>");
  }

  /**
   * Basic delete.
   */
  @Test
  public void delete2() {
    query(transform("<a><b/><c/><d/></a>",
        "delete node $input//b, delete node $input//c"), "<a><d/></a>");
  }

  /**
   * Basic delete.
   */
  @Test
  public void delete3() {
    final String doc =
        "<n1>" +
            "<n2/><n3/><n4/><n5/><n6/><n7/><n8/><n9/><n10/><n11/>" +
        "</n1>";
    query(transform(doc, "delete node ($input//n3, $input//n5, $input//n7)"),
        "<n1><n2/><n4/><n6/><n8/><n9/><n10/><n11/></n1>");
  }

  /** Transform expression shadowing another variable. */
  @Test
  public void transform() {
    query("let $c := <x/> return copy $c := $c modify () return $c", "<x/>");
    query("declare variable $d := document{ <x/> } update (); $d/x", "<x/>");
  }

  /**
   * Basic insert into.
   */
  @Test
  public void insertinto() {
    query(transform("<n><a/></n>",
        "insert node <x2/> into $input, insert node <x1/> into $input//a"),
        "<n><a><x1/></a><x2/></n>");
  }

  /**
   * Basic insert into.
   */
  @Test
  public void insertinto2() {
    query(transform("<n><a/><b/></n>",
        "insert node <x1/> into $input//a, insert node <x2/> into $input//b"),
        "<n><a><x1/></a><b><x2/></b></n>");
  }

  /**
   * Tests {@link InsertIntoAsFirst} on T and a {@link DeleteNode} on the attribute of T.
   */
  @Test
  public void insertIntoAsFirst() {
    query(transform("<a att='0'/>",
        "insert node <b/> as first into $input, delete node $input/@att"),
        "<a><b/></a>");
  }

  /**
   * Tests {@link InsertIntoAsFirst} on T and a {@link ReplaceNode} on the attribute of T.
   */
  @Test
  public void insertIntoAsFirst2() {
    query(transform("<a att='0'/>",
        "insert node <b/> as first into $input, replace node $input/@att with " +
        "(attribute {'att1'}{0}, attribute {'att2'}{0})"),
        "<a att1=\"0\" att2=\"0\"><b/></a>");
  }

  /**
   * Tests if insertion sequences are merged in a consistent way.
   */
  @Test
  public void insertSequenceMerging() {
    query(transform("<n/>",
        "insert node <e1/> into $input, insert node <e2/> into $input"),
        "<n><e1/><e2/></n>");

    query(transform("<n/>",
        "insert node <e1/> as last into $input, insert node <e2/> as last into $input"),
        "<n><e1/><e2/></n>");

    query(transform("<n/>",
        "insert node <e1/> into $input, insert node <e2/> as last into $input"),
        "<n><e1/><e2/></n>");

    query(transform("<n/>",
        "insert node <e1/> as last into $input, insert node <e2/> into $input"),
        "<n><e2/><e1/></n>");
  }

  /**
   * Delete last node of a data instance. Checks if table limits are crossed.
   */
  @Test
  public void deleteLastNode() {
    query(transform("<a><b/><c/></a>", "delete node $input//c"),
        "<a><b/></a>");
  }

  /**
   * Basic lazy replace test.
   */
  @Test
  public void lazyReplace() {
    query(transform("<n><r>a</r></n>", "replace node $input/r with <r>b</r>"),
        "<n><r>b</r></n>");

    query(transform("<n><r>a</r></n>", "replace node $input/r with <r>b</r>"),
        "<n><r>b</r></n>");
  }
  /**
   * ReplaceValue on attribute.
   */
  @Test
  public void replaceValue() {
    final String result = "<a b=\"a\"/>";
    final String[] replacements = {
        "'a'",
        "attribute a { 'a' }",
        "attribute b { 'a' }",
        "text { 'a' }",
        "comment { 'a' }",
        "processing-instruction x { 'a' }",
        "document { 'a' }",
        "element x { 'a' }",
    };
    for(final String r : replacements)
      query(transform("<a b=''/>", "replace value of node $input/@b with " + r), result);
  }

  /**
   * Replacement of an identical value.
   */
  @Test
  public void replaceIdenticalValue() {
    query(transform("<a>A</a>",
        "replace value of node $input/text() with 'A'"), "<a>A</a>");
    query(transform("<a a='a'/>",
        "replace value of node $input/@a with 'a'"), "<a a=\"a\"/>");
    query(transform("<?a a?>",
        "replace value of node $input with 'a'"), "<?a a?>");
    query(transform("<!--a-->",
        "replace value of node $input with 'a'"), "<!--a-->");
    query(transform("text { 'A' }",
        "replace value of node $input with 'A'"), "A");
    query(transform("document { 'A' }",
        "replace value of node $input/text() with 'A'"), "A");
  }

  /**
   * Tests detection of duplicate attributes in insertion sequences and whether a
   * combination of delete/insert/replace can lead to duplicate attributes.
   */
  @Test
  public void duplicateAttribute() {
    // check 'global' duplicate detection
    String q = transform("<x/>",
        "for $i in 1 to 2 return insert node attribute a { 'b' } into $input");
    error(q, Err.UPATTDUPL);

    // check if insertion sequence itself is duplicate free (which it is not)
    q = transform("<x a='a'/>",
        "delete node $input/@a," +
        "for $i in 1 to 2 return insert node attribute a { 'b' } into $input");
    error(q, Err.UPATTDUPL);

    // replace with a + delete a + insert a
    q = transform("<x a='a'/>",
        "delete node $input/@a, replace node $input/@a with attribute a {'b'}," +
        "insert node attribute a { 'b' } into $input");
    error(q, Err.UPATTDUPL);
  }

  /**
   * Replace last node of a data instance. Checks if table limits are crossed.
   */
  @Test
  public void replaceLastNode() {
    query(transform("<a><b/><c><d/><d/><d/></c></a>", "replace node $input//c with <c/>"),
        "<a><b/><c/></a>");
  }

  /**
   * Replaces an attribute with two attributes.
   */
  @Test
  public void replaceAttribute() {
    query(transform("<a att0='0'/>",
        "replace node $input/@att0 with (attribute att1 {'1'}, attribute att2 {'2'})"),
        "<a att1=\"1\" att2=\"2\"/>");
  }

  /**
   * Replaces a target T which is an attribute and has a following node with another
   * parent than T itself.
   */
  @Test
  public void replaceAttribute2() {
    query(transform("<a><b att0='0'/><c/></a>",
        "replace node $input/b/@att0 with attribute att1 {'1'}"),
        "<a><b att1=\"1\"/><c/></a>");
  }

  /**
   * Replaces a single attribute with a sequence of two attributes to test if i.e. attribute sizes
   * are correctly adjusted.
   */
  @Test
  public void replaceAttribute3() {
    query(transform("<a id='0' id2='0'/>",
        "replace node $input/@id with (attribute b {'1'}, attribute c {'2'})"),
        "<a b=\"1\" c=\"2\" id2=\"0\"/>");
  }

  /**
   *  **** TC tries to provoke multiple delete atomics on the same PRE within the same
   * snapshot. *****
   *
   * Only delete primitives {@link DeleteNode} are allowed to create atomic delete
   * updates {@link AtomicUpdateCache}. This ensures that
   * no PRE value is deleted twice by an atomic delete, as the XQUF module resolves
   * multiple operations of the same type on the same node. Deleting a PRE value twice
   * would lead to deleting another node due to PRE shifts after the first delete.
   *
   * If another {@link UpdatePrimitive} leads to the deletion of a node (i.e.
   * {@link ReplaceValue} it must be substituted first by a sequence of other
   * {@link UpdatePrimitive} that contains the {@link DeleteNode} primitive.
   *
   */
  @Test
  public void noMultipleAtomicDeletes() {
    query(transform("<n>text<doNotDelete/></n>",
        "replace value of node $input//text() with \"\", " +
        "delete node $input//text()"),
        "<n><doNotDelete/></n>");
  }

  /**
   * Tests if the substitution of a {@link ReplaceNode} primitive does not interfere with
   * the primitive types used for substitution that are actually called by the user.
   */
  @Test
  public void replaceSubstitution() {
    final String doc = "<n><a/></n>";
    // both queries should yield the same result
    final String a = query(transform(doc,
        "replace node $input//a with element shouldBeLast {}, " +
        "insert node <b/> before $input//a"));
    final String b = query(transform(doc,
        "insert node <b/> before $input//a, " +
        "replace node $input//a with element shouldBeLast {}"));
    assertEquals(a, b);
  }

  /**
   * ReplaceElementContent with non-empty text node.
   */
  @Test
  public void replaceelementcontent1() {
    query(transform("<n id='0'><a/><b><c/></b></n>",
        "replace value of node $input with 'Hello'"),
        "<n id=\"0\">Hello</n>");
  }

  /**
   * ReplaceElementContent with empty text node.
   */
  @Test
  public void replaceelementcontent2() {
    query(transform("<n id='0'><a/><b><c/></b></n>",
        "replace value of node $input with ''"),
        "<n id=\"0\"/>");
  }

  /**
   * ReplaceElementContent on a target T with a text node and insertBefore
   * on a child of T.
   */
  @Test
  public void replaceelementcontent3() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "insert node <newA/> before $input/A");
  }

  /**
   * ReplaceElementContent on a target T with a text node and a replaceNode on a child
   * of T.
   */
  @Test
  public void replaceelementcontent4() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "replace node $input/A with <newA/>");
  }

  /**
   * ReplaceElementContent on a target T with a text node and insertInto on T.
   */
  @Test
  public void replaceelementcontent5() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'", "insert node <newA/> into $input");
  }

  /**
   * ReplaceElementContent on a target T with a text node and rename on a child of
   * T. As a rename does not introduce any new node identities the rename is lost and T
   * is expected to have no children after the end of the snapshot (except the new text
   * node).
   */
  @Test
  public void replaceelementcontent6() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "rename node $input/A as 'newA'");
  }

  /**
   * ReplaceElementContent on a target T and replaceElementContent on the only child of
   * T.
   */
  @Test
  public void replaceelementcontent7() {
    testBothSequences("<n><A><B/></A></n>", "<n>newtextA</n>",
        "replace value of node $input with 'newtextA'",
        "replace value of node $input/A with 'newtextB'");
  }

  /**
   * ReplaceNode on a target T and replaceElementContent on the only child of
   * T.
   */
  @Test
  public void replaceelementcontent8() {
    testBothSequences("<n><A><B/></A></n>", "<n><newA/></n>",
        "replace node $input/A with <newA/>",
        "replace value of node $input/A/B with 'newContentForA'");
  }

  /**
   * ReplaceNode and ReplaceElementContent on a target T.
   */
  @Test
  public void replaceelementcontent9() {
    testBothSequences("<n><A/></n>", "<n><newA/></n>",
        "replace node $input/A with <newA/>",
        "replace value of node $input/A with 'newContentForA'");
  }

  /**
   * Delete/replace same target.
   */
  @Test
  public void deleteAndReplaceOnSameTarget() {
    testBothSequences("<a><b/></a>", "<a><newB/></a>",
        "replace node $input/b with <newB/>", "delete node $input/b");
  }

  /**
   * Tests if adding the two given statements to the pending update list leads to the
   * desired result regardless of the order they are added.
   * @param doc input document
   * @param result expected result
   * @param statement1 update statement 1
   * @param statement2 update statement 2
   */
  private static void testBothSequences(final String doc, final String result, final String
    statement1, final String statement2) {
    query(transform(doc, statement1 + ',' + statement2), result);
    query(transform(doc, statement2 + ',' + statement1), result);
  }

  /**
   * Replaces the value of the documents root node. Related to
   * github issue #141.
   * @throws BaseXException database exception
   */
  @Test
  public void replaceValueOfEmptyRoot() throws BaseXException {
    createDB("<a/>");
    query("replace value of node /a with 'a'");
    query("/", "<a>a</a>");
  }

  /**
   * Insertion into an empty element.
   */
  @Test
  public void emptyInsert1() {
    query(
      "copy $x := <X/> modify insert nodes <A/> into $x return $x",
      "<X><A/></X>");
  }

  /**
   * Insertion into an empty document.
   */
  @Test
  public void emptyInsert2() {
    query(
      "copy $x := document {()} modify insert nodes <X/> into $x return $x",
      "<X/>");
  }

  /**
   * Insertion into an empty document.
   * @throws BaseXException database exception
   */
  @Test
  public void emptyInsert3() throws BaseXException {
    createDB("<a/>");
    query("delete node /a");
    query("insert nodes <X/> into doc('" + NAME + "')");
    query("/", "<X/>");
  }

  /**
   * Tests a simple call of the optimize command.
   * @throws BaseXException database exception
   */
  @Test
  public void optimize() throws BaseXException {
    createDB(null);
    query("let $w := //item[@id = 'item0'] " +
      "return (if($w/@id) " +
      "then (delete node $w/@id, db:optimize('" + NAME + "')) else ())");
  }

  /** Variable from the inner scope shouldn't be visible. */
  @Test
  public void outOfScope() {
    error("let $d := copy $e := <a/> modify () return $e return $e",
        Err.VARUNDEF);
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test
  public void setNSFlag() {
    query("declare namespace x='x';" +
      "copy $x := <x/> modify insert node attribute x:x {} into $x return $x",
      "<x xmlns:x=\"x\" x:x=\"\"/>");
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test
  public void setNSFlag2() {
    query("declare namespace x='x';" +
      "copy $x := <x><a/></x> " +
      "modify insert node attribute x:x {} into $x return $x/a",
      "<a xmlns:x=\"x\"/>");
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test
  public void setNSFlag3() {
    query("declare namespace x='x';" +
      "copy $x := <x><a/></x> " +
      "modify insert node attribute x:x {} into $x/a return $x/a",
      "<a xmlns:x=\"x\" x:x=\"\"/>");
  }

  /**
   * Text merging after a simple delete.
   */
  @Test
  public void textMerging00() {
    query(transform("<a>AA<b/>CC</a>", "delete node $input//b",
        "($input,count($input//text()))"),
        "<a>AACC</a>1");
  }

  /**
   * Text merging for a simple insert into statement.
   * @throws Exception exception
   */
  @Test
  public void textMerging01() throws Exception {
    createDB(null);
    query("insert node 'foo' into //item[@id='item0']/location");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging for a simple insert into statement in combination with an
   * insert before statement. Checks if pre value shifts are taken into
   * account.
   * @throws Exception exception
   */
  @Test
  public void textMerging02() throws Exception {
    createDB(null);
    query("insert node 'foo' into //item[@id='item0']/location," +
      "insert node <a/> before //item[@id='item0']");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test. Test 'insert into as first' and whether pre value
   * shifts are taken into account correctly.
   * @throws Exception exception
   */
  @Test
  public void textMerging03() throws Exception {
    createDB(null);
    query("let $i := //item[@id='item0'] return (insert node 'foo' as " +
      "first into $i/location, insert node 'foo' before $i/location, " +
      "insert node 'foo' into $i/quantity)");
    query("let $i := //item[@id='item0'] return " +
      "(($i/location/text())[1], ($i/quantity/text())[1])",
      "fooUnited States1foo");
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   * @throws Exception exception
   */
  @Test
  public void textMerging04() throws Exception {
    createDB(null);
    query("let $i := //item[@id='item0'] return insert node 'foo' " +
      "before $i/location/text()");
    query("let $i := //item[@id='item0'] return " +
      "($i/location/text())[1]", "fooUnited States");
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   * @throws Exception exception
   */
  @Test
  public void textMerging05() throws Exception {
    createDB(null);
    query("let $i := //item[@id='item0']/location return " +
      "(insert node 'foo' into $i, delete node $i/text())");
    query("//item[@id='item0']/location/text()", "foo");
  }

  /**
   * Text merging test.
   * @throws Exception exception
   */
  @Test
  public void textMerging06() throws Exception {
    createDB(null);
    query("let $i := //item[@id='item0']/location return " +
      "(insert node <n/> into $i, insert node 'foo' as last into $i)");
    query("let $i := //item[@id='item0']/location return " +
      "delete node $i/n");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test.
   * @throws Exception exception
   */
  @Test
  public void textMerging07() throws Exception {
    createDB(null);
    query("let $i := //item[@id='item0']/location return " +
      "insert node 'foo' after $i/text()");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test.
   * @throws Exception exception
   */
  @Test
  public void textMerging08() throws Exception {
    createDB(null);
    query("let $i := //item[@id='item0'] return " +
      "(insert node 'foo' after $i/location)");
    query("let $i := //item[@id='item0']/location return " +
      "(insert node 'foo' after $i, insert node 'faa' before $i, insert " +
      "node 'faa' into $i, delete node $i/text())");
    query("let $i := //item[@id='item0']/location " +
      "return ($i/text(), ($i/../text())[2])", "faafoofoo");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test
  public void textMerging09() {
    query(transform("<n>aa<d/><d/>cc</n>",
        "delete node $input//d, insert node 'bb' after ($input//d)[1]",
        "count($input//text()), $input//text()"), "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test
  public void textMerging10() {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
      "return (count($c//text()), $c//text())", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test
  public void textMerging11() {
    query(
      "copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
      "return (count($c//text()), $c//text())", "1aabbcc");
  }

  /**
   * Text node merging for replace.
   */
  @Test
  public void textMerging12() {
    final String doc = "<a>shouldBe<Single/>TextNode</a>";
    final String query = "replace node $input/Single with 'Single'";
    final String expected = "<a>shouldBeSingleTextNode</a>";
    checkTextNodeMerging(doc, query, expected, 1);
  }

  /**
   * Text node merging in multiple locations.
   */
  @Test
  public void textMerging13() {
    final String doc = "<n>T1<a/>T2<b/>T3<c/>T4<d/>T5<e/></n>";
    final String query =
        "replace node $input//e with 'T6'," +
        "insert node 'T11' before $input//a," +
        "delete node ($input//a,$input//b,$input//c), " +
        "replace node $input//d with (<newd/>,'T44')";
    final String expected = "<n>T1T11T2T3T4<newd/>T44T5T6</n>";
    checkTextNodeMerging(doc, query, expected, 2);
  }

  /**
   * Convenience method for text node merging.
   * @param input input document
   * @param query modification query
   * @param result expected result
   * @param textCount expected number of text nodes in result
   */
  private static void checkTextNodeMerging(final String input, final String query,
                                           final String result, final int textCount) {
    query(transform(input, query), result);
    assertEquals(textCount,
        Integer.parseInt(query(transform(input, query, "count($input//text())"))));
  }

  /**
   * Simple delete.
   */
  @Test
  public void delayedDistanceAdjustment000() {
    query(transform("<a><b/><c/></a>", "delete node $input/b"), "<a><c/></a>");
  }

  /**
   * Two simple deletes on two empty siblings.
   */
  @Test
  public void delayedDistanceAdjustment00() {
    query(transform("<a><b/><c/><d/></a>", "delete node ($input/b, $input/c)"),
        "<a><d/></a>");
  }

  /**
   * Tests the distance cache for replacements where the new nodes have
   * the same size as the replaced ones.
   */
  @Test
  public void delayedDistanceAdjustment0() {
    query("copy $c := <n><a/><n><a/><n/><a/></n><a/><n/><a/></n> " +
          "modify for $a in $c//a return replace node $a with <b/> " +
          "return $c",
          "<n><b/><n><b/><n/><b/></n><b/><n/><b/></n>");
  }

  /**
   * Tests if the common data instance for all insert sequences is built correctly.
   * [LK][CG] XQUF: maybe we should add some more low-level TCs ...
   */
  @Test
  public void dataClipBuildFail() {
    query("copy $c := <n><a/><a/></n> " +
        "modify for $a in $c//a return replace node $a with <b/> " +
        "return $c",
        "<n><b/><b/></n>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment1() throws BaseXException {
    createDB("<A><B><C/></B><D/></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XD/> before //D");
    query("/", "<A><XB/><B><XC/><C/></B><XD/><D/></A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment2() throws BaseXException {
    createDB("<A><B><C/></B><D><E/></D></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XD/> before //D, insert node <XE/> before //E");
    query("/", "<A><XB/><B><XC/><C/></B><XD/><D><XE/><E/></D></A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment3() throws BaseXException {
    createDB("<A><B><C/><D/></B></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XXC/> into //C");
    query("/", "<A><XB/><B><XC/><C><XXC/></C><D/></B></A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment4() throws BaseXException {
    createDB("<A><B><C/><D/></B><E/></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XXC/> into //C");
    query("/", "<A><XB/><B><XC/><C><XXC/></C><D/></B><E/></A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment5() throws BaseXException {
    createDB("<A><B><C/></B></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C");
    query("/", "<A><XB/><B><XC/><C/></B></A>");
  }

  /**
   * Distance caching tested for simple deletes.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment6() throws BaseXException {
    createDB("<A><B/><C/></A>");
    query("delete node //B");
    query("/", "<A><C/></A>");
  }

  /**
   * Distance caching tested for simple deletes.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment7() throws BaseXException {
    createDB("<A><B/><C/><D/><E/></A>");
    query("delete node (//B,//D)");
    query("/", "<A><C/><E/></A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment8() throws BaseXException {
    createDB("<A><B/><C><D/><E/></C></A>");
    query("delete node (//B,//D)");
    query("/", "<A><C><E/></C></A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment9() throws BaseXException {
    createDB("<A><B/><C><D/><E/><F/></C></A>");
    query("delete node (//B,//D)");
    query("/", "<A><C><E/><F/></C></A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment10() throws BaseXException {
    createDB("<A><B/><C><D/><E/></C><F/></A>");
    query("delete node (//B,//D)");
    query("/", "<A><C><E/></C><F/></A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment11() throws BaseXException {
    createDB("<A><B/><C/><D/><E/></A>");
    query("delete node (//C,//D)");
    query("/", "<A><B/><E/></A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment12() throws BaseXException {
    createDB("<A><B/><C/><D/><E/></A>");
    query("delete node (//B, //C), insert node <CNEW><X/></CNEW> before //D");
    query("/", "<A><CNEW><X/></CNEW><D/><E/></A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment13() throws BaseXException {
    createDB("<A><B><C/><D/></B></A>");
    query("insert node <X/> into //C," +
        "insert node <X><Y/></X> before //C," +
        "insert node <X/> after //D");
    query("/", "<A><B><X><Y/></X><C><X/></C><D/><X/></B></A>");
  }

  /**
   * Distance caching tested for neighboring inserts.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment14() throws BaseXException {
    createDB("<A><B><C/><D/></B></A>");
    query("insert node <X><Y/></X> into //C," +
        "insert node <X><Y/></X> before //C," +
        "insert node <X><Y/></X> after //D");
    query("/", "<A><B><X><Y/></X><C><X><Y/></X></C><D/><X><Y/></X></B></A>");
  }

  /**
   * Distance caching tested for neighboring inserts.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment15() throws BaseXException {
    createDB("<A><B/><C/></A>");
    query("insert node <X2/> before //C," +
        "insert node <X1/> after //B");
    query("/", "<A><B/><X1/><X2/><C/></A>");
  }

  /**
   * Distance caching tested for neighboring inserts.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment16() throws BaseXException {
    createDB("<A><B/><C><D/></C><E/></A>");
    query("insert node <X1/> into //C," +
        "insert node <X2/> as last into //C");
    query("/", "<A><B/><C><D/><X1/><X2/></C><E/></A>");
  }

  /**
   * Tests if pre cache is clear / free of ambiguous entries.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment17() throws BaseXException {
    createDB("<A><B/><C/><D/></A>");
    query("insert node <X/> after //B, delete node //C");
    query("/", "<A><B/><X/><D/></A>");
  }

  /**
   * Tests if pre cache is clear / free of ambiguous entries.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment18() throws BaseXException {
    createDB("<A><B/><C/><D/></A>");
    query("insert node <X/> before //D, delete node //C");
    query("/", "<A><B/><X/><D/></A>");
  }

  /**
   * Tests if pre cache is clear / free of ambiguous entries.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment19() throws BaseXException {
    createDB("<A><B/><C/><D/></A>");
    query("replace node //C with <X/>");
    query("/", "<A><B/><X/><D/></A>");
  }

  /**
   * Testing cached distance updates.
   */
  @Test
  public void delayedDistanceAdjustment20() {
    query("copy $c := <n><a/><a/><a/><a/><a/></n> " +
        "modify (replace node ($c//a)[1] with <b/>, " +
        "replace node ($c//a)[2] with <b/>, " +
        "insert node <c/> into ($c//a)[3], " +
        "replace node ($c//a)[4] with <b/>, " +
        "replace node ($c//a)[5] with <b/>) " +
        "return $c");
  }

  /**
   * Testing cached distance updates. Insert + replace statement on the same
   * target node.
   */
  @Test
  public void delayedDistanceAdjustment21() {
    query("copy $c := <n><a/><a/><a/><a/><a/></n> " +
        "modify (replace node ($c//a)[1] with <b/>, " +
        "replace node ($c//a)[2] with <b/>, " +
        "insert node <c/> into ($c//a)[3], " +
        "replace node ($c//a)[4] with <b/>, " +
        "replace node ($c//a)[5] with <b/>) " +
        "return $c");
  }

  /**
   * Testing distance caching when a node is deleted and there have been updates on the
   * descendant axis. Tests effect on following nodes.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment22() throws BaseXException {
    createDB("<A><B><C/><D/></B><E/></A>");
    query("insert node <X/> into //D, delete node //B");
    query("/", "<A><E/></A>");
  }

  /**
   * Tests if updates are executed in the correct order and if the sorting of updates
   * is stable.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment23() throws BaseXException {
    createDB("<A><B/></A>");
    query("insert node <P2/> into //B, insert node <P3/> into //A");
    query("/", "<A><B><P2/></B><P3/></A>");
  }

  /**
   * Tests if reordering of updates works correctly.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment24() throws BaseXException {
    createDB("<A><B/><C/><D/><E/></A>");
    query("insert node <X/> into //B, delete node //C");
    query("/", "<A><B><X/></B><D/><E/></A>");
  }

  /**
   * Tests if reordering of updates works correctly.
   * @throws BaseXException excp
   */
  @Test
  public void delayedDistanceAdjustment25() throws BaseXException {
    createDB("<A><B><C/></B><D/><E/></A>");
    query("insert node <X/> into //B, delete node //C");
    query("/", "<A><B><X/></B><D/><E/></A>");
  }

  /**
   * Tests if side-effecting updates within transform expressions are rejected.
   * Also includes db:output() and fn:put().
   */
  @Test
  public void dbUpdateTransform() {
    error("copy $c := <a/> modify db:output('x') return $c", Err.BASX_DBTRANSFORM);
    error("copy $c := <a/> modify db:add('" + NAME + "','<x/>','x.xml') return $c",
        Err.BASX_DBTRANSFORM);
    error("copy $c := <a/> modify put(<a/>, 'x.txt') return $c", Err.BASX_DBTRANSFORM);
  }

  /**
   * Replaces a node with two others.
   */
  @Test
  public void duplAttribute() {
    query("replace node document { <A><B/></A> }//B with (<X/>, <X/>)");
  }

  /**
   * Inserts attributes.
   */
  @Test
  public void attributeInserts() {
    // Issue #736
    query("declare namespace x='x';" +
        "let $x := <n01><n/><n/></n01> " +
        "for $n in $x//n " +
        "for $i in 1 to 16 " +
        "return insert node attribute {concat('x:', 'att', $i)} {} into  $n");
  }

  /**
   * Tests the combination of transform expressions and xquery:eval().
   */
  @Test
  public void evalFItem() {
    query("declare function local:c() { copy $a := <a/> modify () return $a };" +
      "xquery:eval('$c()', { 'c' : local:c#0 })", "<a/>");
  }

  /**
   * Tests the expressions in modify clauses for updates.
   */
  @Test
  public void modifyCheck() {
    error("copy $c:= <a>X</a> modify 'a' return $c", Err.UPMODIFY);
    error("copy $c:= <a>X</a> modify(delete node $c/text(),'a') return $c", Err.UPALL);
  }

  /** Tests adding an attribute and thus crossing the {@link IO#MAXATTS} line (GH-752). */
  @Test
  public void insertAttrMaxAtt() {
    query(
        transform(
            "<x a01='' a02='' a03='' a04='' a05='' a06='' a07='' a08='' a09='' a10=''" +
            "   a11='' a12='' a13='' a14='' a15='' a16='' a17='' a18='' a19='' a20=''" +
            "   a21='' a22='' a23='' a24='' a25='' a26='' a27='' a28='' a29='' a30=''/>",
            "insert node attribute { 'b' } { '' } into $input",
            "count($input/@*)"
        ),
        "31"
    );
  }

  /** Tests the experimental modify operator ("update"). */
  @Test
  public void modify() {
    query("let $c := <x/> return $c update ()", "<x/>");
    query("let $c := <x/> return $c update insert node <y/> into .", "<x><y/></x>");
  }

  /**
   * Tests the relaxed rule that the last step of a path may be simple, vacuous, or updating.
   */
  @Test
  public void updateLastStep() {
    query("<X/>!(delete node .)");
    query("<X/>/(delete node .)");
    query("(<X/>,<Y/>)/(insert node <Z/> into .)");
  }
}
