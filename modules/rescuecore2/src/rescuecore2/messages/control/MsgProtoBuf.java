package rescuecore2.messages.control;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rescuecore2.commands.Command;
import rescuecore2.config.Config;
import rescuecore2.messages.control.ControlMessageProto.CommandProto;
import rescuecore2.messages.control.ControlMessageProto.ConfigProto;
import rescuecore2.messages.control.ControlMessageProto.IntListProto;
import rescuecore2.messages.control.ControlMessageProto.IntMatrixProto;
import rescuecore2.messages.control.ControlMessageProto.PropertyProto;
import rescuecore2.messages.control.ControlMessageProto.StrListProto;
import rescuecore2.messages.control.ControlMessageProto.ValueProto;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.Property;

public class MsgProtoBuf {

  public static CommandProto setCommandProto( Command command ) {
    CommandProto.Builder commandProtoBuilder = CommandProto.newBuilder()
        .setUrn( command.getURN() );

    for ( String f : command.getFields().keySet() ) {
      ValueProto.Builder valueProtoBuilder = ValueProto.newBuilder();

      Object value = command.getFields().get( f );
      // Boolean
      if ( value instanceof Boolean ) {
        valueProtoBuilder.setValueBool( (boolean) value );
        // Double
      } else if ( value instanceof Double ) {
        valueProtoBuilder.setValueDouble( (double) value );
        // Integer
      } else if ( value instanceof Integer ) {
        valueProtoBuilder.setValueInt( (int) value );
        // Byte Array
      } else if ( value instanceof byte[] ) {
        valueProtoBuilder.setListByte( ByteString.copyFrom( (byte[]) value ) );
        // Integer Array
      } else if ( value instanceof int[] ) {
        int[] values = (int[]) value;

        IntListProto.Builder intListProtoBuilder = IntListProto.newBuilder();
        for ( int i = 0; i < values.length; i++ ) {
          intListProtoBuilder.addValues( values[i] );
        }
        valueProtoBuilder.setListInt( intListProtoBuilder.build() );
        // Integer Matrix
      } else if ( value instanceof int[][] ) {
        int[][] values = (int[][]) value;

        IntMatrixProto.Builder intMatrixProtoBuilder = IntMatrixProto
            .newBuilder();
        for ( int i = 0; i < values.length; i++ ) {
          IntListProto.Builder intListProtoBuilder = IntListProto.newBuilder();
          for ( int j = 0; j < values[i].length; j++ ) {
            intListProtoBuilder.addValues( values[i][j] );
          }
          intMatrixProtoBuilder.addValues( intListProtoBuilder.build() );
        }
        valueProtoBuilder.setMatrixInt( intMatrixProtoBuilder );
      }

      commandProtoBuilder.putFields( f, valueProtoBuilder.build() );
    }

    return commandProtoBuilder.build();
  }


  public static Map<String, Object>
      setCommandFields( CommandProto commandProto ) {
    Map<String, Object> fields = new HashMap<String, Object>();

    Map<String, ValueProto> fieldsMap = commandProto.getFieldsMap();
    for ( String field : fieldsMap.keySet() ) {
      Object value = null;

      ValueProto valueProto = fieldsMap.get( field );
      switch ( valueProto.getValueCase().getNumber() ) {
        case ValueProto.VALUEBOOL_FIELD_NUMBER:
          value = valueProto.getValueBool();
          break;
        case ValueProto.VALUEDOUBLE_FIELD_NUMBER:
          value = valueProto.getValueDouble();
          break;
        case ValueProto.VALUEINT_FIELD_NUMBER:
          value = valueProto.getValueInt();
          break;
        case ValueProto.LISTBYTE_FIELD_NUMBER:
          value = valueProto.getListByte().toByteArray();
          break;
        case ValueProto.LISTINT_FIELD_NUMBER:
          List<Integer> valuesList = valueProto.getListInt().getValuesList();
          int[] list = new int[valuesList.size()];
          for ( int i = 0; i < valuesList.size(); i++ ) {
            list[i] = (int) valuesList.get( i );
          }
          value = list;
          break;
        case ValueProto.MATRIXINT_FIELD_NUMBER:
          List<IntListProto> matrixList = valueProto.getMatrixInt()
              .getValuesList();

          int[][] matrix = new int[valueProto.getMatrixInt()
              .getValuesCount()][];
          for ( int i = 0; i < matrixList.size(); i++ ) {
            List<Integer> valuesMatrix = matrixList.get( i ).getValuesList();
            int[] column = new int[valuesMatrix.size()];
            for ( int j = 0; j < valuesMatrix.size(); j++ ) {
              column[j] = valuesMatrix.get( j );
            }
            matrix[i] = column;
          }
          value = matrix;
          break;
        default:
          value = null;
      }

      if ( value != null ) {
        fields.put( field, value );
      }
    }

    return fields;
  }


  public static PropertyProto setPropertyProto( Property<?> property ) {
    PropertyProto.Builder propertyProtoBuilder = PropertyProto.newBuilder()
        .setUrnID(
            StandardPropertyURN.fromString( property.getURN() ).ordinal() );

    for ( Object value : property.getFields() ) {
      ValueProto.Builder valueProtoBuilder = ValueProto.newBuilder();

      // Boolean
      if ( value instanceof Boolean ) {
        valueProtoBuilder.setValueBool( (boolean) value );
        // Double
      } else if ( value instanceof Double ) {
        valueProtoBuilder.setValueDouble( (double) value );
        // Integer
      } else if ( value instanceof Integer ) {
        valueProtoBuilder.setValueInt( (int) value );
        // Byte Array
      } else if ( value instanceof byte[] ) {
        valueProtoBuilder.setListByte( ByteString.copyFrom( (byte[]) value ) );
        // Integer Array
      } else if ( value instanceof int[] ) {
        int[] values = (int[]) value;

        IntListProto.Builder intListProtoBuilder = IntListProto.newBuilder();
        for ( int i = 0; i < values.length; i++ ) {
          intListProtoBuilder.addValues( values[i] );
        }
        valueProtoBuilder.setListInt( intListProtoBuilder.build() );
        // Integer Matrix
      } else if ( value instanceof int[][] ) {
        int[][] values = (int[][]) value;

        IntMatrixProto.Builder intMatrixProtoBuilder = IntMatrixProto
            .newBuilder();
        for ( int i = 0; i < values.length; i++ ) {
          IntListProto.Builder intListProtoBuilder = IntListProto.newBuilder();
          for ( int j = 0; j < values[i].length; j++ ) {
            intListProtoBuilder.addValues( values[i][j] );
          }
          intMatrixProtoBuilder.addValues( intListProtoBuilder.build() );
        }
        valueProtoBuilder.setMatrixInt( intMatrixProtoBuilder );
      }
      propertyProtoBuilder.addFields( valueProtoBuilder.build() );
    }

    return propertyProtoBuilder.build();
  }


  public static List<Object> setPropertyFields( PropertyProto propertyProto ) {
    List<Object> property = new ArrayList<Object>();

    for ( ValueProto valueProto : propertyProto.getFieldsList() ) {
      Object value = null;

      switch ( valueProto.getValueCase().getNumber() ) {
        case ValueProto.VALUEBOOL_FIELD_NUMBER:
          value = valueProto.getValueBool();
          break;
        case ValueProto.VALUEDOUBLE_FIELD_NUMBER:
          value = valueProto.getValueDouble();
          break;
        case ValueProto.VALUEINT_FIELD_NUMBER:
          value = valueProto.getValueInt();
          break;
        case ValueProto.LISTBYTE_FIELD_NUMBER:
          value = valueProto.getListByte().toByteArray();
          break;
        case ValueProto.LISTINT_FIELD_NUMBER:
          List<Integer> valuesList = valueProto.getListInt().getValuesList();
          int[] list = new int[valuesList.size()];
          for ( int i = 0; i < valuesList.size(); i++ ) {
            list[i] = (int) valuesList.get( i );
          }
          value = list;
          break;
        case ValueProto.MATRIXINT_FIELD_NUMBER:
          List<IntListProto> matrixList = valueProto.getMatrixInt()
              .getValuesList();

          int[][] matrix = new int[valueProto.getMatrixInt()
              .getValuesCount()][];
          for ( int i = 0; i < matrixList.size(); i++ ) {
            List<Integer> valuesMatrix = matrixList.get( i ).getValuesList();
            int[] column = new int[valuesMatrix.size()];
            for ( int j = 0; j < valuesMatrix.size(); j++ ) {
              column[j] = valuesMatrix.get( j );
            }
            matrix[i] = column;
          }
          value = matrix;
          break;
        default:
          value = null;
      }

      if ( value != null ) {
        property.add( value );
      }
    }

    return property;
  }


  public static ConfigProto setConfigProto( Config config ) {
    ConfigProto.Builder configBuilder = ConfigProto.newBuilder();

    // All keys
    for ( String key : config.getAllKeys() ) {
      configBuilder.putData( key, config.getValue( key ) );
    }

    // Integer keys
    for ( String key : config.getIntKeys() ) {
      configBuilder.putIntData( key, config.getIntValue( key ) );
    }

    // Float keys
    for ( String key : config.getFloatKeys() ) {
      configBuilder.putFloatData( key, config.getFloatValue( key ) );
    }

    // Boolean keys
    for ( String key : config.getBooleanKeys() ) {
      configBuilder.putBooleanData( key, config.getBooleanValue( key ) );
    }

    // Array keys
    for ( String key : config.getArrayKeys() ) {
      StrListProto.Builder stringListBuild = StrListProto.newBuilder();

      for ( String value : config.getArrayValue( key ) ) {
        stringListBuild.addValues( value );
      }

      configBuilder.putArrayData( key, stringListBuild.build() );
    }

    return configBuilder.build();
  }


  public static Config setConfig( ConfigProto configProto ) {
    Config config = new Config();

    // All keys
    Map<String, String> allKeys = configProto.getDataMap();
    for ( String key : allKeys.keySet() ) {
      config.setValue( key, allKeys.get( key ) );
    }

    // Integer keys
    Map<String, Integer> intKeys = configProto.getIntDataMap();
    for ( String key : intKeys.keySet() ) {
      config.setIntValue( key, intKeys.get( key ) );
    }

    // Float keys
    Map<String, Double> floatKeys = configProto.getFloatDataMap();
    for ( String key : floatKeys.keySet() ) {
      config.setFloatValue( key, floatKeys.get( key ) );
    }

    // Boolean keys
    Map<String, Boolean> booleanKeys = configProto.getBooleanDataMap();
    for ( String key : booleanKeys.keySet() ) {
      config.setBooleanValue( key, booleanKeys.get( key ) );
    }

    // Array keys
    Map<String, StrListProto> arrayKeys = configProto.getArrayDataMap();
    for ( String key : arrayKeys.keySet() ) {

      List<String> arrayList = new ArrayList<String>();

      StrListProto stringList = arrayKeys.get( key );
      for ( int i = 0; i < stringList.getValuesCount(); i++ ) {
        arrayList.add( stringList.getValues( i ) );
      }

      config.setArrayValue( key, arrayList );
    }

    return config;
  }
}