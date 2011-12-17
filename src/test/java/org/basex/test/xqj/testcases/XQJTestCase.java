// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj.testcases;

import java.io.FileInputStream;
import java.util.Properties;
import junit.framework.TestCase;
import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQJTestCase extends TestCase {
  protected XQDataSource xqds;
  protected XQConnection xqc;

  protected void setUp() throws Exception {
    System.setProperty("com.oracle.xqj.tck.datasource", "src/test/resources/bxq.properties");
    //System.setProperty("com.oracle.xqj.tck.datasource", "saxonxq.properties");

    // Get the file name of the properties file
    final String fileName = System.getProperty("com.oracle.xqj.tck.datasource");
    if (fileName == null)
      throw new Exception("The property 'com.oracle.xqj.tck.datasource' must be set.");
    // load the properties file
    final Properties p = new Properties();
    final FileInputStream fis = new FileInputStream(fileName);
    p.load(fis);
    fis.close();
    // create an XQDataSource instance using reflection
    final String xqdsClassName = p.getProperty("XQDataSourceClassName");
    final Class xqdsClass = Class.forName(xqdsClassName);
    // create the XQDataSource instance
    xqds = (XQDataSource)xqdsClass.newInstance();
    // remove the XQDataSourceClassName property
    // as the XQJ implementation doesn't know about it and raise an error
    p.remove("XQDataSourceClassName");
    if (!p.isEmpty())
      // set the remaining properties
      xqds.setProperties(p);

    xqc = xqds.getConnection();
  }

  protected void tearDown() throws Exception {
   xqc.close();
  }

  // dummy method to suppress JUnit warning for finding no tests
  public void testDummy() {
  }
}
