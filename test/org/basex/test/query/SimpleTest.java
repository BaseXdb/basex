package org.basex.test.query;

import org.basex.core.AProp;

/**
 * XPathMark simple tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class SimpleTest extends AbstractTest {
  /** Constructor. */
  SimpleTest() {
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
      { "Boolean 1", bool(false), "false()" },
      { "Boolean 2", bool(true), "true()" },
      { "Int 1", itr(1), "1" },
      { "Int 2", itr(-1), "-1" },
      { "Int 3", itr(1234567890), "1234567890" },
      { "Double 1", dec(1.1), "1.1" },
      { "Double 2", dec(-1.1), "-1.1" },
      { "Double 3", dec(1234567890.12), "1234567890.12" },
      { "String 1", string("string"), "\"string\"" },
      { "String 2", string(""), "\"\"" },
      { "Root 1", nodes(0), "/" },
      { "Root 2", nodes(0), "/." },
      { "Root 3", nodes(), "/.." },
      { "Absolute 1", nodes(1), "/*" },
      { "Absolute 2", nodes(1), "/node()" },
      { "Child 1", nodes(1), "*" },
      { "Child 2", nodes(1), "node()" },
      { "Child 3", nodes(2, 3, 7, 8, 24), "node()/node()" },
      { "Child 4", nodes(17, 19), "/*/*/*/*" },
      { "Child 5", nodes(0), "./." },
      { "Child 6", nodes(24), "node()/node()[last()]" },
      { "Child Error 1", "./" },
      { "Child Error 2", "html/" },
      { "Desc 1", nodes(20, 22), "//li" },
      { "Desc 2", nodes(20, 22), "//ul/li" },
      { "Desc 3", nodes(20, 22), "//ul//li" },
      { "Desc 4", nodes(17, 19, 20, 22), "//*//*//*//*" },
      { "Desc 5", nodes(1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18, 19, 20, 21,
          22, 23, 24), "//node()" },
      { "Desc 6", nodes(0, 1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18, 19, 20,
          21, 22, 23, 24), ".//." },
      { "Desc 7", nodes(20, 22), ".//li" },
      { "Desc 8", nodes(23), "/descendant-or-self::*[last()]/text()" },
      { "Desc 9", nodes(23), "/descendant::*[last()]/text()" },
      { "Desc Error 1", "//" },
      { "Desc Error 2", "///" },
      { "Ancestor 1", nodes(0), "/*/ancestor::node()" },
      { "Ancestor 2", nodes(0, 1), "/*/*/ancestor::node()" },
      { "Attribute 1", nodes(4, 9, 10, 11, 12, 16), "//@*" },
      { "Attribute 2", nodes(4, 9, 10, 11, 12, 16), "//*/attribute::node()" },
      { "Index 1", nodes(20), "//li[text() = 'Exercise 1']" },
      { "Index 2", nodes(21), "//li[text() = 'Exercise 1']/text()" },
      { "Pred 1", nodes(1), "/*[/]" },
      { "Pred 2", nodes(1), "/*[/*]" },
      { "Pred 3", nodes(19), "//ul[li]" },
      { "Pred 4", nodes(19), "//ul[li = 'Exercise 1']" },
      { "Pred 5", nodes(20), "//ul/li[text() = 'Exercise 1']" },
      { "Pred 6", nodes(20), "/html/body/div/ul/li[text() = 'Exercise 1']" },
      { "Pred Error 1", "/[/]" },
      { "Pred Error 2", "/*[]" },
      { "Pred Error 3", "/*[//]" },
      { "Pred Error 4", "/*[li" },
      { "PosPred 1", nodes(20), "//li[1]" },
      { "PosPred 2", nodes(20), "//li[position() = 1]" },
      { "PosPred 3", nodes(22), "//li[2]" },
      { "PosPred 4", nodes(22), "//li[2][1]" },
      { "PosPred 5", nodes(), "//li[2][2]" },
      { "PosPred 6", nodes(8, 15, 19, 22), "//*[.[1]][2]" },
      { "PosPred 7", nodes(1, 3, 5, 13, 17, 20), "//*[.[1]][1]" },
      { "PosPred 8", nodes(), "//*[1.1]" },
      { "PosPred 9", nodes(8, 15, 19, 22), "//*[position() > 1.1]" },
      { "PosPred A", nodes(), "//*[position() <= 0.9]" },
      { "PosPred B", string("XML"), "(('XML')[1])[1]" },
      { "Prec 1", nodes(3, 5), "//body/preceding::*" },
      { "Prec 2", nodes(3, 5), "//@id/preceding::*" },
      { "Union 1", nodes(0), ".|." },
      { "Union 2", nodes(0), ". | ." },
      { "Union 3", nodes(1), "*|*" },
      { "Func 1 Error", "count()" },
      { "Func 2 Error", "count(1, 1)" },
      { "Func 3 Error", "contains(.)" },
      { "Func 3 Error", "contains(. .)" }
    };
  }

  @Override
  String details(final AProp prop) { return ""; }

  /* TABLE REPRESENTATION
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
  */
}
