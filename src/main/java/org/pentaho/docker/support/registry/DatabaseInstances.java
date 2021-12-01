package org.pentaho.docker.support.registry;

import java.util.List;

public class DatabaseInstances {

  private List<DatabaseInstance> databaseInstances;

  public List<DatabaseInstance> getDatabaseInstances() {
    return databaseInstances;
  }

  public void setDatabaseInstances( List<DatabaseInstance> databaseInstances ) {
    this.databaseInstances = databaseInstances;
  }
}
