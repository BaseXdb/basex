package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Strings Module.
 * Many of the Soundex and ColognePhonetic tests have been adopted from the
 * Apache Commons project (SoundexTest.java, ColognePhoneticTest.java).
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class StringsModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void levenshtein() {
    query(_STRINGS_LEVENSHTEIN.args("ab", "ab"), "1");
    query(_STRINGS_LEVENSHTEIN.args("ab", "a"), "0.5");
    query(_STRINGS_LEVENSHTEIN.args("ab", "a"), "0.5");
    query(_STRINGS_LEVENSHTEIN.args("ab", ""), "0");

    query(_STRINGS_LEVENSHTEIN.args("ac", "ab"), "0.5");
    query(_STRINGS_LEVENSHTEIN.args("a", "ab"), "0.5");
    query(_STRINGS_LEVENSHTEIN.args("", "ab"), "0");

    query(_STRINGS_LEVENSHTEIN.args("ab", "ba"), "0.5");

    query(_STRINGS_LEVENSHTEIN.args("", ""), 1);

    query("let $x := string-join((1 to 1000) ! 'a') " +
        "return " + _STRINGS_LEVENSHTEIN.args("$x", "$x"), "1");
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test
  public void soundex() {
    query(_STRINGS_SOUNDEX.args(""), "0000");
    query(_STRINGS_SOUNDEX.args(" \"\""), "0000");

    query(_STRINGS_SOUNDEX.args(" \"&#x9;&#xa;&#xd; Washington &#x9;&#xa;&#xd;\" "), "W252");
    query(_STRINGS_SOUNDEX.args("Ashcraft"), "A261");
    query(_STRINGS_SOUNDEX.args("BOOTHDAVIS"), "B312");
    query(_STRINGS_SOUNDEX.args("BOOTH-DAVIS"), "B312");
    query(_STRINGS_SOUNDEX.args("Smith"), "S530");
    query(_STRINGS_SOUNDEX.args("Smythe"), "S530");
    query(_STRINGS_SOUNDEX.args("Williams"), "W452");

    query(_STRINGS_SOUNDEX.args("testing"), "T235");
    query(_STRINGS_SOUNDEX.args("The"), "T000");
    query(_STRINGS_SOUNDEX.args("quick"), "Q200");
    query(_STRINGS_SOUNDEX.args("brown"), "B650");
    query(_STRINGS_SOUNDEX.args("fox"), "F200");
    query(_STRINGS_SOUNDEX.args("jumped"), "J513");
    query(_STRINGS_SOUNDEX.args("over"), "O160");
    query(_STRINGS_SOUNDEX.args("the"), "T000");
    query(_STRINGS_SOUNDEX.args("lazy"), "L200");
    query(_STRINGS_SOUNDEX.args("dogs"), "D200");

    query(_STRINGS_SOUNDEX.args("Allricht"), "A462");
    query(_STRINGS_SOUNDEX.args("Eberhard"), "E166");
    query(_STRINGS_SOUNDEX.args("Engebrethson"), "E521");
    query(_STRINGS_SOUNDEX.args("Heimbach"), "H512");
    query(_STRINGS_SOUNDEX.args("Hanselmann"), "H524");
    query(_STRINGS_SOUNDEX.args("Hildebrand"), "H431");
    query(_STRINGS_SOUNDEX.args("Kavanagh"), "K152");
    query(_STRINGS_SOUNDEX.args("Lind"), "L530");
    query(_STRINGS_SOUNDEX.args("Lukaschowsky"), "L222");
    query(_STRINGS_SOUNDEX.args("McDonnell"), "M235");
    query(_STRINGS_SOUNDEX.args("McGee"), "M200");
    query(_STRINGS_SOUNDEX.args("Opnian"), "O155");
    query(_STRINGS_SOUNDEX.args("Oppenheimer"), "O155");
    query(_STRINGS_SOUNDEX.args("Riedemanas"), "R355");
    query(_STRINGS_SOUNDEX.args("Zita"), "Z300");
    query(_STRINGS_SOUNDEX.args("Zitzmeinn"), "Z325");

    query(_STRINGS_SOUNDEX.args("Washington"), "W252");
    query(_STRINGS_SOUNDEX.args("Lee"), "L000");
    query(_STRINGS_SOUNDEX.args("Gutierrez"), "G362");
    query(_STRINGS_SOUNDEX.args("Pfister"), "P236");
    query(_STRINGS_SOUNDEX.args("Jackson"), "J250");
    query(_STRINGS_SOUNDEX.args("Tymczak"), "T522");
    query(_STRINGS_SOUNDEX.args("VanDeusen"), "V532");

    query(_STRINGS_SOUNDEX.args("HOLMES"), "H452");
    query(_STRINGS_SOUNDEX.args("ADOMOMI"), "A355");
    query(_STRINGS_SOUNDEX.args("VONDERLEHR"), "V536");
    query(_STRINGS_SOUNDEX.args("BALL"), "B400");
    query(_STRINGS_SOUNDEX.args("SHAW"), "S000");
    query(_STRINGS_SOUNDEX.args("JACKSON"), "J250");
    query(_STRINGS_SOUNDEX.args("SCANLON"), "S545");
    query(_STRINGS_SOUNDEX.args("SAINTJOHN"), "S532");

    query(_STRINGS_SOUNDEX.args("Ann"), "A500");
    query(_STRINGS_SOUNDEX.args("Andrew"), "A536");
    query(_STRINGS_SOUNDEX.args("Janet"), "J530");
    query(_STRINGS_SOUNDEX.args("Margaret"), "M626");
    query(_STRINGS_SOUNDEX.args("Steven"), "S315");
    query(_STRINGS_SOUNDEX.args("Michael"), "M240");
    query(_STRINGS_SOUNDEX.args("Robert"), "R163");
    query(_STRINGS_SOUNDEX.args("Laura"), "L600");
    query(_STRINGS_SOUNDEX.args("Anne"), "A500");
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test
  public void soundexVariations() {
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
    query(_STRINGS_SOUNDEX.args("Williams"), "W452");
    soundexVariations("S460",
      "Sgler", "Swhgler",
      "SAILOR", "SALYER", "SAYLOR", "SCHALLER", "SCHELLER", "SCHILLER", "SCHOOLER", "SCHULER",
      "SCHUYLER", "SEILER", "SEYLER", "SHOLAR", "SHULER", "SILAR", "SILER", "SILLER"
    );
    soundexVariations("E625",
      "Erickson", "Erickson", "Erikson", "Ericson", "Ericksen", "Ericsen"
    );
  }

  /** Tests, adopted from Apache Commons project (SoundexTest.java). */
  @Test
  public void soundexDifference() {
    soundexDifference("Smith", "Smythe", 4);
    soundexDifference("Ann", "Andrew", 2);
    soundexDifference("Margaret", "Andrew", 1);
    soundexDifference("Janet", "Margaret", 0);

    soundexDifference("Green", "Greene", 4);
    soundexDifference("Blotchet-Halls", "Greene", 0);

    soundexDifference("Smith", "Smythe", 4);
    soundexDifference("Smithers", "Smythers", 4);
    soundexDifference("Anothers", "Brothers", 2);
  }

  /**
   * Checks Soundex variations.
   * @param code expected code
   * @param variations variations
   */
  private static void soundexVariations(final String code, final String... variations) {
    for(final String string : variations) query(_STRINGS_SOUNDEX.args(string), code);
  }

  /**
   * Checks Soundex differences.
   * @param string1 first string
   * @param string2 second string
   * @param diff difference
   */
  private static void soundexDifference(final String string1, final String string2,
      final int diff) {

    query(SUM.args(FOR_EACH_PAIR.args(
      STRING_TO_CODEPOINTS.args(_STRINGS_SOUNDEX.args(string1)) + ',' +
      STRING_TO_CODEPOINTS.args(_STRINGS_SOUNDEX.args(string2)) + ',' +
      "function($cp1, $cp2) { if($cp1 = $cp2) then 1 else 0 }")), diff);
  }

  /** Test method. */
  @Test
  public void colognePhonetic() {
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
  @Test
  public void colognePhoneticEquals() {
    cologneEquals("Mayr", "Meyer");
    cologneEquals("house", "house");
    cologneEquals("House", "house");
    cologneEquals("Haus", "house");
    cologneEquals("Gans", "ganz");
    cologneEquals("Gänse", "ganz");
    cologneEquals("Miyagi", "Miyako");
  }

  /** Test method. */
  @Test
  public void colognePhoneticVariations() {
    cologneVariations("65", "mella", "milah", "moulla", "mellah", "muehle", "mule");
    cologneVariations("67", "Meier", "Maier", "Mair", "Meyer", "Meyr", "Mejer", "Major");
  }

  /**
   * Tests the cologne-phonetic method.
   * @param arg argument
   * @param result result
   */
  private static void colognePhonetic(final String arg, final String result) {
    query(_STRINGS_COLOGNE_PHONETIC.args(arg), result);
  }

  /**
   * Tests the cologne-phonetic method.
   * @param string1 first string
   * @param string2 second string
   */
  private static void cologneEquals(final String string1, final String string2) {
    query(_STRINGS_COLOGNE_PHONETIC.args(string1) + " = " +
          _STRINGS_COLOGNE_PHONETIC.args(string2), true);
  }

  /**
   * Checks Cologne Phonetic variations.
   * @param code expected code
   * @param variations variations
   */
  private static void cologneVariations(final String code, final String... variations) {
    for(final String string : variations) query(_STRINGS_COLOGNE_PHONETIC.args(string), code);
  }
}
