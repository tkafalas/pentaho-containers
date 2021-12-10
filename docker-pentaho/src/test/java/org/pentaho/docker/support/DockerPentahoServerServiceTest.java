package org.pentaho.docker.support;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

public class DockerPentahoServerServiceTest {
  private static final String LINE = StringUtils.repeat( "-", 80 );
  private static final String PENTAHO_VERSION = "9.2.2.2/998/ee";
  private static final String PATCH_VERSION = "9.2.2.3/999/ee";
  private static final boolean IS_WINDOWS = System.getProperty( "os.name" ).toLowerCase().contains( "win" );

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void setup() throws Exception {
    //Make it use our special properties file that uses different artifact file names and generatedFile folder instead
    //of the one it comes with.
    DockerPentahoServerService.CONFIG_FILE_PATH = "./src/test/resources/DockerPentahoServer.properties";
  }

  @Test
  public void testBadSyntax() throws Exception {
    outputBanner( "testBadSyntax" );
    expectedException.expect( CommandLineSyntaxException.class );
    expectedException.expectMessage( "Value must contain between 1 and 2 slashes." );
    DockerPentahoServerParams.Builder builder = new DockerPentahoServerParams.Builder( true, "badVersion" );
    DockerPentahoServerParams params = new DockerPentahoServerParams( builder );
    new DockerPentahoServerService( params ).executeService();
  }

  @Test
  public void testHappyPath() throws Exception {
    outputBanner( "testHappyPath" );

    CliPentahoVersion pentahoVersion = new CliPentahoVersion( "noarg", PENTAHO_VERSION );
    CliPentahoVersion patchVersion = new CliPentahoVersion( "noarg", PATCH_VERSION );
    String expandedVersion =
      pentahoVersion.edition + "-" + pentahoVersion.versionNumber + "-" + pentahoVersion.distributionNumber;
    makeMockInstaller( "pentaho-server-" + expandedVersion, "pdd-plugin-" + expandedVersion, false );
    makeMockInstaller( "pentaho-server-" + expandedVersion, "paz-plugin-" + expandedVersion,false );
    makeMockInstaller( "pentaho-server-" + expandedVersion, "pir-plugin-" + expandedVersion,false );
    //makeMockInstaller( "pentaho-server-" + expandedVersion, "PentahoServer-SP-" +
        //patchVersion.versionNumber + "-" + patchVersion.distributionNumber, true );

    DockerPentahoServerParams.Builder builder = new DockerPentahoServerParams.Builder( true, PENTAHO_VERSION )
      .eulaAccept( true )
      .useExistingDownloads( true )
      .combinedPlugins( "std" );
      //.combinedPatchVersion( PATCH_VERSION );

    DockerPentahoServerParams params = new DockerPentahoServerParams( builder );
    DockerPentahoServerService dockerPentahoServerService = new DockerPentahoServerService( params );
    dockerPentahoServerService.executeService();
    String generatedFiles = "target/generatedFiles";
    String absolutePath = new File( generatedFiles ).getAbsolutePath();
    assert ( new File( generatedFiles ).exists() );
    assert ( new File( generatedFiles + "/Dockerfile" ).exists() );
    assert ( new File( generatedFiles + "/docker-compose.yml" ).exists() );
    assert ( new File( generatedFiles + "/stagedArtifacts" ).exists() );
    assertEquals( 4, new File( generatedFiles + "/stagedArtifacts" ).listFiles().length );

    if ( IS_WINDOWS ) {
      String command =
        "docker build -f " + absolutePath + "/Dockerfile -t pentaho/test-image-do-not-use " + absolutePath;
      DockerPentahoUtil.runCommand( command, false, true );
    }
  }

  @Test
  public void testBadDatabase() throws Exception {
    outputBanner( "testBadDatabase" );
    String invalidDatabase = "invalid/database";
    expectedException.expect( DockerPentahoException.class );
    expectedException.expectMessage( "Database " + invalidDatabase + " is not supported" );
    DockerPentahoServerParams.Builder builder = new DockerPentahoServerParams.Builder( true, PENTAHO_VERSION )
      .eulaAccept( true )
      .useExistingDownloads( true )
      .database( "invalid/database" );
    DockerPentahoServerParams params = new DockerPentahoServerParams( builder );
    DockerPentahoServerService dockerPentahoServerService = new DockerPentahoServerService( params );
    dockerPentahoServerService.executeService();
  }

  @Test
  public void testBadPort() throws Exception {
    outputBanner( "testBadPort" );
    expectedException.expect( DockerPentahoException.class );
    expectedException.expectMessage( "Port number is not numeric" );
    DockerPentahoServerParams.Builder builder = new DockerPentahoServerParams.Builder( true, PENTAHO_VERSION )
      .eulaAccept( true )
      .useExistingDownloads( true )
      .port( "badPort" );
    DockerPentahoServerParams params = new DockerPentahoServerParams( builder );
    DockerPentahoServerService dockerPentahoServerService = new DockerPentahoServerService( params );
    dockerPentahoServerService.executeService();
  }

  @Test
  public void testBadPort2() throws Exception {
    outputBanner( "testBadPort2" );
    expectedException.expect( DockerPentahoException.class );
    expectedException.expectMessage( "less than or equal to 65535" );
    DockerPentahoServerParams.Builder builder = new DockerPentahoServerParams.Builder( true, PENTAHO_VERSION )
      .eulaAccept( true )
      .useExistingDownloads( true )
      .port( "80000" );
    DockerPentahoServerParams params = new DockerPentahoServerParams( builder );
    DockerPentahoServerService dockerPentahoServerService = new DockerPentahoServerService( params );
    dockerPentahoServerService.executeService();
  }

  @Test
  public void testInvalidInstallPath() throws Exception {
    outputBanner( "invalidInstallPathTest" );
    String installPath = "485:!#";
    expectedException.expect( DockerPentahoException.class );
    expectedException.expectMessage( "Invalid installPath of " + installPath );
    DockerPentahoServerParams.Builder builder = new DockerPentahoServerParams.Builder( true, PENTAHO_VERSION )
      .eulaAccept( true )
      .useExistingDownloads( true )
      .installPath( installPath );
    DockerPentahoServerParams params = new DockerPentahoServerParams( builder );
    DockerPentahoServerService dockerPentahoServerService = new DockerPentahoServerService( params );
    dockerPentahoServerService.executeService();
  }

  private void makeMockInstaller( String sourceZipFile, String destZipFile, boolean isSP ) throws IOException {
    final String tempFolder = "target/tmp";
    File t = new File( tempFolder );
    if ( t.exists() ) {
      FileUtils.deleteDirectory( t );
    }
    DockerPentahoUtil.unzip( "target/artifactCache/mock-" + sourceZipFile + "-dist.zip", tempFolder );
    //rename the folder
    File dir = new File( tempFolder + "/" + sourceZipFile );
    if ( !dir.isDirectory() ) {
      fail( "There is no directory at the given path " + sourceZipFile );
    } else {
      File newDir = new File( dir.getParent() + "/" + destZipFile );
      dir.renameTo( newDir );
    }
    //zip it up
    if ( isSP ){
      DockerPentahoUtil.zipDirectory( "target/artifactCache/mock-" + destZipFile + ".bin",
        tempFolder + "/" + destZipFile );
    } else {
      DockerPentahoUtil.zipDirectory( "target/artifactCache/mock-" + destZipFile + "-dist.zip",
        tempFolder + "/" + destZipFile );
    }
  }

  private void outputBanner( String title ) {
    System.out.println( LINE );
    System.out.println( title );
    System.out.println( LINE );
  }

}