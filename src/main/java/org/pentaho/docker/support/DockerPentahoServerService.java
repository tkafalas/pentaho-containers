package org.pentaho.docker.support;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.docker.support.registry.VersionInstance;
import org.pentaho.support.encryption.Encr;
import org.pentaho.docker.support.registry.DatabaseInstance;
import org.pentaho.docker.support.registry.DockerPentahoServerRegistry;
import org.pentaho.support.encryption.PasswordEncoderException;
import org.pentaho.support.utils.XmlParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.pentaho.di.core.util.StringUtil.isEmpty;

public class DockerPentahoServerService {
  protected static final String[] KNOWN_PLUGINS = { "paz", "pdd", "pir" };
  protected static final List<String> VALID_PRODUCT_TYPES = Arrays.asList( "server", "pdi", "carte" );
  protected static final String DEFAULT_INSTALL_PATH = "/opt/pentaho";
  protected static final String GENERATED_ROOT_FOLDER = "generatedFiles";

  private static final String SERVER_CONFIG_FILE_PREFIX = "docker.server.";
  private static final String PDI_CONFIG_FILE_PREFIX = "docker.pdi.";
  private static final String CONFIG_SERVER_ARTIFACT_PROP = SERVER_CONFIG_FILE_PREFIX + "serverArtifactFileName";
  private static final String CONFIG_SERVER_ARTIFACT_URL_PROP = SERVER_CONFIG_FILE_PREFIX + "serverArtifactUrl";
  private static final String CONFIG_PDI_ARTIFACT_PROP = PDI_CONFIG_FILE_PREFIX + "pdiArtifactFileName";
  private static final String CONFIG_PDI_ARTIFACT_URL_PROP = PDI_CONFIG_FILE_PREFIX + "pdiArtifactUrl";
  private static final String CONFIG_PATCH_ARTIFACT_PROP = SERVER_CONFIG_FILE_PREFIX + "serverPatchFileName";
  private static final String CONFIG_PATCH_ARTIFACT_URL_PROP = SERVER_CONFIG_FILE_PREFIX + "serverPatchUrl";
  private static final String CONFIG_PlUGIN_ARTIFACT_PROP = SERVER_CONFIG_FILE_PREFIX + "serverPluginFileName";
  private static final String CONFIG_PLUGIN_ARTIFACT_URL_PROP = SERVER_CONFIG_FILE_PREFIX + "serverPluginUrl";
  private static final String CONFIG_KAR_ARTIFACT_PROP = SERVER_CONFIG_FILE_PREFIX + "karFileName";
  private static final String CONFIG_KAR_ARTIFACT_URL_PROP = SERVER_CONFIG_FILE_PREFIX + "karUrl";
  private static final String CONFIG_CURL_COMMAND_PROP = SERVER_CONFIG_FILE_PREFIX + "curlCommand";
  private static final String CONFIG_ARTIFACT_CACHE = SERVER_CONFIG_FILE_PREFIX + "artifactCache";
  private static final String CONFIG_USER_VARIABLE_PREFIX = SERVER_CONFIG_FILE_PREFIX + "var.";
  private static final String SLASH = File.separator;

  private static final String TEMP_ROOT_FOLDER = "temp";
  private static final String SERVER_IMAGE_ROOT_FOLDER = "containers/pentaho-server/pentaho-server-auto/";
  private static final String PDI_IMAGE_ROOT_FOLDER = "containers/pentaho-data-integration/pdi-client-auto/";
  private static final String SERVER_COMPOSE_ROOT_FOLDER = "containers/templates/single-server-auto/";
  private static final String PDI_COMPOSE_ROOT_FOLDER = PDI_IMAGE_ROOT_FOLDER;
  private static final String CONFIG_FILE_NAME = "DockerPentahoServer.properties";
  private static final String ENV_FILE_LOCATION = ".env";
  private static final String DATABASE_OVERRIDE_FILE_ROOT_PREFIX =
    SERVER_COMPOSE_ROOT_FOLDER + "configuration_override/";
  private static final String WEB_XML_FILE_LOCATION = "tomcat/webapps/pentaho/WEB-INF/web.xml";
  private static final String ENTRY_POINT_FILE_LOCATION = "entrypoint/docker-entrypoint.sh";

  private static final String DEFAULT_PENTAHO_EDITION = "ee";
  private static final boolean IS_WINDOWS = System.getProperty( "os.name" ).toLowerCase().contains( "win" );
  private static final String COULD_NOT_COPY = "Could not copy ";

  //The parameter values for the command line
  private String installPath = DEFAULT_INSTALL_PATH;
  private final boolean useExistingDownloads;
  private final boolean noCache;
  private final String port;
  private final String database;
  private final String karIds;
  private final DatabaseInstance activeDatabaseInstance;
  private final String productType;
  private final String user;
  private final String password;
  private final boolean execute;
  private final boolean eulaAccept;
  private String javaVersion;
  private CliPentahoVersion cliPentahoVersion;
  private CliPentahoVersion cliPatchVersion;

  //processing
  private ExceptionHandler exceptionHandler;
  private DockerPentahoServerRegistry dockerPentahoServerRegistry = DockerPentahoUtil.getDockerPentahoServerRegistry();
  private Path jarFolder;
  private Set<String> plugins;
  private OrderedProperties configMap;
  private OrderedProperties parsedConfigMap;
  private String artifactStagingFolder;
  private String downloadCacheFolder;
  private Map<String, Artifact> artifacts;
  private String dockerFile; //contents of file
  private String envFile; //contents of file
  private String entryPointFile; //contents of file
  private String tempFolder;
  private String databaseOverrideFolderPath; //path to override file root
  private String finalOverrideFolder; // Where all the overrides get merged
  private String generatedFolder; //All generated content goes here
  private String activeComposeRootFolder; //The folder with the templates for docker-compose
  private String activeImageRootFolder; //The folder with the templates for DockerFile
  private VersionInstance versionInstance; //The Version instance corresponding to the version required.

  public DockerPentahoServerService( boolean asService, String combinedPentahoVersion, String combinedPatchVersion,
                                     String combinedPlugins, String installPath, boolean useExistingDownloads,
                                     boolean noCache, String port, String database, String karIds, String productType,
                                     String user, String password, boolean execute, boolean eulaAccept, String javaVersion )
    throws DockerPentahoException {
    //asService means that you are being called from some class as opposed to the CLI.  Errors in CLI cause an immediate
    //shutdown of the VM where running asService will bubble up an exception to be handled by the caller.
    exceptionHandler = new ExceptionHandler( asService );
    this.productType = productType == null ? VALID_PRODUCT_TYPES.get( 0 ) : productType;
    this.installPath = installPath == null ? DEFAULT_INSTALL_PATH : installPath;
    this.useExistingDownloads = useExistingDownloads;
    this.noCache = noCache;

    if ( port == null ) {
      port = isServer() ? "8081" : "8082";
    }
    this.port = port;

    this.karIds = karIds;
    this.user = user == null ? "cluster" : user;
    if ( password == null ) {
      password = isServer() ? "password" : "cluster";
    }
    this.password = password;
    this.execute = execute;
    this.eulaAccept = eulaAccept;
    this.javaVersion = javaVersion;

    //make sure the port is number
    try {
      int p = Integer.parseInt( this.port );
      if ( p < 0 || p > 65535 ) {
        errorOut( "port number must greater than 0 and less than or equal to 65535" );
      }
    } catch ( Exception e ) {
      errorOut( "Port number is not numeric", e );
    }

    if ( database == null || database.isEmpty() ) {
      database = DockerPentahoUtil.supportedDatabaseTypes().get( 0 );
    }
    if ( !DockerPentahoUtil.supportedDatabaseTypes().contains( database ) ) {
      errorOut( "Database " + database + " is not supported.  Please correct and try again." );
    }

    if ( !VALID_PRODUCT_TYPES.contains( this.productType ) ) {
      errorOut( "Product type of \"" + this.productType + " is invalid." );
    }

    activeDatabaseInstance = dockerPentahoServerRegistry.getDatabaseRegistry().getDatabaseInstance( database );
    this.database = database;

    try {
      cliPentahoVersion =
        new CliPentahoVersion( DockerPentahoServerCli.PENTAHO_VERSION_ARGUMENT, combinedPentahoVersion );
      if ( combinedPatchVersion != null && !combinedPatchVersion.equals( "" ) ) {
        cliPatchVersion =
          new CliPentahoVersion( DockerPentahoServerCli.PENTAHO_PATCH_VERSION_ARGUMENT, combinedPatchVersion );
      } else {
        cliPatchVersion = null; //No patch is being applied
      }
    } catch ( CommandLineSyntaxException e ) {
      errorOut( e.getMessage() );
    }

    preProcessAdditionalPlugins( combinedPlugins );
    try {
      process();
    } catch ( Exception e ) {
      errorOut( "Unanticipated exception", e );
    }
  }

  /*
  Orchestrates the main logic
   */
  public void process() throws DockerPentahoException, IOException {
    if ( !EulaExecutor.executeEulaAgreement( eulaAccept ) ) {
      errorOut( "EULA not accepted." );
    }

    try {
      jarFolder =
        Paths.get( this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getParent();
    } catch ( URISyntaxException e ) {
      errorOut( "Could not determine jar's folder", e );
    }

    determineVersionInstance();
    initializeWorkspace(); // Clear and initialize "generatedFiles", "temp" folder, etc.

    if ( !isValidPath( installPath ) ) {
      errorOut( "Invalid installPath of " + installPath );
    }

    //Get the config file
    readConfigFile();
    //Make a copy because we will be modifying it
    parsedConfigMap = new OrderedProperties( configMap );

    setVariablesForSubstitution(); //Also determines the databaseOverrideFolderPath

    generatedFolder = jarFolder + "/" + GENERATED_ROOT_FOLDER;
    artifactStagingFolder = generatedFolder + "/stagedArtifacts";
    finalOverrideFolder = generatedFolder + "/fileOverride";
    tempFolder = jarFolder + "/" + TEMP_ROOT_FOLDER;

    artifacts = new HashMap<>();
    computeDerivedVariables();
    determineArtifactCacheFolder();

    //Get the artifacts
    if ( isServer() ) {
      addServerArtifact();
      addPatchArtifact();
      addPluginArtifacts();
    } else {
      addPdiArtifact();
    }

    addKarArtifacts();

    //Now download them all
    downloadArtifacts();
    //Copy the artifacts required to staging folder
    stageArtifacts();

    setupFinalOverrideFolder();

    //Modify and stage the docker file for the server
    dockerFile = readAnyFile( activeImageRootFolder + "Dockerfile" );
    modifyDockerFile();
    writeAnyFile( generatedFolder + "/Dockerfile", dockerFile );

    //Modify install Path on web.xml
    if ( isServer() ) {
      modifyWebXmlFile( finalOverrideFolder + SLASH + WEB_XML_FILE_LOCATION );
    }

    //Modify the .env file
    String envFilePath = generatedFolder + "/" + ENV_FILE_LOCATION;
    envFile = readAnyFile( envFilePath );
    modifyEnvFile();
    writeAnyFile( envFilePath, envFile );


    //Modify the docker-entrypoint.sh file
    String entryPointPath = generatedFolder + "/" + ENTRY_POINT_FILE_LOCATION;
    entryPointFile = readAnyFile( entryPointPath );
    modifyEntryPointFile();
    writeAnyFile( entryPointPath, entryPointFile );
    boolean succeeded = new File( entryPointPath ).setExecutable( true, false );
    if ( !succeeded ) {
      outputLine( "WARNING: Could not set file \"" + entryPointPath + "\" to executable" );
    }

    //Modify the carte-config.xml file for carte/pdi, or login password for server
    modifyCredentials();

    //Install pentaho License Information
    installLicenseFile();

    createDockerComposeYml();

    //Create the docker image
    createDockerImage();

    //do a docker compose
    createDockerCompose();

  }

  private void initializeWorkspace() throws DockerPentahoException {
    File generatedFolderFile = new File( jarFolder + "/" + GENERATED_ROOT_FOLDER );
    deleteFolder( generatedFolderFile );
    createSubFolder( generatedFolderFile, "local" );
    deleteFolder( new File( jarFolder + TEMP_ROOT_FOLDER ) );
    activeImageRootFolder = isServer() ? SERVER_IMAGE_ROOT_FOLDER : PDI_IMAGE_ROOT_FOLDER;
    copyFolder( activeImageRootFolder + "entrypoint", generatedFolderFile.getAbsolutePath() + "/entrypoint" );
    activeComposeRootFolder = isServer() ? SERVER_COMPOSE_ROOT_FOLDER : PDI_COMPOSE_ROOT_FOLDER;
    copyFile( activeComposeRootFolder + ENV_FILE_LOCATION, generatedFolderFile + "/" + ENV_FILE_LOCATION );
    if ( isServer() ) {
      copyFolder( activeComposeRootFolder + activeDatabaseInstance.getDbInitFolder(),
        generatedFolderFile + "/" + activeDatabaseInstance.getDbInitFolder() );
    }
  }


  private boolean isServer() {
    return productType.equals( "server" );
  }

  private void deleteFolder( File folder ) throws DockerPentahoException {
    if ( folder.exists() ) {
      try {
        FileUtils.deleteDirectory( folder );
      } catch ( IOException e ) {
        errorOut( "Could not delete " + folder.getPath(), e );
      }
    }
  }

  private void createFolder( File folder ) throws DockerPentahoException {
    if ( !folder.mkdirs() ) {
      errorOut( "Could not create folder " + folder.getPath() );
    }
  }

  private void createSubFolder( File parentFolder, String subFolder ) throws DockerPentahoException {
    createFolder( new File( parentFolder.getAbsolutePath() + "/" + subFolder ) );
  }

  private void copyFolder( String srcDir, String destDir ) throws DockerPentahoException {
    copyFolder( new File( srcDir ), new File( destDir ) );
  }

  private void copyFolder( File srcDir, File destDir ) throws DockerPentahoException {
    try {
      FileUtils.copyDirectory( srcDir, destDir );
    } catch ( IOException e ) {
      errorOut( COULD_NOT_COPY + srcDir.getPath() + " to " + destDir.getPath() );
    }
  }

  private void copyFile( String sourceFile, String destFile ) throws DockerPentahoException {
    try {
      FileUtils.copyFile( new File( sourceFile ), new File( destFile ) );
    } catch ( IOException e ) {
      errorOut( COULD_NOT_COPY + sourceFile + " to " + destFile );
    }
  }

  private void determineArtifactCacheFolder() throws DockerPentahoException {
    downloadCacheFolder = parsedConfigMap.get( CONFIG_ARTIFACT_CACHE );
    if ( downloadCacheFolder.startsWith( "./" ) ) {
      downloadCacheFolder = jarFolder + "/" + downloadCacheFolder.substring( 2 );
    }
    File cacheFolder = new File( downloadCacheFolder );
    if ( !cacheFolder.isDirectory() && !cacheFolder.exists() ) {
      if ( !cacheFolder.mkdirs() ) {
        errorOut( "Could not create cache directory " + downloadCacheFolder );
      } else {
        errorOut( downloadCacheFolder + " is not a directory" );
      }
    }
  }

  private void setVariablesForSubstitution() throws DockerPentahoException {
    //Set the fixed values that will be available for substitution.  Put them at the top of list
    parsedConfigMap.put( "version", cliPentahoVersion.versionNumber, 0 );
    parsedConfigMap.put( "distNumber", cliPentahoVersion.distributionNumber, 1 );
    parsedConfigMap.put( "edition", cliPentahoVersion.edition, 2 );

    if ( cliPatchVersion != null ) {
      parsedConfigMap.put( "patchVersion", cliPatchVersion.versionNumber, 3 );
      parsedConfigMap.put( "patchDistNumber", cliPatchVersion.distributionNumber, 4 );
    }

    parsedConfigMap.put( "PORT", port );
    parsedConfigMap.put( "USER", user );
    parsedConfigMap.put( "PASSWORD", isServer() ? encryptPassword( password ) : password );

    if ( isServer() ) {
      //Find the database override directory for this version by checking the version number.  If not found drop the
      // last character and try again, till a folder is found.
      VersionIterator vi = new VersionIterator( cliPentahoVersion.versionNumber );
      boolean found = false;
      while ( vi.hasNext() ) {
        String candidateOverrideFolder = "version" + vi.next();
        if ( testOverrideFolder( candidateOverrideFolder + "-" + database.replaceFirst( "/", "-" ) )
          || testOverrideFolder( candidateOverrideFolder + "-" + getDatabaseType() ) ) {
          found = true;
          break;
        }
      }

      if ( !found ) {
        errorOut( "Could not find database override folder to use." );
      }
    }
  }

  //Return true if testFolder exists.  Set the substitution variable and instance variable if found
  private boolean testOverrideFolder( String testFolder ) {
    File file =
      new File( DATABASE_OVERRIDE_FILE_ROOT_PREFIX + testFolder );
    if ( file.exists() ) {
      //The name of the database override folder used
      databaseOverrideFolderPath = file.getAbsolutePath();
      parsedConfigMap.put( "databaseOverrideFolderPath", testFolder,
        5 ); //We don't need this for substitution but somebody else might
      return true;
    }
    return false;
  }

  private void setupFinalOverrideFolder() throws DockerPentahoException {
    if ( !isServer() ) {
      copyFolder( activeComposeRootFolder + "overrideFiles", finalOverrideFolder );
    } else {
      //Copy in the database override
      copyFolder( databaseOverrideFolderPath, finalOverrideFolder );
    }
    // unzip the kar artifacts to the override drivers folder
    for ( Artifact artifact : artifacts.values() ) {
      if ( artifact.artifactType == ArtifactType.KAR ) {
        String installationPath =
          isServer() ? finalOverrideFolder + "/pentaho-solutions/drivers" : finalOverrideFolder + "/driver";
        try {
          if ( artifact.destinationFileName.endsWith( "-dist.zip" ) ) {
            String filename = artifact.destinationFileName.substring( 0, artifact.destinationFileName.length() - 9 );
            unzip( artifactStagingFolder + "/" + artifact.destinationFileName, tempFolder );
            String command = "java -DINSTALL_PATH=" + installationPath + " -jar "
              + tempFolder + "/" + filename + "/installer.jar -options-system";
            runCommand( command, false, true );
          } else {
            unzip( artifactStagingFolder + "/" + artifact.destinationFileName,
              finalOverrideFolder + "pentaho-solutions/drivers" );
          }

        } catch ( IOException e ) {
          errorOut( "Could not unzip " + artifact.destinationFileName, e );
        }
      }
    }
  }

  public void unzip( String file, String outputFolder ) throws IOException {
    try ( ZipFile zipFile = new ZipFile( file ) ) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while ( entries.hasMoreElements() ) {
        ZipEntry entry = entries.nextElement();
        File entryDestination = new File( outputFolder, entry.getName() );
        if ( entry.isDirectory() ) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          try ( InputStream in = zipFile.getInputStream( entry );
                OutputStream out = new FileOutputStream( entryDestination ) ) {
            IOUtils.copy( in, out );
          }
        }
      }
    }
  }

  private void createDockerComposeYml() throws DockerPentahoException {
    String sourceFilename;
    if ( isServer() ) {
      sourceFilename = "docker-compose-" + getDatabaseType() + ".yml";
    } else {
      sourceFilename = productType.equals( "carte" ) ? "docker-compose-carte.yml" : "docker-compose.yml";
    }

    Path source = Paths.get( activeComposeRootFolder + sourceFilename );
    Path destination = Paths.get( generatedFolder + "/docker-compose.yml" );
    try {
      Files.copy( source, destination, StandardCopyOption.REPLACE_EXISTING );
    } catch ( IOException e ) {
      errorOut( COULD_NOT_COPY + "file \"" + source.toAbsolutePath() + "\" to \"" + destination.toAbsolutePath() + "\"",
        e );
    }
  }

  private String getDatabaseType() {
    return database.substring( 0, database.indexOf( "/" ) );
  }

  private void createDockerCompose() throws DockerPentahoException {
    String command = IS_WINDOWS ? "docker compose up" : "sudo docker-compose up";
    if ( execute ) {
      runCommand( command, false, true, generatedFolder );
    } else {
      outputLine( "NOT EXECUTED: " + command );
    }
  }

  private void createDockerImage() throws DockerPentahoException {
    StringBuilder command = new StringBuilder( IS_WINDOWS ? "" : "sudo " );
    command.append( "docker build -f " ).append( generatedFolder ).append( "/Dockerfile" );
    if ( noCache ) {
      command.append( " --no-cache" );
    }
    if ( isServer() ) {
      command.append( " -t pentaho/pentaho-server:" );
    } else {
      command.append( " -t pentaho/pdi:" );
    }
    command.append( computeFinalVersion() );
    command.append( " " ).append( generatedFolder );
    if ( execute ) {
      runCommand( command.toString(), false, true );
    } else {
      outputLine( "NOT EXECUTED: " + command.toString() );
    }
  }

  private String computeFinalVersion() {
    if ( cliPatchVersion != null ) {
      return cliPatchVersion.versionNumber + "p";
    } else {
      return cliPentahoVersion.versionNumber;
    }
  }

  private int runCommand( String command, boolean quiet, boolean abortOnError ) throws DockerPentahoException {
    return runCommand( command, quiet, abortOnError, System.getProperty( "user.dir" ) );
  }

  private int runCommand( String command, boolean quiet, boolean abortOnError, String workingDir )
    throws DockerPentahoException {
    int exitCode = -1;
    if ( !quiet ) {
      outputLine( "\n" + command );
    }

    ProcessBuilder processBuilder = new ProcessBuilder( command.split( " " ) );
    Process process = null;
    try {
      processBuilder.directory( new File( workingDir ) );
      processBuilder.inheritIO();  //Send all IO out to the console
      process = processBuilder.start();
      exitCode = process.waitFor();
      if ( exitCode != 0 ) {
        if ( abortOnError ) {
          errorOut( "Command \"" + command + "\" has failed with exit code of " + exitCode );
        } else {
          if ( !quiet ) {
            outputLine( "Command failed with exit code " + exitCode );
          }
        }
      }
    } catch ( IOException | InterruptedException e ) {
      errorOut( "Error running curl command ", e );
    }
    return exitCode;
  }

  private void modifyCredentials() throws DockerPentahoException {
    if ( !isServer() ) {
      anyFileSubstitute( finalOverrideFolder + "/carte-config.xml" );
    } else {
      anyFileSubstitute( finalOverrideFolder + "/pentaho-solutions/system/defaultUser.spring.properties" );
    }
  }

  private void anyFileSubstitute( String pathToFile ) throws DockerPentahoException {
    String fileContents = readAnyFile( pathToFile );
    fileContents = StringUtil.environmentSubstitute( fileContents, parsedConfigMap );
    try {
      writeAnyFile( pathToFile, fileContents );
    } catch ( IOException e ) {
      errorOut( "Could not perform substitutions on file \"" + pathToFile + "\"" );
    }
  }

  private void installLicenseFile() throws DockerPentahoException {
    String licenseFileProperty = "PENTAHO_INSTALLED_LICENSE_PATH";
    String sourceFilePath = System.getenv( licenseFileProperty );
    File sourceFile;
    if ( sourceFilePath == null ) {
      sourceFile = new File( "/pentaho/.installedLicenses.xml" );
      if ( !sourceFile.exists() ) {
        errorOut( "No environment variable \"" + licenseFileProperty + "\"" );
      }
    } else {
      sourceFile = new File( sourceFilePath );
    }
    if ( !sourceFile.exists() ) {
      errorOut( "The license file " + sourceFilePath + " is not present.  Aborting." );
    }
    String destinationFilePath = generatedFolder + "/local/installedLicenses.xml";
    File destinationFile = new File( destinationFilePath );
    try {
      FileUtils.copyFile( sourceFile, destinationFile );
    } catch ( IOException e ) {
      errorOut( "Could not copy file " + sourceFilePath + " to " + destinationFilePath, e );
    }

  }

  private void modifyEnvFile() {
    replaceEnvFileArg( "PENTAHO_VERSION", computeFinalVersion() );
    replaceEnvFileArg( "OVERRIDE_FOLDER", finalOverrideFolder );
    replaceEnvFileArg( "PORT", port );

    try {
      //We use the pentaho version logic to get the version from the database argument.  But be aware the 3 values
      //represent different things for a database ( ie: oracle/latest/ex, or mysql/8.0)
      replaceEnvFileArg( "DATABASE_VERSION",
        new CliPentahoVersion( DockerPentahoServerCli.DATABASE_ARGUMENT, database ).distributionNumber );
      replaceEnvFileArg( "DATABASE_EDITION",
        new CliPentahoVersion( DockerPentahoServerCli.DATABASE_ARGUMENT, database ).edition );
    } catch ( CommandLineSyntaxException e ) {
      e.printStackTrace();
    }
    replaceEnvFileArg( "DOCKER_DATABASE_IMAGE", activeDatabaseInstance.image );
  }

  private void modifyWebXmlFile( String pathToFile ) throws DockerPentahoException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      File xmlFile = new File( pathToFile );
      Document doc = db.parse( xmlFile );
      XPath xpath = XPathFactory.newInstance().newXPath();
      Element element = (Element) xpath.evaluate(
        "/web-app/context-param/param-name[text()='hsqldb-databases']/following-sibling::param-value",
        doc, XPathConstants.NODE );
      element.setTextContent( "sampleData@" + installPath + "/pentaho-server/data/hsqldb/sampledata" );

      //Now Write back the file
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      DOMSource source = new DOMSource( doc );
      FileWriter writer = new FileWriter( xmlFile );
      StreamResult result = new StreamResult( writer );
      transformer.transform( source, result );

    } catch ( Exception e ) {
      errorOut( "Could not parse XML file " + pathToFile, e );
    }
  }

  private void modifyEntryPointFile() {
    replaceEntryPointSetting( "INSTALLATION_PATH", installPath );
  }

  private void determineVersionInstance() throws DockerPentahoException {
    versionInstance = getVersionInstance( cliPentahoVersion.versionNumber );
    if ( versionInstance == null ) {
      errorOut( "The version desired of \"" + cliPentahoVersion.versionNumber
        + "\" is not supported by DockerPentahoServer.  To add it, modify the registry.yml file to support"
        + " it." );
    }
  }

  private void modifyDockerFile() throws DockerPentahoException {
    boolean found = false;
    String[] allowedVersions = versionInstance.javaVersions.split( "," );
    if ( javaVersion == null ) {
      javaVersion = allowedVersions[ 0 ];
    }
    for ( String allowedVersion : allowedVersions ) {
      if ( allowedVersion.trim().equals( javaVersion ) ) {
        found = true;
        break;
      }
    }
    if ( !found ) {
      errorOut( "The version desired of \"" + cliPentahoVersion.versionNumber
        + "\" only supports the following java versions: " + DockerPentahoUtil.friendlyListToString(
        Arrays.asList( versionInstance.javaVersions.split( "," ) ) ) );
    }

    outputLine( "Modifying Docker File" );
    replaceDockerFileArg( "INSTALLATION_PATH", installPath );
    replaceDockerFileArg( "JAVA_VERSION", javaVersion );
    if ( isServer() ) {
      replaceDockerFileArg( "PENTAHO_INSTALLER_NAME", "pentaho-server-" + cliPentahoVersion.edition );
    } else {
      replaceDockerFileArg( "PENTAHO_INSTALLER_NAME", "pdi-" + cliPentahoVersion.edition + "-client" );
    }
    replaceDockerFileArg( "PENTAHO_VERSION", cliPentahoVersion.versionNumber );
    replaceDockerFileArg( "PENTAHO_PACKAGE_DIST", cliPentahoVersion.distributionNumber );
    if ( isServer() ) {
      replaceDockerFileArg( "FILE_SOFTWARE", artifacts.get( ArtifactType.SERVER.toString() ).destinationFileName );
    } else {
      replaceDockerFileArg( "FILE_SOFTWARE", artifacts.get( ArtifactType.PDI.toString() ).destinationFileName );
    }

    if ( isServer() ) {
      for ( String plugin : KNOWN_PLUGINS ) {
        String value =
          plugins.contains( plugin ) ? artifacts.get( getPluginArtifactType( plugin ).toString() ).destinationFileName :
            "MakeSureThisFileNameDoesNotMatch";
        replaceDockerFileArg( "FILE_" + plugin.toUpperCase(), value );
      }
    }

    replaceDockerFileArg( "SERVICE_PACK_VERSION", cliPatchVersion == null ? "" : cliPatchVersion.versionNumber );
    replaceDockerFileArg( "SERVICE_PACK_DIST", cliPatchVersion == null ? "" : cliPatchVersion.distributionNumber );
    replaceDockerFileArg( "SERVICE_PACK_FILENAME",
      cliPatchVersion == null ? "" : artifacts.get( ArtifactType.PATCH.toString() ).destinationFileName );
    outputLine( "Docker File successfully modified" );
  }

  private void replaceDockerFileArg( String argumentName, String value ) {
    dockerFile = replaceArgGeneric( dockerFile, argumentName, value, "ARG ", "dockerfile" );
  }

  private VersionInstance getVersionInstance( String version ) {
    Map<String, VersionInstance> versionMap = dockerPentahoServerRegistry.getBuildRegistry().getVersionMap();
    VersionIterator vi = new VersionIterator( version );
    while ( vi.hasNext() ) {
      String candidateVersion = vi.next();
      VersionInstance tmpVersionInstance = versionMap.get( candidateVersion );
      if ( tmpVersionInstance != null ) {
        return tmpVersionInstance;
      }
    }
    return null;
  }

  private String replaceArgGeneric( String fileContents, String argumentName, String value, String argumentPrefix,
                                    String friendlyFileName ) {
    if ( fileContents != null ) {
      String prefix = argumentPrefix + argumentName + "=";
      int startPos = fileContents.indexOf( "\n" + prefix ) + 1;
      if ( startPos == 0 ) {
        outputLine( "WARNING: Could not find " + argumentName + " in " + friendlyFileName
          + ".  Value will not be present.  This may cause the image to fail." );
      } else {
        int endPos = fileContents.indexOf( "\n", startPos );
        return fileContents.substring( 0, startPos ) + prefix + value + fileContents.substring( endPos );
      }
    }
    return fileContents;
  }

  private void replaceEnvFileArg( String argumentName, String value ) {
    envFile = replaceArgGeneric( envFile, argumentName, value, "", ".env" );
  }

  private void replaceEntryPointSetting( String argumentName, String value ) {
    entryPointFile = replaceArgGeneric( entryPointFile, argumentName, value, "", "docker-entrypoint.sh" );
  }

  private String readAnyFile( String pathToFile ) throws DockerPentahoException {
    try {
      return new String( Files.readAllBytes( Paths.get( pathToFile ) ), StandardCharsets.UTF_8 );
    } catch ( IOException e ) {
      errorOut( "Could not load contents of file " + pathToFile );
      return null;
    }
  }

  private void writeAnyFile( String pathToFile, String contents ) throws DockerPentahoException, IOException {
    try {
      Path path = Paths.get( pathToFile );
      byte[] strToBytes = contents.getBytes();
      Files.write( path, strToBytes );
    } catch ( IOException e ) {
      errorOut( "Could not write the modified file " + pathToFile );
    }
  }

  private void downloadArtifacts() throws DockerPentahoException {
    // We got all the artifacts.  Start downloading
    outputLine( "Downloading Artifacts" );
    for ( Artifact artifact : artifacts.values() ) {
      parsedConfigMap.put( "url", artifact.sourceUrl );
      parsedConfigMap.put( "fileName", artifact.destinationFileName );
      parsedConfigMap.put( "outputFolder", artifact.downloadCacheFolder );

      if ( useExistingDownloads && checkDestinationFile( artifact ) ) {
        outputLine( " skipping download of " + artifact.destinationFileName + " it is already downloaded" );
      } else {
        String curlCommand =
          StringUtil.environmentSubstitute( parsedConfigMap.get( CONFIG_CURL_COMMAND_PROP ), parsedConfigMap.getMap() );
        runCommand( curlCommand, false, true );
      }
      //Check size of file
      if ( !checkDestinationFile( artifact ) ) {
        errorOut( "File " + artifact.destinationFileName + " is too small to be a valid artifact.  Aborting!" );
      }
    }
  }

  private void stageArtifacts() throws DockerPentahoException {
    outputLine( "Staging artifacts" );
    for ( Artifact artifact : artifacts.values() ) {
      File source = new File( artifact.downloadCacheFolder + SLASH + artifact.destinationFileName );
      File destination = new File( artifact.destinationFolder + SLASH + artifact.destinationFileName );
      try {
        FileUtils.copyFile( source, destination );
      } catch ( IOException e ) {
        errorOut( COULD_NOT_COPY + source.getAbsolutePath() + " to " + destination.getAbsolutePath(), e );
      }
    }
  }

  private boolean checkDestinationFile( Artifact artifact ) {
    //The file must be at least 2k to be valid.  Anything less it probably an error response
    File file = new File( artifact.downloadCacheFolder + SLASH + artifact.destinationFileName );
    return file.exists() && file.length() > 2048;
  }

  private void readConfigFile() throws DockerPentahoException {
    configMap = new OrderedProperties();
    try {
      configMap.load( CONFIG_FILE_NAME );
    } catch ( IOException e ) {
      errorOut( "Could Not load configuration file", e );
    }
  }

  private void computeDerivedVariables() throws DockerPentahoException {
    List<String> properties = parsedConfigMap.getKeys();
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName( "js" );
    for ( String property : properties ) {
      if ( property.startsWith( CONFIG_USER_VARIABLE_PREFIX ) ) {
        int pos = property.indexOf( ".", CONFIG_USER_VARIABLE_PREFIX.length() ) + 1;
        if ( pos == 0 ) {
          errorOut( "Bad Syntax for property \"" + property + "\".  Expecting \"" + CONFIG_USER_VARIABLE_PREFIX
            + "\" followed by a number followed by a period"
            + " followed by the variable name being set followed by a period followed by the value of the value"
            + "to set the variable to." );
        }
        int pos1 = property.indexOf( ".", pos );
        String variableName = property.substring( pos, pos1 );
        String variableValue = property.substring( pos1 + 1 );
        String criteria = StringUtil.environmentSubstitute( parsedConfigMap.get( property ), parsedConfigMap.getMap() );

        boolean result = false;
        try {
          result = (boolean) engine.eval( criteria );
        } catch ( ScriptException e ) {
          errorOut( "Unable to evaluate javascript conditional \"" + criteria + "\".", e );
        }
        if ( result ) {
          parsedConfigMap.put( variableName, variableValue ); //Conditions is true - set the variable
        }
      }
    }
  }

  private void addArtifact( String fileNameProperty, String urlProperty, ArtifactType artifactType )
    throws DockerPentahoException {
    String fileName =
      StringUtil.environmentSubstitute( getRequiredPropertyValue( fileNameProperty ), parsedConfigMap.getMap() );
    String source =
      StringUtil.environmentSubstitute( getRequiredPropertyValue( urlProperty ), parsedConfigMap.getMap() );
    artifacts.put( artifactType.toString(),
      new Artifact( source, downloadCacheFolder, artifactStagingFolder, fileName, artifactType ) );
  }

  private void addServerArtifact() throws DockerPentahoException {
    addArtifact( CONFIG_SERVER_ARTIFACT_PROP, CONFIG_SERVER_ARTIFACT_URL_PROP, ArtifactType.SERVER );
  }

  private void addPdiArtifact() throws DockerPentahoException {
    addArtifact( CONFIG_PDI_ARTIFACT_PROP, CONFIG_PDI_ARTIFACT_URL_PROP, ArtifactType.PDI );
  }

  private void addPatchArtifact() throws DockerPentahoException {
    if ( cliPatchVersion != null && isServer() ) {
      addArtifact( CONFIG_PATCH_ARTIFACT_PROP, CONFIG_PATCH_ARTIFACT_URL_PROP, ArtifactType.PATCH );
    }
  }

  private void addPluginArtifacts() throws DockerPentahoException {
    for ( String plugin : plugins ) {
      parsedConfigMap.put( "pluginId", plugin );
      addArtifact( CONFIG_PlUGIN_ARTIFACT_PROP, CONFIG_PLUGIN_ARTIFACT_URL_PROP, getPluginArtifactType( plugin ) );
    }
  }

  private void addKarArtifacts() throws DockerPentahoException {
    if ( !isEmpty( karIds ) && Boolean.TRUE.equals( versionInstance.karFilesInstalledSeparately ) ) {
      //scrape web page for build numbers

      //Find the karDistNumber by scrapping the parent folder
      parsedConfigMap.put( "karDistNumber", ".*" ); //wildcard it
      String fileName = StringUtil.environmentSubstitute( getRequiredPropertyValue( CONFIG_KAR_ARTIFACT_PROP ),
        parsedConfigMap.getMap() );
      String source = StringUtil.environmentSubstitute( getRequiredPropertyValue( CONFIG_KAR_ARTIFACT_URL_PROP ),
        parsedConfigMap.getMap() );
      //Get the parent folder by stripping the wild-carded file name off of it
      int pos = source.lastIndexOf( fileName );
      if ( pos == -1 ) {
        //If here, we probably have some misformed properties in DockerPentahoServer.properties
        outputLine( "Warning: Cannot strip kar file name \"" + fileName + "\" from kar file Url of \"" + source );
      }
      String parentFolder = source.substring( 0, pos );

      Artifact folderScrape =
        new Artifact( parentFolder, downloadCacheFolder, null, fileName, ArtifactType.valueOf( "KAR" ) );

      scrapeParentPage( folderScrape );
    }

  }

  private void scrapeParentPage( Artifact artifact ) throws DockerPentahoException {
    //This method will scrape the web page to determine the artifact names for the kars required, and add them to the
    // artifact list.
    org.jsoup.nodes.Document doc = null;
    try {
      doc = Jsoup.connect( artifact.sourceUrl ).get();
      if ( doc == null ) {
        errorOnScrape( null );
      }
    } catch ( IOException e ) {
      errorOnScrape( e );
    }

    Elements elements = doc.getElementsByAttributeValueMatching( "href", "pentaho-hadoop-shims.*\\.zip$" );
    List<String> karIdsNotProcessed = new ArrayList<>( Arrays.asList( karIds.split( "," ) ) );
    int karCount = 1;
    for ( org.jsoup.nodes.Element element : elements ) {
      String linkedFile = element.attr( "href" );
      for ( String karId : karIds.split( "," ) ) {
        parsedConfigMap.put( "karId", karId );
        String fileName = StringUtil.environmentSubstitute( getRequiredPropertyValue( CONFIG_KAR_ARTIFACT_PROP ),
          parsedConfigMap.getMap() ); //Still wild carded where the kar build number is located
        Pattern p = Pattern.compile( fileName );
        if ( p.matcher( linkedFile ).matches() ) {
          String karSourceUrl = artifact.sourceUrl + linkedFile;
          //Add the file found to the artifact list
          artifacts.put( "KAR" + karCount++,
            new Artifact( karSourceUrl, downloadCacheFolder, artifactStagingFolder
              , linkedFile, ArtifactType.valueOf( "KAR" ) ) );
          karIdsNotProcessed.remove( karId );
        }
      }
    }
    if ( !karIdsNotProcessed.isEmpty() ) {
      errorOut( "Could not locate artifacts for " + DockerPentahoUtil.friendlyListToString( karIdsNotProcessed ) );
    }
  }

  private void errorOnScrape( Exception e ) throws DockerPentahoException {
    errorOut( "Could not parse kar file scrap page", e );
  }

  private ArtifactType getPluginArtifactType( String plugin ) {
    return ArtifactType.valueOf( "PLUGIN" + plugin.toUpperCase() );
  }

  private String getRequiredPropertyValue( String key ) throws DockerPentahoException {
    //return the original value stored in the file
    if ( !configMap.containsKey( key ) ) {
      errorOut( "The " + CONFIG_FILE_NAME + " file does not contain the required key \"" + key + "\"" );
    }
    return configMap.get( key );
  }

  private void errorOut( String message ) throws DockerPentahoException {
    exceptionHandler.errorMessage( message, null );
  }

  private void errorOut( String message, Exception cause ) throws DockerPentahoException {
    exceptionHandler.errorOut( message, cause );
  }

  private void outputLine( String message ) {
    System.out.println( message );
  }

  private void preProcessAdditionalPlugins( String combinedAdditionalPlugins ) throws DockerPentahoException {
    plugins = new HashSet<>();
    if ( combinedAdditionalPlugins != null ) {
      String[] items = combinedAdditionalPlugins.split( "," );
      for ( String plugin : items ) {
        plugin = plugin.toLowerCase().trim();
        if ( plugin.equals( "" ) ) {
          continue;
        }
        if ( plugin.length() != 3 ) {
          errorOut(
            "All plugins should be 3 characters long.  Plugin value \"" + plugin + "\" is not 3 character long" );
        }
        if ( plugin.equals( "std" ) ) {
          Collections.addAll( plugins, KNOWN_PLUGINS );
        } else {
          plugins.add( plugin );
        }
      }
    }
  }

  public static class CliPentahoVersion {
    public String versionNumber;
    public String distributionNumber;
    public String edition;

    CliPentahoVersion( String argumentBeingProcessed, String combinedPentahoVersion )
      throws CommandLineSyntaxException {
      String[] pieces = combinedPentahoVersion.split( "/" );
      if ( pieces.length < 1 || pieces.length > 3 ) {
        throw new CommandLineSyntaxException(
          "Too many/few slashes in " + argumentBeingProcessed + " argument value."
            + "  Value must contain between 1 and 2 slashes." );
      }
      this.versionNumber = pieces[ 0 ];
      this.distributionNumber = pieces[ 1 ];

      if ( "database".equals( argumentBeingProcessed ) ) {
        this.edition = pieces.length == 3 ? pieces[ 2 ].toLowerCase() : "";
      } else {
        this.edition = pieces.length == 3 ? pieces[ 2 ].toLowerCase() : DEFAULT_PENTAHO_EDITION;
        if ( !"ce".equals( edition ) && !"ee".equals( edition ) ) {
          throw new CommandLineSyntaxException( "The text after the second slash in the " + argumentBeingProcessed +
            " argument value must contain \"ce\" or \"ee\"." );
        }
      }
    }
  }

  private String encryptPassword( String password ) throws DockerPentahoException {
    Encr encr = null;
    try {
      encr = Encr.getInstance();
      return encr.encryptPasswordIfNotUsingVariables( password );
    } catch ( PasswordEncoderException | XmlParseException e ) {
      errorOut( "Could not encrypt password" );
    }
    return null;
  }

  public static class Artifact {
    public String sourceUrl;
    public String downloadCacheFolder;
    public String destinationFolder;
    public String destinationFileName;
    public ArtifactType artifactType;

    public Artifact( String sourceUrl, String downloadCacheFolder, String destinationFolder,
                     String destinationFileName,
                     ArtifactType artifactType ) {
      this.sourceUrl = sourceUrl;
      this.downloadCacheFolder = downloadCacheFolder;
      this.destinationFolder = destinationFolder;
      this.destinationFileName = destinationFileName;
      this.artifactType = artifactType;
    }
  }

  public enum ArtifactType {
    SERVER, PLUGINPAZ, PLUGINPDD, PLUGINPIR, PATCH, KAR, PDI
  }

  private static boolean isValidPath( String path ) {
    try {
      Paths.get( path );
    } catch ( InvalidPathException | NullPointerException ex ) {
      return false;
    }
    return true;
  }

}
