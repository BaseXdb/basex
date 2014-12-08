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
import org.basex.io.in.DataInput;
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
  private final ArrayList<User> list = new ArrayList<>(0);
  /** Global permissions. */
  private boolean global;
  /** Filename; set to {@code null} if the instance handles local users. */
  private final IOFile file;

  /**
   * Constructor for local users.
   * @param file file
   * @param global global flag
   */
  public Users(final IOFile file, final boolean global) {
    this.global = global;
    this.file = file;
  }

  /**
   * Constructor for global users.
   * @param sopts static options
   */
  public Users(final StaticOptions sopts) {
    this(globalPath(sopts), true);
    // ensure that default admin user exists
    read();
    if(get(ADMIN) == null) list.add(new User(ADMIN, ADMIN, Perm.ADMIN));
  }

  /**
   * Reads user permissions.
   */
  public synchronized void read() {
    if(file == null || !file.exists()) return;

    try {
      final byte[] io = file.read();
      final IOContent content = new IOContent(io, file.path());
      if(startsWith(io, '<')) {
        final MainOptions options = new MainOptions(false);
        options.set(MainOptions.INTPARSE, true);
        final ANode doc = new DBNode(Parser.singleParser(content, options, ""));
        final ANode users = children(doc, USERS).next();
        if(users == null) {
          Util.errln(file.name() + ": Missing 'users' root element.");
        } else {
          for(final ANode user : children(users, USER)) {
            try {
              // only accept users with complete data
              list.add(new User(user, global));
            } catch(final BaseXException ex) {
              Util.errln(file.name() + ": " + ex.getLocalizedMessage());
            }
          }
        }
      } else {
        // legacy (Version < 8)
        read(new DataInput(content));
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
        for(final User user : list) user.write(xml);
        file.write(xml.finish());
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    } else {
      file.delete();
    }
  }

  /**
   * Reads users from disk (legacy code, Version < 8; data will be ignored).
   * @param in input stream
   * @throws IOException I/O exception
   */
  public synchronized void read(final DataInput in) throws IOException {
    for(int u = in.readNum(); u > 0; --u) { in.readToken(); in.readToken(); in.readNum(); }
  }

  /**
   * Stores a user and encrypted password.
   * @param username user name
   * @param password password (plain text)
   */
  public synchronized void create(final String username, final String password) {
    add(new User(username, password, Perm.NONE));
  }

  /**
   * Adds the specified user.
   * @param user user to be added
   */
  public synchronized void add(final User user) {
    list.add(user);
    write();
  }

  /**
   * Changes the password of a user.
   * @param user user
   * @param password password
   */
  public synchronized void password(final User user, final String password) {
    user.password(password);
    write();
  }

  /**
   * Sets the permission of a user.
   * @param user user
   * @param prm permission
   */
  public void perm(final User user, final Perm prm) {
    user.perm(prm);
    write();
  }

  /**
   * Renames a user.
   * @param user user reference
   * @param name new name
   */
  public synchronized void alter(final User user, final String name) {
    user.name(name);
    write();
  }

  /**
   * Drops a user from the list.
   * @param user user reference
   * @return success flag
   */
  public synchronized boolean drop(final User user) {
    if(!list.remove(user)) return false;
    write();
    return true;
  }

  /**
   * Returns a user reference with the specified name.
   * @param username user name
   * @return success of operation
   */
  public synchronized User get(final String username) {
    for(final User user : list) if(user.name().equals(username)) return user;
    return null;
  }

  /**
   * Returns all users that match the specified pattern.
   * @param pattern user pattern
   * @return user list
   */
  public synchronized String[] find(final Pattern pattern) {
    final StringList sl = new StringList();
    for(final User u : list) {
      final String name = u.name();
      if(pattern.matcher(name).matches()) sl.add(name);
    }
    return sl.finish();
  }

  /**
   * Returns information on all users.
   * @param users optional global user list (for ignoring obsolete local users)
   * @return user information
   */
  public synchronized Table info(final Users users) {
    final Table table = new Table();
    table.description = Text.USERS_X;

    final int sz = global ? 5 : 3;
    for(int u = 0; u < sz; ++u) table.header.add(S_USERINFO[u]);

    for(final User user : users(users)) {
      final TokenList tl = new TokenList();
      tl.add(user.name());
      tl.add(user.has(Perm.READ) ? "X" : "");
      tl.add(user.has(Perm.WRITE) ? "X" : "");
      if(global) {
        tl.add(user.has(Perm.CREATE) ? "X" : "");
        tl.add(user.has(Perm.ADMIN) ? "X" : "");
      }
      table.contents.add(tl);
    }
    return table.sort().toTop(token(ADMIN));
  }

  /**
   * Returns all users.
   * @param users optional second list
   * @return user information
   */
  public synchronized User[] users(final Users users) {
    final ArrayList<User> al = new ArrayList<>();
    for(final User user : list) {
      if(users == null || users.get(user.name()) != null) al.add(user);
    }
    return al.toArray(new User[al.size()]);
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
    if(list.size() != 1) return !list.isEmpty();
    final User user = list.get(0);
    return !user.name().equals(ADMIN) ||
           !user.code(Algorithm.DIGEST, Code.HASH).equals(User.digest(ADMIN, ADMIN));
  }
}
