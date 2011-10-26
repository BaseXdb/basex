package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.basex.io.IO;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.Table;
import org.basex.util.Util;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * This class organizes all users.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class Users {
  /** User array. */
  private final List<User> list =
    Collections.synchronizedList(new ArrayList<User>(0));
  /** Filename; set to {@code null} if the instance handles local users. */
  private File file;

  /**
   * Constructor for global users.
   * @param global global flag
   */
  public Users(final boolean global) {
    if(!global) return;

    file = new File(Prop.HOME, IO.BASEXSUFFIX + "perm");
    if(!file.exists()) {
      // define default admin user with all rights
      list.add(new User(ADMIN, token(md5(ADMIN)), User.ADMIN));
    } else {
      DataInput in = null;
      try {
        in = new DataInput(file);
        read(in);
      } catch(final IOException ex) {
        Util.errln(ex);
      } finally {
        if(in != null) try { in.close(); } catch(final IOException ex) { }
      }
    }
  }

  /**
   * Reads users from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public synchronized void read(final DataInput in) throws IOException {
    final int s = in.readNum();
    for(int u = 0; u < s; ++u) {
      final User user = new User(string(in.readToken()),
        in.readToken(), in.readNum());
      list.add(user);
    }
  }

  /**
   * Writes global permissions to disk.
   */
  public synchronized void write() {
    if(file == null) return;
    try {
      final DataOutput out = new DataOutput(file);
      write(out);
      out.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  /**
   * Stores a user and encrypted password.
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public synchronized boolean create(final String usern, final String pass) {
    // check if user exists already
    return get(usern) == null &&
      create(new User(usern, token(pass), User.WRITE));
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
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public synchronized boolean alter(final String usern, final String pass) {
    // check if user exists already
    final User user = get(usern);
    if(user == null) return false;

    user.password = token(pass);
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
   * @param usern user name
   * @return success of operation
   */
  public synchronized User get(final String usern) {
    for(final User user : list) if(user.name.equals(usern)) return user;
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
      if(pattern.matcher(u.name).matches()) sl.add(u.name);
    }
    return sl.toArray();
  }

  /**
   * Writes permissions to disk.
   * @param out output stream; if set to null, the global rights are written
   * @throws IOException I/O exception
   */
  public synchronized void write(final DataOutput out) throws IOException {
    // skip writing of local rights
    out.writeNum(list.size());
    for(final User user : list) {
      out.writeToken(token(user.name));
      out.writeToken(user.password);
      out.writeNum(user.perm);
    }
  }

  /**
   * Returns information on all users.
   * @param users optional second list
   * @return user information
   */
  public synchronized byte[] info(final Users users) {
    final Table table = new Table();
    table.description = USERS;

    final int sz = file == null ? 3 : 5;
    for(int u = 0; u < sz; ++u) table.header.add(USERHEAD[u]);

    for(final User user : list) {
      if(users != null) if(users.get(user.name) == null) continue;

      final TokenList tl = new TokenList();
      tl.add(user.name);
      tl.add(user.perm(User.READ) ? "X" : "");
      tl.add(user.perm(User.WRITE) ? "X" : "");
      if(sz == 5) {
        tl.add(user.perm(User.CREATE) ? "X" : "");
        tl.add(user.perm(User.ADMIN) ? "X" : "");
      }
      table.contents.add(tl);
    }
    table.sort();
    table.toTop(token(ADMIN));
    return table.finish();
  }
}
