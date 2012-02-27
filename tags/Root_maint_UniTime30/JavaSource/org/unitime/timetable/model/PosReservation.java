/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BasePosReservation;



public class PosReservation extends BasePosReservation {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public PosReservation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public PosReservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public PosReservation (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ReservationType reservationType,
		java.lang.String ownerClassId,
		java.lang.Long owner,
		java.lang.Integer priority) {

		super (
			uniqueId,
			reservationType,
			ownerClassId,
			owner,
			priority);
	}

/*[CONSTRUCTOR MARKER END]*/


}