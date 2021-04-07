package org.basex.query.up;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.node.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * General test of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class UpdateTest extends SandboxTest {
  /** Test document. */
  private static final String DOC = "src/test/resources/xmark.xml";

  /**
   * Creates a database.
   * @param input input database string; use default if {@code null}
   */
  private static void createDB(final String input) {
    execute(new CreateDB(NAME, input == null ? DOC : input));
  }

  /**
   * Closes the currently opened database.
   */
  @AfterEach public void finish() {
    execute(new Close());
  }

  // BASIC XQUF TESTS *********************************************
  /**
   * Basic delete.
   */
  @Test public void delete() {
    query(transform("<n><a><b/></a><c/><d><e><f/></e><g/></d></n>",
        "delete node $input//b, delete node $input//f, delete node $input//e"),
        "<n>\n<a/>\n<c/>\n<d>\n<g/>\n</d>\n</n>");
  }

  /**
   * Basic delete.
   */
  @Test public void delete2() {
    query(transform("<a><b/><c/><d/></a>",
        "delete node $input//b, delete node $input//c"),
        "<a>\n<d/>\n</a>");
  }

  /**
   * Basic delete.
   */
  @Test public void delete3() {
    final String doc =
        "<n1>" +
            "<n2/><n3/><n4/><n5/><n6/><n7/><n8/><n9/><n10/><n11/>" +
        "</n1>";
    query(transform(doc, "delete node ($input//n3, $input//n5, $input//n7)"),
        "<n1>\n<n2/>\n<n4/>\n<n6/>\n<n8/>\n<n9/>\n<n10/>\n<n11/>\n</n1>");
  }

  /** Transform expression shadowing another variable. */
  @Test public void shadowing() {
    query("let $c := <x/> return copy $c := $c modify () return $c", "<x/>");
    query("declare variable $d := document{ <x/> } update (); $d/x", "<x/>");
  }

  /** Transform expression containing a simple expression. */
  @Test public void transSimple() {
    error("<a/> update ('')", UPMODIFY);
    error("copy $a := <a/> modify ('') return $a", UPMODIFY);
  }

  /**
   * Checks whether existing indexes etc. are NOT recycled for the copy of a transform expression.
   */
  @Test public void transform2() {
    set(MainOptions.MAINMEM, true);
    try {
      execute(new CreateDB("DBtransform", "<instance><data><vocable hits='1'/></data></instance>"));
      query(
          "for $voc in 1 to 2 " +
          "let $xml := db:open('DBtransform') " +
          "return (" +
          "$xml update (" +
          "for $i in 1 to 2 return " +
          "insert node $xml/instance/data/vocable[@hits = '1'] into ./instance))",
          "<instance>\n<data>\n" +
          "<vocable hits=\"1\"/>\n</data>\n" +
          "<vocable hits=\"1\"/>\n" +
          "<vocable hits=\"1\"/>\n</instance>\n<instance>\n<data>\n" +
          "<vocable hits=\"1\"/>\n</data>\n" +
          "<vocable hits=\"1\"/>\n" +
          "<vocable hits=\"1\"/>\n</instance>");
    } finally {
      set(MainOptions.MAINMEM, false);
    }
  }

  /**
   * Basic insert into.
   */
  @Test public void insertinto() {
    query(transform("<n><a/></n>",
        "insert node <x2/> into $input, insert node <x1/> into $input//a"),
        "<n>\n<a>\n<x1/>\n</a>\n<x2/>\n</n>");
  }

  /**
   * Basic insert into.
   */
  @Test public void insertinto2() {
    query(transform("<n><a/><b/></n>",
        "insert node <x1/> into $input//a, insert node <x2/> into $input//b"),
        "<n>\n<a>\n<x1/>\n</a>\n<b>\n<x2/>\n</b>\n</n>");
  }

  /**
   * Tests {@link InsertIntoAsFirst} on T and a {@link DeleteNode} on the attribute of T.
   */
  @Test public void insertIntoAsFirst() {
    query(transform("<a att='0'/>",
        "insert node <b/> as first into $input, delete node $input/@att"),
        "<a>\n<b/>\n</a>");
  }

  /**
   * Tests {@link InsertIntoAsFirst} on T and a {@link ReplaceNode} on the attribute of T.
   */
  @Test public void insertIntoAsFirst2() {
    query(transform("<a att='0'/>",
        "insert node <b/> as first into $input, replace node $input/@att with " +
        "(attribute {'att1'}{0}, attribute {'att2'}{0})"),
        "<a att1=\"0\" att2=\"0\">\n<b/>\n</a>");
  }

  /**
   * Tests if insertion sequences are merged in a consistent way.
   */
  @Test public void insertSequenceMerging() {
    query(transform("<n/>",
        "insert node <e1/> into $input, insert node <e2/> into $input"),
        "<n>\n<e1/>\n<e2/>\n</n>");
    query(transform("<n/>",
        "insert node <e1/> as last into $input, insert node <e2/> as last into $input"),
        "<n>\n<e1/>\n<e2/>\n</n>");
    query(transform("<n/>",
        "insert node <e1/> into $input, insert node <e2/> as last into $input"),
        "<n>\n<e1/>\n<e2/>\n</n>");
    query(transform("<n/>",
        "insert node <e1/> as last into $input, insert node <e2/> into $input"),
        "<n>\n<e2/>\n<e1/>\n</n>");
  }

  /**
   * Delete last node of a data instance. Checks if table limits are crossed.
   */
  @Test public void deleteLastNode() {
    query(transform("<a><b/><c/></a>", "delete node $input//c"), "<a>\n<b/>\n</a>");
  }

  /**
   * Basic lazy replace test.
   */
  @Test public void lazyReplace() {
    query(transform("<n><r>a</r></n>", "replace node $input/r with <r>b</r>"),
        "<n>\n<r>b</r>\n</n>");
    query(transform("<n><r>a</r></n>", "replace node $input/r with <r>b</r>"),
        "<n>\n<r>b</r>\n</n>");
  }

  /**
   * ReplaceValue on attribute.
   */
  @Test public void replaceValue() {
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
  @Test public void replaceIdenticalValue() {
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
  @Test public void duplicateAttribute() {
    // check 'global' duplicate detection
    String q = transform("<x/>",
        "for $i in 1 to 2 return insert node attribute a { 'b' } into $input");
    error(q, UPATTDUPL_X);

    // check if insertion sequence itself is duplicate free (which it is not)
    q = transform("<x a='a'/>",
        "delete node $input/@a," +
        "for $i in 1 to 2 return insert node attribute a { 'b' } into $input");
    error(q, UPATTDUPL_X);

    // replace with a + delete a + insert a
    q = transform("<x a='a'/>",
        "delete node $input/@a, replace node $input/@a with attribute a {'b'}," +
        "insert node attribute a { 'b' } into $input");
    error(q, UPATTDUPL_X);
  }

  /**
   * Replace last node of a data instance. Checks if table limits are crossed.
   */
  @Test public void replaceLastNode() {
    query(transform("<a><b/><c><d/><d/><d/></c></a>", "replace node $input//c with <c/>"),
        "<a>\n<b/>\n<c/>\n</a>");
  }

  /**
   * Replaces an attribute with two attributes.
   */
  @Test public void replaceAttribute() {
    query(transform("<a att0='0'/>",
        "replace node $input/@att0 with (attribute att1 {'1'}, attribute att2 {'2'})"),
        "<a att1=\"1\" att2=\"2\"/>");
  }

  /**
   * Replaces a target T which is an attribute and has a following node with another
   * parent than T itself.
   */
  @Test public void replaceAttribute2() {
    query(transform("<a><b att0='0'/><c/></a>",
        "replace node $input/b/@att0 with attribute att1 {'1'}"),
        "<a>\n<b att1=\"1\"/>\n<c/>\n</a>");
  }

  /**
   * Replaces a single attribute with a sequence of two attributes to test if i.e. attribute sizes
   * are correctly adjusted.
   */
  @Test public void replaceAttribute3() {
    query(transform("<a id='0' id2='0'/>",
        "replace node $input/@id with (attribute b {'1'}, attribute c {'2'})"),
        "<a b=\"1\" c=\"2\" id2=\"0\"/>");
  }

  /**
   * TC tries to provoke multiple delete atomics on the same PRE within the same snapshot.
   *
   * Only delete primitives {@link DeleteNode} are allowed to create atomic delete
   * updates {@link AtomicUpdateCache}. This ensures that
   * no PRE value is deleted twice by an atomic delete, as the XQUF module resolves
   * multiple operations of the same type on the same node. Deleting a PRE value twice
   * would lead to deleting another node due to PRE shifts after the first delete.
   *
   * If another {@link NodeUpdate} leads to the deletion of a node (i.e.
   * {@link ReplaceValue} it must be substituted first by a sequence of other
   * {@link NodeUpdate} that contains the {@link DeleteNode} primitive.
   *
   */
  @Test public void noMultipleAtomicDeletes() {
    query(transform("<n>text<doNotDelete/></n>",
        "replace value of node $input//text() with \"\", " +
        "delete node $input//text()"),
        "<n>\n<doNotDelete/>\n</n>");
  }

  /**
   * Tests if the substitution of a {@link ReplaceNode} primitive does not interfere with
   * the update types used for substitution that are actually called by the user.
   */
  @Test public void replaceSubstitution() {
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
  @Test public void replaceelementcontent1() {
    query(transform("<n id='0'><a/><b><c/></b></n>",
        "replace value of node $input with 'Hello'"),
        "<n id=\"0\">Hello</n>");
  }

  /**
   * ReplaceElementContent with empty text node.
   */
  @Test public void replaceelementcontent2() {
    query(transform("<n id='0'><a/><b><c/></b></n>",
        "replace value of node $input with ''"),
        "<n id=\"0\"/>");
  }

  /**
   * ReplaceElementContent on a target T with a text node and insertBefore on a child of T.
   */
  @Test public void replaceelementcontent3() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "insert node <newA/> before $input/A");
  }

  /**
   * ReplaceElementContent on a target T with a text node and a replaceNode on a child
   * of T.
   */
  @Test public void replaceelementcontent4() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "replace node $input/A with <newA/>");
  }

  /**
   * ReplaceElementContent on a target T with a text node and insertInto on T.
   */
  @Test public void replaceelementcontent5() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "insert node <newA/> into $input");
  }

  /**
   * ReplaceElementContent on a target T with a text node and rename on a child of
   * T. As a rename does not introduce any new node identities the rename is lost and T
   * is expected to have no children after the end of the snapshot (except the new text
   * node).
   */
  @Test public void replaceelementcontent6() {
    testBothSequences("<n><A/></n>", "<n>newText</n>",
        "replace value of node $input with 'newText'",
        "rename node $input/A as 'newA'");
  }

  /**
   * ReplaceElementContent on a target T and replaceElementContent on the only child of
   * T.
   */
  @Test public void replaceelementcontent7() {
    testBothSequences("<n><A><B/></A></n>", "<n>newtextA</n>",
        "replace value of node $input with 'newtextA'",
        "replace value of node $input/A with 'newtextB'");
  }

  /**
   * ReplaceNode on a target T and replaceElementContent on the only child of T.
   */
  @Test public void replaceelementcontent8() {
    testBothSequences("<n><A><B/></A></n>", "<n>\n<newA/>\n</n>",
        "replace node $input/A with <newA/>",
        "replace value of node $input/A/B with 'newContentForA'");
  }

  /**
   * ReplaceNode and ReplaceElementContent on a target T.
   */
  @Test public void replaceelementcontent9() {
    testBothSequences("<n><A/></n>", "<n>\n<newA/>\n</n>",
        "replace node $input/A with <newA/>",
        "replace value of node $input/A with 'newContentForA'");
  }

  /**
   * Delete/replace same target.
   */
  @Test public void deleteAndReplaceOnSameTarget() {
    testBothSequences("<a><b/></a>", "<a>\n<newB/>\n</a>",
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
   */
  @Test public void replaceValueOfEmptyRoot() {
    createDB("<a/>");
    query("replace value of node /a with 'a'");
    query("/", "<a>a</a>");
  }

  /**
   * Insertion into an empty element.
   */
  @Test public void emptyInsert1() {
    query(
      "copy $x := <X/> modify insert nodes <A/> into $x return $x",
      "<X>\n<A/>\n</X>");
  }

  /**
   * Insertion into an empty document.
   */
  @Test public void emptyInsert2() {
    query(
      "copy $x := document {()} modify insert nodes <X/> into $x return $x",
      "<X/>");
  }

  /**
   * Insertion into an empty document.
   */
  @Test public void emptyInsert3() {
    createDB("<a/>");
    query("delete node /a");
    query("insert nodes <X/> into db:open('" + NAME + "')");
    query("/", "<X/>");
  }

  /**
   * Transform expression in function.
   */
  @Test public void transformFunc() {
    final String cmr = "copy $o := <x/> modify () return delete node <a/>";
    query(cmr);
    query("declare %updating function local:f() { " + cmr + " }; local:f()");
    query("declare function local:f() { copy $o := <x/> modify () return 1 }; local:f()", 1);

    error("declare function local:f() { " + cmr + " }; local:f()", UPNOT_X);
  }

  /**
   * Tests a simple call of the optimize command.
   */
  @Test public void optimize() {
    createDB(null);
    query("let $w := //item[@id = 'item0'] " +
      "return (if($w/@id) " +
      "then (delete node $w/@id, db:optimize('" + NAME + "')) else ())");
  }

  /** Variable from the inner scope shouldn't be visible. */
  @Test public void outOfScope() {
    error("let $d := copy $e := <a/> modify () return $e return $e", VARUNDEF_X);
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test public void setNSFlag() {
    query("declare namespace x='x';" +
      "copy $x := <x/> modify insert node attribute x:x {} into $x return $x",
      "<x xmlns:x=\"x\" x:x=\"\"/>");
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test public void setNSFlag2() {
    query("declare namespace x='x';" +
      "copy $x := <x><a/></x> " +
      "modify insert node attribute x:x {} into $x return $x/a",
      "<a xmlns:x=\"x\"/>");
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test public void setNSFlag3() {
    query("declare namespace x='x';" +
      "copy $x := <x><a/></x> " +
      "modify insert node attribute x:x {} into $x/a return $x/a",
      "<a xmlns:x=\"x\" x:x=\"\"/>");
  }

  /**
   * Text merging after a simple delete.
   */
  @Test public void textMerging00() {
    query(transform("<a>AA<b/>CC</a>", "delete node $input//b",
        "($input,count($input//text()))"),
        "<a>AACC</a>\n1");
  }

  /**
   * Text merging for a simple insert into statement.
   */
  @Test public void textMerging01() {
    createDB(null);
    query("insert node 'foo' into //item[@id='item0']/location");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging for a simple insert into statement in combination with an
   * insert before statement. Checks if pre value shifts are taken into account.
   */
  @Test public void textMerging02() {
    createDB(null);
    query("insert node 'foo' into //item[@id='item0']/location," +
      "insert node <a/> before //item[@id='item0']");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test. Test 'insert into as first' and whether pre value
   * shifts are taken into account correctly.
   */
  @Test public void textMerging03() {
    createDB(null);
    query("let $i := //item[@id='item0'] return (insert node 'foo' as " +
      "first into $i/location, insert node 'foo' before $i/location, " +
      "insert node 'foo' into $i/quantity)");
    query("let $i := //item[@id='item0'] return " +
      "(($i/location/text())[1], ($i/quantity/text())[1])",
      "fooUnited States\n1foo");
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   */
  @Test public void textMerging04() {
    createDB(null);
    query("let $i := //item[@id='item0'] return insert node 'foo' before $i/location/text()");
    query("let $i := //item[@id='item0'] return ($i/location/text())[1]", "fooUnited States");
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   */
  @Test public void textMerging05() {
    createDB(null);
    query("let $i := //item[@id='item0']/location return " +
      "(insert node 'foo' into $i, delete node $i/text())");
    query("//item[@id='item0']/location/text()", "foo");
  }

  /**
   * Text merging test.
   */
  @Test public void textMerging06() {
    createDB(null);
    query("let $i := //item[@id='item0']/location return " +
      "(insert node <n/> into $i, insert node 'foo' as last into $i)");
    query("let $i := //item[@id='item0']/location return delete node $i/n");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test.
   */
  @Test public void textMerging07() {
    createDB(null);
    query("let $i := //item[@id='item0']/location return insert node 'foo' after $i/text()");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test.
   */
  @Test public void textMerging08() {
    createDB(null);
    query("let $i := //item[@id='item0'] return (insert node 'foo' after $i/location)");
    query("let $i := //item[@id='item0']/location return " +
      "(insert node 'foo' after $i, insert node 'faa' before $i, insert " +
      "node 'faa' into $i, delete node $i/text())");
    query("let $i := //item[@id='item0']/location return ($i/text(), ($i/../text())[2])",
        "faa\nfoofoo");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test public void textMerging09() {
    query(transform("<n>aa<d/><d/>cc</n>",
        "delete node $input//d, insert node 'bb' after ($input//d)[1]",
        "count($input//text()), $input//text()"), "1\naabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test public void textMerging10() {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
      "return (count($c//text()), $c//text())", "1\naabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test public void textMerging11() {
    query(
      "copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
      "return (count($c//text()), $c//text())", "1\naabbcc");
  }

  /**
   * Text node merging for replace.
   */
  @Test public void textMerging12() {
    final String doc = "<a>shouldBe<Single/>TextNode</a>";
    final String query = "replace node $input/Single with 'Single'";
    final String expected = "<a>shouldBeSingleTextNode</a>";
    checkTextNodeMerging(doc, query, expected, 1);
  }

  /**
   * Text node merging in multiple locations.
   */
  @Test public void textMerging13() {
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
  @Test public void delayedDistanceAdjustment000() {
    query(transform("<a><b/><c/></a>", "delete node $input/b"), "<a>\n<c/>\n</a>");
  }

  /**
   * Two simple deletes on two empty siblings.
   */
  @Test public void delayedDistanceAdjustment00() {
    query(transform("<a><b/><c/><d/></a>", "delete node ($input/b, $input/c)"),
        "<a>\n<d/>\n</a>");
  }

  /**
   * Tests the distance cache for replacements where the new nodes have
   * the same size as the replaced ones.
   */
  @Test public void delayedDistanceAdjustment0() {
    query("copy $c := <n><a/><n><a/><n/><a/></n><a/><n/><a/></n> " +
          "modify for $a in $c//a return replace node $a with <b/> " +
          "return $c",
          "<n>\n<b/>\n<n>\n<b/>\n<n/>\n<b/>\n</n>\n<b/>\n<n/>\n<b/>\n</n>");
  }

  /**
   * Tests if the common data instance for all insert sequences is built correctly.
   */
  @Test public void dataClipBuildFail() {
    query("copy $c := <n><a/><a/></n> " +
        "modify for $a in $c//a return replace node $a with <b/> " +
        "return $c",
        "<n>\n<b/>\n<b/>\n</n>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   */
  @Test public void delayedDistanceAdjustment1() {
    createDB("<A><B><C/></B><D/></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XD/> before //D");
    query("/", "<A>\n<XB/>\n<B>\n<XC/>\n<C/>\n</B>\n<XD/>\n<D/>\n</A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   */
  @Test public void delayedDistanceAdjustment2() {
    createDB("<A><B><C/></B><D><E/></D></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XD/> before //D, insert node <XE/> before //E");
    query("/", "<A>\n<XB/>\n<B>\n<XC/>\n<C/>\n</B>\n<XD/>\n<D>\n<XE/>\n<E/>\n</D>\n</A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   */
  @Test public void delayedDistanceAdjustment3() {
    createDB("<A><B><C/><D/></B></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XXC/> into //C");
    query("/", "<A>\n<XB/>\n<B>\n<XC/>\n<C>\n<XXC/>\n</C>\n<D/>\n</B>\n</A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   */
  @Test public void delayedDistanceAdjustment4() {
    createDB("<A><B><C/><D/></B><E/></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C, " +
        "insert node <XXC/> into //C");
    query("/", "<A>\n<XB/>\n<B>\n<XC/>\n<C>\n<XXC/>\n</C>\n<D/>\n</B>\n<E/>\n</A>");
  }

  /**
   * Distance caching tested for inserts at different levels.
   */
  @Test public void delayedDistanceAdjustment5() {
    createDB("<A><B><C/></B></A>");
    query("insert node <XB/> before //B, insert node <XC/> before //C");
    query("/", "<A>\n<XB/>\n<B>\n<XC/>\n<C/>\n</B>\n</A>");
  }

  /**
   * Distance caching tested for simple deletes.
   */
  @Test public void delayedDistanceAdjustment6() {
    createDB("<A><B/><C/></A>");
    query("delete node //B");
    query("/", "<A>\n<C/>\n</A>");
  }

  /**
   * Distance caching tested for simple deletes.
   */
  @Test public void delayedDistanceAdjustment7() {
    createDB("<A><B/><C/><D/><E/></A>");
    query("delete node (//B,//D)");
    query("/", "<A>\n<C/>\n<E/>\n</A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   */
  @Test public void delayedDistanceAdjustment8() {
    createDB("<A><B/><C><D/><E/></C></A>");
    query("delete node (//B,//D)");
    query("/", "<A>\n<C>\n<E/>\n</C>\n</A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   */
  @Test public void delayedDistanceAdjustment9() {
    createDB("<A><B/><C><D/><E/><F/></C></A>");
    query("delete node (//B,//D)");
    query("/", "<A>\n<C>\n<E/>\n<F/>\n</C>\n</A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   */
  @Test public void delayedDistanceAdjustment10() {
    createDB("<A><B/><C><D/><E/></C><F/></A>");
    query("delete node (//B,//D)");
    query("/", "<A>\n<C>\n<E/>\n</C>\n<F/>\n</A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   */
  @Test public void delayedDistanceAdjustment11() {
    createDB("<A><B/><C/><D/><E/></A>");
    query("delete node (//C,//D)");
    query("/", "<A>\n<B/>\n<E/>\n</A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   */
  @Test public void delayedDistanceAdjustment12() {
    createDB("<A><B/><C/><D/><E/></A>");
    query("delete node (//B, //C), insert node <CNEW><X/></CNEW> before //D");
    query("/", "<A>\n<CNEW>\n<X/>\n</CNEW>\n<D/>\n<E/>\n</A>");
  }

  /**
   * Distance caching tested for deletes at different levels.
   */
  @Test public void delayedDistanceAdjustment13() {
    createDB("<A><B><C/><D/></B></A>");
    query("insert node <X/> into //C," +
        "insert node <X><Y/></X> before //C," +
        "insert node <X/> after //D");
    query("/", "<A>\n<B>\n<X>\n<Y/>\n</X>\n<C>\n<X/>\n</C>\n<D/>\n<X/>\n</B>\n</A>");
  }

  /**
   * Distance caching tested for neighboring inserts.
   */
  @Test public void delayedDistanceAdjustment14() {
    createDB("<A><B><C/><D/></B></A>");
    query("insert node <X><Y/></X> into //C," +
        "insert node <X><Y/></X> before //C," +
        "insert node <X><Y/></X> after //D");
    query("/", "<A>\n<B>\n<X>\n<Y/>\n</X>\n<C>\n<X>\n<Y/>\n</X>\n" +
        "</C>\n<D/>\n<X>\n<Y/>\n</X>\n</B>\n</A>");
  }

  /**
   * Distance caching tested for neighboring inserts.
   */
  @Test public void delayedDistanceAdjustment15() {
    createDB("<A><B/><C/></A>");
    query("insert node <X2/> before //C," +
        "insert node <X1/> after //B");
    query("/", "<A>\n<B/>\n<X1/>\n<X2/>\n<C/>\n</A>");
  }

  /**
   * Distance caching tested for neighboring inserts.
   */
  @Test public void delayedDistanceAdjustment16() {
    createDB("<A><B/><C><D/></C><E/></A>");
    query("insert node <X1/> into //C," +
        "insert node <X2/> as last into //C");
    query("/", "<A>\n<B/>\n<C>\n<D/>\n<X1/>\n<X2/>\n</C>\n<E/>\n</A>");
  }

  /**
   * Tests if pre cache is clear / free of ambiguous entries.
   */
  @Test public void delayedDistanceAdjustment17() {
    createDB("<A><B/><C/><D/></A>");
    query("insert node <X/> after //B, delete node //C");
    query("/", "<A>\n<B/>\n<X/>\n<D/>\n</A>");
  }

  /**
   * Tests if pre cache is clear / free of ambiguous entries.
   */
  @Test public void delayedDistanceAdjustment18() {
    createDB("<A><B/><C/><D/></A>");
    query("insert node <X/> before //D, delete node //C");
    query("/", "<A>\n<B/>\n<X/>\n<D/>\n</A>");
  }

  /**
   * Tests if pre cache is clear / free of ambiguous entries.
   */
  @Test public void delayedDistanceAdjustment19() {
    createDB("<A><B/><C/><D/></A>");
    query("replace node //C with <X/>");
    query("/", "<A>\n<B/>\n<X/>\n<D/>\n</A>");
  }

  /**
   * Testing cached distance updates.
   */
  @Test public void delayedDistanceAdjustment20() {
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
  @Test public void delayedDistanceAdjustment21() {
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
   */
  @Test public void delayedDistanceAdjustment22() {
    createDB("<A><B><C/><D/></B><E/></A>");
    query("insert node <X/> into //D, delete node //B");
    query("/", "<A>\n<E/>\n</A>");
  }

  /**
   * Tests if updates are executed in the correct order and if the sorting of updates
   * is stable.
   */
  @Test public void delayedDistanceAdjustment23() {
    createDB("<A><B/></A>");
    query("insert node <P2/> into //B, insert node <P3/> into //A");
    query("/", "<A>\n<B>\n<P2/>\n</B>\n<P3/>\n</A>");
  }

  /**
   * Tests if reordering of updates works correctly.
   */
  @Test public void delayedDistanceAdjustment24() {
    createDB("<A><B/><C/><D/><E/></A>");
    query("insert node <X/> into //B, delete node //C");
    query("/", "<A>\n<B>\n<X/>\n</B>\n<D/>\n<E/>\n</A>");
  }

  /**
   * Tests if reordering of updates works correctly.
   */
  @Test public void delayedDistanceAdjustment25() {
    createDB("<A><B><C/></B><D/><E/></A>");
    query("insert node <X/> into //B, delete node //C");
    query("/", "<A>\n<B>\n<X/>\n</B>\n<D/>\n<E/>\n</A>");
  }

  /**
   * Tests if pre value shifts are accumulated correctly for a mixture of structural and non-
   * structural updates.
   */
  @Test public void delayedDistanceAdjustment26() {
    query(
        "<n><x at1='0' at2='0'/><y at3='0'/><b/></n> update (" +
        "delete node .//@at1," +
        "delete node .//@at3," +
        "rename node (.//@at2)[1] as 'at2x')",
        "<n>\n<x at2x=\"0\"/>\n<y/>\n<b/>\n</n>");
  }

  /**
   * Tests if side-effecting updates within transform expressions are rejected.
   * Also includes update:output() and fn:put().
   */
  @Test public void dbUpdateTransform() {
    error("copy $c := <a/> modify update:output('x') return $c", BASEX_UPDATE);
    error("copy $c := <a/> modify db:add('" + NAME + "','<x/>','x.xml') return $c",
        BASEX_UPDATE);
    error("copy $c := <a/> modify put(<a/>, 'x.txt') return $c", BASEX_UPDATE);
  }

  /** Reject updating functions in built-in higher-order function. */
  @Test public void updatingHof() {
    error("apply(update:output#1, [<b/>])", FUNCUP_X);
    error("for-each(<a/>, update:output#1)", FUNCUP_X);
    error("for-each-pair(<a/>, <b/>, put#2)", FUNCUP_X);
  }

  /** Replaces a node with two others. */
  @Test public void duplAttribute() {
    query("replace node document { <A><B/></A> }//B with (<X/>, <X/>)");
  }

  /** Inserts attributes. */
  @Test public void attributeInserts() {
    // Issue #736
    query("declare namespace x='x';" +
        "let $x := <n01><n/><n/></n01> " +
        "for $n in $x//n " +
        "for $i in 1 to 16 " +
        "return insert node attribute {concat('x:', 'att', $i)} {} into  $n");
  }

  /** Tests the combination of transform expressions and xquery:eval(). */
  @Test public void evalFItem() {
    query("declare function local:c() { copy $a := <a/> modify () return $a };" +
      "xquery:eval('declare variable $c external; $c()', map { 'c': local:c#0 })", "<a/>");
  }

  /** Tests adding an attribute and thus crossing the {@link IO#MAXATTS} line. */
  @Test public void gh752() {
    query(
        transform(
            "<x a01='' a02='' a03='' a04='' a05='' a06='' a07='' a08='' a09='' a10=''" +
            "   a11='' a12='' a13='' a14='' a15='' a16='' a17='' a18='' a19='' a20=''" +
            "   a21='' a22='' a23='' a24='' a25='' a26='' a27='' a28='' a29='' a30=''/>",
            "insert node attribute { 'b' } { '' } into $input",
            "count($input/@*)"
        ),
        31
    );
  }

  /** Tests the "update" and "transform with" operators. */
  @Test public void update() {
    query("<x/> update ()", "<x/>");
    query("<x/> update insert node <y/> into .", "<x>\n<y/>\n</x>");
    query("<x/> update (insert node <y/> into .)", "<x>\n<y/>\n</x>");
    query("<x/> update { insert node <y/> into . }", "<x>\n<y/>\n</x>");
    query("<x/> update {}", "<x/>");
    query("<x/> update {} update {}", "<x/>");
    query("<x/> update {}", "<x/>");
    query("(<x/>,<y/>) update {}", "<x/>\n<y/>");
    error("update:output('a') update ()", UPNOT_X);
    error("<x/> update 1", UPMODIFY);

    query("(<a>X</a>/text() update {})/..", "");

    query("<x/> transform with {}", "<x/>");
    query("<x/> transform with { insert node <y/> into . }", "<x>\n<y/>\n</x>");
    error("update:output('a') transform with {}", UPNOT_X);
    error("<x/> transform with { 1 }", UPMODIFY);
  }

  /** Tests the relaxed rule that the last step of a path may be simple, vacuous, or updating. */
  @Test public void updateLastStep() {
    query("<X/>!(delete node .)");
    query("<X/>/(delete node .)");
    query("(<X/>,<Y/>)/(insert node <Z/> into .)");
  }

  /** Tests the expressions in modify clauses for updates. */
  @Test public void modifyCheck() {
    error("copy $c:= <a>X</a> modify 'a' return $c", UPMODIFY);
    error("copy $c:= <a>X</a> modify(delete node $c/text(),'a') return $c", UPALL);

    error("text { <a/> update (delete node <a/>,<b/>) }", UPALL);
    error("1[<a/> update (delete node <a/>,<b/>)]", UPALL);
    error("for $i in 1 order by (<a/> update (delete node <a/>,<b/>)) return $i", UPALL);
  }

  /** Reject updating function items. */
  @Test public void updatingFuncItems() {
    query("update:output(?)", "(anonymous-function)#1");
    query("update:output#1", "update:output#1");
    query("update:output#1, update:output#1", "update:output#1\nupdate:output#1");

    query("function() { () }()", "");
    query("declare updating function local:a() { () }; local:a#0", "local:a#0");
    query("declare function local:a() { local:a#0 };"
        + "declare updating function local:b() { update:output('1') }; local:a()", "local:a#0");
    query("updating function() { delete node <a/> }()");

    query("let $f := %updating function() {} return updating $f()", "");
    query("updating %updating function() {}()", "");
    query("declare function local:a() { () }; updating local:a#0()", "");

    error("function() { delete node <a/> }()", FUNCUP_X);
    error("updating %updating function() { 1 }()", UPEXPECTF);
    error("%updating function() { 1 }()", UPEXPECTF);

    error("update:output(?)(<a/>)", FUNCUP_X);
    error("update:output#1(<a/>)", FUNCUP_X);
    error("%updating function($a) { update:output($a) }(1)", FUNCUP_X);
    error("declare updating function local:a() { () }; local:a#0()", FUNCUP_X);

    query("declare function local:a() { local:b#0 };"
        + "declare updating function local:b() { update:output('1') }; updating local:a()()", 1);
    error("declare function local:a() { local:b#0 };"
        + "declare updating function local:b() { update:output('1') }; local:a()()", FUNCUP_X);

    error("updating count(?)(1)", FUNCNOTUP_X);
    error("updating count#1(1)", FUNCNOTUP_X);
    error("updating function($a) { count($a) }(1)", FUNCNOTUP_X);
    error("declare function local:a() { local:b#0 };"
        + "declare function local:b() { count('1') }; updating local:a()()", FUNCNOTUP_X);
  }

  /** Coercion of updating functions. */
  @Test public void gh1430() {
    query("let $f := "
        + "function($arg as %updating function(item()*) as empty-sequence()) { $arg } "
        + "return prof:void($f(function($e) { $e ! (delete node .) }))", "");
  }

  /** Test method. */
  @Test public void updateCheck() {
    query("declare function local:a() { fold-left((), (), function($a, $b) { local:a() }) };"
        + "declare function local:b() { () }; ()", "");
  }

  /** Test output URI is correctly resolved. */
  @Test public void put() {
    query("declare base-uri \"" + sandbox() + "\"; " + PUT.args(" <a/>", "test.xml"));
    query(_FILE_EXISTS.args(sandbox() + "test.xml"), true);
  }

  /** Allows empty-sequence() as return type for updating functions. */
  @Test public void returnType() {
    query("declare %updating function local:f() as empty-sequence() {delete node <a/>};local:f()");
    query("updating function() as empty-sequence() {delete node <a/>}()");
  }

  /** Window clauses. */
  @Test public void window() {
    query("for tumbling window $w in 1 start when true() return update:output($w)", 1);
    query("for sliding window $w in 1 start when true() end when true() "
        + "return update:output($w)", 1);
    error("for tumbling window $w in 1 start when delete node <a/> return ()", UPNOT_X);
    error("for tumbling window $w in 1 start when () end when delete node <a/> return ()", UPNOT_X);
    error("for tumbling window $w in update:output(1) start when () return ()", UPNOT_X);
  }

  /** Checks if updating expressions are treated like non-deterministic code. */
  @Test public void noOptimization() {
    query("<a/> update { . ! (insert node text { '1' } into .) }", "<a>1</a>");
    query("<a/> update { for $a in (1,2) return insert node text { '1' } into . }", "<a>11</a>");
    query("(update:output('1'), update:output('2'))", "1\n2");
  }

  /** Test. */
  @Test public void gh1576() {
    query("update:output([])", "[]");
    query("update:output([1,(2,[3,4])])", "[1, (2, [3, 4])]");
    query("update:output(map { })", "map {\n}");
    query("update:output(map { 1: map { 2: 3 }})", "map {\n1: map {\n2: 3\n}\n}");

    error("update:output(true#0)", BASEX_FUNCTION_X);
    error("update:output([true#0])", BASEX_FUNCTION_X);
    error("update:output([1,(2,[3,true#0])])", BASEX_FUNCTION_X);
    error("update:output(map { 1: map { 2: true#0 }})", BASEX_FUNCTION_X);
  }

  /** Test. */
  @Test public void gh1693() {
    query("try { () } catch * { () }, delete node <a/>");
  }

  /** Do not merge non-updating expressions. */
  @Test public void gh1943() {
    query("let $c := <a/> ! (. update {}, . update {}) return $c[1] is $c[2]", false);
    query("let $a := <a/> let $c := ($a update {}, $a update {}) return $c[1] is $c[2]", false);

    query("<a/> ! (copy $_ := . modify () return $_)", "<a/>");
    query("let $a := <a/> let $c := ("
        + "  copy $_ := $a modify () return $_,"
        + "  copy $_ := $a modify () return $_"
        + ") return $c[1] is $c[2]", false);
  }

  /** Simple map, update checks. */
  @Test public void gh1957() {
    query("declare %updating function local:f() { (1, 2) ! prof:void(.) }; local:f()", "");
  }

  /** Simple map, update checks. */
  @Test public void gh1978() {
    createDB(null);
    set(MainOptions.ADDCACHE, true);
    try {
      final String doc = "<_>qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
          + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqsqqquqqqqqsqqsqqq"
          + "yqqsqqqqqqqqsqsqqqqqqqqqquqqquqqsqqqqqquqyuqqqqsqqquqqqqquqs"
          + "sqqqqqqsqqsqqqqsqqqqqqqqsqqqqqyqqqqsqqsqqqsqqqquqqquqqqqqquq"
          + "qqqqqqquqqqsqqqqqsuqqsqsqqqsqqqsqqsqqqqsqqqsqqqqqqqqsqsqqqsq"
          + "qqqqqqssqqsqqsqqquqqqquqqqqqqqqqsqququqqqsqqqququqqqqqqqsqqq"
          + "qsqqsqqqqqqqqquqqqqqqqqqqquqsqsqqqqqqqqqssqqsqyuqqqqsqqquqqq"
          + "qqqqqqqqqqqqqsqqqqqqqqqqqqqququuyqqsqqqqqqqqsqqqqussqqsqqsqq"
          + "qqyqqqsqqqqsqqqqqqsqqqsusqyqsqqqqqsqqqqqqqssqqqqqqqqsqsquuss"
          + "quuqqqyqqquqqqquqqqqqqqsqqusqsqqqssqquqqqsqqqquusqqqsqqusqsq"
          + "qqqqqqqqqquqqussqqqqyqqqsqyqqsqqqqqqqqqsqqqyqqsqqqqsqqqqsqqs"
          + "qsqqqqqqququqqqsqqqqqqqqququqqqqqqqsqqqqsqqqqqqqqququuyqqquq"
          + "sqsqqqqqqqqqquuqqqqqssuqqssqqqsqsqqqqqqqqqqqqqqqsqqqqquqqqqq"
          + "ssqqsqyuqqqqsqqquqqqqsqqqqqquuqsqqqqqqsqqqqqqquqqquqqssqqqsq"
          + "sqqqqqquqqqqqqqsququqqqqqqysqqqqqqqqqsqqqqqqqqsqqquuqqqqsqqs"
          + "qqqqqqqqqqqqqqsqqqqqquqqqqsqqsqqqqqqqqqsqqqqqqqqsqqsqqqqqqsq"
          + "qqyqqsqqqqqqsqqqqqqsqqqqsqqsqqqqsquqqsqyqqqqqqqsqqqqqqsququq"
          + "qqqsqqqssqqsqqqqqqqqqqquqqqssqqsusqqquqsqqquqqqsqqsqsqqsquqq"
          + "qqqqssqqusqsssqsqqqqqqsqqsqsquqqyqqqqsqqqqqqqqqqqusqsqqqsqqq"
          + "suqqssqqqssqqqqsssqqsqqqqqsqqqqqquqqqqqsqqsqquqqqqqqyqqqqqqq"
          + "qqqqsqqqqssququqqqqqqqqsqqqsqqqquqqqqqsqqqqqqqsqqqqsqqsqsqqq"
          + "qqqqququqqyqqqquqsqqqqqqqqqqsqqqqsqqqqquqqqqqssqqsqqqqsqqqqq"
          + "uqququqsqqsqqqqquqqqququqsqqqqqqsqqsqqqqqsquyqqqqqqqqqqqqsqq"
          + "qssqqqsqqqqqqqqqquqqqssqqsusqqqqqqqsqqqqsqqsqsqqqyqqqqqqqqss"
          + "qsqqsqqqqsqqqquqququqsqqsqqqquqqqququqsqqqqqqqussqsqqsqqqqqs"
          + "quyqqsqqqqsqqqqqqqqqqqsqqqyqqsquqqqqqqqsqqsqsqqqqqqququqqqsq"
          + "qqqqqqqququqqqqqqqqquqqqqqqqqqqqququuyqqquqsqsqqqqqqqqqquuqq"
          + "qqqssuqqssqqqsqsqqqqqqqqqqqqqqsqqqqquqqqqqssqqsqyuqqqqsqqquq"
          + "qqqsqqqqqquuqsqqqqqqsqqqqqqquqqquqqssqqqsqsqqqqquqqqqqqqsquq"
          + "uqqqqqqysqqqqqqqqqsqqqqqqqqsqqquuqqqqqqqqqsqqqqquqquqqqqqqqq"
          + "qqqqsqqqqqqqqsqqsqqqqqqsqqqyqqqqsquqqsqyqqqqqqqsqsqqqqqqsqqq"
          + "qqqsqqqqsqqsqqqqqqsququqqqqsqqqssqqsqqqqqqqqqqquqqqssqqsusqq"
          + "quqsqqquqqqsqqsqsqqsqqqqqqqqqsqqqqquqquqqqqqsquqqqqqqsuqqssq"
          + "qqssqqqqsssqqqssqqusqsssqsqqqqqqsqqqqqusqsqqqqssququqqqqqqqq"
          + "sqqqsqsquqqyqqqqsqqqqqqqquqqqqqqyqqqsqqqqqqqqqqssqqqqqqqqsqq"
          + "qqsqqqqquqqqqqssqqqsqqqqsqqsqsqqqqqssqqqqqqqqqqqsququuqqqssq"
          + "qqqqqqqsqqqqsqqqqquqqqqqssqqsqqquqququqsqqsqqqququqsqqqqqqsq"
          + "qsqqqqqsquyqqqqqqqqqqqqsqqqssqqqsqqqqqqqqqquqqqssqqsuqqqsqqs"
          + "qqqqussqqqsqqqyqqsqqqqsqqqqsqqsqsqqqqququqqqsqqqqqqququqqqsq"
          + "qqsqqqyqqqqqqsqqqqsqqqqqqqqququuyqqquqsqsqqqqqqqqqqqqqqqqqqq"
          + "qqqqqqsqqqqqquqqqqqssqqsqyuqqqqsqqquqqqqqqqqsqyqqqqqqqqqsqqq"
          + "qqssuqqqqqqysqqqqqqqqqqqsqqqqussqqsqqqqsqqsqsqqqqququqqqsqqq"
          + "qqqququqqquqsqqquqqqqqsusqyqqqqsqqqqqqqquqsqsqqqqqqqqqqsquqq"
          + "qssqsqqsqqqqqqqysqqquuqqqqquqqqqqqsqqqsqqqyqqqqsqqqqqqsqqqqq"
          + "qqqqqusqqqqsqqsqqqqqqqqqqqqsqqqqqqqqsqqqqqqqsqqqqqqsqqqyqqqu"
          + "uqqqquqqqqqqqqsqqqqqqsqqqqqqqqsqsqqusqqqqsqqqyqqqyqqqqqsqqsq"
          + "qqqqsqqqqqqqqsqsqqsqqsqyqqqqqqqqqqqsqsqqqqqqqqqsqqqquqqqqquq"
          + "uqsqqsqqqququqsqqqsqqqqsqqsqqqqqsqqqqqqqqsqqqsqqqsququqqsqqs"
          + "qyqqqqqqqqsqqqqqqqqqsqqqquqqqqqqququqsqqquqsqqquqqqsqqsqsqqs"
          + "qqqsqqsqqquqsqsqqqqqqqqquqqqqqsqqusqqqqqsqqquuyqqsquqqqqqqqq"
          + "qqqqqssqsqqqqqqsqqsqqqqqqqqsqqqqsqqqqquqqqqqssqqsqqqqqqsqqqq"
          + "qqqqqqqqssququqqqqqqqqsqqqqqqquqqqqqqqqqqqssqssqqqsqquqqqqqq"
          + "yqqsqqsqqqqqsquyqqsqqqqsqqqqsqqqqsqqsqsqqsqqsquqsqsssqqqqqqq"
          + "qsqqqqsqqqqquqqqqqssqqsquqqqqqqqqqqququqqyqqqquqsqqqqsqquuqq"
          + "ssqqqqqsqqsqsqququqsqqqqqqqqqqqqquqqqqqqqsqqqqquqqqqqsqqqqqq"
          + "qqqqqququqsqqqqqqqququqsqqqssqsqqsqqqqquqqqqququqsqqsqqqququ"
          + "qsqqqsqqqqqssqqqqqqqqqqqsququuqqqssqqsqqqqqsqqqqqquqqqqququq"
          + "sqqsqqqququqsqqsqqsqqqqqsquyqqsqqqqsqqqqsqqqqsqqsqsqqssssqsq"
          + "uqsqqqquqsqsssqsqqqqqqqqqsqqqqqqqqqsqquqsqqqqsqquqqqqqququqs"
          + "qssuyqqsqqqqqsqqquqsqsqqqqqqqqqsqqqqqquqqqqqqqqussqqqsqqqyqq"
          + "squqqqqqqsqqsqsqqqqququqqqsqqqqqqququqqqqqqqqquqqqqqqqqqqqqu"
          + "quuyqqquqsqsqqqqqqqqqqqqqqqqqqqqqqqqqqqqsqqqqqquqqqqqqqqssqq"
          + "sqyuqqqqsqqquqqqqqsqqqqqssuqqqqqqysqqqqqqqqqqqsqqqqussqqsqqq"
          + "uuquqqqqqsqqqqqqqqquqqqqqqqsqqqqsqqsqsqququqsqqsqqsqsqqqququ"
          + "qsqqsqqqqqqqqquqqqqqqqsqqqqqqqqqqqsqqqqqqqqsqqsqqqqqqsqqqyqq"
          + "qqqqqqsququqqsqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
          + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
          + "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
          + "qqqqqqqqqqqqqqqqqqq</_>";
      execute(new Replace("x.xml", doc));
      query("string-length(_)", 4099);
    } finally {
      set(MainOptions.ADDCACHE, false);
    }
  }

  /** db:replace, ADDCACHE option. */
  @Test public void gh1989() {
    createDB("<a/>");
    query("db:replace('" + NAME + "', 'Sandbox.xml', '" + DOC + "', map { 'addcache': true() })");
    query("db:exists('" + NAME + ".0')", false);
  }

  /** Update expression, context position. */
  @Test public void gh2002() {
    query("(<a/>, <b/>) update { insert node position() into . }", "<a>1</a>\n<b>2</b>");
  }
}
