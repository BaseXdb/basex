package net.xqj.basex.local;

import javax.xml.xquery.*;

import org.junit.*;

/**
 * Base class for all XQJ local tests.
 *
 * @author Charles Foster
 */
public abstract class XQJBaseTest {
  /** Data source. */
  protected BaseXXQDataSource xqds;
  /** Connection. */
  protected XQConnection xqc;

  /**
   * Initializes a test.
   */
  @Before
  public void setUp() {
    xqds = new BaseXXQDataSource();
    xqc = xqds.getConnection();
  }

  /**
   * Finalizes a test.
   * @throws XQException xquery exception
   */
  @After
  public void tearDown() throws XQException {
    xqc.close();
  }
}
