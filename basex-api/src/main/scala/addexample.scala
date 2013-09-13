import java.io._

/**
 * This example shows how documents can be added to databases.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
 */
object addexample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  def main(args: Array[String]) {
    // create session
    val session = new BaseXClient("localhost", 1984, "admin", "admin")

    // create empty database
    session.execute("create db database")
    println(session.info)

    // define input stream
    var bais = new ByteArrayInputStream("<x>Hello World!</x>".getBytes)

    // add document
    session.add("world/world.xml", bais)
    println(session.info)

    // define input stream
    bais = new ByteArrayInputStream("<x>Hello Universe!</x>".getBytes)

    // add document
    session.add("universe.xml", bais)
    println(session.info)

    // run query on database
    println
    println(session.execute("xquery collection('database')"))

    // drop database
    session.execute("drop db database")

    // close session
    session.close
  }
}
