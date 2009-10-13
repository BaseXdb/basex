package org.basex.core;

/**
 * This class contains information on a single user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class User {
  /** Flag for read permissions. */
  public static final int READ = 256;
  /** Flag for write permissions. */
  public static final int WRITE = 512;
  /** Flag for create permissions. */
  public static final int CREATE = 1024;
  /** Flag for admin permissions. */
  public static final int ADMIN = 2048;

  /** User name. */
  public String name;
  /** Password. */
  public byte[] pw;
  /** Permission. */
  public int perm;
  
  /**
   * Constructor.
   * @param n user name
   * @param p password
   * @param r rights
   */
  public User(final String n, final byte[] p, final int r) {
    name = n;
    pw = p;
    perm = r;
  }
  
  /**
   * Returns if the user has read permissions.
   * @return result of check
   */
  public boolean read() {
    return (perm & READ) != 0;
  }
  
  /**
   * Returns if the user has write permissions.
   * @return result of check
   */
  public boolean write() {
    return (perm & WRITE) != 0;
  }
  
  /**
   * Returns if the user has create permissions.
   * @return result of check
   */
  public boolean create() {
    return (perm & CREATE) != 0;
  }
  
  /**
   * Returns if the user has admin permissions.
   * @return result of check
   */
  public boolean admin() {
    return (perm & ADMIN) != 0;
  }
}
