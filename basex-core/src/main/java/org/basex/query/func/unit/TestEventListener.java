package org.basex.query.func.unit;

import org.basex.query.QueryException;
import org.basex.query.func.StaticFunc;
import org.basex.query.value.item.QNm;

/**
 * XQUnit tests: Trace Event Listener for Unit Tests
 *
 * Enable External Test Frameworks to listen in on the status of tests
 * Enabling JUnit / ScalaTest and Continuous Integration Frameworks to add
 * XQuery Tests as part of their build process
 *
 * @author Charles Foster
 */

public interface TestEventListener {

  /**
   * tell listeners that an atomic test is about to start.
   * @param func XQuery Function Reference executing the test
   */
  void fireTestStarted(final StaticFunc func);

  /**
   * Tell listeners that an atomic test failed
   * @param func XQuery Function Reference executing the test
   * @param prefixId QName prefixId
   */
  void fireTestFailure(final StaticFunc func, byte[] prefixId);

  /**
   * Tell listeners that an atomic test failed due to an Exception
   * @param func XQuery Function Reference executing the test
   * @param e Query Exception which is ultimately the cause
   * @param code QName code
   */
  void fireTestFailure(
    final StaticFunc func,
    final QueryException e,
    final QNm code
  );

  /**
   * Tell listeners that an atomic test was ignored.
   * @param func XQuery Function Reference executing the test
   * @ignoreArgs ignore arguments serialized as a byte array
   */
  void fireTestIgnored(StaticFunc func, byte[] ignoreArgs);

  /**
   * Tell listeners that an atomic test finished.
   * @param func XQuery Function Reference executing the test
   * @time time it took to execute the test serialized in a byte array
   */
  void fireTestFinished(StaticFunc func, byte[] time);

}
