package rescuecore2.messages.control;

/**
 * URNs for control messages.
 */
public enum ControlMessageURN {

  /** Kernel-GIS connect. */
  KG_CONNECT( "message:kg_connect" ),
  /** Kernel-GIS acknowledge. */
  KG_ACKNOWLEDGE( "message:kg_acknowledge" ),
  /** GIS-Kernel OK. */
  GK_CONNECT_OK( "message:gk_connect_ok" ),
  /** GIS-Kernel error. */
  GK_CONNECT_ERROR( "message:gk_connect_error" ),

  /** Simulator-Kernel connect. */
  SK_CONNECT( "message:sk_connect" ),
  /** Simulator-Kernel acknowledge. */
  SK_ACKNOWLEDGE( "message:sk_acknowledge" ),
  /** Simulator-Kernel update. */
  SK_UPDATE( "message:sk_update" ),
  /** Kernel-Simulator OK. */
  KS_CONNECT_OK( "message:ks_connect_ok" ),
  /** Kernel-Simulator error. */
  KS_CONNECT_ERROR( "message:ks_connect_error" ),
  /** Kernel update broadcast. */
  KS_UPDATE( "message:ks_update" ),
  /** Kernel commands broadcast. */
  KS_COMMANDS( "message:ks_commands" ),
  /** Kernel commands aftershocks info. */
  KS_AFTERSHOCK_INFO( "message:ks_aftershock_info" ),

  /** Viewer-Kernel connect. */
  VK_CONNECT( "message:vk_connect" ),
  /** Viewer-Kernel acknowledge. */
  VK_ACKNOWLEDGE( "message:vk_acknowledge" ),
  /** Kernel-Viewer OK. */
  KV_CONNECT_OK( "message:kv_connect_ok" ),
  /** Kernel-Viewer error. */
  KV_CONNECT_ERROR( "message:kv_connect_error" ),
  /** Kernel-Viewer timestep. */
  KV_TIMESTEP( "message:kv_timestep" ),

  /** Agent-Kernel connect. */
  AK_CONNECT( "message:ak_connect" ),
  /** Agent-Kernel acknowledge. */
  AK_ACKNOWLEDGE( "message:ak_acknowledge" ),
  /** Agent-Kernel command. */
  AK_COMMAND( "message:ak_command" ),
  /** Kernel-Agent OK. */
  KA_CONNECT_OK( "message:ka_connect_ok" ),
  /** Kernel-Agent error. */
  KA_CONNECT_ERROR( "message:ka_connect_error" ),
  /** Kernel-Agent perception update. */
  KA_SENSE( "message:ka_sense" ),

  /** Shutdown message. */
  SHUTDOWN( "message:shutdown" ),

  /** New EntityID request. */
  ENTITY_ID_REQUEST( "message:entity_id_request" ),

  /** New EntityID response. */
  ENTITY_ID_RESPONSE( "message:entity_id_response" );


  private String urn;


  private ControlMessageURN( String urn ) {
    this.urn = urn;
  }


  @Override
  public String toString() {
    return urn;
  }


  /**
   * Convert a String to a ControlMessageURN.
   *
   * @param s
   *          The String to convert.
   * @return A ConotrlMessageURN.
   */
  public static ControlMessageURN fromString( String s ) {
    for ( ControlMessageURN next : ControlMessageURN.values() ) {
      if ( next.urn.equals( s ) ) {
        return next;
      }
    }
    throw new IllegalArgumentException( s );
  }
}