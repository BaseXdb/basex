import java.io._

/**
 * This example shows how commands can be executed via the server instance.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
 */
object example {
  /**
   * Main method.
   * @param args command-line arguments
   */
  def main(args: Array[String]) {
    // initialize timer
    val time = System.nanoTime

    // create session
    val session = new BaseXClient("localhost", 1984, "admin", "admin")

    // version 1: perform command and print returned string
    println(session.execute("info"))

    // version 2 (faster): perform command and pass on result to output stream
    val out = System.out
    session.execute("xquery 1 to 10", out)

    // close session
    session.close

    // print time needed
    val ms = (System.nanoTime - time) / 1000000d
    println("\n\n" + ms + " ms")
  }
}
