package org.pentaho.docker.support.registry;

import org.apache.commons.lang3.ObjectUtils;
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

  @Override
  public boolean equals( Object o ) {
    if ( o == this ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    BuildRegistry d = (BuildRegistry) o;
    return !ObjectUtils.notEqual( versionMap, d.versionMap );
  }

}
