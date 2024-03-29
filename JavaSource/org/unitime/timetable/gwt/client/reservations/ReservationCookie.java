/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.reservations;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class ReservationCookie {
	private boolean iCourseDetails = false;
	
	private static ReservationCookie sInstance = null;
	
	private ReservationCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Reservations");
			if (cookie != null && cookie.length() > 0) {
				String[] values = cookie.split(":");
				iCourseDetails = "T".equals(values[0]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = 
			(iCourseDetails ? "T": "F");
		Cookies.setCookie("UniTime:Reservations", cookie);
	}
	
	public static ReservationCookie getInstance() {
		if (sInstance == null)
			sInstance = new ReservationCookie();
		return sInstance;
	}
	
	public boolean getReservationCoursesDetails() {
		return iCourseDetails;
	}
	
	public void setReservationCoursesDetails(boolean details) {
		iCourseDetails = details;
		save();
	}
}
