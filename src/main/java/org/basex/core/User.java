package org.basex.core;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** User name. */
  public final String name;
  /** Password. */
  public String password;
  /** Permission. */
  public Perm perm;

  /**
   * Constructor.
   * @param n user name
   * @param p password
   * @param r rights
   */
  User(final String n, final String p, final Perm r) {
    name = n;
    password = p;
    perm = r;
  }

  /**
   * Tests if the user has the specified permission.
   * @param p permission to be checked
   * @return result of check
   */
  public boolean has(final Perm p) {
    return perm.num >= p.num;
  }

  /**
   * Returns a local copy of this user.
   * @return user copy
   */
  public User copy() {
    return new User(name, password, perm.min(Perm.WRITE));
  }
}
