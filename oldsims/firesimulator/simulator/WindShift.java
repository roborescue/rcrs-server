package firesimulator.simulator;

/**
 * @author Timo N�ssle
 *
 */
public class WindShift {

  float   speed;

  float   direction;

  float   directionDg;

  int[][] grid    = new int[4][2];

  float[] weights = new float[4];


  public WindShift( float direction, float speed, int gridSize ) {
    this.speed = speed % gridSize;
    directionDg = direction;
    direction = direction % 360;
    direction = (float) (direction / (360 / (2 * Math.PI)));
    this.direction = direction;
    float v_y = -((float) Math.cos( direction ) * speed);
    float v_x = -((float) Math.sin( direction ) * speed);
    float[][] points = new float[4][2];
    points[0][0] = v_x;
    points[0][1] = v_y;
    points[1][0] = v_x + gridSize;
    points[1][1] = v_y;
    points[2][0] = v_x + gridSize;
    points[2][1] = v_y + gridSize;
    points[3][0] = v_x;
    points[3][1] = v_y + gridSize;
    float areaTotal = gridSize * gridSize;
    for ( int c = 0; c < 4; c++ ) {
      float tx = grid[c][0] * gridSize;
      float ty = grid[c][1] * gridSize;
      float sx = points[0][0];
      float sy = points[0][1];
      float dx = gridSize - Math.abs( sx - tx );
      float dy = gridSize - Math.abs( sy - ty );
      float weight = (dx * dy) / areaTotal;
      weights[c] = weight;
    }
  }


  public float getDirection() {
    return directionDg;
  }


  public double[][] shift( double[][] source, Simulator sim ) {
    if ( speed == 0 )
      return source;
    double[][] result = new double[source.length][source[0].length];
    for ( int x = 0; x < source.length; x++ )
      for ( int y = 0; y < source[0].length; y++ ) {
        float temp = 0;
        for ( int c = 0; c < 4; c++ ) {
          temp += sim.getTempAt( x - grid[c][0], y - grid[c][1] ) * weights[c];
          System.out.println( weights[c] );
        }
        result[x][y] = temp;
      }
    return result;
  }
}