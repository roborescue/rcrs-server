/*
 * Last change: $Date: 2004/06/07 22:08:08 $
 * $Revision: 1.5 $
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

package rescuecore.view;

import java.awt.Color;

public final class ViewConstants {
	private ViewConstants() {}

    public final static int LINE_MODE_SOLID = 0;
    public final static int LINE_MODE_DOT = 1;
    public final static int LINE_MODE_DASH = 2;
    public final static int LINE_MODE_DOT_DASH = 3;

    public final static int FILL_MODE_SOLID = 0;
    public final static int FILL_MODE_HORIZONTAL_LINES = 1;
    public final static int FILL_MODE_VERTICAL_LINES = 2;
    public final static int FILL_MODE_DIAGONAL_LINES = 3;
    public final static int FILL_MODE_REVERSE_DIAGONAL_LINES = 4;
    public final static int FILL_MODE_HATCH = 5;
    public final static int FILL_MODE_CROSS_HATCH = 6;

    public final static Color NO_COLOUR = new Color(0,0,0,255);
    public final static Color BACKGROUND_COLOUR = new Color(96,96,96);

    public final static Color AGENT_COLOUR = Color.blue;
    public final static Color TARGET_COLOUR = Color.red;
    public final static Color PRIMARY_TARGETS_COLOUR = Color.white;
    public final static Color SECONDARY_TARGETS_COLOUR = Color.yellow;
    public final static Color TERTIARY_TARGETS_COLOUR = Color.orange;
    public final static Color BAD_TARGETS_COLOUR = Color.pink;
    public final static Color PATH_COLOUR = Color.white;

    public final static Color FIRE_STATION_COLOUR = Color.yellow;
    public final static Color POLICE_OFFICE_COLOUR = Color.blue;
    public final static Color AMBULANCE_CENTER_COLOUR = Color.white;
    public final static Color REFUGE_COLOUR = Color.pink;
    public final static Color BUILDING_COLOUR = new Color(128,128,128);

    public final static Color HEATING_COLOUR = new Color(255,128,0);
    public final static Color FIRE_COLOUR = Color.red;
    public final static Color INFERNO_COLOUR = Color.red.darker();
    public final static Color BURNT_OUT_COLOUR = Color.black;
	public final static Color WATER_DAMAGE_COLOUR = new Color(48,48,216);
    public final static Color EXTINGUISHED_COLOUR = new Color(96,96,216);

    public final static Color CIVILIAN_COLOUR = Color.green;
    public final static Color FIRE_BRIGADE_COLOUR = Color.red;
    public final static Color POLICE_FORCE_COLOUR = Color.blue;
    public final static Color AMBULANCE_TEAM_COLOUR = Color.white;
    public final static Color CAR_COLOUR = Color.pink;

    public final static Color UNBLOCKED_COLOUR = Color.black;
    public final static Color PARTIALLY_BLOCKED_COLOUR = Color.lightGray;
    public final static Color TOTALLY_BLOCKED_COLOUR = Color.white;
}
