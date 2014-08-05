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
  /** Password Digest (md5-encoded with realm "basex.org", lower case). */
  public String digest;
  /** Permission. */
  public Perm perm;

  /**
   * Constructor.
   * @param n user name
   * @param p password
   * @param d digest password
   * @param r rights
   */
  User(final String n, final String p, final String d, final Perm r) {
    name = n;
    password = p.toLowerCase(Locale.ENGLISH);
    digest = d.toLowerCase(Locale.ENGLISH);
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
    return new User(name, password, digest, perm.min(Perm.WRITE));
  }

}
