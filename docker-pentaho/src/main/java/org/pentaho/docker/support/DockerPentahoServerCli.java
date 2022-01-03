package org.pentaho.docker.support;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pentaho.docker.support.registry.DockerPentahoServerRegistry;

/**
 * Command Line processor for DockerPentahoServer
 */
public class DockerPentahoServerCli {
  //Tho following 3 used by DockerPentahoServerService to process errors
  protected static final String PENTAHO_VERSION_ARGUMENT = "pentaho-version"; // ie: 9.1.0.0/324
  protected static final String DATABASE_ARGUMENT = "database";
  protected static final String PENTAHO_PATCH_VERSION_ARGUMENT = "patch-version"; //ie: 9.1.0.6/567

  private static final String INSTALL_PATH_ARGUMENT = "install-path";
  private static final String ADDITIONAL_PLUGINS_ARGUMENT = "additional-plugins"; // ie: std,paz,pdd,pir
  private static final String USE_EXISTING_ARGUMENT = "use-existing-downloads";
  private static final String NO_CACHE_ARGUMENT = "no-cache";
  private static final String PORT_ARGUMENT = "port";
  private static final String KAR_FILE_ARGUMENT = "kar-ids";
  private static final String PRODUCT_TYPE_ARGUMENT = "product-type";
  private static final String USER_ARGUMENT = "user";
  private static final String PASSWORD_ARGUMENT = "password";
  private static final String EXECUTE_ARGUMENT = "execute";
  private static final String EULA_ACCEPT_ARGUMENT = "EULA_ACCEPT";
  private static final String JAVA_VERSION_ARGUMENT = "java-version";
  private static final String METASTORE_ARGUMENT = "metastore";
  private static ExceptionHandler exceptionHandler;

  private static DockerPentahoServerRegistry dockerPentahoServerRegistry =
    DockerPentahoUtil.getDockerPentahoServerRegistry();//We only need one instance

  public static void main( String[] args ) throws DockerPentahoException {
    exceptionHandler = new ExceptionHandler( false );
    DockerPentahoUtil.loadDatabaseRegistry();
    Options options = setupCommandOptions();

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine = null;
    try {
      commandLine = parser.parse( options, args );
    } catch ( ParseException e ) {
      errorMessage( "\nInvalid command line.  Reason: " + e.getMessage() );
      printOptions( options );
    }
    if ( commandLine == null ) {
      errorOut( "No command line" );
    }

    assert commandLine != null;
    String combinedPentahoVersion = commandLine.getOptionValue( PENTAHO_VERSION_ARGUMENT );
    String combinedPatchVersion = commandLine.getOptionValue( PENTAHO_PATCH_VERSION_ARGUMENT );
    String combinedPlugins = commandLine.getOptionValue( ADDITIONAL_PLUGINS_ARGUMENT );
    String installPath = commandLine.getOptionValue( INSTALL_PATH_ARGUMENT );
    boolean useExistingDownloads = commandLine.hasOption( USE_EXISTING_ARGUMENT );
    boolean noCache = commandLine.hasOption( NO_CACHE_ARGUMENT );
    String port = commandLine.getOptionValue( PORT_ARGUMENT );
    String database = commandLine.getOptionValue( DATABASE_ARGUMENT );
    String karIds = commandLine.getOptionValue( KAR_FILE_ARGUMENT );
    String productType = commandLine.getOptionValue( PRODUCT_TYPE_ARGUMENT );
    String user = commandLine.getOptionValue( USER_ARGUMENT );
    String password = commandLine.getOptionValue( PASSWORD_ARGUMENT );
    boolean execute = commandLine.hasOption( EXECUTE_ARGUMENT );
    String eulaValue = commandLine.getOptionValue( EULA_ACCEPT_ARGUMENT );
    String javaVersion = commandLine.getOptionValue( JAVA_VERSION_ARGUMENT );
    String metastore = commandLine.getOptionValue( METASTORE_ARGUMENT );
    if ( eulaValue != null ) {
      eulaValue = eulaValue.trim().toLowerCase();
    }
    boolean eulaAccept = "true".equals( eulaValue ) || "yes".equals( eulaValue );

    try {
      DockerPentahoServerParams.Builder builder =
        new DockerPentahoServerParams.Builder( false, combinedPentahoVersion );
      DockerPentahoServerParams dockerPentahoServerParams = builder.combinedPatchVersion( combinedPatchVersion )
        .combinedPlugins( combinedPlugins )
        .installPath( installPath )
        .useExistingDownloads( useExistingDownloads )
        .noCache( noCache )
        .port( port )
        .database( database )
        .karIds( karIds )
        .productType( productType )
        .user( user )
        .password( password )
        .execute( execute )
        .eulaAccept( eulaAccept )
        .javaVersion( javaVersion )
        .metastore( metastore )
        .build();

      new DockerPentahoServerService( dockerPentahoServerParams ).executeService();

    } catch ( DockerPentahoException | CommandLineSyntaxException e ) {
      //Should not happen in command line mode
    }
  }

  private static void printOptions( Options options ) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "DockerPentahoServer", options );
    System.exit( 1 );
  }

  private static void errorOut( String message ) throws DockerPentahoException {
    exceptionHandler.errorOut( message );
  }

  private static void errorMessage( String message ) {
    exceptionHandler.errorMessage( message, null );
  }

  private static Options setupCommandOptions() {
    Option optPentahoVersion = Option.builder( "V" )
      .longOpt( PENTAHO_VERSION_ARGUMENT )
      .required( true )
      .hasArg( true )
      .desc(
        "The version of the pentaho server to download as a base installation followed by a \"/\", followed by the"
          + " distribution  build number, followed by another \"/\", followed by the distribution build number,"
          + " followed by \"ce\" or \"ee\".  For example \"9.1.0.0/324/ee\" installs"
          + " pentaho server-ee version 9.1.0.0 build 324." )
      .valueSeparator()
      .build();
    Option optPentahoPatchVersion = Option.builder( "P" )
      .longOpt( PENTAHO_PATCH_VERSION_ARGUMENT )
      .required( false )
      .hasArg( true )
      .desc(
        "The version of the pentaho server patch to download as a base installation followed by a \"/\" followed "
          + "by the distribution build number, followed by another \"/\", followed by \"ce\" or \"ee\".  For example"
          + "\"9.1.0.8/627/ee\" patches to pentaho server-ee version 9.1.0.8 build 627." )
      .build();
    Option optIntallPath = Option.builder( "I" )
      .longOpt( INSTALL_PATH_ARGUMENT )
      .required( false )
      .hasArg( true )
      .desc( "Optional path from root to install pentaho server in the image.  If omitted, defaults to \""
        + DockerPentahoServerService.DEFAULT_INSTALL_PATH + "\"" )
      .valueSeparator()
      .build();
    Option optAdditionalPlugins = Option.builder( "A" )
      .longOpt( ADDITIONAL_PLUGINS_ARGUMENT )
      .hasArg( true )
      .desc(
        "Contains acronyms for the plugin products to be installed with the server. Known plugin types are \"std\", "
          + DockerPentahoUtil.friendlyArrayToString( DockerPentahoServerService.KNOWN_PLUGINS )
          + ".  Enter all plugins to install separated with commas, or leave blank to install no additional plugins. "
          + " \"std\" is a special entry that will add all the plugins without the need to specify them individually." )
      .build();
    Option optUseExisting = Option.builder( "U" )
      .longOpt( USE_EXISTING_ARGUMENT )
      .desc(
        "Set this flag to re-use any existing downloaded artifacts.  If omitted, artifacts will always be "
          + "downloaded." )
      .build();
    Option optNoCache = Option.builder( "N" )
      .longOpt( NO_CACHE_ARGUMENT )
      .desc(
        "Set this flag to force docker build to use the --no-catch option." )
      .build();
    Option optPort = Option.builder( "p" )
      .longOpt( PORT_ARGUMENT )
      .hasArg( true )
      .desc(
        "Sets the tomcat port number to use for server communication.  If omitted, it uses port 8081 for the server,"
          + " and 8082 for carte." )
      .build();
    Option optDatabase = Option.builder( "D" )
      .longOpt( DATABASE_ARGUMENT )
      .hasArg( true )
      .desc(
        "Sets the underlaying database for the repository.  Supported values are: "
          + DockerPentahoUtil.friendlySupportedDatabaseTypes()
          + ".  Defaults to \"" + DockerPentahoUtil.supportedDatabaseTypes().get( 0 ) + "\" if omitted." )
      .build();
    Option optKarFiles = Option.builder( "K" )
      .longOpt( KAR_FILE_ARGUMENT )
      .hasArg( true )
      .desc(
        "Sets the kar files to include in the docker image.  Supported values are: "
          + DockerPentahoUtil.friendlyListToString( dockerPentahoServerRegistry.getKarFileRegistry().getKarFiles() )
          + ".  Multiple kar files can be loaded by providing a comma separated list.  No kar files are loaded if "
          + "omitted." )
      .build();
    Option optProductType = Option.builder( "T" )
      .longOpt( PRODUCT_TYPE_ARGUMENT )
      .hasArg( true )
      .desc(
        "Defines the product being built.  If it is omitted then \"server\" will be built.  Valid values are: "
          + DockerPentahoUtil.friendlyListToString( DockerPentahoServerService.VALID_PRODUCT_TYPES ) )
      .build();
    Option optUser = Option.builder()
      .longOpt( USER_ARGUMENT )
      .hasArg( true )
      .desc(
        "User for carte builds.  Will default to \"cluster\" if omitted when creating a carte server." )
      .build();
    Option optPassword = Option.builder()
      .longOpt( PASSWORD_ARGUMENT )
      .hasArg( true )
      .desc(
        "Sets the admin password for a server build.  Sets the carte user password for a carte server build" )
      .build();
    Option optExecute = Option.builder( "X" )
      .longOpt( EXECUTE_ARGUMENT )
      .desc( "If omitted, builds the \"" + DockerPentahoServerService.GENERATED_ROOT_FOLDER
        + "\" folder only.  The system will display the docker build and docker compose commands but not execute "
        + "them.  If present, DockerPentahoServer will build the image and run docker compose to bring "
        + "everything up and running after the " + DockerPentahoServerService.GENERATED_ROOT_FOLDER + " is built." )
      .build();
    Option optEulaAccept = Option.builder()
      .longOpt( EULA_ACCEPT_ARGUMENT )
      .hasArg( true )
      .desc(
        "By setting this option to true you accept the end user license aggreement found at [insert website here]."
          + "When set to true, the command will run unattended.  If omitted or set to false, the user will need to "
          + " accept a EULA that will be displayed." )
      .build();
    Option optJavaVersion = Option.builder( "J" )
      .longOpt( JAVA_VERSION_ARGUMENT )
      .hasArg( true )
      .desc( "The java version desired.  Care should be taken to use a java version that is compatible with"
        + " the image being built." )
      .build();
    Option optMetastore = Option.builder( "M" )
      .longOpt( METASTORE_ARGUMENT )
      .hasArg( true )
      .desc( "Contains the path to a local folder that will be mounted as the metastore.  This folder will be kept in"
        + " with the one on the container, so any metastore changes made in the container will persist in the local"
        + " file." )
      .build();

    Options options = new Options();
    options.addOption( optPentahoVersion );
    options.addOption( optPentahoPatchVersion );
    options.addOption( optAdditionalPlugins );
    options.addOption( optIntallPath );
    options.addOption( optUseExisting );
    options.addOption( optNoCache );
    options.addOption( optPort );
    options.addOption( optDatabase );
    options.addOption( optKarFiles );
    options.addOption( optProductType );
    options.addOption( optUser );
    options.addOption( optPassword );
    options.addOption( optExecute );
    options.addOption( optEulaAccept );
    options.addOption( optJavaVersion );
    options.addOption( optMetastore );
    return options;
  }

}
