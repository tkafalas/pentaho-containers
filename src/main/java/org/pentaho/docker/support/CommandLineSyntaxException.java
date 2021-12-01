package org.pentaho.docker.support;

/**
 * Exceptions that are detected to be a syntax problem with the CLI
 */
public class CommandLineSyntaxException extends Exception {
  public static final String CR = System.getProperty( "line.separator" );
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public CommandLineSyntaxException() {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public CommandLineSyntaxException( String message ) {
    super( message );
  }

  /**
   * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause
   * .toString())
   * (which typically contains the class and detail message of cause).
   *
   * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted,
   *              and indicates that the cause is nonexistent or unknown.)
   */
  public CommandLineSyntaxException( Throwable cause ) {
    super( cause );
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the getMessage() method).
   * @param cause   the cause (which is saved for later retrieval by the getCause() method). (A null value is
   *                permitted,
   *                and indicates that the cause is nonexistent or unknown.)
   */
  public CommandLineSyntaxException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * get the messages back to it's origin cause.
   */
  @Override
  public String getMessage() {
    String retval = CR;
    retval += super.getMessage() + CR;

    Throwable cause = getCause();
    if ( cause != null ) {
      String message = cause.getMessage();
      if ( message != null ) {
        retval += message + CR;
      } else {
        // Add with stack trace elements of cause...
        StackTraceElement[] ste = cause.getStackTrace();
        for ( int i = ste.length - 1; i >= 0; i-- ) {
          retval +=
            " at "
              + ste[ i ].getClassName() + "." + ste[ i ].getMethodName() + " (" + ste[ i ].getFileName() + ":"
              + ste[ i ].getLineNumber() + ")" + CR;
        }
      }
    }

    return retval;
  }

  public String getSuperMessage() {
    return super.getMessage();
  }
}
