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
 * This is an object that contains data related to the DESIGNATOR table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DESIGNATOR"
 */

public abstract class BaseDesignator  implements Serializable {

	public static String REF = "Designator";
	public static String PROP_CODE = "code";


	// constructors
	public BaseDesignator () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDesignator (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseDesignator (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SubjectArea subjectArea,
		org.unitime.timetable.model.DepartmentalInstructor instructor,
		java.lang.String code) {

		this.setUniqueId(uniqueId);
		this.setSubjectArea(subjectArea);
		this.setInstructor(instructor);
		this.setCode(code);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String code;

	// many to one
	private org.unitime.timetable.model.SubjectArea subjectArea;
	private org.unitime.timetable.model.DepartmentalInstructor instructor;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: CODE
	 */
	public java.lang.String getCode () {
		return code;
	}

	/**
	 * Set the value related to the column: CODE
	 * @param code the CODE value
	 */
	public void setCode (java.lang.String code) {
		this.code = code;
	}



	/**
	 * Return the value associated with the column: SUBJECT_AREA_ID
	 */
	public org.unitime.timetable.model.SubjectArea getSubjectArea () {
		return subjectArea;
	}

	/**
	 * Set the value related to the column: SUBJECT_AREA_ID
	 * @param subjectArea the SUBJECT_AREA_ID value
	 */
	public void setSubjectArea (org.unitime.timetable.model.SubjectArea subjectArea) {
		this.subjectArea = subjectArea;
	}



	/**
	 * Return the value associated with the column: INSTRUCTOR_ID
	 */
	public org.unitime.timetable.model.DepartmentalInstructor getInstructor () {
		return instructor;
	}

	/**
	 * Set the value related to the column: INSTRUCTOR_ID
	 * @param instructor the INSTRUCTOR_ID value
	 */
	public void setInstructor (org.unitime.timetable.model.DepartmentalInstructor instructor) {
		this.instructor = instructor;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Designator)) return false;
		else {
			org.unitime.timetable.model.Designator designator = (org.unitime.timetable.model.Designator) obj;
			if (null == this.getUniqueId() || null == designator.getUniqueId()) return false;
			else return (this.getUniqueId().equals(designator.getUniqueId()));
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