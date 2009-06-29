/*
 * Copyright # 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/**
 * An object that provides hooks for connection pool management.  
 * A <code>PooledXQConnection</code> object
 * represents a physical connection to a data source.  The connection
 * can be recycled rather than being closed when an application is
 * finished with it, thus reducing the number of connections that
 * need to be made.
 * <P>
 * An application programmer does not use the <code>PooledXQConnection</code>
 * interface directly; rather, it is used by a middle tier infrastructure
 * that manages the pooling of connections.
 * <P>
 * When an application calls the method <code>XQDataSource.getConnection</code>,
 * it gets back an <code>XQConnection</code> object.  If connection pooling is
 * being done, that <code>XQConnection</code> object is actually a handle to
 * a <code>PooledXQConnection</code> object, which is a physical connection.
 * <P>
 * The connection pool manager, typically the application server, maintains
 * a pool of <code>PooledXQConnection</code> objects.  If there is a
 * <code>PooledXQConnection</code> object available in the pool, the
 * connection pool manager returns an <code>XQConnection</code> object that
 * is a handle to that physical connection.
 * If no <code>PooledXQConnection</code> object is available, the 
 * connection pool manager calls the <code>ConnectionPoolXQDataSource</code>
 * method <code>getPooledConnection</code> to create a new physical connection and
 * returns a handle to it.
 * <P>
 * When an application closes a connection, it calls the <code>XQConnection</code>
 * method <code>close</code>. When connection pooling is being done,
 * the connection pool manager is notified because it has registered itself as
 * an <code>XQConnectionEventListener</code> object using the 
 * <code>PooledXQConnection</code> method <code>addConnectionEventListener</code>.
 * The connection pool manager deactivates the handle to
 * the <code>PooledXQConnection</code> object and returns the 
 * <code>PooledXQConnection</code> object to the pool of connections so that
 * it can be used again.  Thus, when an application closes its connection,
 * the underlying physical connection is recycled rather than being closed.
 * <P>
 * The physical connection is not closed until the connection pool manager
 * calls the <code>PooledXQConnection</code> method <code>close</code>.
 * This method is generally called to have an orderly shutdown of the server or
 * if a fatal error has made the physical connection unusable.
 */

public interface PooledXQConnection {

  /**
   * Creates and returns an <code>XQConnection</code> object that is a handle
   * for the physical connection that this <code>PooledXQConnection</code>
   * object represents.
   * The connection pool manager calls this method when an application has
   * called the <code>XQDataSource</code> method <code>getConnection</code>
   * and there are no <code>PooledXQConnection</code> objects available.
   *
   * @return  an <code>XQConnection</code> object that is a handle to
   *          this <code>PooledXQConnection</code> object
   * @exception XQException if a datasource access error occurs
   */
  XQConnection getConnection() throws XQException;

  /**
   * Closes the physical connection that this <code>PooledXQConnection</code>
   * object represents.  An application never calls this method directly;
   * it is called by the connection pool manager.
   *
   * @exception XQException if a datasource access error occurs
   */
  void close() throws XQException;
      
  /**
   * Registers the given event listener so that it will be notified
   * when an event occurs on this <code>PooledXQConnection</code> object.
   *
   * @param listener a component, usually the connection pool manager,
   *        that has implemented the
   *        <code>XQConnectionEventListener</code> interface and wants to be
   *        notified when the connection is closed or has an error
   * @see #removeConnectionEventListener
   */
  void addConnectionEventListener(XQConnectionEventListener listener);

  /**
   * Removes the given event listener from the list of components that
   * will be notified when an event occurs on this
   * <code>PooledXQConnection</code> object.
   *
   * @param listener a component, usually the connection pool manager,
   *        that has implemented the
   *        <code>XQConnectionEventListener</code> interface and 
   *        been registered with this <code>PooledXQConnection</code> object as 
   *        a listener
   * @see #addConnectionEventListener
   */
  void removeConnectionEventListener(XQConnectionEventListener listener);
}
