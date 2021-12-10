package org.pentaho.docker.support.registry;

import org.apache.commons.lang3.ObjectUtils;

public class DatabaseInstance {
  public String versions;
  public String edition;
  public String composeYml;
  public String dbInitFolder;
  public String image;

  public String getVersions() {
    return versions;
  }

  public void setVersions( String versions ) {
    this.versions = versions;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition( String edition ) {
    this.edition = edition;
  }

  public String getComposeYml() {
    return composeYml;
  }

  public void setComposeYml( String composeYml ) {
    this.composeYml = composeYml;
  }

  public String getImage() {
    return image;
  }

  public void setImage( String image ) {
    this.image = image;
  }

  public String getDbInitFolder() {
    return dbInitFolder;
  }

  public void setDbInitFolder( String dbInitFolder ) {
    this.dbInitFolder = dbInitFolder;
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == this ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    DatabaseInstance d = (DatabaseInstance) o;
    return !( ObjectUtils.notEqual( versions, d.versions ) || ObjectUtils.notEqual( edition,
      d.edition ) || ObjectUtils.notEqual( composeYml, d.composeYml ) || ObjectUtils.notEqual( dbInitFolder,
      d.dbInitFolder ) || ObjectUtils.notEqual( image, d.image ) );
  }
}
