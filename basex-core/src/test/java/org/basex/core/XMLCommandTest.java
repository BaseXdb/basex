package org.basex.core;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.parse.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the XML syntax of the database commands.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class XMLCommandTest extends SandboxTest {
  /** Check syntax of all commands. */
  @Test public void commands() {
    ok("<commands/>");
    ok("<commands> </commands>");
    ok("<commands> \n </commands>");
    ok("<commands> <!-- xyz --> </commands>");
    ok("<commands><add>x</add></commands>");
    ok("<commands><info/><info/></commands>");

    ok("<add>x</add>");
    ok("<add path='X'>X</add>");
    ok("<add path='X'><X/></add>");

    ok("<alter-password name='X'/>");
    ok("<alter-password name='X'>X</alter-password>");

    ok("<alter-user name='X' newname='Y'/>");

    ok("<binary-get path='X'/>");

    ok("<binary-put>X</binary-put>");
    ok("<binary-put path='X'>X</binary-put>");
    ok("<binary-put path='X'><X/></binary-put>");

    ok("<check input='X'/>");

    ok("<close/>");

    ok("<copy name='X' newname='X'/>");

    ok("<create-backup name='X'/>");
    ok("<create-backup name='X' comment='bla'/>");

    ok("<create-db name='X'/>");
    ok("<create-db name='X'>X</create-db>");
    ok("<create-db name='X'><X/></create-db>");

    ok("<create-index type='X'/>");

    ok("<create-user name='X'/>");
    ok("<create-user name='X'>X</create-user>");

    ok("<delete path='X'/>");

    ok("<drop-backup name='X'/>");

    ok("<drop-db name='X'/>");

    ok("<drop-index type='X'/>");

    ok("<drop-user name='X' pattern='X'/>");
    ok("<drop-user name='X'/>");

    ok("<execute><info/><info/></execute>");
    ok("<execute>info</execute>");

    ok("<exit/>");

    ok("<export path='X'/>");

    ok("<find>X</find>");

    ok("<flush/>");

    ok("<grant name='X' permission='X' pattern='X'/>");
    ok("<grant name='X' permission='X'/>");

    ok("<help>X</help>");
    ok("<help/>");

    ok("<info/>");
    ok("<info-db/>");
    ok("<info-index type='X'/>");
    ok("<info-index/>");
    ok("<info-storage/>");
    ok("<info-storage start='1'/>");
    ok("<info-storage start='1' end='2'/>");
    ok("<info-storage end='1'/>");
    no("<info-storage>X</info-storage>");

    ok("<kill target='X'/>");

    ok("<list/>");
    ok("<list name='X'/>");
    ok("<list name='X' path='X'/>");

    ok("<open name='X'/>");
    ok("<open name='X' path='Y'/>");

    ok("<optimize/>");

    ok("<optimize-all/>");

    ok("<password/>");
    ok("<password>X</password>");

    ok("<put path='X'>X</put>");
    ok("<put path='X'><X/></put>");

    ok("<rename path='X' newpath='X'/>");

    ok("<repo-delete name='X'/>");

    ok("<repo-install path='X'/>");

    ok("<repo-list/>");

    ok("<restore name='X'/>");

    ok("<run file='X'/>");

    ok("<set option='X'/>");
    ok("<set option='X'>X</set>");

    ok("<show-backups/>");

    ok("<show-options/>");
    ok("<show-options name='X'/>");

    ok("<show-sessions/>");

    ok("<show-users/>");
    ok("<show-users database='X'/>");

    ok("<xquery>X</xquery>");
  }

  /** Evaluates some commands with invalid syntax. */
  @Test public void failing() {
    no("<commands>X</commands>");
    no("<add/>");
    no("<add x='X'>X</add>");
    no("<add path='X' x='X'>X</add>");
    no("<alter-db/>");
    no("<alter-db>X</alter-db>");
    no("<alter-db name='X' newname='X'>X</alter-db>");
    no("<alter-db name='X'/>");
    no("<alter-db newname='X'/>");
  }

  /**
   * Assumes that this command is successful.
   * @param string XML command string
   */
  private static void ok(final String string) {
    try {
      CommandParser.get(string, context).parse();
    } catch(final QueryException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Assumes that this command fails.
   * @param string XML command string
   */
  private static void no(final String string) {
    try {
      CommandParser.get(string, context).parse();
      fail('"' + string + "\" was supposed to fail.");
    } catch(final QueryException ex) {
      Util.debug(ex);
      /* expected */
    }
  }
}
