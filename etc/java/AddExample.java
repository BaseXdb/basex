import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This example shows how documents can be added to databases.
 * Documentation: http://basex.org/api
 *
 * @author BaseX Team 2005-11, ISC License
 */
public final class AddExample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new AddExample();
  }

  /**
   * Constructor.
   */
  private AddExample() {
    try {
      // create session
      BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      try {
        // create empty database
        session.execute("create db testdb");
        System.out.println(session.info());

        // define input stream
        InputStream bais =
          new ByteArrayInputStream("<x>Hello World!</x>".getBytes());

        // add document
        session.add("world.xml", "/world", bais);
        System.out.println(session.info());

        // define input stream
        bais = new ByteArrayInputStream("<x>Hello Universe!</x>".getBytes());

        // add document
        session.add("universe.xml", "", bais);
        System.out.println(session.info());

        // run query on database
        System.out.println();
        System.out.println(session.execute("xquery collection('testdb')"));

        // drop database
        session.execute("drop db testdb");

      } catch(IOException ex) {
        // print exception
        ex.printStackTrace();
      }

      // close session
      session.close();

    } catch(IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  }
}
