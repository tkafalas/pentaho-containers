package org.pentaho.docker.support;

/**
 * Breaks down the compositeVersionNumber (ie: 9.1.0.8/364/ee) into it's component pieces.
 */
public class CliPentahoVersion {
  private static final String DEFAULT_PENTAHO_EDITION = "ee";

  public String versionNumber;
  public String distributionNumber;
  public String edition;

  CliPentahoVersion( String argumentBeingProcessed, String combinedPentahoVersion )
    throws CommandLineSyntaxException {
    String[] pieces = combinedPentahoVersion.split( "/" );
    if ( pieces.length < 2 || pieces.length > 3 ) {
      throw new CommandLineSyntaxException(
        "Too many/few slashes in " + argumentBeingProcessed + " argument value."
          + "  Value must contain between 1 and 2 slashes." );
    }
    this.versionNumber = pieces[ 0 ];
    this.distributionNumber = pieces[ 1 ];

    if ( "database".equals( argumentBeingProcessed ) ) {
      this.edition = pieces.length == 3 ? pieces[ 2 ].toLowerCase() : "";
    } else {
      this.edition =
        pieces.length == 3 ? pieces[ 2 ].toLowerCase() : DEFAULT_PENTAHO_EDITION;
      if ( !"ce".equals( edition ) && !"ee".equals( edition ) ) {
        throw new CommandLineSyntaxException( "The text after the second slash in the " + argumentBeingProcessed +
          " argument value must contain \"ce\" or \"ee\"." );
      }
    }
  }
}
