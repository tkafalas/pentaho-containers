package org.pentaho.docker.support.registry;

import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public class DatabaseInstances {

  private List<DatabaseInstance> databaseInstances;

  public List<DatabaseInstance> getDatabaseInstances() {
    return databaseInstances;
  }

  public void setDatabaseInstances( List<DatabaseInstance> databaseInstances ) {
    this.databaseInstances = databaseInstances;
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == this ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    DatabaseInstances d = (DatabaseInstances) o;
    return !ObjectUtils.notEqual( databaseInstances, d.databaseInstances );
  }
}
