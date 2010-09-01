package org.basex.test.query;

/**
 * XQuery 1.1 tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XQuery11Test extends QueryTest {
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
        { "FLWOR 1", itr(1), "for $i in (1,1) group by $i return $i"},
        { "FLWOR 2", itr(1, 2), "for $i in (1, 2, 2, 1) group by $i return $i"},
        { "FLWOR 3", itr(1, 1, 2, 2, 2, 2, 1, 1),
           "for $x in (1, 2, 2, 1) for $y in ('a','a') group by $y return $x "},
        { "FLWOR 4", itr(1, 2, 1, 1, 2, 2),
           "for $x in (1, 2) for $y in ('b','a','a') group by $y return $x "},
        { "FLWOR 5", itr(1, 2),
           "for $a in 1 let $b := (1, 2) group by $a return $b" },

        { "FLWOR group varref", itr(2, 1),
           "for $x in (2,1) let $y:=($x+1) group by $y return $x"},
        { "GFLWOR varref ordered", itr(1, 2),
           "for $x in (2,1) let $y:=($x+1) group by $y order by $y return $x"},
        { "FLWOR group ngvar", itr(3, 2, 1),
             "for $x in (1,2,3) for $y in ('a') group by $x order by $y," +
             " $x descending return $x"
        },
        { "FLWOR Err 1", "let $x := (1,2) group by $x return $x" },
        { "FLWOR Err 2", "let $x := (1,2) group by $z return $x"},
        { "FLWOR Err 3", 
        "for $a in (1,1) let $b := $a group by $b order by $a return 1" },
        { "FLWOR Err 4", 
        "for $a in (1,1) let $b := $a group by $b order by $a return 1" },
        { "FLWOR 6", itr(2),
        "for $a in 1 for $a in 2 group by $a return $a" },
      { "FLWOR 7", itr(2, 3),
        "for $a in 1 for $a in (2,3) group by $a return $a" },
        
        /* [MS] to be checked...
      { "FLWOR Err 4", 
        "declare variable $a := 1; for $b in 1 group by $a return $b" },
        */
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
