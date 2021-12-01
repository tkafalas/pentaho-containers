package org.pentaho.docker.support;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class VersionIteratorTest {
  @Test
  public void testIterator() {
    assertEquals( Arrays.asList( new String[] { "8.3.0.4", "8.3.0", "8.3", "8" } ), getIterations( "8.3.0.4" ) );
    assertEquals( Arrays.asList( new String[] { "9.3.0.0-SNAPSHOT", "9.3.0.0", "9.3.0", "9.3", "9" } ),
      getIterations( "9.3.0.0-SNAPSHOT" ) );
    assertEquals( Arrays.asList( new String[] { "9.3.0...", "9.3.0", "9.3", "9" } ), getIterations( "9.3.0..." ) );
  }

  private List<String> getIterations( String version ) {
    List list = new ArrayList<String>();
    VersionIterator iterator = new VersionIterator( version );
    while ( iterator.hasNext() ) {
      list.add( iterator.next() );
    }
    return list;
  }
}