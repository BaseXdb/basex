package org.basex.query.simple;

import org.basex.query.*;

/**
 * Simple document tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends QueryTest {
  /** Constructor. */
  static {
    create(
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
      "</html>");

    queries = new Object[][] {
      { "Root 1", nodes(0), "/" },
      { "Root 2", nodes(0), "/." },
      { "Root 3", nodes(1), "/*" },
      { "Root 4", nodes(1), "/node()" },

      { "Child 1", nodes(1), "*" },
      { "Child 2", nodes(1), "node()" },
      { "Child 3", nodes(2, 3, 7, 8, 24), "node()/node()" },
      { "Child 4", nodes(17, 19), "/*/*/*/*" },
      { "Child 5", nodes(0), "./." },
      { "Child 6", nodes(24), "node()/node()[last()]" },
      { "Child Error 1", "./" },
      { "Child Error 2", "html/" },

      // should only return each element once, see GH-1001
      { "Parent 1", nodes(13, 15), "/html/body/*/../*" },
      { "Parent 2", nodes(20, 22), "//li/parent::ul/li" },
      { "Following 1", nodes(3, 7, 8, 24), "/*/node()/following-sibling::node()" },
      { "Following-Sibling 1", nodes(20, 22), "/*/node()/following::li" },

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

      { "Pred 1", nodes(1), "/*[/]" },
      { "Pred 2", nodes(1), "/*[/*]" },
      { "Pred 3", nodes(19), "//ul[li]" },
      { "Pred 4", nodes(19), "//ul[li = 'Exercise 1']" },
      { "Pred 5", nodes(20), "//ul/li[text() = 'Exercise 1']" },
      { "Pred 6", nodes(20), "/html/body/div/ul/li[text() = 'Exercise 1']" },
      { "Pred 7", nodes(8), "//*[@* = '#FFFFFF']" },
      { "Pred 8", nodes(8), "//*[@id = 1]" },
      { "Pred 9", nodes(5, 17), "//*[text() = 'XML' or text()='Assignments']" },
      { "Pred A", nodes(5, 17), "//*[text() = ('XML', 'Assignments')]" },
      { "Pred B", nodes(5), "//title[text() = .]" },
      { "Pred C", integers(1), "1[.]" },

      { "PredIndex 1", nodes(21), "//text()[. = 'Exercise 1']" },
      { "PredIndex 2", nodes(20), "//text()[. = 'Exercise 1']/.." },
      { "PredIndex 3", strings("1"), "//@*[. = '1']/string()" },
      { "PredIndex 4", strings("1"), "//@id[. = '1']/string()" },
      { "PredIndex 5", nodes(8), "//@id[. = '1']/.." },
      { "PredIndex 6", strings("#000000"), "//@*[. = '#000000']/string()" },
      { "PredIndex 7", empty(), "//@id[. = '#000000']" },

      { "Pred Error 1", "/*[]" },
      { "Pred Error 2", "/*[//]" },
      { "Pred Error 3", "/*[li" },

      { "PosPred 1", nodes(20), "//li[1]" },
      { "PosPred 2", nodes(20), "//li[position() = 1]" },
      { "PosPred 3", nodes(22), "//li[2]" },
      { "PosPred 4", nodes(22), "//li[2][1]" },
      { "PosPred 5", empty(), "//li[2][2]" },
      { "PosPred 6", nodes(8, 15, 19, 22), "//*[.[1]][2]" },
      { "PosPred 7", nodes(1, 3, 5, 13, 17, 20), "//*[.[1]][1]" },
      { "PosPred 8", empty(), "//*[1.1]" },
      { "PosPred 9", nodes(8, 15, 19, 22), "//*[position() > 1.1]" },
      { "PosPred A", empty(), "//*[position() <= 0.9]" },
      { "PosPred B", strings("XML"), "(('XML')[1])[1]" },
      { "PosPred C", integers(1), "1[position() = 1 to 2]" },
      { "PosPred D", empty(), "//li[last()][contains(text(), '1')]" },
      { "PosPred D", nodes(22), "//li[last()][contains(text(), '2')]" },

      { "Prec 1", nodes(3, 5), "//body/preceding::*" },
      { "Prec 2", nodes(3, 5), "//@id/preceding::*" },

      { "Union 1", nodes(0), ".|." },
      { "Union 2", nodes(0), ". | ." },
      { "Union 3", nodes(1), "*|*" },

      { "Index 1", nodes(20), "//li[text() = 'Exercise 1']" },
      { "Index 2", nodes(21), "//li[text() = 'Exercise 1']/text()" },
      { "Index 3", nodes(5), "for $a in //title where $a = 'XML' return $a" },
      { "Index 4", nodes(5), "for $a in //* where $a/text() = 'XML' return $a" },
      { "Index 5", nodes(3, 5), "for $a in //* where $a = 'XML' return $a" },
      { "Index 6", nodes(5), "//*[text() = 'XML' and text()]" },
      { "Index 7", empty(), "//*[text() = 'XM' and text()]" },
      { "Index 8", nodes(5), "//title[text() = 'XML' or text()]" },
      { "Index 9", nodes(5), "//title[text() = 'XM' or text()]" },

      { "RangeIndex 1", nodes(8), "//*[@id = 1]" },
      { "RangeIndex 2", nodes(3, 8), "//*[@id >= 0 and @id <= 1]" },
      { "RangeIndex 3", strings("1"), "//@id[. = 1]/string()" },
    };
  }

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
