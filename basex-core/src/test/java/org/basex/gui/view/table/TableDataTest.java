package org.basex.gui.view.table;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.data.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests the table data of the table view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TableDataTest extends SandboxTest {
  /** Test document. */
  private static final String DOC = "<r>"
      + "<a><x>2</x><y>b</y><z>c</z></a>"
      + "<a><x>1</x><y>a</y><z>d</z></a>"
      + "<a><x>3</x><y>c</y><z>e</z></a>"
      + "</r>";

  /** Drops the test database. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
  }

  /** Returning to the root context keeps the sort column of the reused root rows. */
  @Test public void rootSort() {
    execute(new CreateDB(NAME, DOC));
    final Data data = context.data();
    final TableData tdata = new TableData(context, null);
    tdata.init(data);
    tdata.init(data, data.elemNames.index(Token.token("a")));

    int col = -1;
    for(int c = 0; c < tdata.cols.length; c++) {
      if(Token.eq(tdata.cols[c].name, Token.token("x"))) col = c;
    }
    tdata.sortCol = col;
    tdata.asc = true;
    tdata.sort();
    final String sorted = tdata.rows.toString();

    // switch to a sub context, then back to the root
    context.current(new DBNodes(data, tdata.rows.get(0)));
    tdata.context(false);
    context.invalidate();
    tdata.context(false);

    assertEquals(sorted, tdata.rows.toString());
    assertEquals(col, tdata.sortCol);
  }
}
