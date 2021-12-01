package org.pentaho.docker.support;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class EulaExecutor {
  public static boolean executeEulaAgreement( boolean eulaAccept ) {
    if ( eulaAccept == false ) {
      return executeHeadlessAgreement();
    }
    return true;
  }

  private static boolean executeHeadlessAgreement() {
    try (
      InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream( "pentaho-standard-eula.txt" ) ) {
      System.out.println( IOUtils.toString( inputStream, StandardCharsets.UTF_8 )
        + "\nDo you accept the license agreement displayed above (yes/no): " );
      Scanner scanner = new Scanner( System.in );
      String answer = scanner.nextLine().toLowerCase();
      if ( "y".equals( answer ) || "yes".equals( answer ) ) {
        return true;
      } else {
        return false;
      }
    } catch ( IOException e ) {
      return false;
    }
  }
}
