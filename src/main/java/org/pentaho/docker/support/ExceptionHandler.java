package org.pentaho.docker.support;

public class ExceptionHandler {
  boolean asService;

  public ExceptionHandler( boolean asService ) {
    this.asService = asService;
  }

  protected void errorOut( String message ) throws DockerPentahoException {
    errorMessage( message, null );
    if ( asService ) {
      throw new DockerPentahoException( message, null );
    }
    System.exit( 1 );
  }

  protected void errorOut( String message, Exception cause ) throws DockerPentahoException {
    errorMessage( "ERROR: " + message, cause );
    if ( asService ) {
      throw new DockerPentahoException( message, null );
    }
    System.exit( 1 );
  }

  protected void errorMessage( String message, Exception cause ) {
    System.err.println( message );
    if ( cause != null ) {
      System.err.print( "Caused by: " );
      cause.printStackTrace( System.err );
    }
  }
}
