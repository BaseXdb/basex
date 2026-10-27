package org.basex.examples;

import java.lang.reflect.*;

import org.basex.examples.create.*;
import org.basex.examples.local.*;
import org.basex.examples.module.*;
import org.basex.examples.rest.*;
import org.basex.examples.server.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Runs the examples that work without an external server.
 *
 * @author BaseX Team, BSD License
 */
public final class ExamplesTest {
  /**
   * Runs an example.
   * @param example example class
   * @throws Exception exception
   */
  @ParameterizedTest
  @ValueSource(classes = {
    BindContext.class, BindVariables.class, CreateCollection.class, QueryCollection.class,
    RunCommands.class, RunQueries.class, WikiExample.class,
    CSVExample.class, HTMLExample.class,
    FruitsExample.class,
    ServerAndLocal.class, ServerCollection.class, ServerCommands.class,
    ServerConcurrency.class, ServerQueries.class, UserExample.class,
    RESTAll.class
  })
  public void run(final Class<?> example) throws Exception {
    runMain(example);
  }

  /**
   * Runs the main method of an example.
   * @param example example class
   * @throws Exception exception
   */
  static void runMain(final Class<?> example) throws Exception {
    final Method main = example.getMethod("main", String[].class);
    try {
      main.invoke(null, (Object) new String[0]);
    } catch(final InvocationTargetException ex) {
      throw ex.getCause() instanceof final Exception e ? e : ex;
    }
  }
}
