import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Installer {

  //java -DINSTALL_PATH=${INSTALLATION_PATH} -DEULA_ACCEPT=true -jar ./${FILE_RELEASE_NAME}/installer.jar -options-system;
  public static void main(final String[] args) throws Exception {
    File zipFile = new File( "resources/mockArtifact.zip" );
    String outputFolder = System.getProperty( "INSTALL_PATH");
    unzip( zipFile, outputFolder);
    return;
  }

  public static void unzip( File file, String outputFolder ) throws IOException {
    try ( ZipFile zipFile = new ZipFile( file ) ) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while ( entries.hasMoreElements() ) {
        ZipEntry entry = entries.nextElement();
        File entryDestination = new File( outputFolder, entry.getName() );
        if ( entry.isDirectory() ) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          try ( InputStream in = zipFile.getInputStream( entry );
                OutputStream out = new FileOutputStream( entryDestination ) ) {
            IOUtils.copy( in, out );
          }
        }
      }
    }
  }
}
