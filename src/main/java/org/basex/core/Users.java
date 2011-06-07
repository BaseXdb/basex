package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Table;
import org.basex.util.TokenList;
import org.basex.util.Util;

/**
 * This class organizes all users.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class Users extends CopyOnWriteArrayList<User> {
  /** Filename; set to {@code null} if the instance handles local users. */
  private File file;

  /**
   * Constructor for global users.
   * @param global global flag
   */
  public Users(final boolean global) {
    if(!global) return;

    file = new File(Prop.HOME + IO.BASEXSUFFIX + "perm");
    if(!file.exists()) {
      // create default admin user with all rights
      add(new User(ADMIN, token(md5(ADMIN)), User.ADMIN));
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
      final User user = new User(string(in.readBytes()),
        in.readBytes(), in.readNum());
      add(user);
    }
  }

  /**
   * Writes global permissions to disk.
   */
  public synchronized void write() {
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
    if(get(usern) != null) return false;

    final User user = new User(usern, token(md5(pass)), User.WRITE);
    add(user);
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

    user.password = token(md5(pass));
    write();
    return true;
  }

  /**
   * Drops a user from the list.
   * @param user user reference
   */
  public synchronized void drop(final User user) {
    remove(user);
    write();
  }

  /**
   * Returns a user reference with the specified name.
   * @param usern user name
   * @return success of operation
   */
  public synchronized User get(final String usern) {
    for(final User user : this) if(user.name.equals(usern)) return user;
    return null;
  }

  /**
   * Writes permissions to disk.
   * @param out output stream; if set to null, the global rights are written
   * @throws IOException I/O exception
   */
  public synchronized void write(final DataOutput out) throws IOException {
    // skip writing of local rights
    out.writeNum(size());
    for(final User user : this) {
      out.writeString(user.name);
      out.writeToken(user.password);
      out.writeNum(user.perm);
    }
  }

  /**
   * Returns information on all users.
   * @return user information
   */
  public synchronized byte[] info() {
    final Table table = new Table();
    table.desc = USERS;

    final int sz = file == null ? 3 : 5;
    for(int u = 0; u < sz; ++u) table.header.add(USERHEAD[u]);

    for(final User user : this) {
      final TokenList entry = new TokenList();
      entry.add(user.name);
      entry.add(user.perm(User.READ) ? "X" : "");
      entry.add(user.perm(User.WRITE) ? "X" : "");
      if(sz == 5) {
        entry.add(user.perm(User.CREATE) ? "X" : "");
        entry.add(user.perm(User.ADMIN) ? "X" : "");
      }
      table.contents.add(entry);
    }
    table.sort();
    table.toTop(token(ADMIN));
    return table.finish();
  }
}
