package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Simple document tests. Node results are checked via their {@code pre} values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends SandboxTest {
  /** Creates the test database.
   * <pre>
   *  PRE PAR  TYPE  CONTENT           PRE PAR  TYPE  CONTENT
   *    0  -1  DOC   test.xml           13   8  ELEM  h1
   *    1   0  ELEM  html               14  13  TEXT  Databases &amp; XML
   *    2   1  COMM   Header            15   8  ELEM  div
   *    3   1  ELEM  head               16  15  ATTR  align="right"
   *    4   3  ATTR  id="0"             17  15  ELEM  b
   *    5   3  ELEM  title              18  17  TEXT  Assignments
   *    6   5  TEXT  XML                19  15  ELEM  ul
   *    7   1  COMM   Body              20  19  ELEM  li
   *    8   1  ELEM  body               21  20  TEXT  Exercise 1
   *    9   8  ATTR  id="1"             22  19  ELEM  li
   *   10   8  ATTR  bgcolor="#FFFFFF"  23  22  TEXT  Exercise 2
   *   11   8  ATTR  text="#000000"     24   1  PI    pi bogus
   *   12   8  ATTR  link="#0000CC"
   * </pre>
   */
  @BeforeAll public static void beforeClass() {
    set(MainOptions.STRIPWS, true);
    execute(new CreateDB(NAME, """
      <?xml version='1.0' encoding='iso-8859-1'?>
      <html>
        <!-- Header -->
        <head id='0'>
          <title>XML</title>
        </head>
        <!-- Body -->
        <body id='1' bgcolor='#FFFFFF' text='#000000' link='#0000CC'>
          <h1>Databases &amp; XML</h1>
          <div align = 'right' >
            <b>Assignments</b>
            <ul>
              <li>Exercise 1</li>
              <li>Exercise 2</li>
            </ul>
          </div>
        </body>
        <?pi bogus?>
      </html>"""));
  }

  /** Drops the test database. */
  @AfterAll public static void afterClass() {
    execute(new DropDB(NAME));
    set(MainOptions.STRIPWS, false);
  }

  /** Root steps. */
  @Test public void root() {
    pre("/", 0);
    pre("/.", 0);
    pre("/*", 1);
    pre("/node()", 1);
  }

  /** Child steps. */
  @Test public void child() {
    pre("*", 1);
    pre("node()", 1);
    pre("node()/node()", 2, 3, 7, 8, 24);
    pre("/*/*/*/*", 17, 19);
    pre("./.", 0);
    pre("node()/node()[last()]", 24);
    error("./", STEPMISS_X);
    error("html/", STEPMISS_X);
  }

  /** Parent and following steps. */
  @Test public void parentFollowing() {
    // should only return each element once, see GH-1001
    pre("/html/body/*/../*", 13, 15);
    pre("//li/parent::ul/li", 20, 22);
    pre("/*/node()/following-sibling::node()", 3, 7, 8, 24);
    pre("/*/node()/following::li", 20, 22);
  }

  /** Descendant steps. */
  @Test public void descendant() {
    pre("//li", 20, 22);
    pre("//ul/li", 20, 22);
    pre("//ul//li", 20, 22);
    pre("//*//*//*//*", 17, 19, 20, 22);
    pre("//node()", 1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24);
    pre(".//.", 0, 1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24);
    pre(".//li", 20, 22);
    pre("/descendant-or-self::*[last()]/text()", 23);
    pre("/descendant::*[last()]/text()", 23);
    error("//", STEPMISS_X);
    error("///", STEPMISS_X);
  }

  /** Ancestor steps. */
  @Test public void ancestor() {
    pre("/*/ancestor::node()", 0);
    pre("/*/*/ancestor::node()", 0, 1);
  }

  /** Predicates. */
  @Test public void predicate() {
    pre("/*[/]", 1);
    pre("/*[/*]", 1);
    pre("//ul[li]", 19);
    pre("//ul[li = 'Exercise 1']", 19);
    pre("//ul/li[text() = 'Exercise 1']", 20);
    pre("/html/body/div/ul/li[text() = 'Exercise 1']", 20);
    pre("//*[@* = '#FFFFFF']", 8);
    pre("//*[@id = 1]", 8);
    pre("//*[text() = 'XML' or text()='Assignments']", 5, 17);
    pre("//*[text() = ('XML', 'Assignments')]", 5, 17);
    pre("//title[text() = .]", 5);
    query("1[.]", 1);
    error("/*[]", INCOMPLETE);
    error("/*[//]", STEPMISS_X);
    error("/*[li", WRONGCHAR_X_X);
  }

  /** Predicates with index access. */
  @Test public void predicateIndex() {
    pre("//text()[. = 'Exercise 1']", 21);
    pre("//text()[. = 'Exercise 1']/..", 20);
    query("//@*[. = '1']/string()", 1);
    query("//@id[. = '1']/string()", 1);
    pre("//@id[. = '1']/..", 8);
    query("//@*[. = '#000000']/string()", "#000000");
    pre("//@id[. = '#000000']");
  }

  /** Positional predicates. */
  @Test public void positionalPredicate() {
    pre("//li[1]", 20);
    pre("//li[position() = 1]", 20);
    pre("//li[2]", 22);
    pre("//li[2][1]", 22);
    pre("//li[2][2]");
    pre("//*[.[1]][2]", 8, 15, 19, 22);
    pre("//*[.[1]][1]", 1, 3, 5, 13, 17, 20);
    pre("//*[1.1]");
    pre("//*[position() > 1.1]", 8, 15, 19, 22);
    pre("//*[position() <= 0.9]");
    pre("//li[last()][contains(text(), '1')]");
    pre("//li[last()][contains(text(), '2')]", 22);
  }

  /** Preceding steps. */
  @Test public void preceding() {
    pre("//body/preceding::*", 3, 5);
    pre("//@id/preceding::*", 3, 5);
  }

  /** Union. */
  @Test public void union() {
    pre(".|.", 0);
    pre(". | .", 0);
    pre("*|*", 1);
  }

  /** Text and range index access. */
  @Test public void index() {
    pre("//li[text() = 'Exercise 1']", 20);
    pre("//li[text() = 'Exercise 1']/text()", 21);
    pre("for $a in //title where $a = 'XML' return $a", 5);
    pre("for $a in //* where $a/text() = 'XML' return $a", 5);
    pre("for $a in //* where $a = 'XML' return $a", 3, 5);
    pre("//*[text() = 'XML' and text()]", 5);
    pre("//*[text() = 'XM' and text()]");
    pre("//title[text() = 'XML' or text()]", 5);
    pre("//title[text() = 'XM' or text()]", 5);
    pre("//*[@id = 1]", 8);
    pre("//*[@id >= 0 and @id <= 1]", 3, 8);
    query("//@id[. = 1]/string()", 1);
  }
}
