package org.basex.test;

/**
 * XPathMark Simple Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public class XPathSimpleTest extends AbstractTest {
  /**
   * Constructor.
   */
  XPathSimpleTest() {
    title = "Simple";

    doc =
      "<?xml version='1.0' encoding='iso-8859-1'?>\n" +
      "<html>\n" +
      "  <!-- Header -->\n" +
      "  <head id='0'>\n" +
      "    <title>XML</title>\n" +
      "  </head>\n" +
      "  <!-- Body -->\n" +
      "  <body id='1' bgcolor='#FFFFFF' text='#000000' link='#0000CC'>\n" +
      "    <h1>Databases &amp; XML</h1>\n" +
      "    <div align = 'right' >\n" +
      "      <b>Assignments</b>\n" +
      "      <ul>\n" +
      "        <li>Exercise 1</li>\n" +
      "        <li>Exercise 2</li>\n" +
      "      </ul>\n" +
      "    </div>\n" +
      "  </body>\n" +
      "  <?pi bogus?>\n" +
      "</html>";

    queries = new Object[][] {
      { "Boolean 1", "false()", bool(false) },
      { "Boolean 2", "true()", bool(true) },
      { "Int 1", "1", num(1) },
      { "Int 2", "1.0", num(1) },
      { "Int 3", "-1.0", num(-1) },
      { "Int 4", "1234567890", num(1234567890) },
      { "Double 1", "1.1", num(1.1) },
      { "Double 2", "-1.1", num(-1.1) },
      { "Double 3", "1234567890.12", num(1234567890.12) },
      { "String 1", "\"string\"", string("string") },
      { "String 2", "\"\"", string("") },
      { "Root 1", "/", nodes(0) },
      { "Root 2", "/.", nodes(0) },
      { "Root 3", "/..", nodes() },
      { "Absolute 1", "/*", nodes(1) },
      { "Absolute 2", "/node()", nodes(1) },
      { "Child 1", "*", nodes(1) },
      { "Child 2", "node()", nodes(1) },
      { "Child 3", "node()/node()", nodes(2, 3, 7, 8, 24) },
      { "Child 4", "/*/*/*/*", nodes(17, 19) },
      { "Child 5", "./.", nodes(0) },
      { "Child 6", "node()/node()[last()]", nodes(24) },
      { "Child Error 1", "./" },
      { "Child Error 2", "html/" },
      { "Desc 1", "//li", nodes(20, 22) },
      { "Desc 2", "//ul/li", nodes(20, 22) },
      { "Desc 3", "//ul//li", nodes(20, 22) },
      { "Desc 4", "//*//*//*//*", nodes(17, 19, 20, 22) },
      { "Desc 5", "//node()", nodes(1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18,
          19, 20, 21, 22, 23, 24) },
      { "Desc 6", ".//.", nodes(0, 1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18,
          19, 20, 21, 22, 23, 24) },
      { "Desc 7", ".//li", nodes(20, 22) },
      { "Desc 8", "/descendant-or-self::*[last()]/text()", nodes(23) },
      { "Desc 9", "/descendant::*[last()]/text()", nodes(23) },
      { "Desc Error 1", "//" },
      { "Desc Error 2", "///" },
      { "Ancestor 1", "/*/ancestor::node()", nodes(0) },
      { "Ancestor 2", "/*/*/ancestor::node()", nodes(0, 1) },
      { "Attribute 1", "//@*", nodes(4, 9, 10, 11, 12, 16) },
      { "Attribute 2", "//*/attribute::node()", nodes(4, 9, 10, 11, 12, 16) },
      { "Index 1", "//li[text() = 'Exercise 1']", nodes(20) },
      { "Index 2", "//li[text() = 'Exercise 1']/text()", nodes(21) },
      { "Pred 1", "/*[/]", nodes(1) },
      { "Pred 2", "/*[/*]", nodes(1) },
      { "Pred 3", "//li[1]", nodes(20) },
      { "Pred 4", "//li[position() = 1]", nodes(20) },
      { "Pred 5", "//li[2]", nodes(22) },
      { "Pred 6", "//li[2][1]", nodes(22) },
      { "Pred 7", "//li[2][2]", nodes() },
      { "Pred 8", "//*[.[1]][2]", nodes(8, 15, 19, 22) },
      { "Pred 9", "//*[.[1]][1]", nodes(1, 3, 5, 13, 17, 20) },
      { "Pred A", "//*[.[1]/.[1]]", nodes(1, 3, 5, 8, 13, 15, 17, 19, 20, 22) },
      { "Pred B", "//ul[li]", nodes(19) },
      { "Pred Error 1", "/[/]" },
      { "Pred Error 2", "/*[]" },
      { "Pred Error 3", "/*[//]" },
      { "Pred Error 4", "/*[li" },
      { "Union 1", ".|.", nodes(0) },
      { "Union 2", ". | .", nodes(0) },
      { "Union 3", "*|*", nodes(1) },
      { "Func 1 Error", "count()" },
      { "Func 2 Error", "count(1, 1)" },
      { "Func 3 Error", "contains(.)" },
      { "Func 3 Error", "contains(. .)" },
      
      // Full-Text queries
      //{ "Fulltext 1", "'abc' ftcontains 'abc'", bool(true) },
      { "Fulltext 2", "//li[text() ftcontains 'Exercise']", nodes(20, 22) },
      { "Fulltext 3", "//li[text() ftcontains '1']", nodes(20) },
    };

    /** TABLE REPRESENTATION
    PRE PAR  TYPE  CONTENT
      0  -1  DOC   test.xml
      1   0  ELEM  html
      2   1  COMM   Header 
      3   1  ELEM  head
      4   3  ATTR  id="0"
      5   3  ELEM  title
      6   5  TEXT  XML
      7   1  COMM   Body 
      8   1  ELEM  body
      9   8  ATTR  id="1"
     10   8  ATTR  bgcolor="#FFFFFF"
     11   8  ATTR  text="#000000"
     12   8  ATTR  link="#0000CC"
     13   8  ELEM  h1
     14  13  TEXT  Databases & XML
     15   8  ELEM  div
     16  15  ATTR  align="right"
     17  15  ELEM  b
     18  17  TEXT  Assignments
     19  15  ELEM  ul
     20  19  ELEM  li
     21  20  TEXT  Exercise 1
     22  19  ELEM  li
     23  22  TEXT  Exercise 2
     24   1  PI    pi bogus
    **/
  }
}
