package org.basex.test.query;

import org.basex.BaseX;
import org.basex.core.Prop;

/**
 * XPathMark Simple Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTTest extends AbstractTest {
  @Override
  String details() {
    final StringBuilder sb = new StringBuilder();
    sb.append(set("ftindex", Prop.ftindex) + ";");
    sb.append(set("ftfuzzy", Prop.ftfuzzy) + ";");
    sb.append(set("ftittr", Prop.ftittr) + ";");
    sb.append(set("ftst", Prop.ftst) + ";");
    sb.append(set("ftdc", Prop.ftdc) + ";");
    sb.append(set("ftcs", Prop.ftcs));
    return sb.toString();
  }
  
  /**
   * Returns a flag string.
   * @param key key
   * @param val value
   * @return string
   */
  private String set(final String key, final boolean val) {
    return "set " + key + " " + BaseX.flag(val);
  }
  
  /** Constructor. */
  FTTest() {
    doc =
      "<fttest>\n" +
      "  <co>\n" +
      "     <w>xml in the first sentence. second sentence. " +
      "third sentence. fourth sentence. fifth sentence.</w>\n" + 
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
      "  <fti>adfas wordt. ook wel eens</fti>" + 
      "  <fti>wordt ook wel een s</fti>" +
      "  <fti>adfad. wordt\nook wel.eens a</fti>" +
      "  <fti>adfad wordt. ook\nwel een s adf</fti>" +
      "  <fti>adfad wordt ook. wel een s</fti>" +
      "  <atr key='value'/>" +
      "  <w>the fifth sentence. fourth sentence. " +
      "third sentence. second sentence. first sentence.</w>\n" +
      "  <wld/>\n" +
      "  <wld>yeah</wld>\n" +
      "  <mix>A<sub/>B</mix>\n" +
      "  <mix>B<sub/>A</mix>\n" +
      "</fttest>";

    queries = new Object[][] {
      { "Simple 1", bool(true),
        "'abc' ftcontains 'abc'" },
      { "Simple 2", bool(true),
        "'a b c' ftcontains 'b'" },
      { "Simple 3", bool(false),
        "'abc' ftcontains 'b'" },
      { "Simple 4", nodes(22),
        "//b['true' ftcontains 'true']" },
      { "Simple 5", bool(true),
        "//@key ftcontains 'value'" },
      { "Simple 6", nodes(36),
        "//@key[. ftcontains 'value']" },
      { "Simple 7", bool(false),
        "//@key ftcontains 'values'" },
      { "Simple 8", bool(true),
        "number('100') + 23 ftcontains '123'" },
      { "Simple 9", bool(true),
        "true() ftcontains 'true'" },
      { "Simple 10", bool(true),
        "false() ftcontains 'false'" },
      { "Simple 11", bool(true),
        "'text' ftcontains ''" },
        
      { "FT 1", nodes(14),
        "//w[text() ftcontains 'HELLO']" },
      { "FT 2", nodes(14),
        "//w[text() ftcontains 'hello']" },
      { "FT 3", nodes(14),
        "//w[text() ftcontains '    hello!...   ']" },
      { "FT 4", nodes(),
        "//w[  text  (   )  ftcontains  '  crap  '  ]  " },
      { "FT 5", nodes(),
        "//w[text() ftcontains 'db']" },
      { "FT 6", nodes(42, 46),
        "//mix[text() ftcontains 'A']" },
      { "FT 7", nodes(14),
        "//w[text() ftcontains 'hello']['A' ftcontains 'A']" },
      { "FT 8", nodes(14),
        "//w[text() ftcontains 'hello']['X' ftcontains 'X' with fuzzy]" },
      { "FT 9", nodes(14),
        "//w[text() = 'hello' and 'X' ftcontains 'X']" },
      { "FT 10", nodes(14),
        "//w[text() = 'hello' and text() ftcontains 'hello']" },
      { "FT 11", nodes(40),
        "//wld[text() ftcontains '']" },

      { "Preds 1", nodes(7, 9, 11),
        "//w[text() ftcontains 'xml'][text() ftcontains 'Databases']" },
      { "Preds 2", nodes(35),
        "//fttest[co/w ftcontains 'xml'][w ftcontains 'fifth']/atr" },
      { "Preds 3", nodes(1),
        "//fttest[*/text() ftcontains 'ook'][*/text() ftcontains 'een']" },
      { "Preds 4", nodes(1),
        "*[*/text() ftcontains 'ook'][*/text() ftcontains 'een']" },
      { "Preds 5", nodes(7),
        "//*[text() ftcontains 'have'][text() ftcontains 'xml']" },
      { "Preds 6", nodes(13),
        "//*[*/text() ftcontains 'hello'][*/text() = 'hello']" },
      { "Preds 7", nodes(7),
        "//w[text()[. ftcontains 'have xml'] ftcontains 'Databases']" },
      { "Preds 8", nodes(5),
        "/descendant::w[text() ftcontains 'xml'][2]" },
      { "Preds 9", nodes(3),
        "//w[text() ftcontains 'xml'][1]" },
      { "Preds 10", nodes(5),
        "//w[text() ftcontains 'xml'][2]" },
      { "Preds 11", nodes(14),
        "//wc/w[text() ftcontains 'hello']" },
      { "Preds 12", nodes(46),
        "//mix[text()[1] ftcontains 'B']" },
        
      { "AndOr 1", nodes(7, 9, 11),
        "//w[text() ftcontains 'xml' and text() ftcontains 'databases']" },
      { "AndOr 2", nodes(2),
        "//*[*/text() ftcontains 'have' and */text() ftcontains 'first']" },
      { "AndOr 3", nodes(25, 29),
        "//fti[text() ftcontains 'eens' or text() ftcontains 'a']" },
      { "AndOr 4", nodes(25, 29),
        "//fti[text() ftcontains 'eens' and text() ftcontains 'ook' or " +
        "text() ftcontains 'a']" },
      { "AndOr 5", nodes(31),
        "//fti[text() ftcontains 'adf s' or text() ftcontains 's adf']" },
      { "AndOr 6", nodes(31),
        "//fti[contains(text(), 'adf') and text() ftcontains 'adf']" },
      { "AndOr 7", nodes(3),
        "//*[text() ftcontains 'sentence' and text() ftcontains 'xml']" },
      { "AndOr 8", nodes(42, 46),
        "//mix[text() ftcontains 'A'][text() ftcontains 'B']" },
        
      { "Phrase 1", nodes(7, 9, 11),
        "//w [text() ftcontains 'xml databases']" },
      { "Phrase 2", nodes(7, 9, 11),
        "//w [text() ftcontains 'xml &amp; databases']" },
      { "Phrase 3", nodes(7, 9, 11),
        "//w [text() ftcontains 'xml :) databases :|']" },
      { "Phrase 4", nodes(),
        "//w [text() ftcontains 'xml db']" },
      { "Phrase 5", nodes(25, 29),
        "/fttest/fti [text() ftcontains 'wordt ook wel eens']" },

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
      { "FTWildCard 9", nodes(5, 9, 11),
        "/fttest/co/w [text() ftcontains 'X.+' " +
        "with wildcards case sensitive]" },
      { "FTWildCard 10", nodes(5, 9, 11),
        "/fttest/co/w [text() ftcontains 'x.+' with wildcards uppercase]" },
      { "FTWildCard 11", nodes(40),
        "/fttest/wld [text() ftcontains '.*' with wildcards]" },
      { "FTWildCard 12", nodes(40),
        "/fttest/wld [text() ftcontains '.+' with wildcards]" },

      { "FTFuzzy 1", nodes(7, 9, 11),
        "//* [text() ftcontains 'Database' with fuzzy]" },
      { "FTFuzzy 2", nodes(7, 9, 11),
        "//* [text() ftcontains 'Databaze' with fuzzy]" },
      { "FTFuzzy 3", nodes(),
        "//* [text() ftcontains 'Databasing' with fuzzy]" },

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
      { "FTTimes 3", nodes(14, 37),
        "//w [text() ftcontains 'xml' occurs exactly 0 times]" },
      { "FTTimes 4", nodes(5),
        "//w [text() ftcontains 'xml' occurs at least 3 times]" },
      { "FTTimes 5", nodes(3, 7, 9, 11, 14, 37),
        "//w [text() ftcontains 'XmL' occurs at most 2 times]" },
      { "FTTimes 6", nodes(3, 7, 9, 11, 14, 37),
        "//w [text() ftcontains 'XmL' occurs from 0 to 1 times]" },
      { "FTTimes 7", nodes(5),
        "//w [text() ftcontains 'xml xml' occurs at least 2 times]" },
      { "FTTimes 8", nodes(),
        "//w [text() ftcontains 'xml xml' occurs at least 4 times]" },
        
      { "FTAndOr 1", nodes(7, 9, 11),
        "//w [text() ftcontains 'XmL' ftand 'Databases']" },
      { "FTAndOr 2", nodes(7, 9, 11, 14),
        "//w [text() ftcontains 'databases' ftor 'hello']" },
      { "FTAndOr 3", nodes(14),
        "//w [text() ftcontains 'HELLO' ftand 'hello']" },
      { "FTAndOr 4", nodes(7, 9, 11, 14),
        "//w [text() ftcontains 'xml' ftand 'databases' ftor 'hello' ]" },
      { "FTAndOr 5", nodes(7, 9, 11),
        "//w [text() ftcontains 'databases' ftand ('xml' ftor 'hello')]" },
      { "FTAndOr 6", nodes(31, 33),
        "//fti [text() ftcontains 'adfad' ftand 'wordt' ftand 'ook' " +
        "ftand 'wel' ftand 'een' ftand 's']" },
      { "FTAndOr 7", nodes(),
        "//* [text() ftcontains 'databases' ftand 'db']" },

      { "FTStemming 1", nodes(7, 9, 11),
        "//w [text() ftcontains 'xml database' with stemming]" },
      { "FTStemming 2", nodes(),
        "//w [text() ftcontains 'xml database' without stemming]" },
      { "FTStemming 3", nodes(7, 9, 11),
        "//w [text() ftcontains 'xml' ftand 'databasing' with stemming]" },
      { "FTStemming 4", nodes(7, 9, 11, 14),
        "//w [text() ftcontains 'hello' ftor 'database' with stemming]" },
      { "FTStemming 5", nodes(3, 5, 14, 37),
        "//w [text() ftcontains ftnot 'database' with stemming]" },
      { "FTStemming 6", bool(true),
        "'base' ftcontains 'bases' with stemming" },
      { "FTStemming 7", bool(true),
        "'bases' ftcontains ('base') with stemming" },
      { "FTStemming 8", bool(true),
        "'base' ftcontains ('bases') with stemming" },
      { "FTStemming 9", bool(true),
        "'base' ftcontains ('bases' with stemming) without stemming" },
                  
      { "FTLanguage 1", nodes(14),
        "//* [text() ftcontains 'hello' language 'en']" },
      { "FTLanguage 2", // error...
        "//* [text() ftcontains 'hello' language 'jp']" },

      { "FTStopWords 1", nodes(7, 9, 11), "//* [text() ftcontains " +
        "'and databases' with stop words ('xml', 'and')]" },
      { "FTStopWords 2", nodes(7),
        "//* [text() ftcontains 'we use xml' with stop words ('use')]" },

      { "FTAtomization 1", nodes(21),
        "//at [. ftcontains 'bad one']" },
      { "FTAtomization 2", nodes(35),
        "//atr [@key ftcontains 'value']" },

      { "FTOrdered 1", nodes(7, 9, 11),
        "//w [. ftcontains 'databases' ordered]" },
      { "FTOrdered 2", nodes(7, 9, 11),
        "//w [. ftcontains 'xml' ftand 'databases' ordered]" },
      { "FTOrdered 3", nodes(),
        "//w [. ftcontains 'databases' ftand 'xml' ordered]" },
      { "FTOrdered 4", bool(true),
        "'A B' ftcontains ('A' ftand 'B' ordered)" },
      { "FTOrdered 5", bool(true),
        "'A B' ftcontains ('A' ftand 'B') ftor ('C' ftand 'D') ordered" },
      { "FTOrdered 6", bool(true),
        "'C D' ftcontains ('A' ftand 'B') ftor ('C' ftand 'D') ordered" },
      { "FTOrdered 7", bool(true),
        "'A B C D' ftcontains ('B' ftand 'A' ordered) ftor " +
        "('C' ftand 'D' ordered) ordered" },
      /* not quite sure about the following ones..
      { "FTOrdered 8", bool(true),
        "'A B' ftcontains ('B' ftor 'A') ordered" },
      { "FTOrdered 9", bool(true),
        "'A B C' ftcontains ('A' ftor 'C') ftand 'B' ordered" },
        */
      { "FTOrdered 10", bool(false),
        "'B A' ftcontains ('A' ftand ftnot 'B') ordered" },
        
      { "FTDistance 1", nodes(3),
        "//w [text() ftcontains 'the' ftand 'fourth' " +
        "distance exactly 2 sentences]" },
      { "FTDistance 2", nodes(3, 37),
        "//w [. ftcontains 'first' ftand 'second' ftand 'third' " +
          "distance exactly 1 words]" },
      { "FTDistance 3", nodes(3, 37),
        "//w [. ftcontains 'first sentence' ftand 'third sentence' " +
        "distance exactly 2 words]" },
      { "FTDistance 4", nodes(3),
        "//w [. ftcontains 'the first sentence' ftand 'third sentence' " +
        "distance exactly 2 words]" },
      { "FTDistance 5", nodes(3, 37),
        "//w [. ftcontains 'sentence' ftand 'the' " +
        "distance exactly 1 words]" },
      { "FTDistance 6", nodes(3, 37),
        "//w [. ftcontains ('second' ftand 'third' window 3 words) " +
        "ftand 'sentence' distance exactly 0 words]" },
      { "FTDistance 7", nodes(3),
        "//w [text() ftcontains ('second' ftand 'third' window 3 words) " +
        "ftand 'sentence' distance exactly 0 words ordered]" },
      { "FTDistance 8", nodes(37),
        "//w [. ftcontains 'third' ftand 'second' " +
        " ftand 'first' distance exactly 1 words ordered]" },
      { "FTDistance 9", nodes(3),
        "//w [. ftcontains 'first' ftand 'second' " +
        " ftand 'third' distance exactly 1 words ordered]" },          
      { "FTDistance 10", bool(true),
        "'a b' ftcontains 'a' ftand ('b') distance exactly 0 words" },
      { "FTWindow 1", nodes(3, 37),
        "//w [. ftcontains 'second' ftand 'fifth' window 7 words]" },
      { "FTWindow 2", nodes(3, 37),
        "//w [. ftcontains 'second sentence' ftand 'fifth sentence' " +
        "window 6 words]" },
      { "FTWindow 3", nodes(3, 37),
        "//w [. ftcontains 'third' ftand 'second' " +
        "ftand 'fifth' window 7 words]" },
      { "FTWindow 4", nodes(37),
        "//w [. ftcontains 'fifth' ftand 'third' " +
        "ftand 'second' window 7 words ordered]" },
        
      { "FTScope 1", nodes(25, 27, 29, 31, 33),
        "//fti [. ftcontains 'wordt ook' same sentence]" },
      { "FTScope 2", nodes(27, 29, 33),
        "//fti [text() ftcontains 'wordt' ftand 'ook' same sentence]" },
      { "FTScope 3", nodes(25, 31),
        "//fti [. ftcontains 'wordt' ftand 'ook' different sentence]" },
      { "FTScope 4", nodes(25, 27, 29, 33),
        "//fti [. ftcontains 'ook' ftand 'wel' same paragraph]" },
      { "FTScope 5", nodes(31),
        "//fti [. ftcontains 'ook' ftand 'wel' different paragraph]" },
      { "FTScope 6", bool(true),
        "'a. a b' ftcontains ('a' ftand 'b') different sentence" },
        
      { "FTContent 1", nodes(3, 5, 9, 11),
        "//w [text() ftcontains 'xml' at start]" },
      { "FTContent 2", nodes(),
        "//w [. ftcontains 'databases' at start]" },
      { "FTContent 3", nodes(9, 11),
        "//w [. ftcontains 'xml databases' at start]" },
      { "FTContent 4", nodes(9, 11),
        "//w [. ftcontains 'xml' ftand 'databases' ordered at start]" },
      { "FTContent 5", nodes(7, 9, 11),
        "//w [. ftcontains 'databases' at end]" },
      { "FTContent 6", nodes(7, 9, 11),
        "//w [. ftcontains 'xml databases' at end]" },
      { "FTContent 7", nodes(7, 9, 11),
        "//w [. ftcontains 'xml' ftand 'databases' at end]" },
      { "FTContent 8", nodes(),
        "//w [. ftcontains 'have xml' at end]" },
      { "FTContent 9", nodes(14),
        "//w [text() ftcontains 'hello' entire content]" },
      { "FTContent 10", nodes(9, 11),
        "//w [. ftcontains 'xml databases' entire content]" },

      { "FTMildNot 1", nodes(3, 5),
        "//w [text() ftcontains 'xml' not in 'xml databases']" },
      { "FTMildNot 2", nodes(14),
        "//w [text() ftcontains 'hello' not in 'xml']" },
      { "FTMildNot 3", bool(false),
        "'a b' ftcontains 'a' not in 'a b'" },
      { "FTMildNot 4", bool(false),
        "'a' ftcontains 'a' not in 'a'" },
      { "FTMildNot 5", bool(true),
        "'a b' ftcontains 'a b' not in 'a'" },
      { "FTMildNot 6", bool(true),
        "'a b a' ftcontains 'a' not in 'a b'" },
      
      { "FTUnaryNot 1", nodes(14, 37),
        "//w [text() ftcontains ftnot 'xml']" },
      { "FTUnaryNot 2", nodes(3, 5),
        "//w [text() ftcontains 'xml' ftand ftnot 'databases']" },
      { "FTUnaryNot 3", nodes(3, 5, 9, 11),
        "//w [text() ftcontains 'xml' ftand ftnot 'databases' " +
        "case sensitive]" },
      { "FTUnaryNot 4", nodes(7, 9, 11, 14, 37),
        "//w [text() ftcontains 'databases' ftor ftnot 'xml']" },
      { "FTUnaryNot 5", nodes(3, 5, 14, 37),
        "//w [text() ftcontains 'hello' ftor ftnot 'databases']" },
      { "FTUnaryNot 6", nodes(3, 5, 7, 9, 11, 14, 37),
        "//w [text() ftcontains ftnot 'bier']" },
      { "FTUnaryNot 7", nodes(3, 5, 7, 9, 11, 14, 37),
        "//w [text() ftcontains ftnot 'bier' ftand ftnot 'wein' ]" },
      { "FTUnaryNot 8", nodes(3, 5, 7, 9, 11, 14, 37),
        "//w [text() ftcontains ftnot 'bier' ftor ftnot 'wein' ]" },
      { "FTUnaryNot 9", nodes(14, 37),
        "//w [text() ftcontains ftnot 'xml' ftand ftnot 'databeses' ]" },
      { "FTUnaryNot 10", nodes(40),
        "//* [text() ftcontains ftnot (ftnot 'yeah') ]" },
    };

    /** TABLE REPRESENTATION
    PRE DIS SIZ ATS  NS  KIND  CONTENT
      0   1  46   1   0  DOC   tmp
      1   1  45   1   0  ELEM  fttest
      2   1  11   1   0  ELEM  co
      3   1   2   1   0  ELEM  w
      4   1   1   1   0  TEXT  xml in the first sentence. second sentence.
        third sentence. fourth sentence. fifth sentence.
      5   3   2   1   0  ELEM  w
      6   1   1   1   0  TEXT  XML xml XmL
      7   5   2   1   0  ELEM  w
      8   1   1   1   0  TEXT  we have xml databases
      9   7   2   1   0  ELEM  w
     10   1   1   1   0  TEXT  XML DATABASES
     11   9   2   1   0  ELEM  w
     12   1   1   1   0  TEXT  XML & Databases
     13  12   3   1   0  ELEM  wc
     14   1   2   1   0  ELEM  w
     15   1   1   1   0  TEXT  hello
     16  15   5   1   0  ELEM  sc
     17   1   2   1   0  ELEM  s
     18   1   1   1   0  TEXT  diät-joghurt
     19   3   2   1   0  ELEM  s
     20   1   1   1   0  TEXT  diat-joghurt
     21  20   4   1   0  ELEM  at
     22   1   2   1   0  ELEM  b
     23   1   1   1   0  TEXT  B
     24   3   1   1   0  TEXT  ad one
     25  24   2   1   0  ELEM  fti
     26   1   1   1   0  TEXT  adfas wordt. ook wel eens
     27  26   2   1   0  ELEM  fti
     28   1   1   1   0  TEXT  wordt ook wel een s
     29  28   2   1   0  ELEM  fti
     30   1   1   1   0  TEXT  adfad. wordt
    ook wel.eens a
     31  30   2   1   0  ELEM  fti
     32   1   1   1   0  TEXT  adfad wordt. ook
    wel een s adf
     33  32   2   1   0  ELEM  fti
     34   1   1   1   0  TEXT  adfad wordt ook. wel een s
     35  34   2   2   0  ELEM  atr
     36   1   1   1   0  ATTR  key="value"
     37  36   2   1   0  ELEM  w
     38   1   1   1   0  TEXT  the fifth sentence. fourth sentence.
       third sentence. second sentence. first sentence.
     39   1   1   1   0  ELEM  wld
     40   1   2   1   0  ELEM  wld
     41   1   1   1   0  TEXT  yeah
     42  41   4   1   0  ELEM  mix
     43   1   1   1   0  TEXT  A
     44   2   1   1   0  ELEM  sub
     45   3   1   1   0  TEXT  B
     */
  }
}
