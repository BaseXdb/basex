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
  /** Filename; set to {@code null} if the instance handles local users. */
  private IOFile file;

  /**
   * Constructor for local users.
   */
  public Users() {
  }

  /**
   * Constructor for global users.
   * @param sopts static options
   */
  public Users(final StaticOptions sopts) {
    // try to find permission file in database and home directory
    final String perm = IO.BASEXSUFFIX + "perm";
    file = new IOFile(sopts.dbpath(), perm);
    if(!file.exists()) file = new IOFile(Prop.HOME, perm);

    if(file.exists()) {
      try {
        final byte[] io = file.read();
        final IOContent content = new IOContent(io, file.path());
        if(startsWith(io, '<')) {
          read(content, true);
        } else {
          // legacy (Version < 8)
          read(new DataInput(content));
        }
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    }

    // ensure that default admin user exists
    if(get(ADMIN) == null) list.add(new User(ADMIN, ADMIN, Perm.ADMIN));
  }

  /**
   * Parses user permissions as XML.
   * @param input file to read from
   * @param global global permissions
   * @throws IOException I/O error while reading from the file
   */
  public synchronized void read(final IO input, final boolean global) throws IOException {
    final MainOptions options = new MainOptions(false);
    options.set(MainOptions.INTPARSE, true);
    final ANode doc = new DBNode(Parser.singleParser(input, options, ""));
    for(final ANode user : children(children(doc, USERS).next(), USER)) {
      try {
        // only accept users with complete data
        list.add(new User(user, global));
      } catch(final BaseXException ex) {
        Util.errln(input.name() + ": " + ex.getLocalizedMessage());
      }
    }
  }

  /**
   * Writes global permissions to disk.
   */
  public synchronized void write() {
    if(file != null) write(file);
  }

  /**
   * Writes permissions to disk.
   * @param output target file
   */
  public synchronized void write(final IOFile output) {
    if(store()) {
      try {
        final XMLBuilder xml = new XMLBuilder().indent().open(USERS);
        for(final User user : list) user.write(xml);
        output.write(xml.finish());
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    } else {
      output.delete();
    }
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
   * @return success of operation
   */
  public synchronized boolean create(final String username, final String password) {
    // check if user already exists
    return get(username) == null && add(new User(username, password, Perm.NONE));
  }

  /**
   * Adds the specified user.
   * @param user user to be added
   * @return success of operation
   */
  public synchronized boolean add(final User user) {
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
   * Returns information on all users.
   * @param users optional global user list (for ignoring obsolete local users)
   * @return user information
   */
  public synchronized Table info(final Users users) {
    final Table table = new Table();
    table.description = Text.USERS_X;

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
}
