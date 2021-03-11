/*
 * Last change: $Date: 2004/07/11 22:26:28 $ $Revision: 1.9 $ Copyright (c)
 * 2004, The Black Sheep, Department of Computer Science, The University of
 * Auckland All rights reserved. Redistribution and use in source and binary
 * forms, with or without modification, are permitted provided that the
 * following conditions are met: Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. Neither the name of
 * The Black Sheep, The Department of Computer Science or The University of
 * Auckland nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package rescuecore.view;

import rescuecore.objects.*;
import rescuecore.*;
import rescuecore.event.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * This class represents a layer of information to be displayed on a map of the
 * world. Each layer is responsible for drawing some subset of the available
 * objects in the world. Layers are responsible for requesting repaints as
 * necessary.
 */
public class Layer {

  // private boolean dirty;
  protected Collection<Object> objects;
  private java.util.Map        shapes;
  // private BufferedImage image;
  protected java.util.Map      renderers;
  protected String             name;
  protected boolean            enabled;
  // private double transparancy;


  public static Layer createRoadLayer( Memory m ) {
    final MemoryLayer result = new MemoryLayer( "Roads" );
    result.addType( RescueConstants.TYPE_ROAD );
    result.addRenderer( Road.class, RoadRenderer.ordinaryRoadRenderer() );
    result.memoryChanged( m );
    return result;
  }


  public static Layer createNodeLayer( Memory m ) {
    final MemoryLayer result = new MemoryLayer( "Nodes" );
    result.addType( RescueConstants.TYPE_NODE );
    result.addRenderer( Node.class, NodeRenderer.ordinaryNodeRenderer() );
    result.memoryChanged( m );
    return result;
  }


  public static Layer createBuildingLayer( Memory m ) {
    final MemoryLayer result = new MemoryLayer( "Buildings" );
    result.addType( RescueConstants.TYPE_BUILDING );
    result.addType( RescueConstants.TYPE_REFUGE );
    result.addType( RescueConstants.TYPE_FIRE_STATION );
    result.addType( RescueConstants.TYPE_POLICE_OFFICE );
    result.addType( RescueConstants.TYPE_AMBULANCE_CENTER );
    result.addRenderer( Building.class,
        BuildingRenderer.ordinaryBuildingRenderer() );
    result.memoryChanged( m );
    return result;
  }


  public static Layer createHumanoidLayer( Memory m ) {
    final MemoryLayer result = new MemoryLayer( "Humanoids" );
    result.addType( RescueConstants.TYPE_CIVILIAN );
    result.addType( RescueConstants.TYPE_FIRE_BRIGADE );
    result.addType( RescueConstants.TYPE_POLICE_FORCE );
    result.addType( RescueConstants.TYPE_AMBULANCE_TEAM );
    result.addRenderer( Humanoid.class,
        HumanoidRenderer.ordinaryHumanoidRenderer() );
    result.memoryChanged( m );
    return result;
  }


  @Deprecated
  public static Layer createLayer( Memory m, Object object, MapRenderer r,
      String name ) {
    return createLayer( object, r, name );
  }


  public static Layer createLayer( Object object, MapRenderer r, String name ) {
    final Layer result = new Layer( name );
    result.addRenderer( object.getClass(), r );
    result.setObject( object );
    return result;
  }


  @Deprecated
  public static Layer createLayer( Memory m, final Object[] objects,
      MapRenderer r, String name ) {
    return createLayer( objects, r, name );
  }


  public static Layer createLayer( final Object[] objects, MapRenderer r,
      String name ) {
    final Layer result = new Layer( name );
    for ( int i = 0; i < objects.length; ++i ) {
      result.addRenderer( objects[i].getClass(), r );
      result.addObject( objects[i] );
    }
    return result;
  }


  public static Layer createOverlayLayer( String name ) {
    Layer result = new Layer( name );
    result.addRenderer( Shape.class, new ShapeRenderer() );
    return result;
  }


  @Deprecated
  public static Layer createEmptyLayer( Memory m, String name ) {
    return createEmptyLayer( name );
  }


  @Deprecated
  public static Layer createEmptyLayer( String name ) {
    Layer result = new Layer( name );
    result.renderers = new HashMap();
    return result;
  }


  @Deprecated
  public Layer( Memory m, String name ) {
    this( name );
  }


  public Layer( String name ) {
    this.name = name;
    // dirty = true;
    // image = null;
    objects = new HashSet<Object>();
    shapes = new HashMap();
    // memory = m;
    renderers = new HashMap();
    // registerDefaultRenderers();
    enabled = true;
    // transparancy = 0;
  }


  public String getName() {
    return name;
  }


  public void setEnabled( boolean b ) {
    enabled = b;
  }


  public boolean isEnabled() {
    return enabled;
  }

  /*
   * public void setTransparancy(double d) { transparancy = d; } public double
   * getTransparancy() { return transparancy; }
   */


  public void memoryChanged( Memory m ) {
  }


  public void addObject( Object o ) {
    objects.add( o );
    // dirty();
  }


  public void addObjects( Object[] os ) {
    for ( int i = 0; i < os.length; ++i )
      objects.add( os[i] );
    // dirty();
  }


  public void addObjects( Collection os ) {
    objects.addAll( os );
    // dirty();
  }


  public void setObject( Object o ) {
    removeAllObjects();
    addObject( o );
  }


  public void setObjects( Object[] os ) {
    removeAllObjects();
    addObjects( os );
  }


  public void setObjects( Collection os ) {
    removeAllObjects();
    addObjects( os );
  }


  public void removeObject( Object o ) {
    objects.remove( o );
    // dirty();
  }


  public void removeObjects( Object[] os ) {
    for ( int i = 0; i < os.length; ++i )
      objects.remove( os[i] );
  }


  public void removeObjects( Collection os ) {
    objects.removeAll( os );
  }


  public void removeAllObjects() {
    objects.clear();
    // dirty();
  }


  public void addRenderer( Class clazz, MapRenderer renderer ) {
    renderers.put( clazz, renderer );
  }


  public void removeRenderer( Class clazz ) {
    renderers.remove( clazz );
  }


  public void paint( Graphics g, int width, int height,
      ScreenTransform transform, Memory m ) {
    // if (dirty || image==null) {
    // image = null;
    // if (image==null || image.getWidth()!=width || image.getHeight()!=height)
    // image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

    // paint(image.createGraphics(),transform);
    // dirty = false;
    // }
    // g.drawImage(image,0,0,null);
    paint( (Graphics2D) g, transform, m );
  }

  // public void dirty() {
  // dirty = true;
  // }


  public Object[] getObjectsAtPoint( Point p ) {
    Collection result = new ArrayList();
    // System.out.println("Getting objects at point "+p);
    for ( Iterator it = objects.iterator(); it.hasNext(); ) {
      Object next = it.next();
      Shape s = (Shape) shapes.get( next );
      // System.out.println("Next object: "+next+", shape: "+s);
      if ( s != null && s.contains( p.x, p.y ) ) {
        result.add( next );
        // System.out.println("Added");
      }
    }
    return result.toArray();
  }


  public Object[] getObjectsInArea( Rectangle2D r ) {
    Collection result = new ArrayList();
    for ( Iterator it = objects.iterator(); it.hasNext(); ) {
      Object next = it.next();
      Shape s = (Shape) shapes.get( next );
      if ( s != null && s.intersects( r ) ) {
        result.add( next );
      }
    }
    return result.toArray();
  }


  private void paint( Graphics2D graphics, ScreenTransform transform,
      Memory memory ) {
    RenderTools.setLineMode( graphics, ViewConstants.LINE_MODE_SOLID,
        Color.black, 1 );
    RenderTools.setFillMode( graphics, ViewConstants.FILL_MODE_SOLID,
        Color.black );
    for ( Iterator it = objects.iterator(); it.hasNext(); ) {
      Object next = it.next();
      if ( next == null ) continue;
      Class clazz = next.getClass();
      MapRenderer renderer = getRenderer( clazz );
      // Copy the graphics context to protect it from irreversible changes
      Graphics g = graphics.create();
      if ( renderer != null && renderer.canRender( next ) ) {
        try {
          shapes.put( next, renderer.render( next, memory, g, transform ) );
        } catch ( CannotFindLocationException e ) {
          System.out.println( e );
        }
      }
    }
    // System.out.println("Layer "+name+" finished painting");
  }


  private MapRenderer getRenderer( Class clazz ) {
    if ( clazz == null ) return null;
    MapRenderer result = (MapRenderer) renderers.get( clazz );
    if ( result == null ) {
      Class[] interfaces = clazz.getInterfaces();
      for ( int i = 0; i < interfaces.length; ++i ) {
        result = getRenderer( interfaces[i] );
        if ( result != null ) return result;
      }
      return getRenderer( clazz.getSuperclass() );
    }
    return result;
  }


  public void registerDefaultRenderers() {
    renderers.put( Road.class, RoadRenderer.ordinaryRoadRenderer() );
    renderers.put( Node.class, NodeRenderer.ordinaryNodeRenderer() );
    renderers.put( Building.class,
        BuildingRenderer.ordinaryBuildingRenderer() );
    renderers.put( Humanoid.class,
        HumanoidRenderer.ordinaryHumanoidRenderer() );
    renderers.put( ConvexHull.class, ConvexHullRenderer.RED );
    renderers.put( Text.class, new TextRenderer() );
  }


  private static class ShapeRenderer implements MapRenderer {

    public boolean canRender( Object o ) {
      return o instanceof Shape;
    }


    public Shape render( Object o, Memory memory, Graphics g,
        ScreenTransform transform ) throws CannotFindLocationException {
      RenderTools.setLineMode( g, ViewConstants.LINE_MODE_DASH, Color.CYAN, 1 );
      RenderTools.setFillMode( g, ViewConstants.FILL_MODE_SOLID,
          new Color( Color.CYAN.getRed(), Color.CYAN.getGreen(),
              Color.CYAN.getBlue(), 128 ) );
      Shape s = (Shape) o;
      ( (Graphics2D) g ).fill( s );
      return s;
    }
  }
}
