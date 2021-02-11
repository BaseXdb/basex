package org.basex.api.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.server.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the correctness of the item types of the client API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XdmInfoTest extends SandboxTest {
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private static TestSession session;

  /**
   * Starts a session.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    server = createServer();
    session = new TestSession(Text.S_LOCALHOST, DB_PORT, UserText.ADMIN, UserText.ADMIN);
  }

  /**
   * Stops a session.
   * @throws Exception exception
   */
  @AfterAll public static void stop() throws Exception {
    session.close();
    stopServer(server);
  }

  /**
   * Tests the returned item types.
   * @throws IOException I/O exception
   */
  @Test public void testIter() throws IOException {
    for(final Object[] exp : TYPES) {
      if(exp.length < 2) continue;
      try(TestQuery tq = session.query(exp[1].toString())) {
        final TestResult tr = tq.iter();
        final Object[] type = TYPES[tr.type];
        assertSame(exp, type, "Types are different.\nExpected: " + exp[0] + "\nFound: " + type[0]);
        assertEquals(Token.string(tr.result), type[2]);
      }
    }
  }

  /**
   * Tests the returned XDM information.
   * @throws IOException I/O exception
   */
  @Test public void testFull() throws IOException {
    for(final Object[] exp : TYPES) {
      if(exp.length < 2) continue;
      try(TestQuery tq = session.query(exp[1].toString())) {
        final TestResult tr = tq.full(exp);
        final Object[] type = TYPES[tr.type];
        assertSame(exp, type, "Types are different.\nExpected: " + exp[0] + "\nFound: " + type[0]);
        assertEquals(Token.string(tr.result), type[2]);
        if(exp.length > 3) assertEquals(Token.string(tr.uri), type[3]);
      }
    }
  }

  /**
   * Extends the client session.
   */
  private static class TestSession extends ClientSession {
    /**
     * Constructor.
     * @param host host name
     * @param port port
     * @param user user name
     * @param pass password
     * @throws IOException I/O exception
     */
    TestSession(final String host, final int port, final String user, final String pass)
        throws IOException {
      super(host, port, user, pass);
    }

    @Override
    public TestQuery query(final String query) throws IOException {
      return new TestQuery(query, this);
    }

    /**
     * Executes a command and sends the result to the specified output stream.
     * @param cmd server command
     * @param arg argument
     * @return string
     * @throws IOException I/O exception
     */
    byte[] exec(final ServerCmd cmd, final String arg) throws IOException {
      final ArrayOutput ao = new ArrayOutput();
      sout.write(cmd.code);
      send(arg);
      sout.flush();
      final BufferInput bi = BufferInput.get(sin);
      ClientSession.receive(bi, ao);
      if(!ClientSession.ok(bi)) throw new BaseXException(bi.readString());
      return ao.finish();
    }
  }

  /**
   * Extends the client session.
   */
  private static class TestQuery extends ClientQuery {
    /**
     * Constructor.
     * @param q query string
     * @param s session reference
     * @throws IOException I/O exception
     */
    TestQuery(final String q, final TestSession s) throws IOException {
      super(q, s, null);
    }

    /**
     * Returns a full result item.
     * @param exp expected data
     * @return full string
     * @throws IOException I/O exception
     */
    TestResult full(final Object[] exp) throws IOException {
      final byte[] result = ((TestSession) cs).exec(ServerCmd.FULL, id);
      final TestResult tr = new TestResult();
      tr.type = result[0];
      final int rl = result.length, b = Token.indexOf(result, 0);
      if(b == -1) {
        tr.result = Arrays.copyOfRange(result, 1, rl);
      } else {
        // result includes URI
        tr.uri = Arrays.copyOfRange(result, 1, b);
        tr.result = Arrays.copyOfRange(result, b + 1, rl);
      }

      final boolean expected = exp.length > 3;
      if(tr.uri == null) {
        if(expected) fail("URI expected for " + TYPES[tr.type][0]);
      } else if(!expected) {
        fail("No URI expected for " + TYPES[tr.type][0] + ": " + Token.string(tr.result));
      }
      return tr;
    }

    /**
     * Returns a typed result.
     * @return full string
     * @throws IOException I/O exception
     */
    TestResult iter() throws IOException {
      final byte[] result = ((TestSession) cs).exec(ServerCmd.RESULTS, id);
      final TestResult tr = new TestResult();
      tr.type = result[0];
      tr.result = Arrays.copyOfRange(result, 1, result.length);
      return tr;
    }
  }

  /**
   * Resulting item.
   */
  static class TestResult {
    /** Item kind/type. */
    int type;
    /** Optional URI. */
    byte[] uri;
    /** Result. */
    byte[] result;
  }

  /**
   * <p>Examples for available data types.</p>
   * <ul>
   * <li>Array position: type id</li>
   * <li>Entry 1: node type</li>
   * <li>Entry 2: query</li>
   * <li>Entry 3: textual result</li>
   * <li>Entry 4: URI</li>
   * </ul>
   */
  public static final Object[][] TYPES = {
    { }, { }, { }, { }, { }, { }, { },
    // code: 7
    { "function item" },
    // code: 8 and higher
    { "node()" },
    { "text()", "text { 'a' }", "a" },
    { "processing-instruction()", "processing-instruction { 'a' } { 'b' }", "<?a b?>" },
    { "element()", "<a/>", "<a/>" },
    { "document-node()", "document { 'a' }", "a", "" },
    { "document-node(element())", "document { <a/> }", "<a/>", "" },
    { "attribute()" },
    { "comment()", "comment { 'a' } ", "<!--a-->" },
    { }, { }, { }, { }, { }, { }, { }, { },
    { }, { }, { }, { }, { }, { }, { }, { },
    // code: 32 and higher
    { "item()" },
    { "xs:untyped" },
    { "xs:anyType" },
    { "xs:anySimpleType" },
    { "xs:anyAtomicType" },
    { "xs:untypedAtomic", "data(<a>a</a>)", "a" },
    { "xs:string", "'a'", "a" },
    { "xs:normalizedString", "xs:normalizedString('a')", "a" },
    { "xs:token", "xs:token('a')", "a" },
    { "xs:language", "xs:language('a')", "a" },
    { "xs:NMTOKEN", "xs:NMTOKEN('a')", "a" },
    { "xs:Name", "xs:Name('a')", "a" },
    { "xs:NCName", "xs:NCName('a')", "a" },
    { "xs:ID", "xs:ID('a')", "a" },
    { "xs:IDREF", "xs:IDREF('a')", "a" },
    { "xs:ENTITY", "xs:ENTITY('a')", "a" },
    { "xs:float", "xs:float(1)", "1" },
    { "xs:double", "1.1e0", "1.1" },
    { "xs:decimal", "1.1", "1.1" },
    { "xs:precisionDecimal" },
    { "xs:integer", "1", "1" },
    { "xs:nonPositiveInteger", "xs:nonPositiveInteger(-1)", "-1" },
    { "xs:negativeInteger", "xs:negativeInteger(-1)", "-1" },
    { "xs:long", "xs:long(1)", "1" },
    { "xs:int", "xs:int(1)", "1" },
    { "xs:short", "xs:short(1)", "1" },
    { "xs:byte", "xs:byte(1)", "1" },
    { "xs:nonNegativeInteger", "xs:nonNegativeInteger(1)", "1" },
    { "xs:unsignedLong", "xs:unsignedLong(1)", "1" },
    { "xs:unsignedInt", "xs:unsignedInt(1)", "1" },
    { "xs:unsignedShort", "xs:unsignedShort(1)", "1" },
    { "xs:unsignedByte", "xs:unsignedByte(1)", "1" },
    { "xs:positiveInteger", "xs:positiveInteger(1)", "1" },
    { "xs:duration", "xs:duration('P1Y')", "P1Y" },
    { "xs:yearMonthDuration", "xs:yearMonthDuration('P1Y')", "P1Y" },
    { "xs:dayTimeDuration", "xs:dayTimeDuration('PT1M')", "PT1M" },
    { "xs:dateTime", "xs:dateTime('2001-01-01T23:59:59')", "2001-01-01T23:59:59" },
    { "xs:dateTimeStamp" },
    { "xs:date", "xs:date('2001-01-01')", "2001-01-01" },
    { "xs:time", "xs:time('01:01:01')", "01:01:01" },
    { "xs:gYearMonth", "xs:gYearMonth('2001-01')", "2001-01" },
    { "xs:gYear", "xs:gYear('2001')", "2001" },
    { "xs:gMonthDay", "xs:gMonthDay(' --01-01 ')", "--01-01" },
    { "xs:gDay", "xs:gDay('---01')", "---01" },
    { "xs:gMonth", "xs:gMonth('--01')", "--01" },
    { "xs:boolean", "true()", "true" },
    { "basex:binary" },
    { "xs:base64Binary" },
    { "xs:hexBinary" },
    { "xs:anyURI", "xs:anyURI('a')", "a" },
    { "xs:QName", "xs:QName('xml:a')", "xml:a", "http://www.w3.org/XML/1998/namespace" },
    { "xs:NOTATION" }
  };
}
