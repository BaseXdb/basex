package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Set;
import org.basex.core.proc.XPath;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.xpath.*;

/**
 * This class serves as an example for executing XPath requests.
 * [...]
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XPathExample {
  /** Input XML file. */
  private static final String XMLFILE = "input.xml";
  /** Name of the resulting database. */
  private static final String DBNAME = "input";
  /** Sample query. */
  private static final String QUERY = "//li";
  /** Result file. */
  private static final String RESULT = "result.txt";

  /** Private constructor. */
  private XPathExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // First example, creating a database and
    // writing the query result to RESULT:
    System.out.println("First example, writing result to " + RESULT + ":");

    // Creates a new database context
    Context context = new Context();
    // Creates a database
    new CreateDB(XMLFILE, DBNAME).execute(context, null);
    // Serializes query results as well-formed XML
    new Set("xmloutput", "on").execute(context, null);

    // Creates a standard output stream
    PrintOutput file = new PrintOutput(RESULT);

    // Executes the XPath query and writes the result to the output stream 
    new XPath(QUERY).execute(context, file);

    // Closes the output stream
    file.close();
    System.out.println();
    
    // Second example, creating a database and
    // writing the query result to the the standard output:
    System.out.println("Second example, writing result to standard output:");

    // Creates a query instance
    QueryProcessor xpath = new XPathProcessor(QUERY);
    // Start a query with the default context set (root node).
    Nodes nodes = context.current();
    // Executes the query
    Result result = xpath.query(nodes);

    // Prints the output to an output stream
    ConsoleOutput console = new ConsoleOutput(System.out);
    result.serialize(new XMLSerializer(console));
    console.flush();
  }
}
