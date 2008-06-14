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
      "<fttest>\n" +
      "  <co>\n" +
      "     <w>xml</w>\n" +
      "     <w>XML xml XmL</w>\n" +
      "     <w>we have xml databases</w>\n" +
      "     <w>XML DATABASES</w>\n" +
      "     <w>XML &amp; Databases</w>\n" +
      "  </co>\n" +
      "  <wc>\n" +
      "     <w>hello</w>\n" +
      "  </wc>\n" +
      "  <sc>\n" +
      "     <s>diät-joghurt</s>\n" +
      "     <s>diat-joghurt</s>\n" +
      "  </sc>\n" +
      "  <at><b>B</b>ad one</at>\n" +
      "  <fti>adfas wordt ook wel eens</fti>" + 
      "  <fti>wordt ook wel een s</fti>" +
      "  <fti>adfad wordt ook wel eens a</fti>" +
      "  <fti>adfad wordt ook wel een s adf</fti>" +
      "  <fti>adfad wordt ook wel een s</fti>" +
      "</fttest>";

    queries = new Object[][] {
        { "FT 1", nodes(14),
          "//w [text() ftcontains 'HELLO']" },
        { "FT 2", nodes(14),
          "//w [text() ftcontains 'hello']" },
        { "FT 4", nodes(14),
          "//w [text() ftcontains '    hello!...   ']" },
        { "FT 5", nodes(),
          "//w [  text  (   )  ftcontains  '  crap  '  ]  " },

        { "Phrase 1", nodes(7, 9, 11),
          "//w [text() ftcontains 'xml databases']" },
        { "Phrase 2", nodes(7, 9, 11),
          "//w [text() ftcontains 'xml &amp; databases']" },
        { "Phrase 3", nodes(7, 9, 11),
          "//w [text() ftcontains 'xml :) databases :|']" },

        { "FTDiacritics 1", nodes(17, 19),
          "//s [text() ftcontains 'diat joghurt']" },
        { "FTDiacritics 2", nodes(17, 19),
          "//s [text() ftcontains 'diät joghurt' diacritics insensitive]" },
        { "FTDiacritics 3", nodes(17),
          "//s [text() ftcontains 'diät joghurt' diacritics sensitive]" },

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
        { "FTTimes 1", nodes(7, 9, 11),
          "//w [text() ftcontains 'DaTaBaSeS' occurs exactly 1 times]" },
        { "FTTimes 2", nodes(5),
          "//w [text() ftcontains 'XmL' occurs exactly 3 times]" },
        { "FTTimes 3", nodes(14),
          "//w [text() ftcontains 'xml' occurs exactly 0 times]" },
        { "FTTimes 4", nodes(5),
          "//w [text() ftcontains 'xml' occurs at least 3 times]" },
        { "FTTimes 5", nodes(3, 7, 9, 11, 14),
          "//w [text() ftcontains 'XmL' occurs at most 2 times]" },
        { "FTTimes 6", nodes(3, 7, 9, 11, 14),
          "//w [text() ftcontains 'XmL' occurs from 0 to 1 times]" },
          
        { "FTAndOr 1", nodes(7, 9, 11),
          "//w [text() ftcontains 'XmL' ftand 'Databases']" },
        { "FTAndOr 3", nodes(14),
          "//w [text() ftcontains 'HELLO' ftand 'hello']" },
        { "FTAndOr 2", nodes(7, 9, 11, 14),
          "//w [text() ftcontains 'databases' ftor 'hello']" },
        { "FTAndOr 4", nodes(7, 9, 11, 14),
          "//w [text() ftcontains 'xml' ftand 'databases'  ftor 'hello' ]" },
        { "FTAndOr 5", nodes(7, 9, 11),
          "//w [text() ftcontains 'databases' ftand ('xml' ftor 'hello')]" },

        { "FTStemming 1", nodes(7, 9, 11),
          "//w [text() ftcontains 'xml database' with stemming]" },
        { "FTStemming 2", nodes(),
          "//w [text() ftcontains 'xml database' without stemming]" },

        { "FTLanguage 1", nodes(14),
          "//* [text() ftcontains 'hello' language 'en']" },
        { "FTLanguage 2", // error...
          "//* [text() ftcontains 'hello' language 'jp']" },

        { "FTStopWords 1", nodes(7, 9, 11), "//* [text() ftcontains " +
          "'and databases' with stop words ('xml', 'and')]" },
        { "FTStopWords 2", nodes(),
          "//* [text() ftcontains 'and databases' with stop words ('and')]" },
        { "FTStopWords 3", nodes(7),
          "//* [text() ftcontains 'have xml' with stop words ('we', 'have')]" },

        { "FTAtomization 1", nodes(21),
          "//at [. ftcontains 'bad one']" },

        { "FTSelection 1", nodes(3, 5, 9, 11),
          "//w [. ftcontains 'xml' at start]" },
        { "FTSelection 2", nodes(),
          "//w [. ftcontains 'databases' at start]" },
        { "FTSelection 3", nodes(9, 11),
          "//w [. ftcontains 'xml databases' at start]" },
        { "FTSelection 4", nodes(7, 9, 11),
          "//w [. ftcontains 'xml databases' ordered]" },
        { "FTSelection 5", nodes(),
          "//w [. ftcontains 'databases xml' ordered]" },
        { "FTSelection 6", nodes(9, 11),
          "//w [. ftcontains 'xml databases' ordered at start]" },
        { "FTSelection 7", nodes(7, 9, 11),
          "//w [. ftcontains 'databases' at end]" },
        { "FTSelection 8", nodes(7, 9, 11),
          "//w [. ftcontains 'xml databases' at end]" },
        { "FTSelection 9", nodes(14),
          "//w [. ftcontains 'hello' entire content]" },
        { "FTSelection 10", nodes(9, 11),
          "//w [. ftcontains 'xml databases' entire content]" },

        { "FTIndex1", nodes(26, 30),
          "/fttest/fti [text() ftcontains 'wordt ook wel eens']" },
    };

    /** TABLE REPRESENTATION
    PRE DIS SIZ ATS  KIND  CONTENT
      0   1  25   1  DOC   tmp
      1   1  24   1  ELEM  fttest
      2   1  11   1  ELEM  co
      3   1   2   1  ELEM  w
      4   1   1   1  TEXT  xml
      5   3   2   1  ELEM  w
      6   1   1   1  TEXT  XML xml XmL
      7   5   2   1  ELEM  w
      8   1   1   1  TEXT  we have xml databases
      9   7   2   1  ELEM  w
     10   1   1   1  TEXT  XML DATABASES
     11   9   2   1  ELEM  w
     12   1   1   1  TEXT  XML & Databases
     13  12   3   1  ELEM  wc
     14   1   2   1  ELEM  w
     15   1   1   1  TEXT  hello
     16  15   5   1  ELEM  sc
     17   1   2   1  ELEM  s
     18   1   1   1  TEXT  diät-joghurt
     19   3   2   1  ELEM  s
     20   1   1   1  TEXT  diat-joghurt
     21  20   4   1  ELEM  at
     22   1   2   1  ELEM  b
     23   1   1   1  TEXT  B
     24   3   1   1  TEXT  ad one
     **/
  }
}
