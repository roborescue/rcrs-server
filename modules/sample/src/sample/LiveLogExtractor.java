package sample;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import rescuecore2.Timestep;
import rescuecore2.Constants;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.view.AnimatedWorldModelViewer;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;

/**
 * A simple viewer.
 */
public class LiveLogExtractor extends StandardViewer {

  private static final int    DEFAULT_FONT_SIZE = 20;
  private static final int    PRECISION         = 3;

  private static final String FONT_SIZE_KEY     = "viewer.font-size";
  private static final String MAXIMISE_KEY      = "viewer.maximise";
  private static final String TEAM_NAME_KEY     = "viewer.team-name";
  private static final String OUTDIR_KEY        = "viewer.output-dir";

  private ScoreFunction       scoreFunction;
  private ViewComponent       viewer;
  private ViewComponent       imageViewer;
  private JLabel              timeLabel;
  private JLabel              scoreLabel;
  private JLabel              teamLabel;
  private JLabel              mapLabel;
  private NumberFormat        format;

  private String              outdir;


  @Override
  protected void postConnect() {
    super.postConnect();
    int fontSize = config.getIntValue( FONT_SIZE_KEY, DEFAULT_FONT_SIZE );
    String teamName = config.getValue( TEAM_NAME_KEY, "" );
    outdir = config.getValue( OUTDIR_KEY, "." );
    scoreFunction = makeScoreFunction();
    format = NumberFormat.getInstance();
    format.setMaximumFractionDigits( PRECISION );
    JFrame frame = new JFrame( "Viewer " + getViewerID() + " ("
        + model.getAllEntities().size() + " entities)" );
    viewer = new AnimatedWorldModelViewer();
    viewer.initialise( config );
    viewer.view( model );
    imageViewer = new AnimatedWorldModelViewer();
    imageViewer.initialise( config );
    imageViewer.view( model );
    viewer.setPreferredSize( new Dimension( 500, 500 ) );
    imageViewer.setBounds( 0, 0, 1024, 786 );
    timeLabel = new JLabel( "Time: Not started", JLabel.CENTER );
    teamLabel = new JLabel( teamName, JLabel.CENTER );
    scoreLabel = new JLabel( "Score: Unknown", JLabel.CENTER );
    String mapdir = config.getValue( "gis.map.dir" ).trim();

    String[] map_spl = mapdir.split( "/" );
    int index = map_spl.length - 1;
    String mapname = map_spl[index].trim();
    if ( mapname.equals( "" ) ) mapname = map_spl[--index].trim();
    if ( mapname.equals( "map" ) ) mapname = map_spl[--index].trim();

    String totalTime = config.getValue( "kernel.timesteps" );
    int channelCount = config.getIntValue( "comms.channels.count" ) - 1;// -1
                                                                        // for
                                                                        // say

    mapLabel = new JLabel(
        mapname + " (" + totalTime + ") | "
            + ( channelCount == 0 ? "No Comm" : channelCount + " channels" ),
        JLabel.CENTER );
    timeLabel.setBackground( Color.WHITE );
    timeLabel.setOpaque( true );
    timeLabel.setFont( timeLabel.getFont().deriveFont( Font.PLAIN, fontSize ) );
    teamLabel.setBackground( Color.WHITE );
    teamLabel.setOpaque( true );
    teamLabel.setFont( timeLabel.getFont().deriveFont( Font.PLAIN, fontSize ) );
    scoreLabel.setBackground( Color.WHITE );
    scoreLabel.setOpaque( true );
    scoreLabel
        .setFont( timeLabel.getFont().deriveFont( Font.PLAIN, fontSize ) );

    mapLabel.setBackground( Color.WHITE );
    mapLabel.setOpaque( true );
    mapLabel.setFont( timeLabel.getFont().deriveFont( Font.PLAIN, fontSize ) );

    frame.add( viewer, BorderLayout.CENTER );
    JPanel labels = new JPanel( new GridLayout( 1, 4 ) );
    labels.add( teamLabel );
    labels.add( timeLabel );
    labels.add( scoreLabel );
    labels.add( mapLabel );
    frame.add( labels, BorderLayout.NORTH );
    frame.pack();
    if ( config.getBooleanValue( MAXIMISE_KEY, false ) ) {
      frame.setExtendedState( JFrame.MAXIMIZED_BOTH );
    }
    frame.setVisible( true );

    double score = scoreFunction.score( model, new Timestep( 0 ) );
    writeFile( outdir + "/init-score.txt", String.valueOf( score ) );
    writeFile( outdir + "/scores.txt", String.valueOf( score ) );
    scoreLabel.setText( "Score: " + format.format( score ) );

    viewer.addViewListener( new ViewListener() {

      @Override
      public void objectsClicked( ViewComponent view,
          List<RenderedObject> objects ) {
        for ( RenderedObject next : objects ) {
          System.out.println( next.getObject() );
        }
      }


      @Override
      public void objectsRollover( ViewComponent view,
          List<RenderedObject> objects ) {
      }
    } );
  }


  @Override
  protected void handleTimestep( final KVTimestep t ) {
    super.handleTimestep( t );
    SwingUtilities.invokeLater( new Runnable() {

      public void run() {
        timeLabel.setText( "Time: " + t.getTime() );
        double score = scoreFunction.score( model,
            new Timestep( t.getTime() ) );
        scoreLabel.setText( "Score: " + format.format( score ) );
        viewer.view( model, t.getCommands() );
        viewer.repaint();

        if ( t.getTime() == 1 ) {
          writeImage( outdir + "/snapshot-init.png" );
        } else if ( t.getTime() % 50 == 0 ) {
          writeImage( outdir + "/snapshot-" + t.getTime() + ".png" );
        }
        appendFile( outdir + "/scores.txt", " " + String.valueOf( score ) );
        if ( t.getTime() == Integer
            .parseInt( config.getValue( "kernel.timesteps" ) ) ) {
          writeImage( outdir + "/snapshot-final.png" );
          writeFile( outdir + "/final-score.txt", String.valueOf( score ) );
          try {
            Thread.sleep( 100000 );
          } catch ( Exception e ) {
            e.printStackTrace();
          }
          System.exit( 0 );
        }

      }
    } );
  }


  @Override
  public String toString() {
    return "Sample viewer";
  }


  private static void writeFile( String filename, String content ) {
    try {
      PrintWriter out = new PrintWriter(
          new BufferedWriter( new FileWriter( filename ) ) );
      out.print( content );
      out.close();
    } catch ( IOException e ) {
      System.out.println( "Error writing file: " + e.getMessage() );
    }

  }


  private static void appendFile( String filename, String content ) {
    try {
      PrintWriter out = new PrintWriter(
          new BufferedWriter( new FileWriter( filename, true ) ) );
      out.print( content );
      out.close();
    } catch ( IOException e ) {
      System.out.println( "Error writing file: " + e.getMessage() );
    }

  }


  public void writeImage( String filename ) {
    BufferedImage bi = paintImage();
    File outfile = new File( filename );
    try {
      ImageIO.write( bi, "png", outfile );
    } catch ( IOException e ) {
      System.out.println( "Error writing image: " + e.getMessage() );
    }
  }


  public BufferedImage paintImage() {
    imageViewer.view( model, null, null );
    // Create the image
    GraphicsConfiguration configuration = GraphicsEnvironment
        .getLocalGraphicsEnvironment().getDefaultScreenDevice()
        .getDefaultConfiguration();
    BufferedImage image = configuration.createCompatibleImage(
        imageViewer.getWidth(), imageViewer.getHeight(),
        Transparency.TRANSLUCENT );

    // Render the component onto the image
    Graphics graphics = image.createGraphics();
    imageViewer.paint( graphics );
    graphics.dispose();
    return image;
  }


  private ScoreFunction makeScoreFunction() {
    String className = config.getValue( Constants.SCORE_FUNCTION_KEY );
    ScoreFunction result = instantiate( className, ScoreFunction.class );
    result.initialise( model, config );
    return result;
  }
}