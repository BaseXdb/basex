package org.basex.test.query;

/**
 * XPathMark Simple Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public class FTTest extends AbstractTest {
  /** Constructor. */
  FTTest() {
    doc =
      "<?xml version='1.0' encoding='iso-8859-1'?>\n" +
      "<fttest>\n" +
      "  <co>\n" +
      "     <w>xml</w>\n" +
      "     <w>XML xml XmL</w>\n" +
      "     <w>xml databases</w>\n" +
      "     <w>XML DATABASES</w>\n" +
      "     <w>XML Databases</w>\n" +
      "  </co>\n" +
      "  <wc>\n" +
      "     <w>hello</w>\n" +
      "  </wc>\n" +

      "</fttest>";

    queries = new Object[][] {
        { "FTCaseOption 1", nodes(11),
          "/fttest/co/w [text() ftcontains 'XML Databases' case sensitive]" },
        { "FTCaseOption 2", nodes(9),
          "/fttest/co/w [text() ftcontains 'xml databases' uppercase]" },
        { "FTCaseOption 3", nodes(3, 5, 7),
          "/fttest/co/w [text() ftcontains 'XML' lowercase]" },
        { "FTCaseOption 4", nodes(5, 9, 11),
          "/fttest/co/w [text() ftcontains 'xml' uppercase]" },
        { "FTCaseOption 5", nodes(7),
          "/fttest/co/w [text() ftcontains 'XML DATABASES' lowercase]" },
        { "FTWildCard 1", nodes(14),
          "/fttest/wc/w [text() ftcontains '.ello' with wildcards]" },
        { "FTWildCard 2", nodes(14),
          "/fttest/wc/w [text() ftcontains 'hell.' with wildcards]" },
        { "FTWildCard 3", nodes(14),
          "/fttest/wc/w [text() ftcontains '.+llo' with wildcards]" },
        { "FTWildCard 4", nodes(14),
          "/fttest/wc/w [text() ftcontains 'hell.+' with wildcards]" },
        { "FTWildCard 5", nodes(14),
          "/fttest/wc/w [text() ftcontains '.*llo' with wildcards]" },
        { "FTWildCard 6", nodes(14),
          "/fttest/wc/w [text() ftcontains 'hel.*' with wildcards]" },
        { "FTWildCard 7", nodes(14),
          "/fttest/wc/w [text() ftcontains '.*' with wildcards]" },
        { "FTWildCard 8", nodes(14),
          "/fttest/wc/w [text() ftcontains '.+' with wildcards]" },
        { "FTAnyAllOption 1", nodes(3, 5, 7, 9, 11),
          "/fttest/co/w [text() ftcontains 'xml' any]" },
        { "FTAnyAllOption 2", nodes(3, 5, 7, 9, 11),
          "/fttest/co/w [text() ftcontains 'xml' all]" },
        { "FTAnyAllOption 3", nodes(3, 5, 7, 9, 11),
          "/fttest/co/w [text() ftcontains 'xml' any word]" },
        { "FTAnyAllOption 4", nodes(3, 5, 7, 9, 11),
          "/fttest/co/w [text() ftcontains 'xml' all words]" },
        { "FTAnyAllOption 5", nodes(3, 5, 7, 9, 11),
          "/fttest/co/w [text() ftcontains 'xml' phrase]" },
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
