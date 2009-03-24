/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.2 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore.tools.simulationrunner;

import java.io.*;
import java.util.*;

public class Session {
	private String name;
	private String configFile, roadFile, nodeFile, buildingFile, gisFile, shindoFile, galFile;
	private RescueProcess[] simulatorComponents;

	public Session(String xmlFile) throws IOException, XMLDecodingException {
		XMLTag tag = XMLDocumentParser.parse(new BufferedReader(new FileReader(xmlFile)));
		XMLTag nameTag = tag.getChild("name",false);
		XMLTag configTag = tag.getChild("config");
		XMLTag roadTag = tag.getChild("road");
		XMLTag nodeTag = tag.getChild("node");
		XMLTag buildingTag = tag.getChild("building");
		XMLTag gisTag = tag.getChild("gis");
		XMLTag shindoTag = tag.getChild("shindo");
		XMLTag galTag = tag.getChild("gal");
		name = nameTag.getText();
		configFile = configTag==null?"config.txt":configTag.getText();
		roadFile = roadTag==null?"road.bin":roadTag.getText();
		nodeFile = nodeTag==null?"node.bin":nodeTag.getText();
		buildingFile = buildingTag==null?"building.bin":buildingTag.getText();
		gisFile = gisTag==null?"gis.ini":gisTag.getText();
		shindoFile = shindoTag==null?"shindopolydata.dat":shindoTag.getText();
		galFile = galTag==null?"galpolydata.dat":galTag.getText();
		XMLTag simulatorsTag = tag.getChild("simulators",false);
		List simulators = simulatorsTag.getChildren("simulator");
		simulatorComponents = new RescueProcess[simulators.size()];
		int i=0;
		for (Iterator it = simulators.iterator();it.hasNext();) {
			XMLTag next = (XMLTag)it.next();
			XMLTag nextName = next.getChild("name",false);
			XMLTag commandLine = next.getChild("commandline",false);
			XMLTag trigger = next.getChild("trigger");
			simulatorComponents[i++] = new RescueProcess(nextName.getText(),commandLine.getText(),trigger==null?null:trigger.getText(),null);
		}
	}

	public String getName() {
		return name;
	}

	public String getConfigFile() {
		return configFile;
	}

	public String getRoadFile() {
		return roadFile;
	}

	public String getNodeFile() {
		return nodeFile;
	}

	public String getBuildingFile() {
		return buildingFile;
	}

	public String getGalFile() {
		return galFile;
	}

	public String getShindoFile() {
		return shindoFile;
	}

	public String getGisFile() {
		return gisFile;
	}

	public RescueProcess[] getProcesses() {
		return simulatorComponents;
	}
}
