import java.io.IOException;

/**
 * This example shows how to use the event feature.
 * The database server must be started first to make this example work.
 * Documentation: http://basex.org/api
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class EventExample {

  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new EventExample();
  }

  /** Constructor. */
  private EventExample() {
    try {
      BaseXClient session1 =
        new BaseXClient("localhost", 1984, "admin", "admin");
      BaseXClient session2 =
        new BaseXClient("localhost", 1984, "admin", "admin");

      session1.execute("create event messenger");
      session2.watch("messenger", new Notifier());
      session2.query("for $i in 1 to 1000000 where $i = 0 return $i").execute();
      session1.query("db:event('messenger', 'Hello World!')").execute();
      session2.unwatch("messenger");
      session1.execute("drop event messenger");
      session1.close();
      session2.close();

    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Implementation of the event notifier interface.
   */
  private class Notifier implements BaseXClient.EventNotifier {

    /** Constructor. */
    public Notifier() { }

    @Override
    public void notify(final String value) {
      System.out.println("Message received: " + value);
    }
  }
}
