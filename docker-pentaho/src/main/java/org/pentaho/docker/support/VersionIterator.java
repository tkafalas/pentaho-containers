package org.pentaho.docker.support;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VersionIterator implements Iterator<String> {
  String version;

  /**
   * This class's sole purpose is to take a version of our product and strip off pieces of the version number,
   * presumably to match up with some template definition.  It is used by DockerPentahoServerCli to find database
   * overrides to use.  It is also used to lookup VersionInstances out of the BuildRegistry.  See the
   * VersionIteratorTest class to see how it works.
   * @param version The version number being parsed.
   */
  public VersionIterator( String version ) {
    this.version = version;
    this.pointer = version.length();
  }

  Integer pointer;

  @Override public boolean hasNext() {
    if ( pointer >= 0 ) {
      return true;
    }
    return false;
  }

  @Override public String next() {
    if ( hasNext() ) {
      String result = version.substring( 0, pointer );
      while ( pointer >= 0 && version.charAt( pointer - 1 ) == '.' ) {
        pointer--;
      }
      if ( result.length() != pointer ) {
        return result;
      }
      int pos = version.substring( 0, pointer ).lastIndexOf( '.' );
      int pos1 = version.substring( 0, pointer ).lastIndexOf( '-' );
      pos = Math.max( pos, pos1 );
      if ( pos == -1 ){
        if ( result.equals( version.substring(0, pointer ) ) ) {
          pointer = pos;
        }
      } else {
        pointer = pos;
      }
      return result;
    }
    throw new NoSuchElementException( );
  }
}
