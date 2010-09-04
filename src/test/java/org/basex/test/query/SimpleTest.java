package org.basex.test.query;

/**
 * Simple XQuery tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SimpleTest extends QueryTest {
  /** Constructor. */
  static {
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
      { "Int 1", itr(1), "1" },
      { "Int 2", itr(-1), "-1" },
      { "Int 3", itr(1234567890), "1234567890" },
      { "Double 1", dbl(1.1), "1.1e0" },
      { "Double 2", dbl(-1.1), "-1.1e0" },
      { "Double 3", dbl(1234567890.12), "1234567890.12e0" },
      { "String 1", str("string"), "\"string\"" },
      { "String 2", str(""), "\"\"" },
      { "Root 1", nod(0), "/" },
      { "Root 2", nod(0), "/." },
      { "Root 3", nod(), "/.." },
      { "Absolute 1", nod(1), "/*" },
      { "Absolute 2", nod(1), "/node()" },
      { "Child 1", nod(1), "*" },
      { "Child 2", nod(1), "node()" },
      { "Child 3", nod(2, 3, 7, 8, 24), "node()/node()" },
      { "Child 4", nod(17, 19), "/*/*/*/*" },
      { "Child 5", nod(0), "./." },
      { "Child 6", nod(24), "node()/node()[last()]" },
      { "Child Error 1", "./" },
      { "Child Error 2", "html/" },
      { "Desc 1", nod(20, 22), "//li" },
      { "Desc 2", nod(20, 22), "//ul/li" },
      { "Desc 3", nod(20, 22), "//ul//li" },
      { "Desc 4", nod(17, 19, 20, 22), "//*//*//*//*" },
      { "Desc 5", nod(1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18, 19, 20, 21,
          22, 23, 24), "//node()" },
      { "Desc 6", nod(0, 1, 2, 3, 5, 6, 7, 8, 13, 14, 15, 17, 18, 19, 20,
          21, 22, 23, 24), ".//." },
      { "Desc 7", nod(20, 22), ".//li" },
      { "Desc 8", nod(23), "/descendant-or-self::*[last()]/text()" },
      { "Desc 9", nod(23), "/descendant::*[last()]/text()" },
      { "Desc Error 1", "//" },
      { "Desc Error 2", "///" },
      { "Ancestor 1", nod(0), "/*/ancestor::node()" },
      { "Ancestor 2", nod(0, 1), "/*/*/ancestor::node()" },
      { "Pred 1", nod(1), "/*[/]" },
      { "Pred 2", nod(1), "/*[/*]" },
      { "Pred 3", nod(19), "//ul[li]" },
      { "Pred 4", nod(19), "//ul[li = 'Exercise 1']" },
      { "Pred 5", nod(20), "//ul/li[text() = 'Exercise 1']" },
      { "Pred 6", nod(20), "/html/body/div/ul/li[text() = 'Exercise 1']" },
      { "Pred 7", nod(8), "//*[@* = '#FFFFFF']" },
      { "Pred 8", nod(8), "//*[@id = 1]" },
      { "Pred 9", nod(5, 17), "//*[text() = 'XML' or text() = 'Assignments']" },
      { "Pred 10", nod(5, 17), "//*[text() = ('XML', 'Assignments')]" },
      { "Pred 11", nod(5), "//title[text() = .]" },

      { "Pred Error 1", "/[/]" },
      { "Pred Error 2", "/*[]" },
      { "Pred Error 3", "/*[//]" },
      { "Pred Error 4", "/*[li" },

      { "PosPred 1", nod(20), "//li[1]" },
      { "PosPred 2", nod(20), "//li[position() = 1]" },
      { "PosPred 3", nod(22), "//li[2]" },
      { "PosPred 4", nod(22), "//li[2][1]" },
      { "PosPred 5", nod(), "//li[2][2]" },
      { "PosPred 6", nod(8, 15, 19, 22), "//*[.[1]][2]" },
      { "PosPred 7", nod(1, 3, 5, 13, 17, 20), "//*[.[1]][1]" },
      { "PosPred 8", nod(), "//*[1.1]" },
      { "PosPred 9", nod(8, 15, 19, 22), "//*[position() > 1.1]" },
      { "PosPred A", nod(), "//*[position() <= 0.9]" },
      { "PosPred B", str("XML"), "(('XML')[1])[1]" },
      { "PosPred C", itr(1), "1[position() = 1 to 2]" },

      { "Prec 1", nod(3, 5), "//body/preceding::*" },
      { "Prec 2", nod(3, 5), "//@id/preceding::*" },

      { "Union 1", nod(0), ".|." },
      { "Union 2", nod(0), ". | ." },
      { "Union 3", nod(1), "*|*" },

      { "FLWOR 1", itr(3), "(for $i in 1 to 5 return $i)[3]" },
      { "FLWOR 2", itr(4),
        "(for $a in 1 to 5 for $b in 1 to 5 return $a * $b)[7]" },
      { "FLWOR 3", bool(true), "declare namespace x = 'X'; " +
        "let $a := <a>0</a> let $b := $a return $b = 0" },
      { "FLWOR 4", itr(1),
        "for $a in (1,2) let $b := 'a' where $a = 1 return $a" },
      { "FLWOR 5", itr(1, 2),
        "for $a in (1,2) let $b := 'a'[$a = 1] return $a" },
      { "FLWOR 6", nod(),
        "for $a in (1,2) let $b := 3 where $b = 4 return $a" },
      { "FLWOR 7", itr(1, 2),
        "for $a in (1,2) let $b := 3[. = 4] return $a" },
      { "FLWOR 8", itr(2),
        "for $a at $p in (1,2) where $a = 2 return $p" },
 
      { "CompForLet 1", itr(3, 3),
        "for $a in 1 to 2 let $b := 3 return $b" },
      { "CompForLet 2", itr(3, 3),
        "for $a in 1 to 2 let $b := 3 let $c := 3 return $c" },
      { "CompForLet 3", itr(4, 4),
        "for $a in 1 to 2 let $b := 3 let $b := 4 return $b" },
      { "CompForLet 4", itr(3),
        "for $a score $s in 1 let $s := 3 return $s" },
      { "CompForLet 4", itr(1),
        "for $a at $p in 1 let $s := $p return $s" },
        
      { "Index 1", nod(20), "//li[text() = 'Exercise 1']" },
      { "Index 2", nod(21), "//li[text() = 'Exercise 1']/text()" },
      { "Index 3", nod(5), "for $a in //title where $a = 'XML' return $a" },
      { "Index 4", nod(5), "for $a in //* where $a/text() = 'XML' return $a" },
      { "Index 5", nod(3, 5), "for $a in //* where $a = 'XML' return $a" },
      { "Index 6", nod(5), "//*[text() = 'XML' and text()]" },
      { "Index 7", nod(), "//*[text() = 'XM' and text()]" },
      { "Index 8", nod(5), "//title[text() = 'XML' or text()]" },
      { "Index 9", nod(5), "//title[text() = 'XM' or text()]" },

      { "ExtVar 1", itr(1), "declare variable $a external; 1" },
      { "ExtVar 2", "declare variable $a external; $a" },
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
