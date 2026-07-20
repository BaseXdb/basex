package org.basex.query.ft;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Full-text queries. Node results are checked via their {@code pre} values. The queries are run
 * against a full-text indexed database; {@link FTSeqTest} re-runs them without an index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FTTest extends SandboxTest {
  /** Test document. */
  static final String DOC = """
    <fttest>
      <co>
         <w>xml in the first sentence. second sentence.
    third sentence. fourth sentence. fifth sentence.</w>
         <w>XML xml XmL</w>
         <w>we have xml databases</w>
         <w>XML DATABASES</w>
         <w>XML &amp; Databases</w>
      </co>
      <wc>
         <w>hello</w>
      </wc>
      <sc>
         <s>diät-joghurt</s>
         <s>diat-joghurt</s>
      </sc>
      <at><b>B</b>ad one</at>
      <fti>adfas wordt. ook wel eens</fti>
      <fti>wordt ook wel een s</fti>
      <fti>adfad. wordt
    ook wel.eens a</fti>
      <fti>adfad wordt. ook
    wel een s adf</fti>
      <fti>adfad wordt ook. wel een s</fti>
      <atr key='value'/>
      <w>the fifth sentence. fourth sentence.
    third sentence. second sentence. first sentence.</w>
      <wld/>
      <wld>yeah</wld>
      <mix>A<sub/>B</mix>
      <mix>B<sub/>A</mix>
      <order>A B A</order>
    </fttest>""";

  /**
   * Creates the test database.
   * @param ftindex full-text index flag
   */
  static void createDB(final boolean ftindex) {
    set(MainOptions.STRIPWS, true);
    set(MainOptions.FTINDEX, ftindex);
    execute(new CreateDB(NAME, DOC));
    set(MainOptions.FTINDEX, false);
    set(MainOptions.STRIPWS, false);
  }

  /** Creates a full-text indexed database. */
  @BeforeAll public static void beforeClass() {
    createDB(true);
  }

  /** Drops the test database. */
  @AfterAll public static void afterClass() {
    execute(new DropDB(NAME));
  }

  /** Simple contains-text expressions. */
  @Test public void simple() {
    query("'a' contains text 'a'", true);
    query("'a b' contains text 'b'", true);
    query("'abc' contains text 'b'", false);
    pre("//b['true' contains text 'true']", 22);
    query("//@key contains text 'value'", true);
    query("//@key[. contains text 'value']/string()", "value");
    query("//@key contains text 'values'", false);
    query("number('100') + 23 contains text '123'", true);
    query("true() contains text 'true'", true);
    query("false() contains text 'false'", true);
    query("'text' contains text ''", false);
    query("'t' contains text ftnot { 't' } distance at most 0 words", false);
  }

  /** Full-text on nodes. */
  @Test public void ft() {
    pre("//w[text() contains text 'HELLO']", 14);
    pre("//w[text() contains text 'hello']", 14);
    pre("//w[text() contains text '    hello!...   ']", 14);
    pre("//w[  text  (   )  contains text  '  anarmophism  '  ]  ");
    pre("//w[text() contains text 'db']");
    pre("//mix[text() contains text 'A']", 42, 46);
    pre("//w[text() contains text 'hello']['A' contains text 'A']", 14);
    pre("//w[text() contains text 'hello']['X' contains text 'X' using fuzzy]", 14);
    pre("//w[text() = 'hello' and 'X' contains text 'X']", 14);
    pre("//w[text() = 'hello' and text() contains text 'hello']", 14);
    pre("//wld[text() contains text '']");
    pre("//wld[text() contains text ' ']");
    pre("//*[text() contains text 'yeah']", 40);
  }

  /** Predicates. */
  @Test public void preds() {
    pre("//w[text() contains text 'xml'][text() contains text 'Databases']", 7, 9, 11);
    pre("//fttest[co/w contains text 'xml'][w contains text 'fifth']/atr", 35);
    pre("//fttest[*/text() contains text 'ook'][*/text() contains text 'een']", 1);
    pre("*[*/text() contains text 'ook'][*/text() contains text 'een']", 1);
    pre("//*[text() contains text 'have'][text() contains text 'xml']", 7);
    pre("//*[*/text() contains text 'hello'][*/text() = 'hello']", 13);
    pre("//w[text()[. contains text 'have xml'] contains text 'Databases']", 7);
    pre("/descendant::w[text() contains text 'xml'][2]", 5);
    pre("//w[text() contains text 'xml'][1]", 3);
    pre("//w[text() contains text 'xml'][2]", 5);
    pre("//wc/w[text() contains text 'hello']", 14);
    pre("//mix[text()[1] contains text 'B']", 46);
  }

  /** and/or. */
  @Test public void andOr() {
    pre("//w[text() contains text 'xml' and text() contains text 'databases']", 7, 9, 11);
    pre("//*[*/text() contains text 'have' and */text() contains text 'first']", 2);
    pre("//fti[text() contains text 'eens' or text() contains text 'a']", 25, 29);
    pre("//fti[text() contains text 'eens' and text() contains text 'ook' or "
        + "text() contains text 'a']", 25, 29);
    pre("//fti[text() contains text 'ook' and text() contains text 'eens' or "
        + "text() contains text 'a']", 25, 29);
    pre("//fti[text() contains text 'adf s' or text() contains text 's adf']", 31);
    pre("//fti[contains(text(), 'adf') and text() contains text 'adf']", 31);
    pre("//*[text() contains text 'sentence' and text() contains text 'xml']", 3);
    pre("//mix[text() contains text 'A'][text() contains text 'B']", 42, 46);
  }

  /** Phrases. */
  @Test public void phrase() {
    pre("//w[text() contains text 'xml databases']", 7, 9, 11);
    pre("//w[text() contains text 'xml &amp; databases']", 7, 9, 11);
    pre("//w[text() contains text 'xml :) databases :|']", 7, 9, 11);
    pre("//w[text() contains text 'xml db']");
    pre("/fttest/fti[text() contains text 'wordt ook wel eens']", 25, 29);
  }

  /** Diacritics. */
  @Test public void diacritics() {
    pre("//s[text() contains text 'diat']", 17, 19);
    pre("//s[text() contains text 'diät joghurt' using diacritics insensitive]", 17, 19);
    pre("//s[text() contains text 'diät joghurt' using diacritics sensitive]", 17);
    query("'éé' contains text 'ee' using diacritics insensitive", true);
  }

  /** Case options. */
  @Test public void caseOption() {
    pre("/fttest/co/w[text() contains text 'xml']", 3, 5, 7, 9, 11);
    pre("/fttest/co/w[text() contains text 'xml' using case insensitive]", 3, 5, 7, 9, 11);
    pre("/fttest/co/w[text() contains text 'XML Databases' using case sensitive]", 11);
    pre("/fttest/co/w[text() contains text 'xml databases' using uppercase]", 9);
    pre("/fttest/co/w[text() contains text 'XML' using lowercase]", 3, 5, 7);
    pre("/fttest/co/w[text() contains text 'xml' using uppercase]", 5, 9, 11);
    pre("/fttest/co/w[text() contains text 'XML DATABASES' using lowercase]", 7);
  }

  /** Wildcards on nodes. */
  @Test public void wildCard() {
    pre("/fttest/wc/w[text() contains text '.ello' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text 'hell.' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text '.+llo' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text 'hell.+' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text '.*llo' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text 'hel.*' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text '.*' using wildcards]", 14);
    pre("/fttest/wc/w[text() contains text '.+' using wildcards]", 14);
    pre("/fttest/co/w[text() contains text 'X.+' using wildcards using case sensitive]",
        5, 9, 11);
    pre("/fttest/co/w[text() contains text 'x.+' using wildcards using uppercase]", 5, 9, 11);
    pre("/fttest/wld[text() contains text '.*' using wildcards]", 40);
    pre("/fttest/wld[text() contains text '.+' using wildcards]", 40);
    pre("//w[text() contains text 'he.{1,2}o' using wildcards]", 14);
    pre("//w[text() contains text 'h.+ll.+' using wildcards]", 14);
    pre("//w[text() contains text 'h.\\llo' using wildcards]", 14);
  }

  /** Wildcards on strings, including parse errors. */
  @Test public void wildCardBoolean() {
    query("'a' contains text 'a.+' using wildcards", false);
    query("'aa' contains text 'a.+' using wildcards", true);
    query("'aaaa' contains text 'a.+' using wildcards", true);
    query("'a' contains text 'a.*' using wildcards", true);
    query("'aaaaaa' contains text 'a.*' using wildcards", true);
    query("'a' contains text 'a.' using wildcards", false);
    query("'aa' contains text 'a.' using wildcards", true);
    query("'a' contains text 'a.{0,1}' using wildcards", true);
    query("'aa' contains text 'a.{0,1}' using wildcards", true);
    // parsing tests: should throw exception
    error("'aa' contains text 'a.{0, 1}' using wildcards", FTWILDCARD_X);
    error("'a' contains text 'a.{-1,1}' using wildcards", FTWILDCARD_X);
    error("'a' contains text 'a.{1}' using wildcards", FTWILDCARD_X);
    error("'a' contains text 'a.{1-5}' using wildcards", FTWILDCARD_X);
    query("'hi' contains text '\\h\\i' using wildcards", true);
    // #660: combination of FTAnyAllOption and wildcards
    query("'a' contains text '.*' all words using wildcards", true);
  }

  /** Fuzzy search. */
  @Test public void fuzzy() {
    pre("//*[text() contains text 'Database' using fuzzy]", 7, 9, 11);
    pre("//*[text() contains text 'Databaze' using fuzzy]", 7, 9, 11);
    pre("//*[text() contains text 'Databasing' using fuzzy]");
  }

  /** Any/all options. */
  @Test public void anyAllOption() {
    pre("/fttest/co/w[text() contains text 'xml' any]", 3, 5, 7, 9, 11);
    pre("/fttest/co/w[text() contains text 'xml' all]", 3, 5, 7, 9, 11);
    pre("/fttest/co/w[text() contains text 'xml' any word]", 3, 5, 7, 9, 11);
    pre("/fttest/co/w[text() contains text 'xml' all words]", 3, 5, 7, 9, 11);
    pre("/fttest/co/w[text() contains text 'xml' phrase]", 3, 5, 7, 9, 11);
    query("'text' contains text { '' } any", false);
    query("'text' contains text { '' } all", false);
    query("'text' contains text { '' } all words", false);
    query("'text' contains text { '' } any word", false);
    query("'text' contains text { '' } phrase", false);
    query("'red balloon' contains text { 'red', '', 'balloon' } any", true);
    query("'red balloon' contains text { 'red', '', 'balloon' } all", false);
    query("'red balloon' contains text { 'red', '', 'balloon' } all words", true);
    query("'red balloon' contains text { 'red', '', 'balloon' } any word", true);
    query("'red balloon' contains text { 'red', '', 'balloon' } phrase", true);
  }

  /** Occurrence counts. */
  @Test public void times() {
    pre("//w[text() contains text 'DaTaBaSeS' occurs exactly 1 times]", 7, 9, 11);
    pre("//w[text() contains text 'XmL' occurs exactly 3 times]", 5);
    pre("//w[text() contains text 'xml' occurs exactly 0 times]", 14, 37);
    pre("//w[text() contains text 'xml' occurs at least 3 times]", 5);
    pre("//w[text() contains text 'XmL' occurs at most 2 times]", 3, 7, 9, 11, 14, 37);
    pre("//w[text() contains text 'XmL' occurs from 0 to 1 times]", 3, 7, 9, 11, 14, 37);
    pre("//w[text() contains text 'xml xml' occurs at least 2 times]", 5);
    pre("//w[text() contains text 'xml xml' occurs at least 4 times]");
  }

  /** Any/all with occurrence counts. */
  @Test public void anyAllTimes() {
    query("'a a a' contains text 'a a' occurs exactly 2 times", true);
    query("'a a a' contains text 'a a' any occurs exactly 2 times", true);
    query("'a a a' contains text 'a a' any word occurs exactly 3 times", true);
    query("'a a a' contains text 'a a' all occurs exactly 2 times", true);
    query("'a a a' contains text 'a a' all words occurs exactly 3 times", true);
    query("'a a a' contains text 'a a' phrase occurs exactly 2 times", true);
    query("'a b c' contains text '.' occurs exactly 3 times using wildcards", true);
    query("'a b c' contains text '. .' occurs exactly 2 times using wildcards", true);
  }

  /** ftand/ftor. */
  @Test public void ftAndOr() {
    pre("//w[text() contains text 'XmL' ftand 'Databases']", 7, 9, 11);
    pre("//w[text() contains text 'databases' ftor 'hello']", 7, 9, 11, 14);
    pre("//w[text() contains text 'HELLO' ftand 'hello']", 14);
    pre("//w[text() contains text 'xml' ftand 'databases' ftor 'hello' ]", 7, 9, 11, 14);
    pre("//w[text() contains text 'databases' ftand ('xml' ftor 'hello')]", 7, 9, 11);
    pre("//fti[text() contains text 'adfad' ftand 'wordt' ftand 'ook' "
        + "ftand 'wel' ftand 'een' ftand 's']", 31, 33);
    pre("//*[text() contains text 'databases' ftand 'db']");
    pre("//*[text() contains text 'hola' ftor 'hello']", 14);
    pre("//*[text() contains text 'hola' ftand 'hello']");
    pre("//w[text() contains text 'HELLO' ftand ('hello' using stemming)]", 14);
    pre("//w[text() contains text 'HELLO' ftand ('hello') using stemming]", 14);
  }

  /** Stemming. */
  @Test public void stemming() {
    pre("//w[text() contains text 'xml database' using stemming]", 7, 9, 11);
    pre("//w[text() contains text 'xml database' using no stemming]");
    pre("//w[text() contains text 'xml' ftand 'databasing' using stemming "
        + "using language 'en']", 7, 9, 11);
    pre("//w[text() contains text 'hello' ftor 'database' using stemming]", 7, 9, 11, 14);
    pre("//w[text() contains text ftnot 'database' using stemming]", 3, 5, 14, 37);
    query("'base' contains text 'bases' using stemming", true);
    query("'bases' contains text ('base') using stemming", true);
    query("'base' contains text ('bases') using stemming", true);
    query("'base' contains text ('bases' using stemming) using no stemming", true);
    query("'книга' contains text 'книги'"
        + " using stemming using language 'Russian'", true);
    query("'de' contains text 'de' using stemming using language 'pt'", true);
    query("'mice' contains text 'mouse' using stemming", true);
    query("'symposia' contains text 'symposium' using stemming", true);
    query("'men' contains text 'man' using stemming", true);
    query("'adverse' contains text 'adverse' all words using stemming", true);
    query("'adverse' contains text 'adverse' any word using stemming", true);
    query("'fox' contains text 'fox' any word using stemming", true);
  }

  /** Language option. */
  @Test public void language() {
    pre("//*[text() contains text 'hello' using language 'en']", 14);
    pre("//*[text() contains text 'hello' using language 'de']", 14);
    error("//*[text() contains text 'hello' using language 'jp']", FTNOTOK_X);
  }

  /** Stop words. */
  @Test public void stopWords() {
    pre("//*[text() contains text 'and databases' using stop words ('xml', 'and')]", 7, 9, 11);
    pre("//*[text() contains text 'we use xml' using stop words ('use')]", 7);
  }

  /** Atomization. */
  @Test public void atomization() {
    pre("//at[. contains text 'bad one']", 21);
    pre("//atr[@key contains text 'value']", 35);
  }

  /** Ordered selection. */
  @Test public void ordered() {
    pre("//w[. contains text 'databases' ordered]", 7, 9, 11);
    pre("//w[. contains text 'xml' ftand 'databases' ordered]", 7, 9, 11);
    pre("//w[. contains text 'databases' ftand 'xml' ordered]");
    query("'A B' contains text ('A' ftand 'B' ordered)", true);
    query("'A B' contains text ('A' ftand 'B') ftor ('C' ftand 'D') ordered", true);
    query("'C D' contains text ('A' ftand 'B') ftor ('C' ftand 'D') ordered", true);
    query("'A B C D' contains text ('B' ftand 'A' ordered) ftor "
        + "('C' ftand 'D' ordered) ordered", true);
    query("'B A' contains text ('A' ftand ftnot 'B') ordered", false);
    query("'A B' contains text 'B' ftor 'A' ordered", true);
    query("'A B' contains text ('B' ftor 'A') ordered", true);
    query("'A B C' contains text ('A' ftor 'C') ftand 'B' ordered", true);
    query("//order contains text { 'A', 'B' } all ordered", true);
    query("//order contains text { 'B', 'A' } all ordered", true);
    query("//order contains text 'A B' all words ordered", true);
    query("//order contains text 'B A' all words ordered", true);
    query("'a b c b' contains text 'a b c' all words ordered", true);
  }

  /** Distance. */
  @Test public void distance() {
    pre("//w[text() contains text 'the' ftand 'fourth' distance exactly 2 sentences]", 3);
    pre("//w[. contains text 'first' ftand 'second' ftand 'third' distance exactly 1 words]",
        3, 37);
    pre("//w[. contains text 'first sentence' ftand 'third sentence' "
        + "distance exactly 2 words]", 3, 37);
    pre("//w[. contains text 'the first sentence' ftand 'third sentence' "
        + "distance exactly 2 words]", 3);
    pre("//w[. contains text 'sentence' ftand 'the' distance exactly 1 words]", 3, 37);
    pre("//w[. contains text ('second' ftand 'third' window 3 words) "
        + "ftand 'sentence' distance exactly 0 words]", 3, 37);
    pre("//w[text() contains text ('second' ftand 'third' window 3 words) "
        + "ftand 'sentence' distance exactly 0 words ordered]", 3, 37);
    pre("//w[. contains text 'third' ftand 'second' "
        + " ftand 'first' distance exactly 1 words ordered]", 37);
    pre("//w[. contains text 'first' ftand 'second' "
        + " ftand 'third' distance exactly 1 words ordered]", 3);
    query("'a b' contains text 'a' ftand ('b') distance exactly 0 words", true);
    query("'a b' contains text ('a') ftand ('b') entire content", true);
  }

  /** Window. */
  @Test public void window() {
    pre("//w[. contains text 'second' ftand 'fifth' window 7 words]", 3, 37);
    pre("//w[. contains text 'second sentence' ftand 'fifth sentence' "
        + "window 8 words]", 3, 37);
    pre("//w[. contains text 'third' ftand 'second' ftand 'fifth' window 7 words]", 3, 37);
    pre("//w[. contains text 'fifth' ftand 'third' "
        + "ftand 'second' ordered window 7 words]", 37);
    pre("//w[. contains text 'fifth' ftand 'third' "
        + "ftand 'second' window 7 words ordered]", 37);
    query("'a b b' contains text 'a b' all words window 2 words", true);
    query("'b a b' contains text 'a b' all words window 2 words", true);
    query("'a b a b' contains text 'a b' all words window 2 words", true);
    query("'a a b' contains text 'a b' all words window 2 words", true);
    query("'a b b' contains text 'a b' all words window 1 words", false);
    query("'a single event during the custodial history of a manuscript or other object.'"
        + " contains text 'custodial history of a manuscript' all words window 5 words", true);
  }

  /** Scope. */
  @Test public void scope() {
    pre("//fti[. contains text 'wordt ook' same sentence]", 27, 29, 33);
    pre("//fti[text() contains text 'wordt' ftand 'ook' same sentence]", 27, 29, 33);
    pre("//fti[. contains text 'wordt' ftand 'ook' different sentence]", 25, 31);
    pre("//fti[. contains text 'ook' ftand 'wel' same paragraph]", 25, 27, 29, 33);
    pre("//fti[. contains text 'ook' ftand 'wel' different paragraph]", 31);
    query("'a. a b' contains text ('a' ftand 'b') different sentence", true);
    query("'a b. a b' contains text 'a b' all words same sentence", true);
    query("'a b. a b' contains text 'a b' all words different sentence", true);
    query("'a x. y b' contains text 'a b' all words same sentence", false);
  }

  /** Content position. */
  @Test public void content() {
    pre("//w[text() contains text 'xml' at start]", 3, 5, 9, 11);
    pre("//w[. contains text 'databases' at start]");
    pre("//w[. contains text 'xml databases' at start]", 9, 11);
    pre("//w[. contains text 'xml' ftand 'databases' ordered at start]", 9, 11);
    pre("//w[. contains text 'databases' at end]", 7, 9, 11);
    pre("//w[. contains text 'xml databases' at end]", 7, 9, 11);
    pre("//w[. contains text 'xml' ftand 'databases' at end]", 7, 9, 11);
    pre("//w[. contains text 'have xml' at end]");
    pre("//w[text() contains text 'hello' entire content]", 14);
    pre("//w[. contains text 'xml databases' entire content]", 9, 11);
    query("'a b c d' contains text 'a' ftand 'b' ftand 'c' ftand 'd' entire content", true);
    query("'a b c d' contains text 'd' ftand 'c' ftand 'b' ftand 'a' entire content", true);
    query("'a b c d' contains text 'a' ftand 'b' ftand 'c' ftand 'd' entire content ordered",
        true);
    query("'a b c d' contains text 'd' ftand 'c' ftand 'b' ftand 'a' entire content ordered",
        false);
    query("'a b c d' contains text 'a' ftand 'b' at start", true);
    query("'a b c d' contains text 'a' ftand 'b' at start ordered", true);
    query("'a b c d' contains text 'b' ftand 'a' at start", true);
    query("'a b c d' contains text 'b' ftand 'a' at start ordered", false);
    query("'a b c d' contains text 'c' ftand 'd' at end", true);
    query("'a b c d' contains text 'c' ftand 'd' at end ordered", true);
    query("'a b c d' contains text 'd' ftand 'c' at end", true);
    query("'a b c d' contains text 'd' ftand 'c' at end ordered", false);
    query("'a b c' contains text 'b c' ftand 'a' entire content", true);
    query("'a b c' contains text 'a' ftand 'b c' entire content", true);
    query("'a b c' contains text 'a b c' entire content", true);
    query("'a b' contains text 'a' entire content", false);
    query("'a b' contains text 'b' entire content", false);
    query("'a b b' contains text 'a b' all words entire content", false);
    query("'a b' contains text 'a b' all words entire content", true);
  }

  /** Mild not. */
  @Test public void mildNot() {
    pre("//w[text() contains text 'xml' not in 'xml databases']", 3, 5);
    pre("//w[text() contains text 'hello' not in 'xml']", 14);
    query("'a b' contains text 'a' not in 'a b'", false);
    query("'a' contains text 'a' not in 'a'", false);
    query("'a b' contains text 'a b' not in 'a'", true);
    query("'a b a' contains text 'a' not in 'a b'", true);
    query("'a b a b' contains text 'a' not in 'a b'", false);
    error("'a' contains text 'a' not in ftnot 'a'", FTMILD);
    pre("//w[text() contains text 'xml' not in 'we have']", 3, 5, 7, 9, 11);
  }

  /** Unary not. */
  @Test public void unaryNot() {
    pre("//w[text() contains text ftnot 'xml']", 14, 37);
    pre("//w[text() contains text 'xml' ftand ftnot 'databases']", 3, 5);
    pre("//w[text() contains text ftnot 'databases' ftand 'xml']", 3, 5);
    pre("//*[text() contains text 'adf' ftand ftnot 'xml']", 31);
    pre("//w[text() contains text 'xml' ftand ftnot 'databases' using case sensitive]",
        3, 5, 9, 11);
    pre("//w[text() contains text 'databases' ftor ftnot 'xml']", 7, 9, 11, 14, 37);
    pre("//w[text() contains text 'hello' ftor ftnot 'databases']", 3, 5, 14, 37);
    pre("//w[text() contains text ftnot 'bier']", 3, 5, 7, 9, 11, 14, 37);
    pre("//w[text() contains text ftnot 'bier' ftand ftnot 'wein' ]", 3, 5, 7, 9, 11, 14, 37);
    pre("//w[text() contains text ftnot 'bier' ftor ftnot 'wein' ]", 3, 5, 7, 9, 11, 14, 37);
    pre("//w[text() contains text ftnot 'xml' ftand ftnot 'databeses' ]", 14, 37);
    pre("//fti[text() contains text ftnot (ftnot 'adf') ]", 31);
    pre("//fti[text() contains text 'adf' ftand ftnot (ftnot 'adf')]", 31);
    pre("//fti[text() contains text 'adf' ftor ftnot (ftnot 'adf')]", 31);
    pre("//fti[text() contains text 'adf' ftor ftnot 'adf']", 25, 27, 29, 31, 33);
  }
}
