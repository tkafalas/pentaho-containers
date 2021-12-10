package org.pentaho.docker.support.registry;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.ObjectUtils;

public class DockerPentahoServerRegistry {
  private DatabaseRegistry databaseRegistry;
  private KarFileRegistry karFileRegistry;
  private BuildRegistry buildRegistry;

  @JsonCreator
  public DockerPentahoServerRegistry( @JsonProperty( "databaseRegistry" ) DatabaseRegistry databaseRegistry,
                                      @JsonProperty( "karFileRegistry" ) KarFileRegistry karFileRegistry,
                                      @JsonProperty( "buildRegistry" ) BuildRegistry buildRegistry ) {
    this.databaseRegistry = databaseRegistry;
    this.karFileRegistry = karFileRegistry;
    this.buildRegistry = buildRegistry;
  }

  public DatabaseRegistry getDatabaseRegistry() {
    return databaseRegistry;
  }

  public KarFileRegistry getKarFileRegistry() {
    return karFileRegistry;
  }

  public BuildRegistry getBuildRegistry() {
    return buildRegistry;
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == this ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    DockerPentahoServerRegistry d = (DockerPentahoServerRegistry) o;
    return !( ObjectUtils.notEqual( databaseRegistry, d.databaseRegistry ) || ObjectUtils.notEqual( karFileRegistry,
      d.karFileRegistry ) || ObjectUtils.notEqual( buildRegistry, d.buildRegistry ) );
  }
}
