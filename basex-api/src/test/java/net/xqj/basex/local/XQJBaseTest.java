package net.xqj.basex.local;

import org.junit.After;
import org.junit.Before;
import javax.xml.xquery.XQConnection;

/**
 * Base class for all XQJ local tests.
 * @author cfoster
 */
public abstract class XQJBaseTest {

  protected BaseXXQDataSource xqds;
  protected XQConnection xqc;

  @Before
  public void setUp() throws Exception {
    xqds = new BaseXXQDataSource();
    xqc = xqds.getConnection();
  }

  @After
  public void tearDown() throws Exception {
    xqc.close();
  }


}
