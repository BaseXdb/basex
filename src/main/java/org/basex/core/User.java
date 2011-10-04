package org.basex.core;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** No permissions. */
  public static final byte NONE = 0;
  /** Read permission (local+global). */
  public static final byte READ = 1;
  /** Write permission (local+global). */
  public static final byte WRITE = 2;
  /** Create permission (global). */
  public static final byte CREATE = 4;
  /** Admin permission (global). */
  public static final byte ADMIN = 8;

  /** User name. */
  public final String name;
  /** Password. */
  public String password;
  /** Permission. */
  public int perm;

  /**
   * Constructor.
   * @param n user name
   * @param p password
   * @param r rights
   */
  User(final String n, final String p, final int r) {
    name = n;
    password = p;
    perm = r;
  }

  /**
   * Tests if the specified permission is set.
   * @param flag flag to be checked
   * @return result of check
   */
  public boolean perm(final int flag) {
    return perm >= flag;
  }

  /**
   * Returns a local copy of this user.
   * @return user copy
   */
  public User copy() {
    return new User(name, password, Math.min(perm, WRITE));
  }
}
