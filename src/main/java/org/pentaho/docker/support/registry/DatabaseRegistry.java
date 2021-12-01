package org.pentaho.docker.support.registry;

import org.codehaus.jackson.annotate.JsonProperty;
import org.pentaho.di.core.util.StringUtil;

import java.util.Map;

public class DatabaseRegistry {
  @JsonProperty Map<String, DatabaseInstances> databaseMap;

  public DatabaseRegistry() {
  }

  public DatabaseRegistry( Map<String, DatabaseInstances> databaseRegistry ) {
    this.databaseMap = databaseRegistry;
  }

  public Map<String, DatabaseInstances> getDatabaseMap() {
    return databaseMap;
  }

  @Override
  public String toString() {
    return "Config{" + "map=" + databaseMap + '}';
  }

  public DatabaseInstance getDatabaseInstance( String databaseId ) {
    String[] databasePieces = databaseId.split( "/" );
    DatabaseInstances databaseInstances = databaseMap.get( databasePieces[ 0 ] );
    for ( DatabaseInstance databaseInstance : databaseInstances.getDatabaseInstances() ) {
      for ( String version : databaseInstance.versions.split( "," ) ) {
        if ( version.equals( databasePieces[ 1 ] )
          && ( ( databaseInstance.edition == null && databasePieces.length <= 2 )
          || ( databaseInstance.edition == null && StringUtil.isEmpty( databasePieces[ 2 ] ) )
          || ( databaseInstance.edition != null && databasePieces.length >= 3 && databaseInstance.edition.equals(
          databasePieces[ 2 ] ) ) ) ) {
        return databaseInstance;
        }
      }
    }
    return null;
  }

  public void setDatabaseMap(
    Map<String, DatabaseInstances> databaseMap ) {
    this.databaseMap = databaseMap;
  }


}
