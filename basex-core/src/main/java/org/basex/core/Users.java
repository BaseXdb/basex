package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
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
  /** Filename; set to {@code null} if the instance handles local users. */
  private IOFile file;

  /**
   * Constructor for global users.
   * @param sopts static options ({@code null} if instance is local)
   */
  public Users(final StaticOptions sopts) {
    if(sopts == null) return;

    // try to find permission file in database and home directory
    final String perm = IO.BASEXSUFFIX + "perm";
    file = new IOFile(sopts.dbpath(), perm);
    if(!file.exists()) file = new IOFile(Prop.HOME, perm);

    if(file.exists()) {
      try {
        read(new DataInput(file));
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    } else {
      // define default admin user with all rights
      list.add(new User(S_ADMIN, S_ADMIN, Perm.ADMIN));
    }
  }

  /**
   * Parses user permissions.
   * @param f file to read from
   * @throws IOException I/O error while reading from the file
   */
  public synchronized void read(final IOFile f) throws IOException {
    if(!f.exists()) return;
    try(final DataInput in = new DataInput(f)) {
      // ...
    }
  }

  /**
   * Reads users from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public synchronized void read(final DataInput in) throws IOException {
    for(int u = in.readNum(); u > 0; --u) list.add(new User(in));
  }

  /**
   * Writes global permissions to disk.
   */
  public synchronized void write() {
    if(file == null) return;
    // [CG] USERS: write to XML
    try(final DataOutput out = new DataOutput(file)) {
      write(out);
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  /**
   * Stores a user and encrypted password.
   * @param username user name
   * @param password password (plain text)
   * @return success of operation
   */
  public synchronized boolean create(final String username, final String password) {
    // check if user already exists
    return get(username) == null && create(new User(username, password, Perm.NONE));
  }

  /**
   * Adds the specified user.
   * @param user user to be added
   * @return success of operation
   */
  public synchronized boolean create(final User user) {
    list.add(user);
    write();
    return true;
  }

  /**
   * Changes the password of a user.
   * @param username user name
   * @param password password
   * @return success of operation
   */
  public synchronized boolean alter(final String username, final String password) {
    // check if user already exists
    final User user = get(username);
    if(user == null) return false;

    user.password(password);
    write();
    return true;
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
   * Writes permissions to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public synchronized void write(final DataOutput out) throws IOException {
    out.writeNum(list.size());
    for(final User user : list) user.write(out);
  }

  /**
   * Returns information on all users.
   * @param users optional global user list (for ignoring obsolete local users)
   * @return user information
   */
  public synchronized Table info(final Users users) {
    final Table table = new Table();
    table.description = USERS_X;

    final int sz = file == null ? 3 : 5;
    for(int u = 0; u < sz; ++u) table.header.add(S_USERINFO[u]);

    for(final User user : users(users)) {
      final TokenList tl = new TokenList();
      tl.add(user.name());
      tl.add(user.has(Perm.READ) ? "X" : "");
      tl.add(user.has(Perm.WRITE) ? "X" : "");
      if(sz == 5) {
        tl.add(user.has(Perm.CREATE) ? "X" : "");
        tl.add(user.has(Perm.ADMIN) ? "X" : "");
      }
      table.contents.add(tl);
    }
    return table.sort().toTop(token(S_ADMIN));
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
}
