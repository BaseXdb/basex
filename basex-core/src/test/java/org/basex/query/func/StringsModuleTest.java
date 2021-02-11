package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Strings Module.
 * Many of the Soundex and ColognePhonetic tests have been adopted from the
 * Apache Commons project (SoundexTest.java, ColognePhoneticTest.java).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringsModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void colognePhonetic() {
    colognePhonetic("", "");

    colognePhonetic("wikipedia", "3412");
    colognePhonetic("WIKIPEDIA", "3412");

    colognePhonetic("ßüöä", "8");
    colognePhonetic("suoa", "8");

    colognePhonetic("Aabjoe", "01");
    colognePhonetic("Aaclan", "0856");
    colognePhonetic("Aychlmajr", "04567");

    colognePhonetic("Müller-Lüdenscheidt", "65752682");
    colognePhonetic("bergisch-gladbach", "174845214");

    // edge cases
    colognePhonetic("a", "0");
    colognePhonetic("e", "0");
    colognePhonetic("i", "0");
    colognePhonetic("o", "0");
    colognePhonetic("u", "0");
    colognePhonetic("ä", "0");
    colognePhonetic("ö", "0");
    colognePhonetic("ü", "0");
    colognePhonetic("aa", "0");
    colognePhonetic("ha", "0");
    colognePhonetic("h", "");
    colognePhonetic("aha", "0");
    colognePhonetic("b", "1");
    colognePhonetic("p", "1");
    colognePhonetic("ph", "3");
    colognePhonetic("f", "3");
    colognePhonetic("v", "3");
    colognePhonetic("w", "3");
    colognePhonetic("g", "4");
    colognePhonetic("k", "4");
    colognePhonetic("q", "4");
    colognePhonetic("x", "48");
    colognePhonetic("ax", "048");
    colognePhonetic("cx", "48");
    colognePhonetic("l", "5");
    colognePhonetic("cl", "45");
    colognePhonetic("acl", "085");
    colognePhonetic("mn", "6");
    colognePhonetic("r", "7");

    // phonetic examples
    colognePhonetic("müller", "657");
    colognePhonetic("schmidt", "862");
    colognePhonetic("schneider", "8627");
    colognePhonetic("fischer", "387");
    colognePhonetic("weber", "317");
    colognePhonetic("wagner", "3467");
    colognePhonetic("becker", "147");
    colognePhonetic("hoffmann", "0366");
    colognePhonetic("schäfer", "837");
    colognePhonetic("Breschnew", "17863");
    colognePhonetic("Wikipedia", "3412");
    colognePhonetic("peter", "127");
    colognePhonetic("pharma", "376");
    colognePhonetic("mönchengladbach", "664645214");
    colognePhonetic("deutsch", "28");
    colognePhonetic("deutz", "28");
    colognePhonetic("hamburg", "06174");
    colognePhonetic("hannover", "0637");
    colognePhonetic("christstollen", "478256");
    colognePhonetic("Xanthippe", "48621");
    colognePhonetic("Zacharias", "8478");
    colognePhonetic("Holzbau", "0581");
    colognePhonetic("matsch", "68");
    colognePhonetic("matz", "68");
    colognePhonetic("Arbeitsamt", "071862");
    colognePhonetic("Eberhard", "01772");
    colognePhonetic("Eberhardt", "01772");
    colognePhonetic("heithabu", "021");
  }

  /** Test method. */
  @Test public void colognePhoneticEquals() {
    cologneEquals("Mayr", "Meyer");
    cologneEquals("house", "house");
    cologneEquals("House", "house");
    cologneEquals("Haus", "house");
    cologneEquals("Gans", "ganz");
    cologneEquals("Gänse", "ganz");
    cologneEquals("Miyagi", "Miyako");
  }

  /** Test method. */
  @Test public void colognePhoneticVariations() {
    cologneVariations("65", "mella", "milah", "moulla", "mellah", "muehle", "mule");
    cologneVariations("67", "Meier", "Maier", "Mair", "Meyer", "Meyr", "Mejer", "Major");
  }

  /** Test method. */
  @Test public void levenshtein() {
    final Function func = _STRINGS_LEVENSHTEIN;
    // queries
    query(func.args("ab", "ab"), 1);
    query(func.args("ab", "a"), 0.5);
    query(func.args("ab", "a"), 0.5);
    query(func.args("ab", ""), 0);

    query(func.args("ac", "ab"), 0.5);
    query(func.args("a", "ab"), 0.5);
    query(func.args("", "ab"), 0);

    query(func.args("ab", "ba"), 0.5);

    query(func.args("", ""), 1);

    query("let $x := string-join((1 to 1000) ! 'a') return " + func.args(" $x", " $x"), 1);
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test public void soundex() {
    final Function func = _STRINGS_SOUNDEX;
    // queries
    query(func.args(""), "0000");
    query(func.args(" \"\""), "0000");

    query(func.args(" \"&#x9;&#xa;&#xd; Washington &#x9;&#xa;&#xd;\" "), "W252");
    query(func.args("Ashcraft"), "A261");
    query(func.args("BOOTHDAVIS"), "B312");
    query(func.args("BOOTH-DAVIS"), "B312");
    query(func.args("Smith"), "S530");
    query(func.args("Smythe"), "S530");
    query(func.args("Williams"), "W452");

    query(func.args("testing"), "T235");
    query(func.args("The"), "T000");
    query(func.args("quick"), "Q200");
    query(func.args("brown"), "B650");
    query(func.args("fox"), "F200");
    query(func.args("jumped"), "J513");
    query(func.args("over"), "O160");
    query(func.args("the"), "T000");
    query(func.args("lazy"), "L200");
    query(func.args("dogs"), "D200");

    query(func.args("Allricht"), "A462");
    query(func.args("Eberhard"), "E166");
    query(func.args("Engebrethson"), "E521");
    query(func.args("Heimbach"), "H512");
    query(func.args("Hanselmann"), "H524");
    query(func.args("Hildebrand"), "H431");
    query(func.args("Kavanagh"), "K152");
    query(func.args("Lind"), "L530");
    query(func.args("Lukaschowsky"), "L222");
    query(func.args("McDonnell"), "M235");
    query(func.args("McGee"), "M200");
    query(func.args("Opnian"), "O155");
    query(func.args("Oppenheimer"), "O155");
    query(func.args("Riedemanas"), "R355");
    query(func.args("Zita"), "Z300");
    query(func.args("Zitzmeinn"), "Z325");

    query(func.args("Washington"), "W252");
    query(func.args("Lee"), "L000");
    query(func.args("Gutierrez"), "G362");
    query(func.args("Pfister"), "P236");
    query(func.args("Jackson"), "J250");
    query(func.args("Tymczak"), "T522");
    query(func.args("VanDeusen"), "V532");

    query(func.args("HOLMES"), "H452");
    query(func.args("ADOMOMI"), "A355");
    query(func.args("VONDERLEHR"), "V536");
    query(func.args("BALL"), "B400");
    query(func.args("SHAW"), "S000");
    query(func.args("JACKSON"), "J250");
    query(func.args("SCANLON"), "S545");
    query(func.args("SAINTJOHN"), "S532");

    query(func.args("Ann"), "A500");
    query(func.args("Andrew"), "A536");
    query(func.args("Janet"), "J530");
    query(func.args("Margaret"), "M626");
    query(func.args("Steven"), "S315");
    query(func.args("Michael"), "M240");
    query(func.args("Robert"), "R163");
    query(func.args("Laura"), "L600");
    query(func.args("Anne"), "A500");
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test public void soundexDifference() {
    soundexDiff("Smith", "Smythe", 4);
    soundexDiff("Ann", "Andrew", 2);
    soundexDiff("Margaret", "Andrew", 1);
    soundexDiff("Janet", "Margaret", 0);

    soundexDiff("Green", "Greene", 4);
    soundexDiff("Blotchet-Halls", "Greene", 0);

    soundexDiff("Smith", "Smythe", 4);
    soundexDiff("Smithers", "Smythers", 4);
    soundexDiff("Anothers", "Brothers", 2);
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test public void soundexVariations() {
    soundexVariations("B650",
      "BARHAM", "BARONE", "BARRON", "BERNA", "BIRNEY", "BIRNIE", "BOOROM", "BOREN", "BORN", "BOURN",
      "BOURNE", "BOWRON", "BRAIN", "BRAME", "BRANN", "BRAUN", "BREEN", "BRIEN", "BRIM", "BRIMM",
      "BRINN", "BRION", "BROOM", "BROOME", "BROWN", "BROWNE", "BRUEN", "BRUHN", "BRUIN", "BRUMM",
      "BRUN", "BRUNO", "BRYAN", "BURIAN", "BURN", "BURNEY", "BYRAM", "BYRNE", "BYRON", "BYRUM"
    );
    soundexVariations("O165",
      "OBrien", "'OBrien", "O'Brien", "OB'rien", "OBr'ien", "OBri'en", "OBrie'n", "OBrien'"
    );
    soundexVariations("K525",
      "KINGSMITH", "-KINGSMITH", "K-INGSMITH", "KI-NGSMITH", "KIN-GSMITH", "KING-SMITH",
      "KINGS-MITH", "KINGSM-ITH", "KINGSMI-TH", "KINGSMIT-H", "KINGSMITH-"
    );
    soundexVariations("W452",
      "Williams"
    );
    soundexVariations("S460",
      "Sgler", "Swhgler",
      "SAILOR", "SALYER", "SAYLOR", "SCHALLER", "SCHELLER", "SCHILLER", "SCHOOLER", "SCHULER",
      "SCHUYLER", "SEILER", "SEYLER", "SHOLAR", "SHULER", "SILAR", "SILER", "SILLER"
    );
    soundexVariations("E625",
      "Erickson", "Erickson", "Erikson", "Ericson", "Ericksen", "Ericsen"
    );
  }

  /**
   * Checks Soundex variations.
   * @param code expected code
   * @param variations variations
   */
  private static void soundexVariations(final String code, final String... variations) {
    final Function func = _STRINGS_SOUNDEX;
    for(final String string : variations) query(func.args(string), code);
  }

  /**
   * Checks Soundex differences.
   * @param string1 first string
   * @param string2 second string
   * @param diff difference
   */
  private static void soundexDiff(final String string1, final String string2, final int diff) {
    final Function func = _STRINGS_SOUNDEX;
    // queries
    query("sum(for-each-pair(" +
      "string-to-codepoints(" + func.args(string1) + "), " +
      "string-to-codepoints(" + func.args(string2) + "), " +
      "function($cp1, $cp2) { if($cp1 = $cp2) then 1 else 0 }))", diff);
  }

  /**
   * Tests the cologne-phonetic method.
   * @param arg argument
   * @param result result
   */
  private static void colognePhonetic(final String arg, final String result) {
    final Function func = _STRINGS_COLOGNE_PHONETIC;
    query(func.args(arg), result);
  }

  /**
   * Tests the cologne-phonetic method.
   * @param string1 first string
   * @param string2 second string
   */
  private static void cologneEquals(final String string1, final String string2) {
    final Function func = _STRINGS_COLOGNE_PHONETIC;
    query(func.args(string1) + " = " + func.args(string2), true);
  }

  /**
   * Checks Cologne Phonetic variations.
   * @param code expected code
   * @param variations variations
   */
  private static void cologneVariations(final String code, final String... variations) {
    final Function func = _STRINGS_COLOGNE_PHONETIC;
    for(final String string : variations) query(func.args(string), code);
  }
}
