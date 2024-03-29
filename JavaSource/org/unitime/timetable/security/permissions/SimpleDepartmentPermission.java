/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.permissions;

import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentStatusType.Status;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("permissionDepartment")
public class SimpleDepartmentPermission implements PermissionDepartment {

	@Override
	public boolean check(UserContext user, Department department) {
		return check(user, department, new DepartmentStatusType.Status[] {}) && checkStatus(department.effectiveStatusType());
	}
	
	@Override
	public boolean check(UserContext user, Department department, DepartmentStatusType.Status... status) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || department == null) return false;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !authority.hasQualifier(department.getSession()))
			return false;
		
		// Department check
		if (!authority.hasRight(Right.DepartmentIndependent) && !authority.hasQualifier(department))
			return false;

		// Check department status
		if (status.length > 0 && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = department.effectiveStatusType();
			if (type == null) return false;
			for (DepartmentStatusType.Status s: status) {
				if (type.can(s)) return true;
			}
			return false;
		}
		
		return true;
	}

	@Override
	public Class<Department> type() {
		return Department.class;
	}
	
	public boolean checkStatus(DepartmentStatusType status) { return true; }

	@Override
	public boolean check(UserContext user, Department controllingDepartment, Status ownerStatus, Department managingDepartment, Status managerStatus) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || controllingDepartment == null) return false;
		if (managingDepartment == null) managingDepartment = controllingDepartment;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !authority.hasQualifier(controllingDepartment.getSession()))
			return false;
		
		// Department check
		if (!authority.hasRight(Right.DepartmentIndependent) && !authority.hasQualifier(controllingDepartment) && !authority.hasQualifier(managingDepartment))
			return false;

		// Check department status
		if ((ownerStatus != null || managerStatus != null) && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = managingDepartment.effectiveStatusType();
			if (ownerStatus != null && authority.hasQualifier(controllingDepartment) && type.can(ownerStatus))
				return true;
			if (managerStatus != null && authority.hasQualifier(managingDepartment) && type.can(managerStatus))
				return true;
			return false;
		}
		
		return true;
	}
}
