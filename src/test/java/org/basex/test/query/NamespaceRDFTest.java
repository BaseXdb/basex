package org.basex.test.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test support of XML namespaces.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Laurent Chevalier
 */
public final class NamespaceRDFTest {
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private ClientSession session;

  /** Database name. */
  protected static final String DB = Util.name(NamespaceTest.class);

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = new BaseXServer("-z -p9999 -e9998");
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    server.stop();
  }


  /**
   * Opens a client session, creates a database and loads documents.
   * @throws IOException IO exception
   */
  @Before
  public void setUp() throws IOException {
    session = new ClientSession("localhost", 9999, "admin", "admin");
    session.execute(String.format("create db %s <default/>", DB));

    // Loads documents.
    addDoc("doc/1");
    addDoc("doc/2");
    addDoc("doc/3");
  }

  /**
   * Adds a document.
   * @param uri uri
   * @throws IOException exception
   */
  private void addDoc(final String uri) throws IOException {
    // Document content.
    final StringBuilder qs = new StringBuilder();
    qs.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-"
        + "ns#\" xmlns:core=\"http://rdfs.cyim.com/CoreContent#\">\n");
    qs.append(String.format("  <core:Container rdf:about=\"%s\"/>\n", uri));
    qs.append("</rdf:RDF>");

    // Adds it.
    final InputStream in = new ByteArrayInputStream(qs.toString().getBytes(
        "UTF-8"));
    try {
      session.add(uri, in);
    } finally {
      in.close();
    }
  }

  /**
   * Drops the database and close remaining client sessions.
   * @throws IOException IO exception
   */
  @After
  public void tearDown() throws IOException {
    session.execute("drop db " + DB);
    session.close();
  }

  /**
   * Inserts a node.
   *
   * @param uri Document uri.
   * @throws IOException IO exception
   */
  private void insertNode(final String uri) throws IOException {
    final StringBuilder qs = new StringBuilder("xquery\n");
    qs.append("declare namespace rdf = "
        + "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n");
    qs.append("declare namespace core = "
        + "\"http://rdfs.cyim.com/CoreContent#\";\n");
    qs.append("declare namespace wfdata = "
        + "\"http://rdfs.cyim.com/WorkflowData#\";\n");
    qs.append("insert node ").append(
        "<wfdata:Workflow xmlns:wfdata="
            + "\"http://rdfs.cyim.com/WorkflowData#\">\n").append(
        "  <wfdata:raiseOn>2011-11-01T11:34:08Z</wfdata:raiseOn>\n").append(
        "</wfdata:Workflow>\n").append(
        "into /rdf:RDF/*[@rdf:about=\"" + uri + "\"] ");
    session.execute(qs.toString());
  }

  /**
   * Replaces a node and declares the 'wfdata' prefix on the 'core:Container'
   * node.
   *
   * @param uri Document uri.
   * @throws IOException IO exception
   */
  private void replaceNode(final String uri) throws IOException {
    final StringBuilder qs = new StringBuilder("xquery\n");
    qs.append("declare namespace rdf = "
        + "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n");
    qs.append("declare namespace core = "
        + "\"http://rdfs.cyim.com/CoreContent#\";\n");
    qs.append("declare namespace wfdata = "
        + "\"http://rdfs.cyim.com/WorkflowData#\";\n");
    qs.append(
        "replace node /rdf:RDF/*[@rdf:about=\"" + uri
            + "\"] with <core:Container rdf:about=\"" + uri
            + "\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
            + "xmlns:core=\"http://rdfs.cyim.com/CoreContent#\" xmlns:wfdata="
            + "\"http://rdfs.cyim.com/WorkflowData#\">\n").append(
        "  <wfdata:Workflow>\n").append(
        "    <wfdata:raiseOn>2011-11-01T11:34:08Z</wfdata:raiseOn>\n").append(
        "  </wfdata:Workflow>\n").append("</core:Container>\n");
    session.execute(qs.toString());
  }

  /**
   * Replaces a node and declares the 'wfdata' prefix on the 'wfdata:Workflow'
   * node.
   *
   * @param uri Document uri.
   * @throws IOException IO exception
   */
  private void replaceNode2(final String uri) throws IOException {
    final StringBuilder qs = new StringBuilder("xquery\n");
    qs.append("declare namespace rdf = "
        + "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n"
        + "declare namespace core = "
        + "\"http://rdfs.cyim.com/CoreContent#\";\n"
        + "declare namespace wfdata = "
        + "\"http://rdfs.cyim.com/WorkflowData#\";\n"
        + "replace node /rdf:RDF/*[@rdf:about=\"" + uri
        + "\"] with <core:Container rdf:about=\"" + uri
        + "\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
        + "xmlns:core=\"http://rdfs.cyim.com/CoreContent#\">\n"
        + "  <wfdata:Workflow xmlns:wfdata= "
        + "\"http://rdfs.cyim.com/WorkflowData#\">\n"
        + "    <wfdata:raiseOn>2011-11-01T11:34:08Z</wfdata:raiseOn>\n"
        + "  </wfdata:Workflow>\n" + "</core:Container>\n");
    session.execute(qs.toString());
  }

  /**
   * Query. Select depending on added node.
   *
   * @param expected Number of expected results.
   * @throws IOException IO exception
   */
  private void query(final int expected) throws IOException {
    // Query using wildcards.
    StringBuilder qs = new StringBuilder("xquery\n");
    qs.append("declare namespace rdf = "
        + "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n");
    qs.append("for $c in /rdf:RDF/*[//*:raiseOn<\"2011-11-02T14:19:07Z\"]\n");
    qs.append("return <item about=\"{data($c/@rdf:about)}\" "
        + "raiseOn=\"{data($c//*:raiseOn)}\"/>\n");
    String results = session.execute(qs.toString());
    Assert.assertEquals("Missing results!", expected, results.isEmpty() ? 0
        : results.split("\r\n|\r|\n").length);

    // Query using namespace.
    qs = new StringBuilder("xquery\n"
        + "declare namespace rdf = "
        + "\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n"
        + "declare namespace wfdata = \"http://rdfs.cyim.com/WorkflowData#\";\n"
        + "for $c in /rdf:RDF/*[//wfdata:raiseOn<\"2011-11-02T14:19:07Z\"]\n"
        + "return <item about=\"{data($c/@rdf:about)}\" "
        + "raiseOn=\"{data($c//wfdata:raiseOn)}\"/>\n");
    results = session.execute(qs.toString());
    Assert.assertEquals("Missing results!", expected, results.isEmpty() ? 0
        : results.split("\r\n|\r|\n").length);
  }

  /**
   * Test entry point.
   * @throws IOException IO exception
   */
  @Test
  public void run() throws IOException {
    // Query using unchanged documents
    query(0);

    // Inserts a node to doc/1
    insertNode("doc/1");

    // Query. Should get 1 document
    query(1);

    // Replaces a node in doc/2 and declares the 'wfdata' prefix on the
    // 'core:Container' node.
    replaceNode("doc/2");

    // Query. Should get 2 documents.
    query(2);

    // Replaces a node in doc/3 and declares the
    // 'wfdata' prefix on the 'wfdata:Workflow' node.
    replaceNode2("doc/3");

    // Query. Should get 3 documents
    // This test is going to fail without wildcards as
    // if the new node linked to 'wfdata' prefix was not seen.
    query(3);
  }
}
