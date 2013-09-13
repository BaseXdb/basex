import java.io._

/**
 * This example shows how new databases can be created.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
 */
object createexample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  def main(args: Array[String]) {
    // create session
    val session = new BaseXClient("localhost", 1984, "admin", "admin")

    // define input stream
    val bais = new ByteArrayInputStream("<xml>Hello World!</xml>".getBytes)

    // create new database
    session.create("database", bais)
    println(session.info)

    // run query on database
    println(session.execute("xquery doc('database')"))

    // drop database
    session.execute("drop db database")

    // close session
    session.close
  }
}
