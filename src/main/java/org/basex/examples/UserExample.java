package org.basex.examples;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Grant;
import org.basex.core.cmd.ShowUsers;

/**
 * This class presents methods to directly access
 * core maintenance and administration features.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public class UserExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  protected UserExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== AdministrationExample ===");

    // ----------------------------------------------------------------------
    // Create a database
    System.out.println("\n* Create a database.");

    new CreateDB("input", "etc/xml/input.xml").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Create a new user
    System.out.println("\n* Create a user.");

    new CreateUser("testuser", "password").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Remove global user rights
    System.out.println("\n* Remove global user rights.");

    new Grant("NONE", "testuser").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Grant local user rights on database 'input'
    System.out.println("\n* Grant local user rights.");

    new Grant("WRITE", "testuser", "input").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Show global users
    System.out.println("\n* Show global users.");

    new ShowUsers().execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Show local users on a single database
    System.out.println("\n* Show local users.");

    new ShowUsers("input").execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Change user password
    System.out.println("\n* Alter a user's password.");

    new AlterUser("testuser", "newpass").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Drop the database and user
    System.out.println("\n* Drop the user and database.");

    new DropUser("testuser").execute(CONTEXT);
    new DropDB("input").execute(CONTEXT);
  }
}
