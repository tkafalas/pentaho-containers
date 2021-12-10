package org.pentaho.docker.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.pentaho.docker.support.registry.DatabaseInstance;
import org.pentaho.docker.support.registry.DatabaseInstances;
import org.pentaho.docker.support.registry.DockerPentahoServerRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//Contains common logic used by both DockerPentahoServerCli and DockerPentahoServerService
public class DockerPentahoUtil {
  protected static DockerPentahoServerRegistry dockerPentahoServerRegistry;//We only need one instance
  protected static final String REGISTRY_YAML_FILE = "containers/registry.yml";

  public static DockerPentahoServerRegistry getDockerPentahoServerRegistry() {
    loadDatabaseRegistry();
    return dockerPentahoServerRegistry;
  }

  public static String friendlySupportedDatabaseTypes() {
    //Suitable for display
    return friendlyListToString( supportedDatabaseTypes() );
  }

  public static String friendlyListToString( List names ) {
    //Suitable for display
    StringBuilder result = new StringBuilder();
    for ( int i = 0; i < names.size(); i++ ) {
      if ( i != 0 ) {
        if ( i == names.size() - 1 ) {
          result.append( " and " );
        } else {
          result.append( ", " );
        }
      }
      result.append( "\"" ).append( names.get( i ) ).append( "\"" );
    }
    return result.toString();
  }

  public static String friendlyArrayToString( String[] array ) {
    StringBuilder s = new StringBuilder();
    for ( int i = 0; i < array.length; i++ ) {
      if ( i != 0 ) {
        if ( i == array.length - 1 ) {
          s.append( " and " );
        } else {
          s.append( ", " );
        }
      }
      s.append( "\"" + array[ i ] + "\"" );
    }
    return s.toString();
  }

  public static int runCommand( String command, boolean quiet, boolean abortOnError ) throws DockerPentahoException {
    return runCommand( command, quiet, abortOnError, System.getProperty( "user.dir" ) );
  }

  public static int runCommand( String command, boolean quiet, boolean abortOnError, String workingDir )
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
          throw new DockerPentahoException( "Command \"" + command + "\" has failed with exit code of " + exitCode );
        } else {
          if ( !quiet ) {
            outputLine( "Command failed with exit code " + exitCode );
          }
        }
      }
    } catch ( IOException | InterruptedException e ) {
      throw new DockerPentahoException( "Error running command \"" + command + "\"", e );
    }
    return exitCode;
  }

  private static void outputLine( String message ) {
    System.out.println( message );
  }

  protected static void loadDatabaseRegistry() {
    //Don't do this again if we already got one
    if ( dockerPentahoServerRegistry == null ) {
      ObjectMapper mapper = new ObjectMapper( new YAMLFactory() );
      mapper.findAndRegisterModules();
      try {
        dockerPentahoServerRegistry =
          mapper.readValue( new File( REGISTRY_YAML_FILE ), DockerPentahoServerRegistry.class );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }

  protected static List<String> supportedDatabaseTypes() {
    List<String> result = new ArrayList<>();
    for ( Map.Entry<String, DatabaseInstances> databaseEntry : dockerPentahoServerRegistry.getDatabaseRegistry()
      .getDatabaseMap().entrySet() ) {
      for ( DatabaseInstance instance : databaseEntry.getValue().getDatabaseInstances() ) {
        for ( String version : instance.versions.split( "," ) ) {
          version = version.trim();
          String databaseId = databaseEntry.getKey() + "/" + version;
          if ( instance.edition != null && !instance.edition.trim().isEmpty() ) {
            databaseId = databaseId + "/" + instance.edition.trim();
          }
          result.add( databaseId );
        }
      }
    }
    return result;
  }

  public static void unzip( String file, String outputFolder ) throws IOException {
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

}
