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
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.DeptStatusTypeEditForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;


/** 
 * @author Tomas Muller
 */
public class DeptStatusTypeEditAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		DeptStatusTypeEditForm myForm = (DeptStatusTypeEditForm) form;
		
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.reset(mapping, request);
            myForm.setOp("Add New");
        }
        
    	User user = Web.getUser(request.getSession());
    	Long sessionId = Session.getCurrentAcadSession(user).getSessionId();

        // Reset Form
        if ("Clear".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setOp("Add New");
        }
        

        // Add / Update
        if ("Update".equals(op) || "Add New".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("display");
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                myForm.setOp("Update");
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("reference", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                return mapping.findForward("display");
            } else {
                DepartmentStatusType s = (new DepartmentStatusTypeDAO()).get(new Long(id));
            	
                if(s==null) {
                    errors.add("reference", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    return mapping.findForward("display");
                } else {
                	myForm.load(s);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	myForm.delete(hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.reset(mapping, request);
            myForm.setOp("Add New");
        }
        
        // Move Up or Down
        if("Move Up".equals(op) || "Move Down".equals(op)) {
            Transaction tx = null;
            
            try {
                org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
                if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    tx = hibSession.beginTransaction();
                
                DepartmentStatusType curStatus = (new DepartmentStatusTypeDAO()).get(myForm.getUniqueId());
                
                if ("Move Up".equals(op)) {
                    boolean found = false;
                    for (Iterator i=DepartmentStatusType.findAll().iterator();i.hasNext();) {
                        DepartmentStatusType s = (DepartmentStatusType)i.next();
                        if (s.getOrd()+1==curStatus.getOrd()) {
                            s.setOrd(s.getOrd()+1); 
                            hibSession.saveOrUpdate(s);
                            found = true;
                        }
                    }
                    if (found) {
                        curStatus.setOrd(curStatus.getOrd()-1);
                        myForm.setOrder(curStatus.getOrd());
                        hibSession.saveOrUpdate(curStatus);
                    }
                } else {
                    boolean found = false;
                    for (Iterator i=DepartmentStatusType.findAll().iterator();i.hasNext();) {
                        DepartmentStatusType s = (DepartmentStatusType)i.next();
                        if (s.getOrd()-1==curStatus.getOrd()) {
                            s.setOrd(s.getOrd()-1); 
                            hibSession.saveOrUpdate(s);
                            found = true;
                        }
                    }
                    if (found) {
                        curStatus.setOrd(curStatus.getOrd()+1);
                        myForm.setOrder(curStatus.getOrd());
                        hibSession.saveOrUpdate(curStatus);
                    }
                }
                
                if (tx!=null) tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                Debug.error(e);
            }
            myForm.setOp("Update");
        }

        // Read all existing settings and store in request
        getDeptStatusList(request, sessionId);    
        
        return mapping.findForward("display");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getDeptStatusList(HttpServletRequest request, Long sessionId) throws Exception {
		WebTable.setOrder(request.getSession(),"deptStatusTypes.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    "Department/Session Statuses", "deptStatusTypeEdit.do?ord=%%",
			    new String[] {"Reference", "Label", "Apply", "Rights"},
			    new String[] {"left", "left", "left","left"},
			    null );
        
        TreeSet statuses = DepartmentStatusType.findAll();
		if(statuses.isEmpty()) {
		    webTable.addLine(null, new String[] {"No status defined."}, null, null );			    
		}
		
        for (Iterator i=statuses.iterator();i.hasNext();) {
        	DepartmentStatusType s = (DepartmentStatusType)i.next();
        	String onClick = "onClick=\"document.location='deptStatusTypeEdit.do?op=Edit&id=" + s.getUniqueId() + "';\"";
        	String rights = "";
            String apply = "";
            if (s.applyDepartment()) {
                if (s.applySession())
                    apply = "Both";
                else
                    apply = "Department";
            } else if (s.applySession())
                apply = "Session";
            if (s.canOwnerView() || s.canOwnerLimitedEdit() || s.canOwnerEdit()) {
                if (rights.length()>0) rights+="; ";
                rights += "owner can ";
                if (s.canOwnerView() && s.canOwnerEdit())
                    rights += "do all";
                else {
                    if (s.canOwnerView())
                        rights += "view";
                    if (s.canOwnerEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "edit";
                    } else if (s.canOwnerLimitedEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "limited edit";
                    }
                }
            }
            if (s.canManagerView() || s.canManagerLimitedEdit() || s.canManagerEdit()) {
                if (rights.length()>0) rights+="; ";
                rights += "manager can ";
                if (s.canManagerView() && s.canManagerEdit())
                    rights += "do all";
                else {
                    if (s.canManagerView())
                        rights += "view";
                    if (s.canManagerEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "edit";
                    } else if (s.canManagerLimitedEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "limited edit";
                    }
                }
            }
            if (s.canAudit()) {
                if (rights.length()>0) rights+="; ";
                rights += "audit";
            }
            if (s.canTimetable()) {
                if (rights.length()>0) rights+="; ";
                rights += "timetable";
            } 
            if (s.canCommit()) {
                if (rights.length()>0) rights+="; ";
                rights += "commit";
            }
            webTable.addLine(onClick, new String[] {
                    s.getReference(),
                    s.getLabel(),
                    apply,
                    rights
        		},new Comparable[] {
        			s.getOrd(),
                    s.getLabel(),
                    s.getApply(),
                    s.getStatus()
        		});
        }
        
        request.setAttribute("DeptStatusType.last", new Integer(statuses.size()-1));
	    request.setAttribute("DeptStatusType.table", webTable.printTable(WebTable.getOrder(request.getSession(),"deptStatusTypes.ord")));
    }	
}

