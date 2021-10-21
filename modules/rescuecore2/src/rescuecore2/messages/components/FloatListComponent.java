package rescuecore2.messages.components;


import static rescuecore2.misc.EncodingTools.readFloat32;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeFloat32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.FloatListProto;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;

/**
 * A message component that is a list of floats.
 */
public class FloatListComponent extends AbstractMessageComponent {
	private List<Float> data;

	/**
	 * Construct an FloatListComponent with no data.
	 * 
	 * @param name
	 *            The name of the component.
	 */
	public FloatListComponent(URN name) {
		super(name);
		data = new ArrayList<Float>();
	}

	/**
	 * Construct an FloatListComponent with a list of floats.
	 * 
	 * @param name
	 *            The name of the component.
	 * @param data
	 *            The data.
	 */
	public FloatListComponent(URN name, List<Float> data) {
		super(name);
		this.data = new ArrayList<Float>(data);
	}

	/**
	 * Get the list of Floats in this component.
	 * 
	 * @return An immutable view of the list of Floats.
	 */
	public List<Float> getValues() {
		return Collections.unmodifiableList(data);
	}

	/**
	 * Set the list of values in this component.
	 * 
	 * @param newData
	 *            The new set of values.
	 */
	public void setValues(List<Float> newData) {
		this.data = new ArrayList<Float>(newData);
	}

	/**
	 * Set the list of values in this component.
	 * 
	 * @param newData
	 *            The new set of values.
	 */
	public void setValues(float... newData) {
		this.data = new ArrayList<Float>();
		for (float i : newData) {
			data.add(i);
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		writeInt32(data.size(), out);
		for (Float next : data) {
			writeFloat32(next.floatValue(), out);
		}
	}

	@Override
	public void read(InputStream in) throws IOException {
		data.clear();
		int count = readInt32(in);
		for (int i = 0; i < count; ++i) {
			data.add(readFloat32(in));
		}
	}

	@Override
	public String toString() {
		return getName() + " = " + data.toString();
	}
	
	@Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
		data.clear();
		for (Float val : proto.getFloatList().getValuesList()) {
			data.add(val);			
        }
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		FloatListProto.Builder builder=FloatListProto.newBuilder();
		for (float next : data) {
            builder.addValues(next);
        }
		return MessageComponentProto.newBuilder().setFloatList(builder).build();
	}
	
	//
//	public static void main(String[] args) throws IOException {
//		System.out.println("Test starts...");
//		FloatListComponent flc = new FloatListComponent("test1");
//		flc.setValues(new float[] { (float) 1.2, (float) 4444.1,
//				(float) 12313.4112 });
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		flc.write(out);
//		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
//		FloatListComponent flc2 = new FloatListComponent("test2");
//		flc2.read(in);
//		List<Float> values1 = flc.getValues();
//		List<Float> values2 = flc2.getValues();
//		for (int i = 0; i < values1.size(); i++) {
//			float f1 = values1.get(i);
//			float f2 = values2.get(i);
//			System.out.println(f1 + " , " + f2 + " => " + (f1 == f2));
//		}
//		
//	}
}
