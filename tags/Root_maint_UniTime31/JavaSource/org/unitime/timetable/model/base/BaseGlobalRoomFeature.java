/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the ROOM_FEATURE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ROOM_FEATURE"
 */

public abstract class BaseGlobalRoomFeature extends org.unitime.timetable.model.RoomFeature  implements Serializable {

	public static String REF = "GlobalRoomFeature";
	public static String PROP_SIS_REFERENCE = "sisReference";
	public static String PROP_SIS_VALUE = "sisValue";


	// constructors
	public BaseGlobalRoomFeature () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseGlobalRoomFeature (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseGlobalRoomFeature (
		java.lang.Long uniqueId,
		java.lang.String label) {

		super (
			uniqueId,
			label);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String sisReference;
	private java.lang.String sisValue;






	/**
	 * Return the value associated with the column: SIS_REFERENCE
	 */
	public java.lang.String getSisReference () {
		return sisReference;
	}

	/**
	 * Set the value related to the column: SIS_REFERENCE
	 * @param sisReference the SIS_REFERENCE value
	 */
	public void setSisReference (java.lang.String sisReference) {
		this.sisReference = sisReference;
	}



	/**
	 * Return the value associated with the column: SIS_VALUE
	 */
	public java.lang.String getSisValue () {
		return sisValue;
	}

	/**
	 * Set the value related to the column: SIS_VALUE
	 * @param sisValue the SIS_VALUE value
	 */
	public void setSisValue (java.lang.String sisValue) {
		this.sisValue = sisValue;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.GlobalRoomFeature)) return false;
		else {
			org.unitime.timetable.model.GlobalRoomFeature globalRoomFeature = (org.unitime.timetable.model.GlobalRoomFeature) obj;
			if (null == this.getUniqueId() || null == globalRoomFeature.getUniqueId()) return false;
			else return (this.getUniqueId().equals(globalRoomFeature.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}