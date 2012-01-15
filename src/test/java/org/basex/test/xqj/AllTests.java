// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj;

import org.basex.test.xqj.testcases.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This class registers and runs all available XQJ tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class AllTests {

  /**
   * Main method
   * @param args (ignored) command-line arguments
   */
  public static void main(final String[] args) {
    TestRunner.run(suite());
  }

  /**
   * Adds all tests.
   * @return test instance
   */
  public static Test suite() {
    final TestSuite suite = new TestSuite("JSR 225 (XQJ) Technology Compatibility Kit");
    suite.addTestSuite(SignatureTest.class);
    suite.addTestSuite(XQConnectionTest.class);
    suite.addTestSuite(XQDataFactoryTest.class);
    suite.addTestSuite(XQDataSourceTest.class);
    suite.addTestSuite(XQDynamicContextTest.class);
    suite.addTestSuite(XQExpressionTest.class);
    suite.addTestSuite(XQItemAccessorTest.class);
    suite.addTestSuite(XQItemTest.class);
    suite.addTestSuite(XQItemTypeTest.class);
    suite.addTestSuite(XQMetaDataTest.class);
    suite.addTestSuite(XQPreparedExpressionTest.class);
    suite.addTestSuite(XQResultItemTest.class);
    suite.addTestSuite(XQResultSequenceTest.class);
    suite.addTestSuite(XQSequenceTest.class);
    suite.addTestSuite(XQSequenceTypeTest.class);
    suite.addTestSuite(XQStaticContextTest.class);
    suite.addTestSuite(XQExceptionTest.class);
    suite.addTestSuite(XQQueryExceptionTest.class);
    suite.addTestSuite(XQCancelledExceptionTest.class);
    suite.addTestSuite(XQStackTraceVariableTest.class);
    suite.addTestSuite(XQStackTraceElementTest.class);
    return suite;
  }
}
