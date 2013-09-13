import java.io._

/**
 * This example shows how external variables can be bound to XQuery expressions.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
 */
object querybindexample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  def main(args: Array[String]) {
    // create session
    val session = new BaseXClient("localhost", 1984, "admin", "admin")

    // create query instance
    val input = "declare variable $name external; " +
        "for $i in 1 to 10 return element { $name } { $i }"
    val query = session.query(input)

    // bind variable
    query.bind("$name", "number");

    // print result
    println(query.execute)

    // close query instance
    query.close

    // close session
    session.close
  }
}
