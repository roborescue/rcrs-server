package maps.gml.view;

import maps.gml.GMLBuilding;
import maps.gml.GMLEdge;
import maps.gml.GMLNode;
import maps.gml.GMLRoad;
import maps.gml.GMLSpace;
import maps.gml.GMLRefuge;
import rescuecore2.misc.gui.ScreenTransform;
import java.awt.Graphics2D;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Draws an overlay consisting of Decorators.
 */
public class DecoratorOverlay implements Overlay {

  private transient Map<GMLNode, NodeDecorator>         nodeDecorators;
  private transient Map<GMLEdge, EdgeDecorator>         edgeDecorators;
  private transient Map<GMLBuilding, BuildingDecorator> buildingDecorators;
  private transient Map<GMLRoad, RoadDecorator>         roadDecorators;
  private transient Map<GMLSpace, SpaceDecorator>       spaceDecorators;


  /**
   * Construct a DecoratorOverlay.
   */
  public DecoratorOverlay() {
    nodeDecorators = new HashMap<GMLNode, NodeDecorator>();
    edgeDecorators = new HashMap<GMLEdge, EdgeDecorator>();
    buildingDecorators = new HashMap<GMLBuilding, BuildingDecorator>();
    roadDecorators = new HashMap<GMLRoad, RoadDecorator>();
    spaceDecorators = new HashMap<GMLSpace, SpaceDecorator>();
  }


  /**
   * Set the NodeDecorator for a set of GMLNodes.
   *
   * @param decorator
   *          The decorator to set.
   * @param nodes
   *          The nodes to set the decorator for.
   */
  public void setNodeDecorator( NodeDecorator decorator, GMLNode... nodes ) {
    setNodeDecorator( decorator, Arrays.asList( nodes ) );
  }


  /**
   * Set the NodeDecorator for a set of GMLNodes.
   *
   * @param decorator
   *          The decorator to set.
   * @param nodes
   *          The nodes to set the decorator for.
   */
  public void setNodeDecorator( NodeDecorator decorator,
      Collection<? extends GMLNode> nodes ) {
    for ( GMLNode next : nodes ) {
      nodeDecorators.put( next, decorator );
    }
  }


  /**
   * Get the NodeDecorator for a GMLNodes.
   *
   * @param node
   *          The node to look up.
   * @return The NodeDecorator for that node. This will be null if no custom
   *         decorator has been set for that node.
   */
  public NodeDecorator getNodeDecorator( GMLNode node ) {
    NodeDecorator result = nodeDecorators.get( node );
    return result;
  }


  /**
   * Remove any custom NodeDecorator for a set of GMLNodes.
   *
   * @param nodes
   *          The nodes to remove any custom decorator for.
   */
  public void clearNodeDecorator( GMLNode... nodes ) {
    clearNodeDecorator( Arrays.asList( nodes ) );
  }


  /**
   * Remove any custom NodeDecorator for a set of GMLNodes.
   *
   * @param nodes
   *          The nodes to remove any custom decorator for.
   */
  public void clearNodeDecorator( Collection<? extends GMLNode> nodes ) {
    for ( GMLNode next : nodes ) {
      nodeDecorators.remove( next );
    }
  }


  /**
   * Remove any custom NodeDecorators.
   */
  public void clearAllNodeDecorators() {
    nodeDecorators.clear();
  }


  /**
   * Set the EdgeDecorator for a set of GMLEdges.
   *
   * @param decorator
   *          The decorator to set.
   * @param edges
   *          The edges to set the decorator for.
   */
  public void setEdgeDecorator( EdgeDecorator decorator, GMLEdge... edges ) {
    setEdgeDecorator( decorator, Arrays.asList( edges ) );
  }


  /**
   * Set the EdgeDecorator for a set of GMLEdges.
   *
   * @param decorator
   *          The decorator to set.
   * @param edges
   *          The edges to set the decorator for.
   */
  public void setEdgeDecorator( EdgeDecorator decorator,
      Collection<? extends GMLEdge> edges ) {
    for ( GMLEdge next : edges ) {
      edgeDecorators.put( next, decorator );
    }
  }


  /**
   * Get the EdgeDecorator for a GMLEdge.
   *
   * @param edge
   *          The edge to look up.
   * @return The EdgeDecorator for that edge. This will be null if no custom
   *         decorator has been set for that edge.
   */
  public EdgeDecorator getEdgeDecorator( GMLEdge edge ) {
    EdgeDecorator result = edgeDecorators.get( edge );
    return result;
  }


  /**
   * Remove any custom EdgeDecorator for a set of GMLEdges.
   *
   * @param edges
   *          The edges to remove any custom decorator for.
   */
  public void clearEdgeDecorator( GMLEdge... edges ) {
    clearEdgeDecorator( Arrays.asList( edges ) );
  }


  /**
   * Remove any custom EdgeDecorator for a set of GMLEdges.
   *
   * @param edges
   *          The edges to remove any custom decorator for.
   */
  public void clearEdgeDecorator( Collection<? extends GMLEdge> edges ) {
    for ( GMLEdge next : edges ) {
      edgeDecorators.remove( next );
    }
  }


  /**
   * Remove any custom EdgeDecorators.
   */
  public void clearAllEdgeDecorators() {
    edgeDecorators.clear();
  }


  /**
   * Set the BuildingDecorator for a set of GMLBuildings.
   *
   * @param decorator
   *          The decorator to set.
   * @param buildings
   *          The buildings to set the decorator for.
   */
  public void setBuildingDecorator( BuildingDecorator decorator,
      GMLBuilding... buildings ) {
    setBuildingDecorator( decorator, Arrays.asList( buildings ) );
  }


  /**
   * Set the BuildingDecorator for a set of GMLBuildings.
   *
   * @param decorator
   *          The decorator to set.
   * @param buildings
   *          The buildings to set the decorator for.
   */
  public void setBuildingDecorator( BuildingDecorator decorator,
      Collection<? extends GMLBuilding> buildings ) {
    for ( GMLBuilding next : buildings ) {
      buildingDecorators.put( next, decorator );
    }
  }


  /**
   * Get the BuildingDecorator for a GMLBuildings.
   *
   * @param building
   *          The building to look up.
   * @return The BuildingDecorator for that building. This will be null if no
   *         custom decorator has been set for that building.
   */
  public BuildingDecorator getBuildingDecorator( GMLBuilding building ) {
    BuildingDecorator result = buildingDecorators.get( building );
    return result;
  }


  /**
   * Remove any custom BuildingDecorator for a set of GMLBuildings.
   *
   * @param buildings
   *          The buildings to remove any custom decorator for.
   */
  public void clearBuildingDecorator( GMLBuilding... buildings ) {
    clearBuildingDecorator( Arrays.asList( buildings ) );
  }


  /**
   * Remove any custom BuildingDecorator for a set of GMLBuildings.
   *
   * @param buildings
   *          The buildings to remove any custom decorator for.
   */
  public void
      clearBuildingDecorator( Collection<? extends GMLBuilding> buildings ) {
    for ( GMLBuilding next : buildings ) {
      buildingDecorators.remove( next );
    }
  }


  /**
   * Remove any custom BuildingDecorators.
   */
  public void clearAllBuildingDecorators() {
    buildingDecorators.clear();
  }


  /**
   * Set the RoadDecorator for a set of GMLRoads.
   *
   * @param decorator
   *          The decorator to set.
   * @param roads
   *          The roads to set the decorator for.
   */
  public void setRoadDecorator( RoadDecorator decorator, GMLRoad... roads ) {
    setRoadDecorator( decorator, Arrays.asList( roads ) );
  }


  /**
   * Set the RoadDecorator for a set of GMLRoads.
   *
   * @param decorator
   *          The decorator to set.
   * @param roads
   *          The roads to set the decorator for.
   */
  public void setRoadDecorator( RoadDecorator decorator,
      Collection<? extends GMLRoad> roads ) {
    for ( GMLRoad next : roads ) {
      roadDecorators.put( next, decorator );
    }
  }


  /**
   * Get the RoadDecorator for a GMLRoads.
   *
   * @param road
   *          The road to look up.
   * @return The RoadDecorator for that road. Will return null if no custom
   *         decorator has been set for that road.
   */
  public RoadDecorator getRoadDecorator( GMLRoad road ) {
    RoadDecorator result = roadDecorators.get( road );
    return result;
  }


  /**
   * Remove any custom RoadDecorator for a set of GMLRoads.
   *
   * @param roads
   *          The roads to remove any custom decorator for.
   */
  public void clearRoadDecorator( GMLRoad... roads ) {
    clearRoadDecorator( Arrays.asList( roads ) );
  }


  /**
   * Remove any custom RoadDecorator for a set of GMLRoads.
   *
   * @param roads
   *          The roads to remove any custom decorator for.
   */
  public void clearRoadDecorator( Collection<? extends GMLRoad> roads ) {
    for ( GMLRoad next : roads ) {
      roadDecorators.remove( next );
    }
  }


  /**
   * Remove any custom RoadDecorators.
   */
  public void clearAllRoadDecorators() {
    roadDecorators.clear();
  }


  /**
   * Set the SpaceDecorator for a set of GMLSpaces.
   *
   * @param decorator
   *          The decorator to set.
   * @param spaces
   *          The spaces to set the decorator for.
   */
  public void setSpaceDecorator( SpaceDecorator decorator,
      GMLSpace... spaces ) {
    setSpaceDecorator( decorator, Arrays.asList( spaces ) );
  }


  /**
   * Set the SpaceDecorator for a set of GMLSpaces.
   *
   * @param decorator
   *          The decorator to set.
   * @param spaces
   *          The spaces to set the decorator for.
   */
  public void setSpaceDecorator( SpaceDecorator decorator,
      Collection<? extends GMLSpace> spaces ) {
    for ( GMLSpace next : spaces ) {
      spaceDecorators.put( next, decorator );
    }
  }


  /**
   * Get the SpaceDecorator for a GMLSpaces.
   *
   * @param space
   *          The space to look up.
   * @return The SpaceDecorator for that space. This will be null if no custom
   *         decorator has been set for that space.
   */
  public SpaceDecorator getSpaceDecorator( GMLSpace space ) {
    SpaceDecorator result = spaceDecorators.get( space );
    return result;
  }


  /**
   * Remove any custom SpaceDecorator for a set of GMLSpaces.
   *
   * @param spaces
   *          The spaces to remove any custom decorator for.
   */
  public void clearSpaceDecorator( GMLSpace... spaces ) {
    clearSpaceDecorator( Arrays.asList( spaces ) );
  }


  /**
   * Remove any custom SpaceDecorator for a set of GMLSpaces.
   *
   * @param spaces
   *          The spaces to remove any custom decorator for.
   */
  public void clearSpaceDecorator( Collection<? extends GMLSpace> spaces ) {
    for ( GMLSpace next : spaces ) {
      spaceDecorators.remove( next );
    }
  }


  /**
   * Remove any custom SpaceDecorators.
   */
  public void clearAllSpaceDecorators() {
    spaceDecorators.clear();
  }


  /**
   * Remove all types of Decorators.
   */
  public void clearAllDecorators() {
    clearAllBuildingDecorators();
    clearAllRoadDecorators();
    clearAllSpaceDecorators();
    clearAllEdgeDecorators();
    clearAllNodeDecorators();
  }


  @Override
  public void render( Graphics2D g, ScreenTransform transform ) {
    for ( Entry<GMLRoad, RoadDecorator> e : roadDecorators.entrySet() ) {
      e.getValue().decorate( e.getKey(), (Graphics2D) g.create(), transform );
    }
    for ( Entry<GMLBuilding, BuildingDecorator> e : buildingDecorators
        .entrySet() ) {
      e.getValue().decorate( e.getKey(), (Graphics2D) g.create(), transform );

      if ( e.getKey() instanceof GMLRefuge ) {
        int x = transform.xToScreen( e.getKey().getCentreX() );
        int y = transform.yToScreen( e.getKey().getCentreY() );
        Graphics2D oldg = g;
        g.setColor( new Color( 0, 0, 0 ) );
        g.setFont( new Font( g.getFont().getName(), Font.BOLD,
            g.getFont().getSize() ) );
        g.drawString(
            String
                .valueOf( "C=" + ( (GMLRefuge) e.getKey() ).getBedCapacity() ),
            x - 20, y );
        // g.drawString(String.valueOf("R " +
        // ((GMLRefuge)e.getKey()).getRefillCapacity()), x - 10, y + 10);
        g = oldg;
      }
    }
    for ( Entry<GMLSpace, SpaceDecorator> e : spaceDecorators.entrySet() ) {
      e.getValue().decorate( e.getKey(), (Graphics2D) g.create(), transform );
    }
    for ( Entry<GMLEdge, EdgeDecorator> e : edgeDecorators.entrySet() ) {
      e.getValue().decorate( e.getKey(), (Graphics2D) g.create(), transform );
    }
    for ( Entry<GMLNode, NodeDecorator> e : nodeDecorators.entrySet() ) {
      e.getValue().decorate( e.getKey(), (Graphics2D) g.create(), transform );
    }
  }
}
