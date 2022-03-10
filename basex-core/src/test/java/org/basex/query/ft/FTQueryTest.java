package org.basex.query.ft;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.func.*;
import org.junit.jupiter.api.*;

/**
 * Full-text query tests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FTQueryTest extends SandboxTest {
  /** Checks optimizations of full-text operations. */
  @Test public void ftOptimize() {
    query("let $a := <a>x</a> "
        + "let $b := $a[. contains text '.*' all using wildcards] "
        + "let $c := $b "
        + "return $c", "<a>x</a>");
  }

  /** Wildcard queries. */
  @Test public void gh1800() {
    final String text = "999 aa 1111";
    query(_DB_CREATE.args(NAME, " <name>" + text + "</name>", NAME,
        " map { 'ftindex': true() }"));

    final Function func = _FT_SEARCH;
    final String options = " map { 'mode': 'all words', 'wildcards': true() }";
    query(func.args(NAME, " ('aa.*', '1111.*')", options), text);
    query(func.args(NAME, " ('aa.*', '111.*')",  options), text);
    query(func.args(NAME, " ('aa.*', '11.*')",   options), text);
    query(func.args(NAME, " ('aa.*', '1.*')",    options), text);
    query(func.args(NAME, " ('a.*', '1111.*')",  options), text);
    query(func.args(NAME, " ('a.*', '111.*')",   options), text);
    query(func.args(NAME, " ('a.*', '11.*')",    options), text);
    query(func.args(NAME, " ('a.*', '1.*')",     options), text);

    query(func.args(NAME, " ('aa', '1111.*')",   options), text);
    query(func.args(NAME, " ('aa.*', '1111')",   options), text);
    query(func.args(NAME, " ('aa', '1111')",     options), text);
  }

  /** Wildcard queries. */
  @Test public void gh1802() {
    final String text = "ab\u1e25";
    final Function func = _FT_SEARCH;
    final String options = " map { 'wildcards': true() }";

    // run queries with and without diacritics
    for(final boolean dc : new boolean[] { false, true }) {
      query(_DB_CREATE.args(NAME, " <name>" + text + "</name>", NAME,
          " map { 'ftindex': true(), 'diacritics': " + dc + "() }"));
      query(func.args(NAME, "...",      options), text);
      query(func.args(NAME, "..\u1e25", options), text);
      query(func.args(NAME, ".b.",      options), text);
      query(func.args(NAME, "a..",      options), text);
      query(func.args(NAME, ".b\u1e25", options), text);
      query(func.args(NAME, "ab.",      options), text);
      query(func.args(NAME, "ab\u1e25", options), text);

      query(func.args(NAME, ".{1,1}b.", options), text);
      query(func.args(NAME, "a.{1,1}.", options), text);
      query(func.args(NAME, "ab.{1,1}", options), text);
    }
  }

  /** Removal of scoring propagation. */
  @Test public void gh1981() {
    // scoring
    query("('a b' ! ft:score(. contains text 'a' ftand 'b')) > 0", true);
    query("let score $s := 'a' contains text 'a' return $s > 0", true);
    query("let score $s := 'a b' contains text 'a' ftand 'b' return $s > 0", true);

    // no scoring
    query("('a b' ! ft:score(. contains text 'a' and . contains text 'b')) > 0", false);
    query("let score $s := 'a b' contains text 'a' and 'a' contains text 'b' return $s > 0", false);
  }

  /** XQuery, ft:search: Levenshtein errors. */
  @Test public void gh1673() {
    query(_DB_CREATE.args(NAME, " <x>1</x>", NAME, " map { 'ftindex': true() }"));

    query(_FT_SEARCH.args(NAME, "2"), "");
    query(_FT_CONTAINS.args("1", "2"), false);
    query(_FT_SEARCH.args(NAME, "2", " map { 'fuzzy': true(), 'errors': 0 }"), "");
    query(_FT_CONTAINS.args("1", "2", " map { 'fuzzy': true(), 'errors': 0 }"), false);
    query(_FT_SEARCH.args(NAME, "2", " map { 'fuzzy': true(), 'errors': 1 }"), 1);
    query(_FT_CONTAINS.args("1", "2", " map { 'fuzzy': true(), 'errors': 1 }"), true);

    query("'a' contains text 'b' using fuzzy 0 errors", false);
    query("'a' contains text 'b' using fuzzy 1 errors", true);
    query("'a' contains text 'b' using fuzzy 10 errors", true);
  }

  /** Fuzzy search: handling many similar terms. */
  @Test public void gh2014() {
    query(_DB_CREATE.args(NAME, " <a><b>aaaa aabc aaab</b><b>bbbb</b><b>aaad</b></a>",
        NAME, " map { 'ftindex': true() }"));
    query(_FT_SEARCH.args(NAME, "aaaa"), "aaaa aabc aaab");
    query(_FT_SEARCH.args(NAME, "aaaa", " map { 'fuzzy': true() }"), "aaaa aabc aaab\naaad");

    query(_DB_CREATE.args(NAME, " <x>{ for $i in 1 to 100000 " +
        "return <b>{ format-number($i, '000000000000') }</b>} </x>",
        NAME, " map { 'ftindex': true() }"));
    query("count(" + _FT_SEARCH.args(NAME, "000000000000", " map { 'fuzzy': true() }") + ')',
        8146);
  }

  /** Full-Text: Modifier Letters. */
  @Test public void gh2015() {
    query("count(tokenize('fara&#700;id faraid fara-id Birkan')"
        + "[. contains text 'fara&#700;id' using fuzzy])", 2);
  }

  /** Full-Text Search: Fuzzy Phrases. */
  @Test public void gh2081() {
    query(_DB_CREATE.args(NAME, " <_>test a text</_>", NAME, " map { 'ftindex': true() }"));
    query(_FT_SEARCH.args(NAME, "test a", " map { 'fuzzy': true() }"), "test a text");
    query(_FT_SEARCH.args(NAME, "a test", " map { 'fuzzy': true() }"), "test a text");
  }
}
