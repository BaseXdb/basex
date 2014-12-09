package org.basex.core.users;

import static org.basex.core.users.UserText.*;
import static org.basex.util.XMLAccess.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class organizes all users.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Users {
  /** User array. */
  private final ArrayList<User> users = new ArrayList<>(0);
  /** Filename; set to {@code null} if the instance handles local users. */
  private final IOFile file;

  /**
   * Constructor for global users.
   * @param sopts static options
   */
  public Users(final StaticOptions sopts) {
    file = globalPath(sopts);
    read();
    // ensure that default admin user exists
    if(get(ADMIN) == null) users.add(new User(ADMIN, ADMIN, Perm.ADMIN));
  }

  /**
   * Reads user permissions.
   */
  public synchronized void read() {
    if(file == null || !file.exists()) return;

    try {
      final byte[] io = file.read();
      final IOContent content = new IOContent(io, file.path());
      // legacy: ignore contents of older permission files
      if(!startsWith(io, '<')) return;

      final MainOptions options = new MainOptions(false);
      options.set(MainOptions.INTPARSE, true);
      final ANode doc = new DBNode(Parser.singleParser(content, options, ""));
      final ANode root = children(doc, USERS).next();
      if(root == null) {
        Util.errln(file.name() + ": Missing 'users' root element.");
      } else {
        for(final ANode child : children(root, USER)) {
          try {
            users.add(new User(child));
          } catch(final BaseXException ex) {
            // reject users with faulty data
            Util.errln(file.name() + ": " + ex.getLocalizedMessage());
          }
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    }
  }

  /**
   * Writes permissions to disk.
   */
  public synchronized void write() {
    if(file == null) return;

    if(store()) {
      try {
        final XMLBuilder xml = new XMLBuilder().indent().open(USERS);
        for(final User user : users) user.write(xml);
        file.write(xml.finish());
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    } else {
      file.delete();
    }
  }

  /**
   * Stores a user and encrypted password.
   * @param name user name
   * @param password password (plain text)
   * @param perm permission (can be {@code null})
   */
  public synchronized void create(final String name, final String password, final Perm perm) {
    users.add(new User(name, password, perm == null ? Perm.NONE : perm));
  }

  /**
   * Changes the password of a user.
   * @param user user
   * @param password password
   */
  public synchronized void password(final User user, final String password) {
    user.password(password);
  }

  /**
   * Sets the permission of a user.
   * @param user user
   * @param prm permission
   * @param db database (can be {@code null})
   */
  public void perm(final User user, final Perm prm, final String db) {
    user.perm(prm, db);
  }

  /**
   * Renames a user.
   * @param user user reference
   * @param name new name
   */
  public synchronized void alter(final User user, final String name) {
    user.name(name);
  }

  /**
   * Drops a user from the list.
   * @param user user reference
   * @param db optional restriction to database
   * @return success flag
   */
  public synchronized boolean drop(final User user, final String db) {
    if(db == null) {
      if(!users.remove(user)) return false;
    } else {
      user.remove(db);
    }
    return true;
  }

  /**
   * Returns user with the specified name.
   * @param name user name
   * @return success of operation
   */
  public synchronized User get(final String name) {
    for(final User user : users) if(user.name().equals(name)) return user;
    return null;
  }

  /**
   * Returns all users that match the specified pattern.
   * @param pattern user pattern
   * @return user list
   */
  public synchronized String[] find(final Pattern pattern) {
    final StringList sl = new StringList();
    for(final User u : users) {
      final String name = u.name();
      if(pattern.matcher(name).matches()) sl.add(name);
    }
    return sl.finish();
  }

  /**
   * Returns table with all users, or users from a specified database.
   * @param db database (can be {@code null})
   * @return user information
   */
  public synchronized Table info(final String db) {
    final Table table = new Table();
    table.description = Text.USERS_X;

    for(int u = 0; u < 2; ++u) table.header.add(S_USERINFO[u]);
    for(final User user : users(db)) {
      final TokenList tl = new TokenList();
      tl.add(user.name());
      tl.add(user.perm(db).toString());
      table.contents.add(tl);
    }
    return table.sort().toTop(token(ADMIN));
  }

  /**
   * Returns all users, or users from a specified database.
   * @param db database (can be {@code null})
   * @return user information
   */
  public synchronized ArrayList<User> users(final String db) {
    if(db == null) return users;
    final ArrayList<User> tmp = new ArrayList<>();
    for(final User user : users) {
      if(user.local().containsKey(db)) tmp.add(user);
    }
    return tmp;
  }

  /**
   * Returns the path to the global permission file.
   * @param sopts static options
   * @return file reference
   */
  private static IOFile globalPath(final StaticOptions sopts) {
    // try to find permission file in database and home directory
    final String perm = IO.BASEXSUFFIX + "perm";
    final IOFile file = new IOFile(sopts.dbpath(), perm);
    return file.exists() ? file : new IOFile(Prop.HOME, perm);
  }

  /**
   * Checks if permissions need to be stored.
   * @return result of check
   */
  private synchronized boolean store() {
    if(users.size() != 1) return !users.isEmpty();
    final User user = users.get(0);
    return !user.name().equals(ADMIN) ||
           !user.code(Algorithm.DIGEST, Code.HASH).equals(User.digest(ADMIN, ADMIN));
  }
}
