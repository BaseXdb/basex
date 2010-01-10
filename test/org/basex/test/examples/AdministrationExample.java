package org.basex.test.examples;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.AlterUser;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Grant;
import org.basex.core.proc.ShowUsers;

/**
 * This class presents methods to directly access
 * core maintenance and administration features.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public class AdministrationExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  protected AdministrationExample() { }

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

    new CreateDB("input.xml", "input").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Create a new user
    System.out.println("\n* Create a user.");

    new CreateUser("testuser", "password").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Remove global user rights
    System.out.println("\n* Remove global user rights.");

    new Grant("NONE", "testuser").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Grant local user rights on database »input«
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
