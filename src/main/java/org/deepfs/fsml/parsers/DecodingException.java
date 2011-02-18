package org.deepfs.fsml.parsers;

/**
 * An exception class used for signaling failure of a decode operation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Bastian Lemke
 */
final class DecodingException extends Exception {
  /**
   * Constructs a new exception with {@code null} as its detail message.
   * The cause is not initialized, and may subsequently be initialized by a
   * call to {@link #initCause}.
   */
  DecodingException() {
    super();
  }

  /**
   * Constructs a new exception with the specified detail message. The cause
   * is not initialized, and may subsequently be initialized by a call to
   * {@link #initCause}.
   * @param message the detail message. The detail message is saved for later
   *          retrieval by the {@link #getMessage()} method
   */
  DecodingException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified cause and a detail message
   * of {@code (cause==null ? null : cause.toString())} (which typically
   * contains the class and detail message of {@code cause}). This
   * constructor is useful for exceptions that are little more than wrappers
   * for other throwables
   * (for example, {@link java.security.PrivilegedActionException}).
   * @param cause the cause (which is saved for later retrieval by the
   *          {@link #getCause()} method). (A {@code null} value is
   *          permitted,
   *          and indicates that the cause is nonexistent or unknown.)
   */
  DecodingException(final Throwable cause) {
    super(cause);
  }
}