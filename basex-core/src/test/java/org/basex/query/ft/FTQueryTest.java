package org.basex.query.ft;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.CreateDB;
import org.junit.*;

/**
 * Full-text query tests.
 *
 * @author BaseX Team 2005-19, BSD License
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
    final String options = "map { 'mode': 'all words', 'wildcards': true() }";

    set(MainOptions.FTINDEX, true);
    execute(new CreateDB(NAME, "<name>" + text + "</name>"));

    query("ft:search('" + NAME + "', ('aa.*', '1111.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('aa.*', '111.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('aa.*', '11.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('aa.*', '1.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('a.*', '1111.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('a.*', '111.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('a.*', '11.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('a.*', '1.*'), " + options + ")", text);

    query("ft:search('" + NAME + "', ('aa', '1111.*'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('aa.*', '1111'), " + options + ")", text);
    query("ft:search('" + NAME + "', ('aa', '1111'), " + options + ")", text);
  }
}
