package org.basex.test.qt3ts;

import static org.basex.core.Prop.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.cmd.Set;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.Compare.Mode;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.tests.bxapi.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Base class for generated QT3TS test sets.
 *
 * @author Leo Woerteler
 */
public abstract class QT3TestSet {
  /** EQName pattern. */
  private static final Pattern BIND = Pattern.compile("^Q\\{(.*?)\\}(.+)$");

  /** Database context. */
  public Context ctx;

  /** Expected results. */
  public final ArrayList<String> expected = new ArrayList<String>();

  /** QT3TS path, possibly {@code null}. */
  public static final String QT3TS = System.getenv("QT3TS");

  /** The current query's result. */
  public QT3Result result;

  /**
   * Initializes the context.
   * @throws BaseXException exception
   */
  @Before
  public void buildUp() throws BaseXException {
    ctx = new Context();
    new Set(Prop.CHOP, false).execute(ctx);
    new Set(Prop.INTPARSE, false).execute(ctx);
    new Set(Prop.XQUERY3, true).execute(ctx);
    result = null;
  }

  /** Closes the context. */
  @After
  public void tearDown() {
    ctx.close();
    ctx = null;
    result = null;
  }

  /** Sets the XQuery version to 1.0. */
  protected void xquery10() {
    ctx.prop.set(Prop.XQUERY3, false);
  }

  /**
   * Tests assertion.
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertQuery(final String exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(exp);
    try {
      return result(new XQuery(exp, ctx).bind("result", value).value().getBoolean(), exp);
    } catch(final XQueryException ex) {
      // should not occur
      return fail(ex.getException().getMessage());
    }
  }

  /**
   * Tests count.
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertCount(final long exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(exp + " items");
    final int res = value.size();
    return result(exp == res, Util.info("% items (% found)", exp, res));
  }

  /**
   * Tests equality.
   * @param expect expected result
   * @return optional expected test suite result
   */
  protected boolean assertEq(final String expect) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Equal to: '%'", expect));

    try {
      final XdmItem exp = new XQuery(expect, ctx).next();
      final XdmItem res = value instanceof XdmItem ? (XdmItem) value : null;
      return result(exp.equal(res), exp.toString());
    } catch(final XQueryException err) {
      return result(expect.equals(value.getString()), err.getException().getMessage());
    }
  }

  /**
   * Tests deep equals.
   * @param expect expected result
   * @return optional expected test suite result
   */
  protected boolean assertDeepEq(final String expect) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Deep-equal to: '%'", expect));
    final XdmValue exp = new XQuery(expect, ctx).value();
    return result(exp.deepEqual(value), exp.toString());
  }

  /**
   * Tests permutation.
   * [LW] wrong
   * @param expect expected result
   * @return optional expected test suite result
   */
  protected boolean assertPermutation(final String expect) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Permutation of: '%'", expect));
    // cache expected results
    final HashSet<String> exp = new HashSet<String>();
    for(final XdmItem it : new XQuery(expect, ctx)) exp.add(it.getString());
    // cache actual results
    final HashSet<String> res = new HashSet<String>();
    for(final XdmItem it : value) res.add(it.getString());

    if(exp.size() != res.size())
      return fail(Util.info("% results (found: %)", exp.size(), res.size()));

    for(final String s : exp.toArray(new String[exp.size()])) {
      if(!res.contains(s)) return fail(Util.info("% (missing)", s));
    }
    for(final String s : res.toArray(new String[exp.size()])) {
      if(!exp.contains(s))
        return fail(Util.info("% (missing in expected result)", s));
    }
    return true;
  }

  /**
   * Tests the serialized result.
   * [LW] wrong
   * @param expect expected result
   * @param ignorePref ignore namespaces
   * @return optional expected test suite result
   */
  protected boolean assertSerialization(final String expect, final boolean ignorePref) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Serializes to: '%'", expect));

    final String res = normNL(asString("serialize(.,map{'indent':='no'})", value));
    if(expect.equals(res)) return true;

    // include check for comments, processing instructions and namespaces
    String flags = "'" + Mode.ALLNODES + "'";
    if(!ignorePref) flags += ",'" + Mode.NAMESPACES + "'";
    final String query = Function.DEEP_EQUAL_OPT.args("<X>" + expect + "</X>",
        "<X>" + res + "</X>" , "(" + flags + ")");
    return result(asBoolean(query, null), expect);
  }

  /**
   * Tests a serialization error.
   * @param code expected error code
   * @return optional expected test suite result
   */
  protected boolean assertSerialError(final String code) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Serialization error: '%'", code));
    try {
      value.toString();
      return fail(code);
    } catch(final RuntimeException qe) {
      final String res = qe.getMessage().replaceAll("\\[|\\].*\r?\n?.*", "");
      return result(code.equals("*") || code.equals(res),
          Util.info("% (found: %)", code, res));
    }
  }

  /**
   * Tests a serialization error.
   * @param code expected error code
   * @return optional expected test suite result
   */
  protected boolean error(final String code) {
    if(result.value != null) return fail(Util.info("Error: '%'", code));
    if(code.equals("*")) return true;

    String name = code, uri = string(QueryText.ERRORURI);
    final Matcher m = BIND.matcher(code);
    if(m.find()) {
      uri = m.group(1);
      name = m.group(2);
    }
    final QNm err = new QNm(name, uri);

    if(result.exc != null) {
      final QueryException qe = result.exc.getException();
      final QNm qn = qe.err() != null ? qe.err().qname() : qe.qname();
      if(qn != null) return result(err.eq(qn), Util.info("% (found: %)", err, qn));
      return fail(Util.info("% (found: %)", err, "?"));
    }

    final String msg = result.error.getMessage();
    final String res = msg == null ? Util.name(result.error)
                                   : msg.replaceAll("\\[|\\].*\r?\n?.*", "");
    return result(code.equals(res), Util.info("% (found: %)", code, res));
  }

  /**
   * Tests string value.
   * @param norm normalize space
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertStringValue(final boolean norm, final String exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("String value: '%'", exp));

    final TokenBuilder tb = new TokenBuilder();
    int c = 0;
    for(final XdmItem it : value) {
      if(c != 0) tb.add(' ');
      tb.add(it.getString());
      c++;
    }
    return result(exp.equals(norm ? string(norm(tb.finish())) : tb.toString()), exp);
  }

  /**
   * Tests boolean.
   * @param exp expected
   * @return optional expected test suite result
   */
  protected boolean assertBoolean(final boolean exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Boolean result: %", exp));
    return result(value.getType().eq(SeqType.BLN) && value.getBoolean() == exp,
        Util.info(exp));
  }

  /**
   * Tests empty sequence.
   * @return optional expected test suite result
   */
  protected boolean assertEmpty() {
    return result(result.value == XdmEmpty.EMPTY, "()");
  }

  /**
   * Tests type.
   * @param exp expected result
   * @return optional expected test suite result
   */
  protected boolean assertType(final String exp) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Has type: '%'", exp));
    try {
      final XQuery query = new XQuery("$result instance of " + exp, ctx);
      return result(query.bind("result", value).value().getBoolean(),
        Util.info("type '%' (found: '%')", exp, value.getType().toString()));
    } catch(final XQueryException ex) {
      // should not occur
      return fail(ex.getException().getMessage());
    }
  }

  /**
   * Matches the serialized result against a regular expression.
   * @param pat regex pattern
   * @param flags matching flags
   * @return optional expected test suite result
   */
  protected boolean serializationMatches(final String pat, final String flags) {
    final XdmValue value = result.value;
    if(value == null) return fail(Util.info("Matches: '%'", pat));
    final XQuery match = new XQuery("fn:matches($in, $pat, $flags)", ctx);
    match.bind("in", value.toString()).bind("pat", pat).bind("flags", flags);
    return result(match.next().getBoolean(), Util.info("Matches: '%'", pat));
  }

  /**
   * Returns the string representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  private String asString(final String query, final XdmValue value) {
    return XQuery.string(query, value, ctx);
  }

  /**
   * Returns the boolean representation of a query result.
   * @param query query string
   * @param value optional context value
   * @return optional expected test suite result
   */
  private boolean asBoolean(final String query, final XdmValue value) {
    final XdmValue xv = new XQuery(query, ctx).context(value).value();
    return xv.size() != 0 && xv.getBoolean();
  }

  /**
   * Removes comments from the specified string.
   * @param in input string
   * @return result
   */
  private static String normNL(final String in) {
    return in.replaceAll("\r\n|\r|\n", NL);
  }

  /**
   * Adds the string to the expected results if {@code success} if {@code false}
   * and returns {@code success}.
   * @param success success flag
   * @param expect expected expression
   * @return {@code success}
   */
  private boolean result(final boolean success, final String expect) {
    return success || fail(expect);
  }

  /**
   * Adds the string to the expected results.
   * @param expect expected string
   * @return {@code false}
   */
  private boolean fail(final String expect) {
    return !expected.add(expect);
  }

  /**
   * Throws an {@link AssertionError} if the test failed.
   * @param success test success
   */
  protected void test(final boolean success) {
    if(!success) {
      final StringBuilder sb = new StringBuilder("\nExpected:");
      if(expected.size() == 1) {
        sb.append(' ' + expected.get(0));
      } else {
        sb.append("one of...");
        for(final String e : expected) sb.append("\n- " + e);
      }
      sb.append("\nFound: ").append(result);
      final AssertionError err = new AssertionError(sb);
      if(result.error != null) err.setStackTrace(result.error.getStackTrace());
      else if(result.exc != null)
        err.setStackTrace(result.exc.getException().getStackTrace());
      throw err;
    }
  }

  /**
   * Reads a query string from a file.
   * @param uri file URI
   * @return file contents
   */
  protected String queryFile(final String uri) {
    try {
      return Token.string(IO.get(uri).read());
    } catch(IOException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Creates an absolute file path for the given resource path.
   * @param path resource path
   * @return absolute file path
   */
  protected String file(final String path) {
    final File f = new File("src/test/resources/qt3ts",
        path).getAbsoluteFile();
    if(f.exists()) return f.getPath();

    if(QT3TS == null) return null;

    final File dir = f.getParentFile();
    if(!dir.exists()) dir.mkdirs();

    final File qt3 = new File(QT3TS, path);
    if(!qt3.canRead()) return null;
    try {
      f.createNewFile();
      final BufferedInputStream in = new BufferedInputStream(new FileInputStream(qt3));
      final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
      final byte[] buffer = new byte[1 << 13];
      for(int len; (len = in.read(buffer)) >= 0;) {
        out.write(buffer, 0, len);
      }
      in.close();
      out.close();
      return f.getPath();
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates a document node from the given file path.
   * @param file file path
   * @return document node
   */
  protected XdmItem node(final String file) {
    return file == null ? null : new XQuery("doc('" + file + "')", ctx).next();
  }

  /**
   * Structure for storing XQuery results.
   */
  public static class QT3Result {
    /** Query result. */
    XdmValue value;
    /** Query exception. */
    XQueryException exc;
    /** Query error. */
    Throwable error;

    /**
     * Constructor for successful evaluation.
     * @param val result value
     */
    public QT3Result(final XdmValue val) {
      value = val;
    }

    /**
     * Constructor for externally thrown errors.
     * @param th cause
     */
    public QT3Result(final Throwable th) {
      if(th instanceof XQueryException) exc = (XQueryException) th;
      else error = th;
    }

    @Override
    public String toString() {
      return value != null ? value.toString() : exc != null
          ? '[' + exc.getCode() + "] " + exc.toString()
          : error.toString();
    }
  }
}
