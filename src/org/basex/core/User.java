package org.basex.core;

/**
 * This class contains information on a single user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class User {
  /** User name. */
  public String name;
  /** Password. */
  public byte[] pw;
  /** Read permission. */
  public boolean read;
  /** Write permission. */
  public boolean write;
  /** Create permission. */
  public boolean create;
  /** Admin rights. */
  public boolean admin;
  
  /**
   * Constructor.
   * @param n user name
   * @param p password
   * @param r read permission
   * @param w write permission
   * @param c create permission
   * @param a admin rights
   */
  public User(final String n, final byte[] p, final boolean r,
      final boolean w, final boolean c, final boolean a) {
    name = n;
    pw = p;
    read = r;
    write = w;
    create = c;
    admin = a;
  }
}
