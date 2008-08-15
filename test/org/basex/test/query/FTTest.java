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
        { "Simple 1", bool(true),
          "'abc' ftcontains 'abc'" },
        { "Simple 2", bool(true),
          "'a b c' ftcontains 'b'" },
        { "Simple 3", bool(false),
          "'abc' ftcontains 'b'" },

        { "FT 1", nodes(14),
          "//w [text() ftcontains 'HELLO']" },
        { "FT 2", nodes(14),
          "//w [text() ftcontains 'hello']" },
        { "FT 3", nodes(14),
          "//w [text() ftcontains '    hello!...   ']" },
        { "FT 4", nodes(),
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

        { "FTPosFilter 1", nodes(3, 5, 9, 11),
          "//w [. ftcontains 'xml' at start]" },
        { "FTPosFilter 2", nodes(),
          "//w [. ftcontains 'databases' at start]" },
        { "FTPosFilter 3", nodes(9, 11),
          "//w [. ftcontains 'xml databases' at start]" },
        { "FTPosFilter 4", nodes(7, 9, 11),
          "//w [. ftcontains 'databases' ordered]" },
        { "FTPosFilter 5", nodes(7, 9, 11),
          "//w [. ftcontains 'xml' ftand 'databases' ordered]" },
        { "FTPosFilter 6", nodes(),
          "//w [. ftcontains 'databases' ftand 'xml' ordered]" },
        { "FTPosFilter 7", nodes(9, 11),
          "//w [. ftcontains 'xml' ftand 'databases' ordered at start]" },
        { "FTPosFilter 8", nodes(7, 9, 11),
          "//w [. ftcontains 'databases' at end]" },
        { "FTPosFilter 9", nodes(7, 9, 11),
          "//w [. ftcontains 'xml' ftand 'databases' at end]" },
        { "FTPosFilter 10", nodes(14),
          "//w [. ftcontains 'hello' entire content]" },
        { "FTPosFilter 11", nodes(9, 11),
          "//w [. ftcontains 'xml databases' entire content]" },

        { "FTIndex1", nodes(25, 29),
          "/fttest/fti [text() ftcontains 'wordt ook wel eens']" },
        
        { "FTMildNot1", nodes(3, 5),
        "//w [text() ftcontains 'xml' not in 'xml databases']" },
        { "FTMildNot2", nodes(14),
        "//w [text() ftcontains 'hello' not in 'xml']" },
        
        { "FTUnaryNot1", nodes(14),
        "//w [text() ftcontains ftnot 'xml']" },
        { "FTUnaryNot2", nodes(3, 5),
        "//w [text() ftcontains 'xml' ftand ftnot 'databases']" },
        { "FTUnaryNot3", nodes(3, 5, 9, 11),
        "//w [text() ftcontains 'xml' ftand ftnot 'databases' " +
          "case sensitive]" },
        { "FTUnaryNot4", nodes(7, 9, 11, 14),
        "//w [text() ftcontains 'databases' ftor ftnot 'xml']" },
        { "FTUnaryNot5", nodes(3, 5, 14),
        "//w [text() ftcontains 'hello' ftor ftnot 'databases']" },

    };

    /** TABLE REPRESENTATION
    PRE DIS SIZ ATS  KIND  CONTENT
      0   1  35   1  DOC   tmp
      1   1  34   1  ELEM  fttest
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
     25  24   2   1  ELEM  fti
     26   1   1   1  TEXT  adfas wordt ook wel eens
     27  26   2   1  ELEM  fti
     28   1   1   1  TEXT  wordt ook wel een s
     29  28   2   1  ELEM  fti
     30   1   1   1  TEXT  adfad wordt ook wel eens a
     31  30   2   1  ELEM  fti
     32   1   1   1  TEXT  adfad wordt ook wel een s adf
     33  32   2   1  ELEM  fti
     34   1   1   1  TEXT  adfad wordt ook wel een s
     **/
  }
}
