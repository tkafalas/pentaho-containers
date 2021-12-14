import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Installer {

  //java -DINSTALL_PATH=${INSTALLATION_PATH} -DEULA_ACCEPT=true -jar ./${FILE_RELEASE_NAME}/installer.jar -options-system;
  public static void main(final String[] args) throws Exception {
    System.out.println( args[0] );
    String installPath = getInstallPath( args );
    if ( installPath != null ) {
      try ( InputStream is = Installer.class.getClassLoader().getResourceAsStream( "mockArtifact.zip" ) ) {
        File file = new File( installPath + "/mockArtifact.zip" );
        Files.copy( is, file.toPath(), StandardCopyOption.REPLACE_EXISTING );
      }
    }
    return;
  }

  public static String getInstallPath( String[] args ) {
    String param = args[0].trim();
    if ( param.startsWith( "-D" ) ) {
      param = param.substring(2).trim();
      if ( param.startsWith( "INSTALL_PATH=" ) ){
        return param.substring( 13 ).trim();
      }
    }
    return null;
  }
}
