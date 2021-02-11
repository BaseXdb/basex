package org.basex.examples.server;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This class presents methods to directly access
 * core maintenance and administration features.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class UserExample {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String... args) throws BaseXException {
    /** Database context. */
    final Context context = new Context();

    System.out.println("=== UserExample ===");

    // Create a database
    System.out.println("\n* Create a database.");

    new CreateDB("input", "src/main/resources/xml/input.xml").execute(context);

    // Create a new user with password 'password' (MD5 encoded)
    System.out.println("\n* Create a user.");

    new CreateUser("testuser", "5f4dcc3b5aa765d61d8327deb882cf99").execute(context);

    // Remove global user rights
    System.out.println("\n* Remove global user rights.");

    new Grant("NONE", "testuser").execute(context);

    // Grant local user rights on database 'input'
    System.out.println("\n* Grant local user rights.");

    new Grant("WRITE", "testuser", "input").execute(context);

    // Show global users
    System.out.println("\n* Show global users.");

    System.out.print(new ShowUsers().execute(context));

    // Show local users on a single database
    System.out.println("\n* Show local users.");

    System.out.print(new ShowUsers("input").execute(context));

    // Change user password to 'new'
    System.out.println("\n* Alter a user's password.");

    new AlterPassword("testuser", "new").execute(context);

    // Drop the database and user
    System.out.println("\n* Drop the user and database.");

    new DropUser("testuser").execute(context);
    new DropDB("input").execute(context);

    // Close the database context
    context.close();
  }
}
