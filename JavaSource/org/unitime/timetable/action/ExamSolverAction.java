/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamSolverForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;


/** 
 * @author Tomas Muller
 */
@Service("/examSolver")
public class ExamSolverAction extends Action {
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamSolverForm myForm = (ExamSolverForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.ExaminationSolver);
		
		if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.CanSelectSolverServer)) {
			List<String> hosts = new ArrayList<String>();
			for (SolverServer server: solverServerService.getServers(true))
				hosts.add(server.getHost());
			Collections.sort(hosts);
			if (ApplicationProperty.SolverLocalEnabled.isTrue())
				hosts.add(0, "local");
			hosts.add(0, "auto");
			request.setAttribute("hosts", hosts);
		}
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        ExamSolverProxy solver = examinationSolverService.getSolver();
        Session acadSession = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
        
        RoomAvailability.setAvailabilityWarning(request, acadSession, (solver==null?myForm.getExamType():solver.getExamTypeId()), true, false);
        
        LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

        if (op==null) {
        	myForm.init("y".equals(request.getParameter("reload")));
        	return mapping.findForward("showSolver");
        }
        
        if ("Export XML".equals(op)) {
            if (solver==null) throw new Exception("Solver is not started.");
            if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            sessionContext.checkPermission(Right.ExaminationSolutionExportXml);
            byte[] buf = solver.exportXml();
            OutputStream out = ExportUtils.getXmlOutputStream(response, "solution");
            out.write(buf);
            out.flush(); out.close();
            return null;
        }

        if ("Restore From Best".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.restoreBest();
        }
        
        if ("Store To Best".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.saveBest();
        }
        
        if (op.startsWith("Save")) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	solver.save();
        }
        
        if ("Unload".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
        	examinationSolverService.removeSolver();
        	myForm.reset(mapping, request);
        	myForm.init(false);
        }
        
        if ("Clear".equals(op)) {
            if (solver==null) throw new Exception("Solver is not started.");
            if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            solver.clear();
        }

        // Reload
        if ("Reload Input Data".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                return mapping.findForward("showSolver");
            }
            DataProperties config = examinationSolverService.createConfig(myForm.getSetting(), myForm.getParameterValues());
            config.setProperty("Exam.Type", String.valueOf(myForm.getExamType()));
            request.getSession().setAttribute("Exam.Type", myForm.getExamType());
            examinationSolverService.reload(config);
        }
        
        if ("Start".equals(op) || "Load".equals(op)) {
        	boolean start = "Start".equals(op); 
        	if (solver!=null && solver.isWorking()) throw new Exception("Solver is working, stop it first.");
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                return mapping.findForward("showSolver");
            }
            DataProperties config = examinationSolverService.createConfig(myForm.getSetting(), myForm.getParameterValues());
            config.put("Exam.Type", String.valueOf(myForm.getExamType()));
            config.put("General.StartSolver", new Boolean(start).toString());
            request.getSession().setAttribute("Exam.Type", myForm.getExamType());
    	    if (myForm.getHost() != null)
    	    	config.setProperty("General.Host", myForm.getHost());
    	    if (solver == null) {
    	    	solver = examinationSolverService.createSolver(config);
    	    } else if (start) {
    	    	solver.setProperties(config);
    	    	solver.start();
        	}
        }
        
        if ("Stop".equals(op)) {
        	if (solver==null) throw new Exception("Solver is not started.");
        	if (solver.isRunning()) solver.stopSolver();
        	myForm.reset(mapping, request);
        	myForm.init(false);
        }
        
        if ("Refresh".equals(op)) {
        	myForm.reset(mapping, request);
        	myForm.init(false);
        }
        
		return mapping.findForward("showSolver");
	}

}

