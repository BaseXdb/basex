package org.basex.core;

import java.util.*;

/**
 * This class contains information on a single user.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class User {
  /** User name. */
  public final String name;
  /** Password (md5-encoded, lower case). */
  public String password;
  /** Permission. */
  public Perm perm;

  /**
   * Constructor.
   * @param name user name
   * @param password password
   * @param perm rights
   */
  User(final String name, final String password, final Perm perm) {
    this.name = name;
    this.password = password.toLowerCase(Locale.ENGLISH);
    this.perm = perm;
  }

  /**
   * Tests if the user has the specified permission.
   * @param prm permission to be checked
   * @return result of check
   */
  public boolean has(final Perm prm) {
    return perm.num >= prm.num;
  }

  /**
   * Returns a local copy of this user.
   * @return user copy
   */
  public User copy() {
    return new User(name, password, perm.min(Perm.WRITE));
  }
}
