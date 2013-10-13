package org.basex.test.core;

import static org.junit.Assert.*;

import org.basex.core.parse.*;
import org.basex.query.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the XML syntax of the database commands.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class XMLCommandTest extends SandboxTest {
  /** Check syntax of all commands. */
  @Test
  public final void commands() {
    // surrounded by <commands/> element (required to process more than one command)
    ok("<commands><add>x</add></commands>");

    ok("<add>x</add>");
    ok("<add path='X'>X</add>");
    ok("<add path='X'><X/></add>");

    ok("<alter-user name='X'/>");
    ok("<alter-user name='X'>X</alter-user>");

    ok("<check input='X'/>");

    ok("<close/>");

    ok("<copy name='X' newname='X'/>");

    ok("<create-backup name='X'/>");

    ok("<create-db name='X'/>");
    ok("<create-db name='X'>X</create-db>");
    ok("<create-db name='X'><X/></create-db>");

    ok("<create-event name='X'/>");

    ok("<create-index type='X'/>");

    ok("<create-user name='X'/>");
    ok("<create-user name='X'>X</create-user>");

    ok("<cs>X</cs>");

    ok("<delete path='X'/>");

    ok("<drop-backup name='X'/>");

    ok("<drop-db name='X'/>");

    ok("<drop-event name='X'/>");

    ok("<drop-index type='X'/>");

    ok("<drop-user name='X' database='X'/>");
    ok("<drop-user name='X'/>");

    ok("<execute><info/><info/></execute>");
    ok("<execute>info</execute>");

    ok("<exit/>");

    ok("<export path='X'/>");

    ok("<find>X</find>");

    ok("<flush/>");

    ok("<get option='X'/>");

    ok("<grant name='X' permission='X' database='X'/>");
    ok("<grant name='X' permission='X'/>");

    ok("<help>X</help>");
    ok("<help/>");

    ok("<info/>");
    ok("<info-db/>");
    ok("<info-index type='X'/>");
    ok("<info-index/>");
    ok("<info-storage/>");
    ok("<info-storage>X</info-storage>");

    ok("<kill target='X'/>");

    ok("<list/>");
    ok("<list name='X'/>");
    ok("<list name='X' path='X'/>");

    ok("<open name='X'/>");

    ok("<optimize/>");

    ok("<optimize-all/>");

    ok("<password/>");
    ok("<password>X</password>");

    ok("<rename path='X' newpath='X'/>");

    ok("<replace path='X'>X</replace>");
    ok("<replace path='X'><X/></replace>");

    ok("<repo-delete name='X'/>");

    ok("<repo-install path='X'/>");

    ok("<repo-list/>");

    ok("<restore name='X'/>");

    ok("<retrieve path='X'/>");

    ok("<run file='X'/>");

    ok("<set option='X'/>");
    ok("<set option='X'>X</set>");

    ok("<show-backups/>");

    ok("<show-events/>");

    ok("<show-sessions/>");

    ok("<show-users/>");
    ok("<show-users database='X'/>");

    ok("<store>X</store>");
    ok("<store path='X'>X</store>");
    ok("<store path='X'><X/></store>");

    ok("<xquery>X</xquery>");
  }

  /** Evaluates some commands with invalid syntax. */
  @Test
  public final void failing() {
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
   * @param xml command
   */
  static final void ok(final String xml) {
    try {
      new CommandParser(xml, context).parse();
    } catch(final QueryException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Assumes that this command fails.
   * @param xml command
   */
  static final void no(final String xml) {
    try {
      new CommandParser(xml, context).parse();
      fail("\"" + xml + "\" was supposed to fail.");
    } catch(final QueryException ex) {
      /* expected */
    }
  }
}
