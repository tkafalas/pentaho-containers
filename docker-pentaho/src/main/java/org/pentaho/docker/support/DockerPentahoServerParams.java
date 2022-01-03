package org.pentaho.docker.support;

public class DockerPentahoServerParams {
  private boolean asService;
  private String combinedPentahoVersion;
  private String combinedPatchVersion;
  private String combinedPlugins;
  private String installPath;
  private boolean useExistingDownloads;
  private boolean noCache;
  private String port;
  private String database;
  private String karIds;
  private String productType;
  private String user;
  private String password;
  private boolean execute;
  private boolean eulaAccept;
  private String javaVersion;
  private String metastore;

  /**
   * Encapsulates all params for DockerPentahoServerCli and DockerPentahoServerService
   *
   * @param builder A DockerPentahoServerParms.Builder object
   */
  DockerPentahoServerParams( DockerPentahoServerParams.Builder builder ) {
    this.asService = builder.asService;
    this.combinedPentahoVersion = builder.combinedPentahoVersion;
    this.combinedPatchVersion = builder.combinedPatchVersion;
    this.combinedPlugins = builder.combinedPlugins;
    this.installPath = builder.installPath;
    this.useExistingDownloads = builder.useExistingDownloads;
    this.noCache = builder.noCache;
    this.port = builder.port;
    this.database = builder.database;
    this.karIds = builder.karIds;
    this.productType = builder.productType;
    this.user = builder.user;
    this.password = builder.password;
    this.execute = builder.execute;
    this.eulaAccept = builder.eulaAccept;
    this.javaVersion = builder.javaVersion;
    this.metastore = builder.metastore;
  }

  public boolean isAsService() {
    return asService;
  }

  public String getCombinedPentahoVersion() {
    return combinedPentahoVersion;
  }

  public String getCombinedPatchVersion() {
    return combinedPatchVersion;
  }

  public String getCombinedPlugins() {
    return combinedPlugins;
  }

  public String getInstallPath() {
    return installPath;
  }

  public boolean isUseExistingDownloads() {
    return useExistingDownloads;
  }

  public boolean isNoCache() {
    return noCache;
  }

  public String getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  public String getKarIds() {
    return karIds;
  }

  public String getProductType() {
    return productType;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public boolean isExecute() {
    return execute;
  }

  public boolean isEulaAccept() {
    return eulaAccept;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public String getMetastore() {
    return metastore;
  }

  public static class Builder {
    private boolean asService;
    private String combinedPentahoVersion;
    private String combinedPatchVersion;
    private String combinedPlugins;
    private String installPath;
    private boolean useExistingDownloads;
    private boolean noCache;
    private String port;
    private String database;
    private String karIds;
    private String productType;
    private String user;
    private String password;
    private boolean execute;
    private boolean eulaAccept;
    private String javaVersion;
    private String metastore;

    public Builder( boolean asService, String combinedPentahoVersion ) throws CommandLineSyntaxException {
      this.asService = asService;
      //parse it now so a badly formed version errors immediately
      new CliPentahoVersion( "pentahoVersion", combinedPentahoVersion );
      this.combinedPentahoVersion = combinedPentahoVersion;
    }

    public DockerPentahoServerParams build() {
      return new DockerPentahoServerParams( this );
    }


    public Builder combinedPatchVersion( String combinedPatchVersion ) throws CommandLineSyntaxException {
      if ( combinedPatchVersion != null ) {
        new CliPentahoVersion( "patchVersion", combinedPatchVersion );
      }
      this.combinedPatchVersion = combinedPatchVersion;
      return this;
    }

    public Builder combinedPlugins( String combinedPlugins ) {
      this.combinedPlugins = combinedPlugins;
      return this;
    }

    public Builder installPath( String installPath ) {
      this.installPath = installPath;
      return this;
    }

    public Builder useExistingDownloads( boolean useExistingDownloads ) {
      this.useExistingDownloads = useExistingDownloads;
      return this;
    }

    public Builder noCache( boolean noCache ) {
      this.noCache = noCache;
      return this;
    }

    public Builder port( String port ) {
      this.port = port;
      return this;
    }

    public Builder database( String database ) {
      this.database = database;
      return this;
    }

    public Builder karIds( String karIds ) {
      this.karIds = karIds;
      return this;
    }

    public Builder productType( String productType ) {
      this.productType = productType;
      return this;
    }

    public Builder user( String user ) {
      this.user = user;
      return this;
    }

    public Builder password( String password ) {
      this.password = password;
      return this;
    }

    public Builder execute( boolean execute ) {
      this.execute = execute;
      return this;
    }

    public Builder eulaAccept( boolean eulaAccept ) {
      this.eulaAccept = eulaAccept;
      return this;
    }

    public Builder javaVersion( String javaVersion ) {
      this.javaVersion = javaVersion;
      return this;
    }

    public Builder metastore( String metastore ) {
      this.metastore = metastore;
      return this;
    }
  }
}
