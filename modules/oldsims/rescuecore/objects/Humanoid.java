/*
 * Last change: $Date: 2004/05/04 03:40:13 $
 * $Revision: 1.7 $
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

package rescuecore.objects;

import rescuecore.*;

/**
   Base class for all humanoid objects (civilians, fire brigades, cars etc)
 */
public abstract class Humanoid extends MovingObject {
	private IntProperty stamina, hp, damage, buriedness;

	public Humanoid() {
		stamina = new IntProperty(RescueConstants.PROPERTY_STAMINA);
		hp = new IntProperty(RescueConstants.PROPERTY_HP);
		damage = new IntProperty(RescueConstants.PROPERTY_DAMAGE);
		buriedness = new IntProperty(RescueConstants.PROPERTY_BURIEDNESS);
	}

	public Humanoid(int pos, int extra, int dir, int[] history, int stam, int health, int dmg, int bury) {
		super(pos,extra,dir,history);
		stamina = new IntProperty(RescueConstants.PROPERTY_STAMINA,stam);
		hp = new IntProperty(RescueConstants.PROPERTY_HP,health);
		damage = new IntProperty(RescueConstants.PROPERTY_DAMAGE,dmg);
		buriedness = new IntProperty(RescueConstants.PROPERTY_BURIEDNESS,bury);
	}

	/*
	public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_STAMINA:
		case RescueConstants.PROPERTY_HP:
		case RescueConstants.PROPERTY_DAMAGE:
		case RescueConstants.PROPERTY_BURIEDNESS:
			return true;
		}
		return super.propertyExists(property);
	}
	*/

	public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_STAMINA:
			return stamina;
		case RescueConstants.PROPERTY_HP:
			return hp;
		case RescueConstants.PROPERTY_DAMAGE:
			return damage;
		case RescueConstants.PROPERTY_BURIEDNESS:
			return buriedness;
		}
		return super.getProperty(property);
	}

	public int getStamina() {
		return stamina.getValue();
	}

	public boolean setStamina(int s, int timestamp, Object source) {
		if (stamina.updateValue(s,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_STAMINA,timestamp,source);
			return true;
		}
		return false;
	}

	public int getHP() {
		return hp.getValue();
	}

	public boolean setHP(int h, int timestamp, Object source) {
		if (hp.updateValue(h,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_HP,timestamp,source);
			return true;
		}
		return false;
	}

	public int getDamage() {
		return damage.getValue();
	}

	public boolean setDamage(int d, int timestamp, Object source) {
		if (damage.updateValue(d,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_DAMAGE,timestamp,source);
			return true;
		}
		return false;
	}

	public int getBuriedness() {
		return buriedness.getValue();
	}

	public boolean setBuriedness(int b, int timestamp, Object source) {
		if (buriedness.updateValue(b,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BURIEDNESS,timestamp,source);
			return true;
		}
		return false;
	}

	public boolean isHurt() {
		return getHP() < 10000;
	}

	public boolean isAlive() {
		return getHP() > 0;
	}

	public boolean isBuried() {
		return getBuriedness() > 0;
	}

	public boolean isDamaged() {
		return getDamage() > 0;
	}
}
