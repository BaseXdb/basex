package org.basex.core;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Crypter;
import org.basex.util.StringList;
import org.basex.util.Table;
import org.basex.util.Token;

/**
 * This class organizes all users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Users {
  /** User list. */
  private final ArrayList<User> users = new ArrayList<User>();
  /** Default permissions for new users. */
  private static final int PERM = User.READ | User.WRITE;
  /** Filename; set to null for local user permissions. */
  private String file;

  /**
   * Global constructor.
   * @param global global flag
   */
  public Users(final boolean global) {
    if(global) {
      file = Prop.HOME + ".basexperm";
      try {
        if(IO.get(file).exists()) {
          final DataInput in = new DataInput(file);
          read(in);
          in.close();
        } else {
          // create initial admin user with all rights
          users.add(new User(ADMIN, Crypter.encrypt(Token.token(ADMIN)),
              User.READ | User.WRITE | User.CREATE | User.ADMIN));
          write();
        }
      } catch(final IOException ex) {
        Main.debug(ex);
      }
    }
  }

  /**
   * Local constructor.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Users(final DataInput in) throws IOException {
    read(in);
  }

  /**
   * Adds the specified user.
   * @param user user reference
   */
  public void add(final User user) {
    users.add(user);
  }

  /**
   * Stores a user and encrypted password.
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public boolean create(final String usern, final String pass) {
    // check if user exists already
    if(get(usern) != null) return false;

    final User user = new User(usern, Crypter.encrypt(Token.token(pass)), PERM);
    users.add(user);
    write();
    return true;
  }

  /**
   * Changes the password of a user.
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public boolean alter(final String usern, final String pass) {
    // check if user exists already
    final User user = get(usern);
    if(user == null || pass == null) return false;

    user.pw = Crypter.encrypt(Token.token(pass));
    write();
    return true;
  }

  /**
   * Drops a user from the list.
   * @param usern user name
   * @return success of operation
   */
  public boolean drop(final String usern) {
    final User user = get(usern);
    if(user == null) return false;
    users.remove(user);
    write();
    return true;
  }

  /**
   * Returns a user reference if the name/password combination is correct.
   * @param usern user name
   * @param pw password
   * @return success of operation
   */
  public User get(final String usern, final String pw) {
    final User user = get(usern);
    return user != null && Token.eq(Crypter.encrypt(Token.token(pw)), user.pw) ?
      user : null;
  }

  /**
   * Returns a user reference with the specified name.
   * @param usern user name
   * @return success of operation
   */
  public User get(final String usern) {
    for(final User user : users) if(user.name.equals(usern)) return user;
    return null;
  }

  /**
   * Reads users from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  private void read(final DataInput in) throws IOException {
    final int s = in.readNum();
    for(int u = 0; u < s; u++) {
      final User user = new User(in.readString(), in.readBytes(),
          in.readNum());
      users.add(user);
    }
  }

  /**
   * Writes global permissions to disk.
   */
  public void write() {
    try {
      final DataOutput out = new DataOutput(file);
      write(out);
      out.close();
    } catch(final IOException ex) {
      Main.debug(ex);
    }
  }

  /**
   * Writes permissions to disk.
   * @param out output stream; if set to null, the global rights are written
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    // skip writing of local rights
    final int s = users.size();
    out.writeNum(s);
    for(int u = 0; u < s; u++) {
      final User user = users.get(u);
      out.writeString(user.name);
      out.writeBytes(user.pw);
      out.writeNum(user.perm);
    }
  }

  /**
   * Returns information on all users.
   * @return user information
   */
  public byte[] info() {
    final Table table = new Table();
    table.desc = "Users";

    final int sz = file == null ? 3 : 5;
    for(int u = 0; u < sz; u++) table.header.add(USERHEAD[u]);

    final int size = users.size();
    for(int i = 0; i < size; i++) {
      final User user = users.get(i);
      final StringList entry = new StringList();
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
    return table.finish();
  }
}
