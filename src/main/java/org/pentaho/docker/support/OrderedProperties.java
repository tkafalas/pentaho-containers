package org.pentaho.docker.support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class OrderedProperties implements Map<String, String> {
  private Map<String, String> properties = new ConcurrentHashMap<>();
  private List<String> propertyOrder = new ArrayList<>();

  public OrderedProperties() {

  }

  public OrderedProperties( OrderedProperties props ) {
    for ( String property : props.getKeys() ) {
      put( property, props.get( property ) );
    }
  }

  public void load( String propertyFileName ) throws IOException {
    try ( BufferedReader br = new BufferedReader( new FileReader( propertyFileName ) ) ) {
      for ( String line; ( line = br.readLine() ) != null; ) {
        if ( line.trim().length() > 0 && !line.trim().startsWith( "#" ) ) {
          int pos = line.indexOf( "=" );
          if ( pos != -1 ) {
            String property = line.substring( 0, pos ).trim();
            String value = line.substring( pos + 1 );
            put( property, value );
          }
        }
      }
    }
  }

  public String get( String property ) {
    return properties.get( property );
  }

  @Override public String put( String p, String v ) {
    return put( p, v, propertyOrder.size() );
  }

  /**
   * Put item in list inserting before position pos
   *
   * @param p   The property name
   * @param v   The property value
   * @param pos = insert before this subscript in the list
   */
  public String put( String p, String v, int pos ) {
    if ( pos > propertyOrder.size() || pos < 0 ) {
      throw new IllegalArgumentException( "attempt put item after position that does not exist." );
    }
    if ( propertyOrder.indexOf( p ) > 0 ) {
      String priorValue = properties.get( p );
      //Will not change the position of an existing entry
      properties.put( p, v );
      return priorValue;
    }

    propertyOrder.add( pos, p );
    properties.put( p, v );
    return null;
  }

  public List<String> getKeys() {
    return new ArrayList<String>( propertyOrder );
  }

  public Map getMap() {
    HashMap<String, String> result = new HashMap<>();
    result.putAll( properties );
    return result;
  }

  public boolean containsKey( String property ) {
    return properties.containsKey( property );
  }

  // The following methods added To implement Map
  @Override public int size() {
    return properties.size();
  }

  @Override public boolean isEmpty() {
    return properties.isEmpty();
  }

  @Override public boolean containsKey( Object key ) {
    return properties.containsKey( key );
  }

  @Override public boolean containsValue( Object value ) {
    return properties.containsValue( value );
  }

  @Override public String get( Object key ) {
    return properties.get( key );
  }

  @Override public String remove( Object key ) {
    propertyOrder.remove( key );
    return properties.remove( key );
  }

  @Override public void putAll( Map<? extends String, ? extends String> m ) {
    for ( String key : m.keySet() ) {
      put( key, m.get( key ), properties.size() );
    }
  }

  @Override public void clear() {
    properties.clear();
    propertyOrder.clear();
  }

  @Override public Set<String> keySet() {
    return properties.keySet();
  }

  @Override public Collection<String> values() {
    List<String> values = new ArrayList<>();
    for ( String key : propertyOrder ) {
      values.add( properties.get( key ) );
    }
    return values;
  }

  @Override public Set<Entry<String, String>> entrySet() {
    return properties.entrySet();
  }
}
