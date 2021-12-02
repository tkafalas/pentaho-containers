package org.pentaho.docker.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.pentaho.docker.support.registry.DatabaseInstance;
import org.pentaho.docker.support.registry.DatabaseInstances;
import org.pentaho.docker.support.registry.DockerPentahoServerRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Contains logic used by both DockerPentahoServerCli and DockerPentahoServerService
public class DockerPentahoUtil {
  private static DockerPentahoServerRegistry dockerPentahoServerRegistry;//We only need one instance
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

  public static String arrayToUserFriendlyString( String[] array ) {
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



}
