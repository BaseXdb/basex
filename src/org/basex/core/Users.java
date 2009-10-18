package org.basex.core;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Crypter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class organizes all users.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Users {
  /** User list. */
  private final ArrayList<User> users = new ArrayList<User>();
  /** Default permissions. */
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
  public String info() {
    final int size = users.size();
    final TokenBuilder tb = new TokenBuilder();
    if(size != 0) {
      // get maximum column widths
      final int sz = file == null ? 3 : 5;
      final int[] ind = new int[sz];
      for(final User u : users) ind[0] = Math.max(ind[0], u.name.length());
      for(int u = 0; u < sz; u++) {
        ind[u] = Math.max(ind[u], USERHEAD[u].length()) + 2;
        tb.add(ind[u], USERHEAD[u]);
      }
      tb.add(NL);
      for(int u = 0; u < sz; u++) {
        for(int i = 0; i < ind[u]; i++) tb.add('-');
      }
      tb.add(NL);
      for(int i = 0; i < size; i++) {
        final User user = users.get(i);
        tb.add(ind[0], user.name);
        tb.add(ind[1], user.perm(User.READ) ? "X" : "");
        tb.add(ind[2], user.perm(User.WRITE) ? "X" : "");
        if(file != null) {
          tb.add(ind[3], user.perm(User.CREATE) ? "X" : "");
          tb.add(ind[4], user.perm(User.ADMIN) ? "X" : "");
        }
        tb.add(NL);
      }
      tb.add(NL);
    }
    tb.add(size + " Users registered.");
    return tb.toString();
  }
}
