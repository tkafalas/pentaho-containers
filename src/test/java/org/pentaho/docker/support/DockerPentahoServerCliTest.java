package org.pentaho.docker.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import org.pentaho.docker.support.registry.BuildRegistry;
import org.pentaho.docker.support.registry.DatabaseInstance;
import org.pentaho.docker.support.registry.DatabaseInstances;
import org.pentaho.docker.support.registry.DatabaseRegistry;
import org.pentaho.docker.support.registry.DockerPentahoServerRegistry;
import org.pentaho.docker.support.registry.KarFileRegistry;
import org.pentaho.docker.support.registry.VersionInstance;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DockerPentahoServerCliTest {
  private DockerPentahoServerRegistry dpsr;

  @Test
  public void registryLoadTest() {
    DatabaseInstance dataBaseInstance = new DatabaseInstance();
    dataBaseInstance.edition="edition";
    dataBaseInstance.versions="1.1";
    dataBaseInstance.image="imagename";
    DatabaseInstances dbInstances = new DatabaseInstances();

    KarFileRegistry karFileRegistry = new KarFileRegistry( Arrays.asList( "kar1", "kar2") );

    dbInstances.setDatabaseInstances( Arrays.asList( dataBaseInstance ) );
    DatabaseRegistry databaseRegistry = new DatabaseRegistry();
    Map<String, DatabaseInstances> instances = new HashMap<>();
    instances.put( "id", dbInstances );
    instances.put("id2", dbInstances );
    databaseRegistry.setDatabaseMap( instances );

    //buildRegistry
    VersionInstance vi1 = new VersionInstance();
    vi1.javaVersions = "myjdk:8";
    vi1.karFilesInstalledSeparately = false;
    VersionInstance vi2 = new VersionInstance();
    vi2.javaVersions = "myjdk:8,myjdk:11";
    vi2.karFilesInstalledSeparately = true;
    Map<String, VersionInstance> versionInstance = new HashMap<>( );
    versionInstance.put( "8", vi1 );
    versionInstance.put( "9", vi2 );
    BuildRegistry buildRegistry = new BuildRegistry( versionInstance );


    DockerPentahoServerRegistry xxx = new DockerPentahoServerRegistry( databaseRegistry, karFileRegistry, buildRegistry );
    //xxx.setDatabaseRegistry( databaseRegistry, karFileRegistry );
    //xxx.setKarFileRegistry( karFileRegistry );


    ObjectMapper objMapper = new ObjectMapper( new YAMLFactory() );
    try {
      String yamlString = objMapper.writeValueAsString( xxx );
      System.out.println( yamlString );
    } catch ( JsonProcessingException e ) {
      e.printStackTrace();
    }


    //DockerPentahoServerCli.loadDatabaseRegistry();
    //Don't do this again if we already got one
    if ( dpsr == null ) {
      ObjectMapper mapper = new ObjectMapper( new YAMLFactory() );
      mapper.findAndRegisterModules();
      try {
        dpsr =
          mapper.readValue( new File( DockerPentahoServerCli.REGISTRY_YAML_FILE ),
            DockerPentahoServerRegistry.class );
        System.out.println( dpsr.toString() );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }

}
