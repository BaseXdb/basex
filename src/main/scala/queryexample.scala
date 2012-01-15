import java.io._

/**
 * This example shows how queries can be executed in an iterative manner.
 * Iterative evaluation will be slower, as more server requests are performed.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
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

    // loop through all results
    while(query.more) println(query.next)

    // close query instance
    query.close

    // close session
    session.close
  }
}
