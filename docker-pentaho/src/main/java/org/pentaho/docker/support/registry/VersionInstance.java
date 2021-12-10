package org.pentaho.docker.support.registry;


import org.apache.commons.lang3.ObjectUtils;

public class VersionInstance {
  public String javaVersions;
  public Boolean karFilesInstalledSeparately;

  @Override
  public boolean equals( Object o ) {
    if ( o == this ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    VersionInstance v = (VersionInstance) o;
    return !( ObjectUtils.notEqual( javaVersions, v.javaVersions )
      || ObjectUtils.notEqual( karFilesInstalledSeparately, v.karFilesInstalledSeparately ) );
  }
}
