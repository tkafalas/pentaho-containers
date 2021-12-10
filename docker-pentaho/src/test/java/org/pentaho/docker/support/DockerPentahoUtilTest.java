package org.pentaho.docker.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.docker.support.registry.BuildRegistry;
import org.pentaho.docker.support.registry.DatabaseInstance;
import org.pentaho.docker.support.registry.DatabaseInstances;
import org.pentaho.docker.support.registry.DatabaseRegistry;
import org.pentaho.docker.support.registry.DockerPentahoServerRegistry;
import org.pentaho.docker.support.registry.KarFileRegistry;
import org.pentaho.docker.support.registry.VersionInstance;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DockerPentahoUtilTest {

  @Test
  public void registryLoadTest() {
    //This test will setup the object model and create the yaml file from the objects (serialize) and then
    //deserialize the yaml file back to the objects and compare.  Useful for adding objects to the structure
    // and having it create the yaml structure from the model.
    DatabaseInstance dataBaseInstance = new DatabaseInstance();
    dataBaseInstance.edition = "edition";
    dataBaseInstance.versions = "1.1";
    dataBaseInstance.image = "imagename";
    DatabaseInstances dbInstances = new DatabaseInstances();

    KarFileRegistry karFileRegistry = new KarFileRegistry( Arrays.asList( "kar1", "kar2" ) );

    dbInstances.setDatabaseInstances( Arrays.asList( dataBaseInstance ) );
    DatabaseRegistry databaseRegistry = new DatabaseRegistry();
    Map<String, DatabaseInstances> instances = new HashMap<>();
    instances.put( "id", dbInstances );
    instances.put( "id2", dbInstances );
    databaseRegistry.setDatabaseMap( instances );

    //buildRegistry
    VersionInstance vi1 = new VersionInstance();
    vi1.javaVersions = "myjdk:8";
    vi1.karFilesInstalledSeparately = false;
    VersionInstance vi2 = new VersionInstance();
    vi2.javaVersions = "myjdk:8,myjdk:11";
    vi2.karFilesInstalledSeparately = true;
    Map<String, VersionInstance> versionInstance = new HashMap<>();
    versionInstance.put( "8", vi1 );
    versionInstance.put( "9", vi2 );
    BuildRegistry buildRegistry = new BuildRegistry( versionInstance );

    DockerPentahoServerRegistry controlDpsr =
      new DockerPentahoServerRegistry( databaseRegistry, karFileRegistry, buildRegistry );

    ObjectMapper objMapper = new ObjectMapper( new YAMLFactory() );
    String yamlString = null;
    try {
      yamlString = objMapper.writeValueAsString( controlDpsr );
      System.out.println( yamlString );
      Assert.assertNotNull( yamlString );
    } catch ( JsonProcessingException e ) {
      e.printStackTrace();
    }

    //DockerPentahoServerCli.loadDatabaseRegistry();
    //Don't do this again if we already got one
    DockerPentahoServerRegistry dpsr = null;
    ObjectMapper mapper = new ObjectMapper( new YAMLFactory() );
    mapper.findAndRegisterModules();
    try {
      dpsr =
        mapper.readValue( yamlString.getBytes( "UTF-8" ), DockerPentahoServerRegistry.class );
      System.out.println( dpsr.toString() );
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    assertEquals( controlDpsr, dpsr );
  }

  @Test
  public void getDockerPentahoServerRegistry() {
    DockerPentahoServerRegistry dpsr = DockerPentahoUtil.getDockerPentahoServerRegistry();
    Assert.assertNotNull( dpsr );
  }

  @Test
  public void friendlySupportedDatabaseTypes() {
    String s = DockerPentahoUtil.friendlySupportedDatabaseTypes();
    assert ( s.contains( "postgres/" ) && s.contains( "mysql/" ) );
  }

  @Test
  public void friendlyListToString() {
    assertEquals( "\"A\", \"B\" and \"C\"", DockerPentahoUtil.friendlyListToString( Arrays.asList( "A", "B", "C" ) ) );
    assertEquals( "\"A\" and \"B\"", DockerPentahoUtil.friendlyListToString( Arrays.asList( "A", "B" ) ) );
    assertEquals( "\"A\"", DockerPentahoUtil.friendlyListToString( Arrays.asList( "A" ) ) );
  }

  @Test
  public void friendlyArrayToString() {
    assertEquals( "\"A\", \"B\" and \"C\"",
      DockerPentahoUtil.friendlyArrayToString( new String[] { "A", "B", "C" } ) );
    assertEquals( "\"A\" and \"B\"", DockerPentahoUtil.friendlyArrayToString( new String[] { "A", "B" } ) );
    assertEquals( "\"A\"", DockerPentahoUtil.friendlyArrayToString( new String[] { "A" } ) );
  }

  @Test
  public void loadDatabaseRegistry() {
    DockerPentahoUtil.loadDatabaseRegistry();
    Assert.assertNotNull( DockerPentahoUtil.dockerPentahoServerRegistry );
  }

  @Test
  public void supportedDatabaseTypes() {
    List<String> dbTypes = DockerPentahoUtil.supportedDatabaseTypes();
    assert ( dbTypes.contains( "postgres/9.6" ) );
    assert ( dbTypes.contains( "mysql/5.7"
      + "" ) );
    assert ( dbTypes.contains( "mysql/8.0" ) );
  }
}
