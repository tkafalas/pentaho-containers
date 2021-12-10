package org.pentaho.docker.support.registry;

import org.apache.commons.lang3.ObjectUtils;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class KarFileRegistry {
  @JsonProperty( "karFiles")
  public List<String> karFiles;

  public KarFileRegistry() {
  }

  public KarFileRegistry( List<String> karFiles ) {
    this.karFiles = karFiles;
  }

  public List<String> getKarFiles() {
    return karFiles;
  }

  public void setKarFiles( List<String> karFiles ) {
    this.karFiles = karFiles;
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == this ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    KarFileRegistry k = (KarFileRegistry) o;
    return !ObjectUtils.notEqual( karFiles, k.karFiles );
  }
}
