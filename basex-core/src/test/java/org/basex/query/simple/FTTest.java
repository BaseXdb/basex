package org.basex.query.simple;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.util.options.*;
import org.junit.Test;

/**
 * Full-text test queries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTTest extends QueryTest {
  /** Test all flag. */
  private static final boolean ALL = true;

  /** Test document. */
  public static final String DOC =
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
      "     <s>di\u00e4t-joghurt</s>\n" +
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

  /** Test queries. */
  public static final Object[][] QUERIES = {
      { "Simple 1", bool(true), "'a' contains text 'a'" },
      { "Simple 2", bool(true), "'a b' contains text 'b'" },
      { "Simple 3", bool(false), "'abc' contains text 'b'" },
      { "Simple 4", node(22), "//b['true' contains text 'true']" },
      { "Simple 5", bool(true), "//@key contains text 'value'" },
      { "Simple 6", str("value"), "//@key[. contains text 'value']/string()" },
      { "Simple 7", bool(false), "//@key contains text 'values'" },
      { "Simple 8", bool(true), "number('100') + 23 contains text '123'" },
      { "Simple 9", bool(true), "true() contains text 'true'" },
      { "Simple 10", bool(true), "false() contains text 'false'" },
      { "Simple 11", bool(false), "'text' contains text ''" },

      { "FT 1", node(14), "//w[text() contains text 'HELLO']" },
      { "FT 2", node(14), "//w[text() <- 'hello']" },
      { "FT 3", node(14), "//w[text() <- '    hello!...   ']" },
      { "FT 4", empty(), "//w[  text  (   )  <-  '  crap  '  ]  " },
      { "FT 5", empty(), "//w[text() <- 'db']" },
      { "FT 6", node(42, 46), "//mix[text() <- 'A']" },
      { "FT 7", node(14), "//w[text() <- 'hello']['A' <- 'A']" },
      { "FT 8", node(14), "//w[text() <- 'hello']['X' <- 'X' using fuzzy]" },
      { "FT 9", node(14), "//w[text() = 'hello' and 'X' <- 'X']" },
      { "FT 10", node(14), "//w[text() = 'hello' and text() <- 'hello']" },
      { "FT 11", empty(), "//wld[text() <- '']" },
      { "FT 12", empty(), "//wld[text() <- ' ']" },
      { "FT 13", node(40), "//*[text() <- 'yeah']" },

      { "Preds 1", node(7, 9, 11),
        "//w[text() <- 'xml'][text() <- 'Databases']" },
      { "Preds 2", node(35),
        "//fttest[co/w <- 'xml'][w <- 'fifth']/atr" },
      { "Preds 3", node(1),
        "//fttest[*/text() <- 'ook']" +
        "[*/text() <- 'een']" },
      { "Preds 4", node(1),
        "*[*/text() <- 'ook'][*/text() <- 'een']" },
      { "Preds 5", node(7),
        "//*[text() <- 'have'][text() <- 'xml']" },
      { "Preds 6", node(13),
        "//*[*/text() <- 'hello'][*/text() = 'hello']" },
      { "Preds 7", node(7),
        "//w[text()[. <- 'have xml'] <- 'Databases']" },
      { "Preds 8", node(5),
        "/descendant::w[text() <- 'xml'][2]" },
      { "Preds 9", node(3),
        "//w[text() <- 'xml'][1]" },
      { "Preds 10", node(5),
        "//w[text() <- 'xml'][2]" },
      { "Preds 11", node(14),
        "//wc/w[text() <- 'hello']" },
      { "Preds 12", node(46),
        "//mix[text()[1] <- 'B']" },

      { "AndOr 1", node(7, 9, 11),
        "//w[text() <- 'xml' and text() <- 'databases']" },
      { "AndOr 2", node(2),
        "//*[*/text() <- 'have' and */text() <- 'first']" },
      { "AndOr 3", node(25, 29),
        "//fti[text() <- 'eens' or text() <- 'a']" },
      { "AndOr 4", node(25, 29),
        "//fti[text() <- 'eens' and text() <- 'ook' or text() <- 'a']" },
      { "AndOr 5", node(31),
        "//fti[text() <- 'adf s' or text() <- 's adf']" },
      { "AndOr 6", node(31),
        "//fti[contains(text(), 'adf') and text() <- 'adf']" },
      { "AndOr 7", node(3),
        "//*[text() <- 'sentence' and text() <- 'xml']" },
      { "AndOr 8", node(42, 46),
        "//mix[text() <- 'A'][text() <- 'B']" },

      { "Phrase 1", node(7, 9, 11),
        "//w [text() <- 'xml databases']" },
      { "Phrase 2", node(7, 9, 11),
        "//w [text() <- 'xml &amp; databases']" },
      { "Phrase 3", node(7, 9, 11),
        "//w [text() <- 'xml :) databases :|']" },
      { "Phrase 4", empty(),
        "//w [text() <- 'xml db']" },
      { "Phrase 5", node(25, 29),
        "/fttest/fti[text() <- 'wordt ook wel eens']" },

      { "FTDiacritics 1", node(17, 19),
        "//s [text() <- 'diat']" },
      { "FTDiacritics 2", node(17, 19),
        "//s [text() <- 'di\u00e4t joghurt' using diacritics " +
        "insensitive]" },
      { "FTDiacritics 3", node(17),
        "//s [text() <- 'di\u00e4t joghurt' " +
        "using diacritics sensitive]" },

      { "FTCaseOption 1", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml']" },
      { "FTCaseOption 2", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml' using case insensitive]" },
      { "FTCaseOption 3", node(11),
        "/fttest/co/w [text() <- 'XML Databases' " +
        "using case sensitive]" },
      { "FTCaseOption 4", node(9),
        "/fttest/co/w [text() <- 'xml databases' using uppercase]" },
      { "FTCaseOption 5", node(3, 5, 7),
        "/fttest/co/w [text() <- 'XML' using lowercase]" },
      { "FTCaseOption 6", node(5, 9, 11),
        "/fttest/co/w [text() <- 'xml' using uppercase]" },
      { "FTCaseOption 7", node(7),
        "/fttest/co/w [text() <- 'XML DATABASES' using lowercase]" },

      { "FTWildCard 1", node(14),
        "/fttest/wc/w [text() <- '.ello' using wildcards]" },
      { "FTWildCard 2", node(14),
        "/fttest/wc/w [text() <- 'hell.' using wildcards]" },
      { "FTWildCard 3", node(14),
        "/fttest/wc/w [text() <- '.+llo' using wildcards]" },
      { "FTWildCard 4", node(14),
        "/fttest/wc/w [text() <- 'hell.+' using wildcards]" },
      { "FTWildCard 5", node(14),
        "/fttest/wc/w [text() <- '.*llo' using wildcards]" },
      { "FTWildCard 6", node(14),
        "/fttest/wc/w [text() <- 'hel.*' using wildcards]" },
      { "FTWildCard 7", node(14),
        "/fttest/wc/w [text() <- '.*' using wildcards]" },
      { "FTWildCard 8", node(14),
        "/fttest/wc/w [text() <- '.+' using wildcards]" },
      { "FTWildCard 9", node(5, 9, 11),
        "/fttest/co/w [text() <- 'X.+' " +
        "using wildcards using case sensitive]" },
      { "FTWildCard 10", node(5, 9, 11),
        "/fttest/co/w [text() <- 'x.+' using wildcards " +
        "using uppercase]" },
      { "FTWildCard 11", node(40),
        "/fttest/wld [text() <- '.*' using wildcards]" },
      { "FTWildCard 12", node(40),
        "/fttest/wld [text() <- '.+' using wildcards]" },
      { "FTWildCard 13", node(14),
        "//w [text() <- 'he.{1,2}o' using wildcards]" },
      { "FTWildCard 14", node(14),
        "//w [text() <- 'h.+ll.+' using wildcards]" },
      { "FTWildCard 15", node(14),
        "//w [text() <- 'h.\\llo' using wildcards]" },

      { "FTWildCard 16", bool(false), "'a' <- 'a.+' using wildcards" },
      { "FTWildCard 17", bool(true), "'aa' <- 'a.+' using wildcards" },
      { "FTWildCard 18", bool(true), "'aaaa' <- 'a.+' using wildcards" },
      { "FTWildCard 19", bool(true), "'a' <- 'a.*' using wildcards" },
      { "FTWildCard 20", bool(true), "'aaaaaa' <- 'a.*' using wildcards" },
      { "FTWildCard 21", bool(false), "'a' <- 'a.' using wildcards" },
      { "FTWildCard 22", bool(true), "'aa' <- 'a.' using wildcards" },
      { "FTWildCard 23", bool(true), "'a' <- 'a.{0,1}' using wildcards" },
      { "FTWildCard 24", bool(true), "'aa' <- 'a.{0,1}' using wildcards" },
      // parsing tests: should throw exception
      { "FTWildCard 25", "'aa' <- 'a.{0, 1}' using wildcards" },
      { "FTWildCard 26", "'a' <- 'a.{-1,1}' using wildcards" },
      { "FTWildCard 27", "'a' <- 'a.{1}' using wildcards" },
      { "FTWildCard 28", "'a' <- 'a.{1-5}' using wildcards" },
      { "FTWildCard 29", bool(true), "'hi' <- '\\h\\i' using wildcards" },
      // #660: combination of FTAnyAllOption and wildcards
      { "FTWildCard 30", bool(true),
        "'a' contains text '.*' all words using wildcards" },

      { "FTFuzzy 1", node(7, 9, 11), "//* [text() <- 'Database' using fuzzy]" },
      { "FTFuzzy 2", node(7, 9, 11), "//* [text() <- 'Databaze' using fuzzy]" },
      { "FTFuzzy 3", empty(), "//* [text() <- 'Databasing' using fuzzy]" },

      { "FTAnyAllOption 1", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml' any]" },
      { "FTAnyAllOption 2", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml' all]" },
      { "FTAnyAllOption 3", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml' any word]" },
      { "FTAnyAllOption 4", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml' all words]" },
      { "FTAnyAllOption 5", node(3, 5, 7, 9, 11),
        "/fttest/co/w [text() <- 'xml' phrase]" },
      { "FTAnyAllOption 6", bool(false),
        "'text' <- { '' } any" },
      { "FTAnyAllOption 7", bool(false),
        "'text' <- { '' } all" },
      { "FTAnyAllOption 8", bool(false),
        "'text' <- { '' } all words" },
      { "FTAnyAllOption 9", bool(false),
        "'text' <- { '' } any word" },
      { "FTAnyAllOption 10", bool(false),
        "'text' <- { '' } phrase" },
      { "FTAnyAllOption 11", bool(true),
        "'red balloon' <- { 'red', '', 'balloon' } any" },
      { "FTAnyAllOption 12", bool(false),
        "'red balloon' <- { 'red', '', 'balloon' } all" },
      { "FTAnyAllOption 13", bool(true),
        "'red balloon' <- { 'red', '', 'balloon' } all words" },
      { "FTAnyAllOption 14", bool(true),
        "'red balloon' <- { 'red', '', 'balloon' } any word" },
      { "FTAnyAllOption 15", bool(true),
        "'red balloon' <- { 'red', '', 'balloon' } phrase" },

      { "FTTimes 1", node(7, 9, 11),
        "//w [text() <- 'DaTaBaSeS' occurs exactly 1 times]" },
      { "FTTimes 2", node(5),
        "//w [text() <- 'XmL' occurs exactly 3 times]" },
      { "FTTimes 3", node(14, 37),
        "//w [text() <- 'xml' occurs exactly 0 times]" },
      { "FTTimes 4", node(5),
        "//w [text() <- 'xml' occurs at least 3 times]" },
      { "FTTimes 5", node(3, 7, 9, 11, 14, 37),
        "//w [text() <- 'XmL' occurs at most 2 times]" },
      { "FTTimes 6", node(3, 7, 9, 11, 14, 37),
        "//w [text() <- 'XmL' occurs from 0 to 1 times]" },
      { "FTTimes 7", node(5),
        "//w [text() <- 'xml xml' occurs at least 2 times]" },
      { "FTTimes 8", empty(),
        "//w [text() <- 'xml xml' occurs at least 4 times]" },

      { "FTAnyAllTimes 1", bool(true),
        "'a a a' <- 'a a' occurs exactly 2 times" },
      { "FTAnyAllTimes 2", bool(true),
        "'a a a' <- 'a a' any occurs exactly 2 times" },
      { "FTAnyAllTimes 3", bool(true),
        "'a a a' <- 'a a' any word occurs exactly 3 times" },
      { "FTAnyAllTimes 4", bool(true),
        "'a a a' <- 'a a' all occurs exactly 2 times" },
      { "FTAnyAllTimes 5", bool(true),
        "'a a a' <- 'a a' all words occurs exactly 3 times" },
      { "FTAnyAllTimes 6", bool(true),
        "'a a a' <- 'a a' phrase occurs exactly 2 times" },
      { "FTAnyAllTimes 7", bool(true),
        "'a b c' <- '.' occurs exactly 3 times using wildcards" },
      { "FTAnyAllTimes 8", bool(true),
        "'a b c' <- '. .' occurs exactly 2 times using wildcards" },

      { "FTAndOr 1", node(7, 9, 11),
        "//w [text() <- 'XmL' ftand 'Databases']" },
      { "FTAndOr 2", node(7, 9, 11, 14),
        "//w [text() <- 'databases' ftor 'hello']" },
      { "FTAndOr 3", node(14),
        "//w [text() <- 'HELLO' ftand 'hello']" },
      { "FTAndOr 4", node(7, 9, 11, 14),
        "//w [text() <- 'xml' ftand 'databases' ftor 'hello' ]" },
      { "FTAndOr 5", node(7, 9, 11),
        "//w [text() <- 'databases' ftand ('xml' ftor 'hello')]" },
      { "FTAndOr 6", node(31, 33),
        "//fti [text() <- 'adfad' ftand 'wordt' ftand 'ook' " +
        "ftand 'wel' ftand 'een' ftand 's']" },
      { "FTAndOr 7", empty(),
        "//* [text() <- 'databases' ftand 'db']" },
      { "FTAndOr 8", node(14),
        "//* [text() <- 'hola' ftor 'hello']" },
      { "FTAndOr 9", empty(),
        "//* [text() <- 'hola' ftand 'hello']" },
      { "FTAndOr 10", node(14),
        "//w [text() <- 'HELLO' ftand ('hello' using stemming)]" },
      { "FTAndOr 11", node(14),
        "//w [text() <- 'HELLO' ftand ('hello') using stemming]" },

      { "FTStemming 1", node(7, 9, 11),
        "//w [text() <- 'xml database' using stemming]" },
      { "FTStemming 2", empty(),
        "//w [text() <- 'xml database' using no stemming]" },
      { "FTStemming 3", node(7, 9, 11),
        "//w [text() <- 'xml' ftand 'databasing' using stemming " +
        "using language 'en']" },
      { "FTStemming 4", node(7, 9, 11, 14),
        "//w [text() <- 'hello' ftor 'database' using stemming]" },
      { "FTStemming 5", node(3, 5, 14, 37),
        "//w [text() <- ftnot 'database' using stemming]" },
      { "FTStemming 6", bool(true),
        "'base' <- 'bases' using stemming" },
      { "FTStemming 7", bool(true),
        "'bases' <- ('base') using stemming" },
      { "FTStemming 8", bool(true),
        "'base' <- ('bases') using stemming" },
      { "FTStemming 9", bool(true),
        "'base' <- ('bases' using stemming) using no stemming" },
      { "FTStemming 10", bool(true),
        "'\u043a\u043d\u0438\u0433\u0430' <- '\u043a\u043d\u0438\u0433\u0438'" +
        " using stemming using language 'Russian'" },
      { "FTStemming 11", bool(true),
        "'de' <- 'de' using stemming using language 'pt'" },

      { "FTLanguage 1", node(14),
        "//* [text() <- 'hello' using language 'en']" },
      { "FTLanguage 2", node(14),
        "//* [text() <- 'hello' using language 'de']" },
      { "FTLanguage 3", // error
        "//* [text() <- 'hello' using language 'jp']" },

      { "FTStopWords 1", node(7, 9, 11), "//* [text() <- " +
        "'and databases' using stop words ('xml', 'and')]" },
      { "FTStopWords 2", node(7),
        "//* [text() <- 'we use xml' using stop words ('use')]" },

      { "FTAtomization 1", node(21),
        "//at [. <- 'bad one']" },
      { "FTAtomization 2", node(35),
        "//atr [@key <- 'value']" },

      { "FTOrdered 1", node(7, 9, 11),
        "//w [. <- 'databases' ordered]" },
      { "FTOrdered 2", node(7, 9, 11),
        "//w [. <- 'xml' ftand 'databases' ordered]" },
      { "FTOrdered 3", empty(),
        "//w [. <- 'databases' ftand 'xml' ordered]" },
      { "FTOrdered 4", bool(true),
        "'A B' <- ('A' ftand 'B' ordered)" },
      { "FTOrdered 5", bool(true),
        "'A B' <- ('A' ftand 'B') ftor ('C' ftand 'D') ordered" },
      { "FTOrdered 6", bool(true),
        "'C D' <- ('A' ftand 'B') ftor ('C' ftand 'D') ordered" },
      { "FTOrdered 7", bool(true),
        "'A B C D' <- ('B' ftand 'A' ordered) ftor " +
        "('C' ftand 'D' ordered) ordered" },
      { "FTOrdered 8", bool(false),
        "'B A' <- ('A' ftand ftnot 'B') ordered" },
      { "FTOrdered 9", bool(true),
        "'A B' <- 'B' ftor 'A' ordered" },
      { "FTOrdered 10", bool(true),
        "'A B' <- ('B' ftor 'A') ordered" },
      { "FTOrdered 11", bool(true),
        "'A B C' <- ('A' ftor 'C') ftand 'B' ordered" },

      { "FTDistance 1", node(3),
        "//w [text() <- 'the' ftand 'fourth' " +
        "distance exactly 2 sentences]" },
      { "FTDistance 2", node(3, 37),
        "//w [. <- 'first' ftand 'second' ftand 'third' " +
          "distance exactly 1 words]" },
      { "FTDistance 3", node(3, 37),
        "//w [. <- 'first sentence' ftand 'third sentence' " +
        "distance exactly 2 words]" },
      { "FTDistance 4", node(3),
        "//w [. <- 'the first sentence' ftand 'third sentence' " +
        "distance exactly 2 words]" },
      { "FTDistance 5", node(3, 37),
        "//w [. <- 'sentence' ftand 'the' " +
        "distance exactly 1 words]" },
      { "FTDistance 6", node(3, 37),
        "//w [. <- ('second' ftand 'third' window 3 words) " +
        "ftand 'sentence' distance exactly 0 words]" },
      { "FTDistance 7", node(3, 37),
        "//w [text() <- ('second' ftand 'third' window 3 words) " +
        "ftand 'sentence' distance exactly 0 words ordered]" },
      { "FTDistance 8", node(37),
        "//w [. <- 'third' ftand 'second' " +
        " ftand 'first' distance exactly 1 words ordered]" },
      { "FTDistance 9", node(3),
        "//w [. <- 'first' ftand 'second' " +
        " ftand 'third' distance exactly 1 words ordered]" },
      { "FTDistance 10", bool(true),
        "'a b' <- 'a' ftand ('b') distance exactly 0 words" },
      { "FTDistance 11", bool(true),
        "'a b' <- ('a') ftand ('b') entire content" },

      { "FTWindow 1", node(3, 37),
        "//w [. <- 'second' ftand 'fifth' window 7 words]" },
      { "FTWindow 2", node(3, 37),
        "//w [. <- 'second sentence' ftand 'fifth sentence' " +
        "window 8 words]" },
      { "FTWindow 3", node(3, 37),
        "//w [. <- 'third' ftand 'second' " +
        "ftand 'fifth' window 7 words]" },
      { "FTWindow 4", node(37),
        "//w [. <- 'fifth' ftand 'third' " +
        "ftand 'second' ordered window 7 words]" },
      { "FTWindow 5", node(37),
        "//w [. <- 'fifth' ftand 'third' " +
        "ftand 'second' window 7 words ordered]" },

      { "FTScope 1", node(25, 27, 29, 31, 33),
        "//fti [. <- 'wordt ook' same sentence]" },
      { "FTScope 2", node(27, 29, 33),
        "//fti [text() <- 'wordt' ftand 'ook' same sentence]" },
      { "FTScope 3", node(25, 31),
        "//fti [. <- 'wordt' ftand 'ook' different sentence]" },
      { "FTScope 4", node(25, 27, 29, 33),
        "//fti [. <- 'ook' ftand 'wel' same paragraph]" },
      { "FTScope 5", node(31),
        "//fti [. <- 'ook' ftand 'wel' different paragraph]" },
      { "FTScope 6", bool(true),
        "'a. a b' <- ('a' ftand 'b') different sentence" },

      { "FTContent 1", node(3, 5, 9, 11),
        "//w [text() <- 'xml' at start]" },
      { "FTContent 2", empty(),
        "//w [. <- 'databases' at start]" },
      { "FTContent 3", node(9, 11),
        "//w [. <- 'xml databases' at start]" },
      { "FTContent 4", node(9, 11),
        "//w [. <- 'xml' ftand 'databases' ordered at start]" },
      { "FTContent 5", node(7, 9, 11),
        "//w [. <- 'databases' at end]" },
      { "FTContent 6", node(7, 9, 11),
        "//w [. <- 'xml databases' at end]" },
      { "FTContent 7", node(7, 9, 11),
        "//w [. <- 'xml' ftand 'databases' at end]" },
      { "FTContent 8", empty(),
        "//w [. <- 'have xml' at end]" },
      { "FTContent 9", node(14),
        "//w [text() <- 'hello' entire content]" },
      { "FTContent 10", node(9, 11),
        "//w [. <- 'xml databases' entire content]" },
      { "FTContent 11", bool(true),
        "'a b c d' <- 'a' ftand 'b' ftand 'c'" +
        " ftand 'd' entire content" },
      { "FTContent 12", bool(true),
        "'a b c d' <- 'd' ftand 'c' ftand 'b'" +
        " ftand 'a' entire content" },
      { "FTContent 13", bool(true),
        "'a b c d' <- 'a' ftand 'b' ftand 'c'" +
        " ftand 'd' entire content ordered" },
      { "FTContent 14", bool(false),
        "'a b c d' <- 'd' ftand 'c' ftand 'b'" +
        " ftand 'a' entire content ordered" },
      { "FTContent 15", bool(true),
        "'a b c d' <- 'a' ftand 'b' at start" },
      { "FTContent 16", bool(true),
        "'a b c d' <- 'a' ftand 'b' at start ordered" },
      { "FTContent 17", bool(true),
        "'a b c d' <- 'b' ftand 'a' at start" },
      { "FTContent 18", bool(false),
        "'a b c d' <- 'b' ftand 'a' at start ordered" },
      { "FTContent 19", bool(true),
        "'a b c d' <- 'c' ftand 'd' at end" },
      { "FTContent 20", bool(true),
        "'a b c d' <- 'c' ftand 'd' at end ordered" },
      { "FTContent 21", bool(true),
        "'a b c d' <- 'd' ftand 'c' at end" },
      { "FTContent 22", bool(false),
        "'a b c d' <- 'd' ftand 'c' at end ordered" },
      { "FTContent 23", bool(true),
        "'a b c' <- 'b c' ftand 'a' entire content" },
      { "FTContent 24", bool(true),
        "'a b c' <- 'a' ftand 'b c' entire content" },
      { "FTContent 25", bool(true),
        "'a b c' <- 'a b c' entire content" },
      { "FTContent 26", bool(false),
        "'a b' <- 'a' entire content" },
      { "FTContent 27", bool(false),
        "'a b' <- 'b' entire content" },

      { "FTMildNot 1", node(3, 5),
        "//w [text() <- 'xml' not in 'xml databases']" },
      { "FTMildNot 2", node(14),
        "//w [text() <- 'hello' not in 'xml']" },
      { "FTMildNot 3", bool(false),
        "'a b' <- 'a' not in 'a b'" },
      { "FTMildNot 4", bool(false),
        "'a' <- 'a' not in 'a'" },
      { "FTMildNot 5", bool(true),
        "'a b' <- 'a b' not in 'a'" },
      { "FTMildNot 6", bool(true),
        "'a b a' <- 'a' not in 'a b'" },
      { "FTMildNot 7", bool(false),
        "'a b a b' <- 'a' not in 'a b'" },
      { "FTMildNot 8",
        "'a' <- 'a' not in ftnot 'a'" },
      { "FTMildNot 9", node(3, 5, 7, 9, 11),
        "//w [text() <- 'xml' not in 'we have']" },

      { "FTUnaryNot 1", node(14, 37),
        "//w [text() <- ftnot 'xml']" },
      { "FTUnaryNot 2", node(3, 5),
        "//w [text() <- 'xml' ftand ftnot 'databases']" },
      { "FTUnaryNot 3", node(3, 5),
        "//w [text() <- ftnot 'databases' ftand 'xml']" },
      { "FTUnaryNot 4", node(31),
        "//* [text() <- 'adf' ftand ftnot 'xml']" },
      { "FTUnaryNot 5", node(3, 5, 9, 11),
        "//w [text() <- 'xml' ftand ftnot 'databases' " +
        "using case sensitive]" },
      { "FTUnaryNot 6", node(7, 9, 11, 14, 37),
        "//w [text() <- 'databases' ftor ftnot 'xml']" },
      { "FTUnaryNot 7", node(3, 5, 14, 37),
        "//w [text() <- 'hello' ftor ftnot 'databases']" },
      { "FTUnaryNot 8", node(3, 5, 7, 9, 11, 14, 37),
        "//w [text() <- ftnot 'bier']" },
      { "FTUnaryNot 9", node(3, 5, 7, 9, 11, 14, 37),
        "//w [text() <- ftnot 'bier' ftand ftnot 'wein' ]" },
      { "FTUnaryNot 10", node(3, 5, 7, 9, 11, 14, 37),
        "//w [text() <- ftnot 'bier' ftor ftnot 'wein' ]" },
      { "FTUnaryNot 11", node(14, 37),
        "//w [text() <- ftnot 'xml' ftand ftnot 'databeses' ]" },
      { "FTUnaryNot 12", node(31),
        "//fti [text() <- ftnot (ftnot 'adf') ]" },
      { "FTUnaryNot 13", node(31),
        "//fti [text() <- 'adf' ftand ftnot (ftnot 'adf')]" },
      { "FTUnaryNot 14", node(31),
        "//fti [text() <- 'adf' ftor ftnot (ftnot 'adf')]" },
      { "FTUnaryNot 15", node(25, 27, 29, 31, 33),
        "//fti [text() <- 'adf' ftor ftnot 'adf']" },
    };

  /** Constructor. */
  static {
    doc = DOC;
    queries = QUERIES;
  }

  @Test
  @Override
  public void test() throws BaseXException {
    final MainOptions opts = context.options;
    if(ALL) {
      // testing all kinds of combinations
      for(int a = 0; a < 2; ++a) {
        opts.set(MainOptions.FTINDEX, a == 0);
        super.test();
      }
    } else {
      // single test
      opts.set(MainOptions.FTINDEX, true);
      opts.set(MainOptions.STEMMING, true);
      opts.set(MainOptions.DIACRITICS, true);
      opts.set(MainOptions.CASESENS, true);
      super.test();
    }
  }

  @Override
  protected String details() {
    final MainOptions opts = context.options;
    final StringBuilder sb = new StringBuilder();
    sb.append(set(opts, MainOptions.FTINDEX)).append(';');
    sb.append(set(opts, MainOptions.STEMMING)).append(';');
    sb.append(set(opts, MainOptions.DIACRITICS)).append(';');
    sb.append(set(opts, MainOptions.CASESENS));
    return sb.toString();
  }

  /**
   * Returns a flag string.
   * @param opts options
   * @param option option
   * @return string
   */
  private static String set(final Options opts, final BooleanOption option) {
    return new Set(option, opts.get(option)).toString();
  }

  /* TABLE REPRESENTATION
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
   18   1   1   1   0  TEXT  di\u00e4t-joghurt
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
