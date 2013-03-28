package firesimulator.world;

public class Hydrant extends StationaryObject {

	public Hydrant(int id) {
		super(id);
	}

	@Override
	public String getType() {
		return "HYDRANT";
	}

}
