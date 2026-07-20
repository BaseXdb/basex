package org.basex.query.func.fn;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for fn:format-integer with the German ('de') language.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatIntegerDeTest extends SandboxTest {
  /** fn:format-integer, German cardinal numbers. */
  @Test public void cardinal() {
    query("format-integer(0, 'w', 'de')", "null");
    query("format-integer(1, 'w', 'de')", "eins");
    query("format-integer(2, 'w', 'de')", "zwei");
    query("format-integer(3, 'w', 'de')", "drei");
    query("format-integer(4, 'w', 'de')", "vier");
    query("format-integer(5, 'w', 'de')", "fünf");
    query("format-integer(6, 'w', 'de')", "sechs");
    query("format-integer(7, 'w', 'de')", "sieben");
    query("format-integer(8, 'w', 'de')", "acht");
    query("format-integer(9, 'w', 'de')", "neun");
    query("format-integer(10, 'w', 'de')", "zehn");
    query("format-integer(11, 'w', 'de')", "elf");
    query("format-integer(12, 'w', 'de')", "zwölf");
    query("format-integer(13, 'w', 'de')", "dreizehn");
    query("format-integer(14, 'w', 'de')", "vierzehn");
    query("format-integer(15, 'w', 'de')", "fünfzehn");
    query("format-integer(16, 'w', 'de')", "sechzehn");
    query("format-integer(17, 'w', 'de')", "siebzehn");
    query("format-integer(18, 'w', 'de')", "achtzehn");
    query("format-integer(19, 'w', 'de')", "neunzehn");
    query("format-integer(20, 'w', 'de')", "zwanzig");
    query("format-integer(21, 'w', 'de')", "einundzwanzig");
    query("format-integer(22, 'w', 'de')", "zweiundzwanzig");
    query("format-integer(23, 'w', 'de')", "dreiundzwanzig");
    query("format-integer(24, 'w', 'de')", "vierundzwanzig");
    query("format-integer(25, 'w', 'de')", "fünfundzwanzig");
    query("format-integer(26, 'w', 'de')", "sechsundzwanzig");
    query("format-integer(27, 'w', 'de')", "siebenundzwanzig");
    query("format-integer(28, 'w', 'de')", "achtundzwanzig");
    query("format-integer(29, 'w', 'de')", "neunundzwanzig");
    query("format-integer(30, 'w', 'de')", "dreißig");
    query("format-integer(31, 'w', 'de')", "einunddreißig");
    query("format-integer(32, 'w', 'de')", "zweiunddreißig");
    query("format-integer(33, 'w', 'de')", "dreiunddreißig");
    query("format-integer(34, 'w', 'de')", "vierunddreißig");
    query("format-integer(35, 'w', 'de')", "fünfunddreißig");
    query("format-integer(36, 'w', 'de')", "sechsunddreißig");
    query("format-integer(37, 'w', 'de')", "siebenunddreißig");
    query("format-integer(38, 'w', 'de')", "achtunddreißig");
    query("format-integer(39, 'w', 'de')", "neununddreißig");
    query("format-integer(40, 'w', 'de')", "vierzig");
    query("format-integer(41, 'w', 'de')", "einundvierzig");
    query("format-integer(42, 'w', 'de')", "zweiundvierzig");
    query("format-integer(43, 'w', 'de')", "dreiundvierzig");
    query("format-integer(44, 'w', 'de')", "vierundvierzig");
    query("format-integer(45, 'w', 'de')", "fünfundvierzig");
    query("format-integer(46, 'w', 'de')", "sechsundvierzig");
    query("format-integer(47, 'w', 'de')", "siebenundvierzig");
    query("format-integer(48, 'w', 'de')", "achtundvierzig");
    query("format-integer(49, 'w', 'de')", "neunundvierzig");
    query("format-integer(50, 'w', 'de')", "fünfzig");
    query("format-integer(51, 'w', 'de')", "einundfünfzig");
    query("format-integer(52, 'w', 'de')", "zweiundfünfzig");
    query("format-integer(53, 'w', 'de')", "dreiundfünfzig");
    query("format-integer(54, 'w', 'de')", "vierundfünfzig");
    query("format-integer(55, 'w', 'de')", "fünfundfünfzig");
    query("format-integer(56, 'w', 'de')", "sechsundfünfzig");
    query("format-integer(57, 'w', 'de')", "siebenundfünfzig");
    query("format-integer(58, 'w', 'de')", "achtundfünfzig");
    query("format-integer(59, 'w', 'de')", "neunundfünfzig");
    query("format-integer(60, 'w', 'de')", "sechzig");
    query("format-integer(61, 'w', 'de')", "einundsechzig");
    query("format-integer(62, 'w', 'de')", "zweiundsechzig");
    query("format-integer(63, 'w', 'de')", "dreiundsechzig");
    query("format-integer(64, 'w', 'de')", "vierundsechzig");
    query("format-integer(65, 'w', 'de')", "fünfundsechzig");
    query("format-integer(66, 'w', 'de')", "sechsundsechzig");
    query("format-integer(67, 'w', 'de')", "siebenundsechzig");
    query("format-integer(68, 'w', 'de')", "achtundsechzig");
    query("format-integer(69, 'w', 'de')", "neunundsechzig");
    query("format-integer(70, 'w', 'de')", "siebzig");
    query("format-integer(71, 'w', 'de')", "einundsiebzig");
    query("format-integer(72, 'w', 'de')", "zweiundsiebzig");
    query("format-integer(73, 'w', 'de')", "dreiundsiebzig");
    query("format-integer(74, 'w', 'de')", "vierundsiebzig");
    query("format-integer(75, 'w', 'de')", "fünfundsiebzig");
    query("format-integer(76, 'w', 'de')", "sechsundsiebzig");
    query("format-integer(77, 'w', 'de')", "siebenundsiebzig");
    query("format-integer(78, 'w', 'de')", "achtundsiebzig");
    query("format-integer(79, 'w', 'de')", "neunundsiebzig");
    query("format-integer(80, 'w', 'de')", "achtzig");
    query("format-integer(81, 'w', 'de')", "einundachtzig");
    query("format-integer(82, 'w', 'de')", "zweiundachtzig");
    query("format-integer(83, 'w', 'de')", "dreiundachtzig");
    query("format-integer(84, 'w', 'de')", "vierundachtzig");
    query("format-integer(85, 'w', 'de')", "fünfundachtzig");
    query("format-integer(86, 'w', 'de')", "sechsundachtzig");
    query("format-integer(87, 'w', 'de')", "siebenundachtzig");
    query("format-integer(88, 'w', 'de')", "achtundachtzig");
    query("format-integer(89, 'w', 'de')", "neunundachtzig");
    query("format-integer(90, 'w', 'de')", "neunzig");
    query("format-integer(91, 'w', 'de')", "einundneunzig");
    query("format-integer(92, 'w', 'de')", "zweiundneunzig");
    query("format-integer(93, 'w', 'de')", "dreiundneunzig");
    query("format-integer(94, 'w', 'de')", "vierundneunzig");
    query("format-integer(95, 'w', 'de')", "fünfundneunzig");
    query("format-integer(96, 'w', 'de')", "sechsundneunzig");
    query("format-integer(97, 'w', 'de')", "siebenundneunzig");
    query("format-integer(98, 'w', 'de')", "achtundneunzig");
    query("format-integer(99, 'w', 'de')", "neunundneunzig");
    query("format-integer(100, 'w', 'de')", "einhundert");
    query("format-integer(101, 'w', 'de')", "einhunderteins");
    query("format-integer(109, 'w', 'de')", "einhundertneun");
    query("format-integer(110, 'w', 'de')", "einhundertzehn");
    query("format-integer(111, 'w', 'de')", "einhundertelf");
    query("format-integer(190, 'w', 'de')", "einhundertneunzig");
    query("format-integer(199, 'w', 'de')", "einhundertneunundneunzig");
    query("format-integer(200, 'w', 'de')", "zweihundert");
    query("format-integer(201, 'w', 'de')", "zweihunderteins");
    query("format-integer(999, 'w', 'de')", "neunhundertneunundneunzig");
    query("format-integer(1000, 'w', 'de')", "eintausend");
    query("format-integer(1001, 'w', 'de')", "eintausendeins");
    query("format-integer(1009, 'w', 'de')", "eintausendneun");
    query("format-integer(1010, 'w', 'de')", "eintausendzehn");
    query("format-integer(1011, 'w', 'de')", "eintausendelf");
    query("format-integer(1111, 'w', 'de')", "eintausendeinhundertelf");
    query("format-integer(9999, 'w', 'de')", "neuntausendneunhundertneunundneunzig");
    query("format-integer(999999, 'w', 'de')",
        "neunhundertneunundneunzigtausendneunhundertneunundneunzig");
    query("format-integer(999999999, 'w', 'de')", "neunhundertneunundneunzig millionen "
        + "neunhundertneunundneunzigtausendneunhundertneunundneunzig");
  }

  /** fn:format-integer, German ordinal numbers. */
  @Test public void ordinal() {
    query("format-integer(0, 'w;o', 'de')", "nullte");
    query("format-integer(1, 'w;o', 'de')", "erste");
    query("format-integer(2, 'w;o', 'de')", "zweite");
    query("format-integer(3, 'w;o', 'de')", "dritte");
    query("format-integer(4, 'w;o', 'de')", "vierte");
    query("format-integer(5, 'w;o', 'de')", "fünfte");
    query("format-integer(6, 'w;o', 'de')", "sechste");
    query("format-integer(7, 'w;o', 'de')", "siebte");
    query("format-integer(8, 'w;o', 'de')", "achte");
    query("format-integer(9, 'w;o', 'de')", "neunte");
    query("format-integer(10, 'w;o', 'de')", "zehnte");
    query("format-integer(11, 'w;o', 'de')", "elfte");
    query("format-integer(12, 'w;o', 'de')", "zwölfte");
    query("format-integer(13, 'w;o', 'de')", "dreizehnte");
    query("format-integer(14, 'w;o', 'de')", "vierzehnte");
    query("format-integer(15, 'w;o', 'de')", "fünfzehnte");
    query("format-integer(16, 'w;o', 'de')", "sechzehnte");
    query("format-integer(17, 'w;o', 'de')", "siebzehnte");
    query("format-integer(18, 'w;o', 'de')", "achtzehnte");
    query("format-integer(19, 'w;o', 'de')", "neunzehnte");
    query("format-integer(20, 'w;o', 'de')", "zwanzigste");
    query("format-integer(21, 'w;o', 'de')", "einundzwanzigste");
    query("format-integer(22, 'w;o', 'de')", "zweiundzwanzigste");
    query("format-integer(23, 'w;o', 'de')", "dreiundzwanzigste");
    query("format-integer(99, 'w;o', 'de')", "neunundneunzigste");
    query("format-integer(999, 'w;o', 'de')", "neunhundertneunundneunzigste");
    query("format-integer(9999, 'w;o', 'de')", "neuntausendneunhundertneunundneunzigste");
    query("format-integer(999999, 'w;o', 'de')",
        "neunhundertneunundneunzigtausendneunhundertneunundneunzigste");
    query("format-integer(100, 'w;o', 'de')", "einhundertste");
    query("format-integer(1000, 'w;o', 'de')", "eintausendste");
    query("format-integer(10000, 'w;o', 'de')", "zehntausendste");
    query("format-integer(100000, 'w;o', 'de')", "einhunderttausendste");
    query("format-integer(1000000, 'w;o', 'de')", "eine millionste");
  }
}
