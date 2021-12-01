package org.pentaho.docker.support.registry;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class BuildRegistry {
  @JsonProperty Map<String, VersionInstance> versionMap;

  public BuildRegistry() {
  }

  public BuildRegistry( Map<String, VersionInstance> versionMap ) {
    this.versionMap = versionMap;
  }

  public Map<String, VersionInstance> getVersionMap() {
    return versionMap;
  }

  @Override
  public String toString() {
    return "Config{" + "map=" + versionMap + '}';
  }

  public void setVersionMap(
    Map<String, VersionInstance> versionMap ) {
    this.versionMap = versionMap;
  }

}
