package org.basex.core.users;

import static org.basex.core.users.UserText.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLAccess.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
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
  private final LinkedHashMap<String, User> users = new LinkedHashMap<>();
  /** Filename. */
  private final IOFile file;

  /**
   * Constructor for global users.
   * @param sopts static options
   */
  public Users(final StaticOptions sopts) {
    file = sopts.dbpath(Token.string(UserText.USERS) + IO.XMLSUFFIX);
    read();
    // ensure that default admin user exists
    if(get(ADMIN) == null) users.put(ADMIN, new User(ADMIN, ADMIN, Perm.ADMIN));
  }

  /**
   * Reads user permissions.
   */
  private synchronized void read() {
    if(!file.exists()) return;

    try {
      final byte[] io = file.read();
      final IOContent content = new IOContent(io, file.path());
      final MainOptions options = new MainOptions(false);
      options.set(MainOptions.INTPARSE, true);
      final ANode doc = new DBNode(Parser.singleParser(content, options, ""));
      final ANode root = children(doc, USERS).next();
      if(root == null) {
        Util.errln(file.name() + ": No 'users' root element.");
      } else {
        for(final ANode child : children(root, USER)) {
          try {
            final User user = new User(child);
            final String name = user.name();
            if(users.get(name) != null) {
              Util.errln(file.name() + ": User \"" + name + "\" supplied more than once.");
            } else {
              users.put(name, user);
            }
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
    if(store()) {
      try {
        final XMLBuilder xml = new XMLBuilder().indent().open(USERS);
        for(final User user : users.values()) user.write(xml);
        file.write(xml.finish());
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    } else if(file.exists()) {
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
    users.put(name, new User(name, password, perm == null ? Perm.NONE : perm));
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
   * @param pattern database pattern (can be {@code null})
   */
  public void perm(final User user, final Perm prm, final String pattern) {
    user.perm(prm, pattern);
  }

  /**
   * Renames a user.
   * @param user user reference
   * @param name new name
   */
  public synchronized void alter(final User user, final String name) {
    users.remove(user.name());
    user.name(name);
    users.put(name, user);
  }

  /**
   * Drops a user from the list.
   * @param user user reference
   * @param pattern database pattern (can be {@code null})
   * @return success flag
   */
  public synchronized boolean drop(final User user, final String pattern) {
    if(pattern == null) {
      if(users.remove(user.name()) == null) return false;
    } else {
      user.remove(pattern);
    }
    return true;
  }

  /**
   * Returns user with the specified name.
   * @param name user name
   * @return success of operation
   */
  public synchronized User get(final String name) {
    return users.get(name);
  }

  /**
   * Returns all user names that match the specified pattern.
   * @param pattern glob pattern
   * @return user list
   */
  public synchronized String[] find(final Pattern pattern) {
    final StringList sl = new StringList();
    for(final String name : users.keySet()) {
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

    for(final String info : S_USERINFO) table.header.add(info);
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
    final ArrayList<User> tmp = new ArrayList<>();
    for(final User user : users.values()) {
      if(db == null) {
        tmp.add(user);
      } else {
        final Entry<String, Perm> entry = user.find(db);
        if(entry != null) tmp.add(user);
      }
    }
    return tmp;
  }

  /**
   * Checks if permissions need to be stored.
   * @return result of check
   */
  private synchronized boolean store() {
    if(users.size() != 1) return !users.isEmpty();
    final User user = users.values().iterator().next();
    return !user.name().equals(ADMIN) ||
           !user.code(Algorithm.DIGEST, Code.HASH).equals(User.digest(ADMIN, ADMIN));
  }

  @Override
  public String toString() {
    final XMLBuilder xml = new XMLBuilder().indent().open(USERS);
    for(final User user : users.values()) user.write(xml);
    return xml.close().toString();
  }
}
