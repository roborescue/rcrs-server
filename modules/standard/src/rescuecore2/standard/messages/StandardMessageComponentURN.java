package rescuecore2.standard.messages;

import static rescuecore2.standard.Constants.STANDARD_MSG_COMPONENT_URN_PREFIX;

import rescuecore2.URN;;

public enum StandardMessageComponentURN implements URN{
	Target(STANDARD_MSG_COMPONENT_URN_PREFIX | 1, "Target"),
	DestinationX(STANDARD_MSG_COMPONENT_URN_PREFIX | 2, "Destination X"),
	DestinationY(STANDARD_MSG_COMPONENT_URN_PREFIX | 3, "Destination Y"),
	Water(STANDARD_MSG_COMPONENT_URN_PREFIX | 4, "Water"),
	Path(STANDARD_MSG_COMPONENT_URN_PREFIX | 5, "Path"),
	Message(STANDARD_MSG_COMPONENT_URN_PREFIX |6 , "Message"),
	Channel(STANDARD_MSG_COMPONENT_URN_PREFIX | 7, "Channel"),
	Channels(STANDARD_MSG_COMPONENT_URN_PREFIX | 8, "Channels")
	;
	

	private String stringUrn;
	private int urn;

	StandardMessageComponentURN(int urn, String stringUrn) {
		this.urn = urn;
		this.stringUrn = stringUrn;

	}

	@Override
	public int getUrn() {
		return urn;
	}
	
	public String getStringUrn() {
		return stringUrn;
	}
}