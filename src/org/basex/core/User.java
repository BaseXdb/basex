package org.basex.core;

/**
 * This class contains information on a single user.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class User {
  /** Flag for read permissions. */
  public static final int READ = 1;
  /** Flag for write permissions. */
  public static final int WRITE = 2;
  /** Flag for create permissions. */
  public static final int CREATE = 4;
  /** Flag for admin permissions. */
  public static final int ADMIN = 8;

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
   * Returns if the specified permission is set.
   * @param flag flag to be checked
   * @return result of check
   */
  public boolean perm(final int flag) {
    return (perm & flag) != 0;
  }

  /**
   * Changes the permission flag.
   * @param set set/remove flag
   * @param p permission to be set/removed
   */
  public void perm(final boolean set, final int p) {
    if(set) perm |= p;
    else perm &= ~p;
  }
  
  /**
   * Returns a copy of this user.
   * @return user copy
   */
  public User copy() {
    return new User(name, pw, perm); 
  }
}
