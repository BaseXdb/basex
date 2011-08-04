import java.io._

/**
 * This example shows how queries can be executed in an iterative manner.
 * The database server must be started first to make this example work.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-11, BSD License
 */
object queryexample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  def main(args: Array[String]) {
    // create session
    val session = new BaseXClient("localhost", 1984, "admin", "admin")

    // create query instance
    val input = "for $i in 1 to 10 return <xml>Text { $i }</xml>"
    val query = session.query(input)

    // initialize iterator
    print(query.init)

    // loop through all results
    while(query.more) println(query.next)

    // close query instance
    print(query.close)

    // close session
    session.close
  }
}
