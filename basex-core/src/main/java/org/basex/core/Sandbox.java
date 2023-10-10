package org.basex.core;

import static org.basex.core.Text.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.Set;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class Sandbox {
  /** Flag for writing queries and results to STDERR; helpful for exporting test cases. */
  private static final boolean OUTPUT = false;
  /** Base uri. */
  private static final String BASEURI = new File(".").getAbsolutePath();

  /** Database port. */
  protected static final int DB_PORT = 9996;
  /** HTTP stop port. */
  protected static final int STOP_PORT = 9999;
  /** HTTP port. */
  protected static final int HTTP_PORT = 9998;

  /** REST identifier. */
  protected static final String REST = "rest";
  /** Root path. */
  protected static final String HTTP_ROOT = "http://" + S_LOCALHOST + ':' + HTTP_PORT + '/';
  /** Root path. */
  protected static final String REST_ROOT = HTTP_ROOT + REST + '/';

  /** Default output stream. */
  protected static final PrintStream OUT = System.out;
  /** Default error stream. */
  protected static final PrintStream ERR = System.err;
  /** Test name. */
  protected static final String NAME = Util.className(Sandbox.class);
  /** Database context. */
  protected static Context context;

  /**
   * Executes a command and returns exceptions into assertion errors.
   * @param cmd command to be run
   * @return string result
   */
  protected static String execute(final Command cmd) {
    try {
      return cmd.execute(context);
    } catch(final BaseXException ex) {
      Util.stack(ex);
      return fail(ex);
    }
  }

  /**
   * Sets an option and returns exceptions into assertion errors.
   * @param option option to be set
   * @param value value to be assigned
   */
  protected static void set(final Option<?> option, final Object value) {
    execute(new Set(option.name(), value));
  }

  /**
   * Runs a query and returns exceptions into assertion errors.
   * @param query query to be evaluated
   * @return string result
   */
  protected static String query(final String query) {
    try {
      return eval(query);
    } catch(final JobException ex) {
      Util.debug(ex);
      return "";
    } catch(final QueryException | IOException ex) {
      Util.stack(ex);
      return fail("Query failed:\n" + query, ex);
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param expected expected result
   */
  protected static void query(final String query, final Object expected) {
    compare(query, query(query), expected, null);
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param result query result
   * @param expected expected result
   * @param plan query plan (can be {@code null})
   */
  protected static void compare(final String query, final String result, final Object expected,
      final ANode plan) {
    final String res = normNL(result), exp = expected.toString();
    if(OUTPUT) {
      Util.errln(query.strip());
      Util.errln(res.replace('\n', ','));
    }
    assertEquals(exp, res, "\n" + query + "\n" + (plan == null ? "" : serialize(plan)));
  }

  /**
   * Returns a string representation of a query plan.
   * @param plan query plan (can be {@code null})
   * @return string
   */
  protected static String serialize(final ANode plan) {
    try {
      return plan != null ? "PLAN: " + plan.serialize(SerializerMode.INDENT.get()) : "";
    } catch(final QueryIOException ex) {
      return fail(ex);
    }
  }

  /**
   * Creates a transform expression from a given input, modification and return clause.
   * @param input input XML fragment, target of the updating expression
   * @param modify updating expression, make sure to address all target nodes via
   * the $input variable, i.e. delete node $input/a
   * @param rtrn return clause
   * @return the query formulated with a transform expression
   */
  protected static String transform(final String input, final String modify, final String rtrn) {
    return
      "copy $input := " + input + ' ' +
      "modify (" + modify + ") " +
      "return (" + (rtrn.isEmpty() ? "$input" : rtrn) + ')';
  }

  /**
   * Creates a transform expression from a given input and modification clause.
   *
   * @param input input XML fragment, target of the updating expression
   * @param modification updating expression, make sure to address all target nodes via
   * the $input variable, i.e. delete node $input/a
   * @return the query formulated with a transform expression
   */
  protected static String transform(final String input, final String modification) {
    return transform(input, modification, "");
  }

  /**
   * Checks if a query yields the specified result.
   * @param query query string
   * @param result query result
   */
  protected static void contains(final String query, final String result) {
    final String res = normNL(query(query));
    if(!res.contains(result)) fail("Result does not contain substring: " + result +
        "\n" + query + "\n[E] " + result + "\n[F] " + res);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param error allowed errors
   */
  protected static void error(final String query, final QueryError... error) {
    if(OUTPUT) {
      Util.errln(query.strip());
      if(error.length > 0) Util.errln(error[0]);
    }
    try {
      final String res = eval(query);
      final TokenBuilder tb = new TokenBuilder().add("Query did not fail:\n");
      tb.add(query).add("\n[E] Error: ");
      for(final QueryError e : error) tb.add(' ').add(e.qname().prefixId());
      fail(tb.add("\n[F] ").add(res).toString());
    } catch(final QueryIOException ex) {
      error(query, ex.getCause(), error);
    } catch(final QueryException ex) {
      error(query, ex, error);
    } catch(final Exception ex) {
      Util.stack(ex);
      fail(ex);
    }
  }

  /**
   * Checks if an exception yields one of the specified error codes.
   * @param query query
   * @param ex resulting query exception
   * @param errors allowed errors
   */
  protected static void error(final String query, final QueryException ex,
      final QueryError... errors) {

    boolean found = false;
    final QueryError err = ex.error();
    for(final QueryError e : errors) found |= err != null ? err == e : e.qname().eq(ex.qname());

    if(!found) {
      final TokenBuilder tb = new TokenBuilder().add('\n');
      if(query != null) tb.add("Query: ").add(query).add('\n');
      tb.add("Error(s): ");
      if(err != null) {
        Util.stack(ex);
        int c = 0;
        for(final QueryError er : errors) tb.add(c++ == 0 ? "" : "/").add(er.name());
        tb.add("\nResult: ").add(err.name() + " (" + ex.getLocalizedMessage() + ')');
      } else {
        int c = 0;
        for(final QueryError er : errors) {
          if(c++ > 0) tb.add('/');
          tb.add(er.qname().local());
        }
        tb.add("\nResult: ").add(ex.qname().string());
      }
      fail(tb.toString());
    }
  }

  /**
   * Runs a query.
   * @param query query string
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected static String eval(final String query) throws QueryException, IOException {
    final ArrayOutput ao = new ArrayOutput();
    try(QueryProcessor qp = new QueryProcessor(query, BASEURI, context, null)) {
      qp.compile();
      qp.register(context);
      try(Serializer ser = qp.serializer(ao)) {
        qp.value().serialize(ser);
      } finally {
        qp.close();
        qp.unregister(context);
      }
    }
    return ao.toString();
  }

  /**
   * Checks the query plan and the result.
   * @param query query
   * @param expected result or {@code null} for no comparison
   * @param tests queries on the query plan
   */
  protected static void check(final String query, final Object expected, final String... tests) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.optimize();
      final FNode plan = qp.toXml();
      // compare result
      if(expected != null) {
        compare(query, qp.value().serialize().toString(), expected, plan);
      }
      // check syntax tree
      for(final String test : tests) {
        final FNode doc = FDoc.build().add(plan).finish();
        try(QueryProcessor qp2 = new QueryProcessor(test, context).context(doc)) {
          if(qp2.value() != Bln.TRUE) fail(Prop.NL + "QUERY: " + query + Prop.NL +
              "OPTIMIZED: " + qp.qc.main + Prop.NL + "TEST: " + test + Prop.NL + serialize(plan));
        }
      }
    } catch(final QueryException | QueryIOException ex) {
      Util.stack(ex);
      fail(ex);
    }
  }

  /**
   * Returns a test to check if the specified expression or path does not occur in the query plan.
   * @param expr expression
   * @return test string
   */
  protected static String empty(final String expr) {
    return "empty(//" + expr + ')';
  }

  /**
   * Returns a test to check if the specified expression does not occur in the query plan.
   * @param clazz name of expression
   * @return test string
   */
  protected static String empty(final Class<?> clazz) {
    return empty(Util.className(clazz));
  }

  /**
   * Returns a test to check if the specified function does not occur in the query plan.
   * @param func function
   * @return test string
   */
  protected static String empty(final Function func) {
    return empty(func.className());
  }

  /**
   * Returns a test to check if the specified expression or path occurs in the query plan.
   * @param expr expression
   * @return test string
   */
  protected static String exists(final String expr) {
    return "exists(//" + expr + ')';
  }

  /**
   * Returns a test to check if the specified expression occurs in the query plan.
   * @param clazz expression class
   * @return test string
   */
  protected static String exists(final Class<?> clazz) {
    return exists(Util.className(clazz));
  }

  /**
   * Returns a test to check if the specified function occurs in the query plan.
   * @param func function
   * @return test string
   */
  protected static String exists(final Function func) {
    return exists(func.className());
  }

  /**
   * Returns a test to check if the query plan is empty.
   * @return test string
   */
  protected static String empty() {
    return root("Empty");
  }

  /**
   * Returns a test to check if the root is an instance of the specified expression.
   * @param expr expression
   * @return test string
   */
  protected static String root(final String expr) {
    return "name(QueryPlan/*) = '" + expr + "'";
  }

  /**
   * Returns a test to check if the root is an instance of the specified expression.
   * @param func function
   * @return test string
   */
  protected static String root(final Function func) {
    return root(func.className());
  }

  /**
   * Returns a test to check if the root is an instance of the specified expression.
   * @param clazz name of expression
   * @return test string
   */
  protected static String root(final Class<?> clazz) {
    return root(Util.className(clazz));
  }

  /**
   * Counts the number of results.
   * @param clazz expression class
   * @param count number of results
   * @return test string
   */
  protected static String count(final Class<?> clazz, final int count) {
    return count(Util.className(clazz), count);
  }

  /**
   * Counts the number of results.
   * @param func function
   * @param count number of results
   * @return test string
   */
  protected static String count(final Function func, final int count) {
    return count(func.className(), count);
  }

  /**
   * Counts the number of results.
   * @param expr expression
   * @param count number of results
   * @return test string
   */
  protected static String count(final String expr, final int count) {
    return "count(//" + expr + ") = " + count;
  }

  /**
   * Returns a test to check the expression type.
   * @param name name of expression
   * @param type type
   * @return test string
   */
  protected static String type(final String name, final String type) {
    return "string(//" + name + "/@type) = '" + type + "'";
  }

  /**
   * Returns a test to check the expression type.
   * @param clazz expression class
   * @param type type
   * @return test string
   */
  protected static String type(final Class<?> clazz, final String type) {
    return type(Util.className(clazz), type);
  }

  /**
   * Returns a test to check the function type.
   * @param func function
   * @param type type
   * @return test string
   */
  protected static String type(final Function func, final String type) {
    return type(func.className(), type);
  }

  /**
   * Writes a test file.
   * @param file file
   * @param data data to write
   */
  protected static void write(final IOFile file, final String data) {
    try {
      file.write(data);
    } catch(final IOException ex) {
      Util.stack(ex);
      fail(ex);
    }
  }

  /**
   * Creates the sandbox.
   */
  public static void initSandbox() {
    final IOFile sb = sandbox();
    //sb.delete();
    if(!sb.md()) fail("Sandbox could not be created.");

    final String path = sb.path();
    Prop.put(StaticOptions.DBPATH, path + "/data");
    Prop.put(StaticOptions.WEBPATH, path + "/webapp");
    Prop.put(StaticOptions.RESTXQPATH, path + "/webapp");
    Prop.put(StaticOptions.REPOPATH, path + "/repo");
    Prop.put(StaticOptions.SERVERPORT, Integer.toString(DB_PORT));
    context = new Context();

    // disable loop unrolling and function inlining
    context.options.set(MainOptions.UNROLLLIMIT, 0);
    context.options.set(MainOptions.INLINELIMIT, 0);
  }

  /**
   * Removes test databases and closes the database context.
   */
  public static void finishSandbox() {
    context.close();
    Prop.clear();
    // cleanup: remove project specific system properties
    final Properties props = System.getProperties();
    for(final Object key : props.keySet()) {
      final String path = key.toString();
      if(path.startsWith(Prop.DBPREFIX)) props.remove(key);
    }
    if(!sandbox().delete()) fail("Sandbox could not be deleted.");
  }

  /**
   * Triggers function inlining.
   * @param enable flag
   */
  protected static void inline(final boolean enable) {
    context.options.set(MainOptions.INLINELIMIT, enable ? 1 << 16 : 0);
  }

  /**
   * Triggers function inlining.
   * @param enable flag
   */
  protected static void unroll(final boolean enable) {
    context.options.set(MainOptions.UNROLLLIMIT, enable ? 1 << 16 : 0);
  }

  /**
   * Creates a new, sandboxed server instance.
   * @param args additional arguments
   * @return server instance
   * @throws IOException I/O exception
   */
  public static BaseXServer createServer(final String... args) throws IOException {
    final StringList sl = new StringList("-z", "-p" + DB_PORT, "-P" + NAME, "-q").add(args);
    final BaseXServer server = new BaseXServer(sl.finish());
    server.context.soptions.set(StaticOptions.DBPATH, sandbox().path());
    return server;
  }

  /**
   * Stops a server instance.
   * @param server server
   * @throws IOException I/O exception
   */
  public static void stopServer(final BaseXServer server) throws IOException {
    if(server != null) server.stop();
  }

  /**
   * Creates a client instance.
   * @param login optional login data
   * @return client instance
   * @throws IOException I/O exception
   */
  public static ClientSession createClient(final String... login) throws IOException {
    final String username = login.length > 0 ? login[0] : UserText.ADMIN;
    final String password = login.length > 1 ? login[1] : NAME;
    return new ClientSession(S_LOCALHOST, DB_PORT, username, password);
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  public static IOFile sandbox() {
    return new IOFile(Prop.TEMPDIR, NAME + '/');
  }

  /**
   * Normalizes newlines in a query result.
   * @param result input string
   * @return normalized string
   */
  public static String normNL(final String result) {
    return result.replaceAll("(\r?\n|\r)", "\n");
  }

  /**
   * Returns a context value reference string that will not be optimized at compile time.
   * @return container
   */
  public static String wrapContext() {
    return " data(attribute _ { . })";
  }

  /**
   * Returns a value string that will not be optimized at compile time.
   * @param value value to return
   * @return container
   */
  public static String wrap(final Object value) {
    return " data(attribute _ { '" + value + "' })";
  }

  /** Client. */
  public static final class SandboxClient extends Thread {
    /** Start signal. */
    private final CountDownLatch startSignal;
    /** Stop signal. */
    private final CountDownLatch stopSignal;
    /** Client session. */
    private final ClientSession session;
    /** Command string. */
    private final Command cmd;
    /** Fail flag. */
    public String error;

    /**
     * Client constructor.
     * @param c command string to execute
     * @param start start signal
     * @param stop stop signal
     * @throws IOException I/O exception while establishing the session
     */
    public SandboxClient(final Command c, final CountDownLatch start, final CountDownLatch stop)
        throws IOException {

      session = createClient();
      cmd = c;
      startSignal = start;
      stopSignal = stop;
      start();
    }

    @Override
    public void run() {
      try {
        if(startSignal != null) startSignal.await();
        session.execute(cmd);
        session.close();
      } catch(final Throwable ex) {
        error = "\n" + cmd + '\n' + ex;
      } finally {
        if(stopSignal != null) stopSignal.countDown();
      }
    }
  }
}
