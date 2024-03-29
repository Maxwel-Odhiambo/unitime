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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.form.PreferencesForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.RequiredTimeTable;


/**
 * Superclass for implementing Preferences
 * @author Heston Fernandes, Tomas Muller, Zuzana Mullerova
 */
public class PreferencesAction extends Action {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
    
    // --------------------------------------------------------- Class Constants
    
    /** Request attribute name for time pattern grid **/
    public static final String TIME_PATTERN_GRID_ATTR = "timePatternGrid";
    
    /** Request attribute name for anchor in form **/
    public static final String HASH_ATTR = "hash";

    /** Anchor names **/
    public final String HASH_TIME_PREF = "TimePref";
    public final String HASH_RM_GROUP = "RoomGroupPref";
    public final String HASH_RM_PREF = "RoomPref";
    public final String HASH_RM_FEAT_PREF = "RoomFeatPref";
    public final String HASH_BLDG_PREF = "BldgPref";
    public final String HASH_DIST_PREF = "DistPref";
    public final String HASH_PERIOD_PREF = "PeriodPref";
    public final String HASH_DATE_PATTERN_PREF = "DatePatternPref";
    
    // --------------------------------------------------------- Methods

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    	
		// Load Combo Box Lists 
        LookupTables.setupItypes(request,true);		 // Itypes
        LookupTables.setupPrefLevels(request);	 // Preference Levels
        LookupTables.setupInstructorDistribTypes(request, sessionContext); // Distribution Types
        
        return mapping.findForward(mapping.getInput());
    }

    /**
     * Process actions performed on Preferences in UI
     * @param request
     * @param frm
     * @param errors
     */
    protected void processPrefAction(
            HttpServletRequest request,
            PreferencesForm frm,
            ActionMessages errors) {
        
        String op = frm.getOp();
        if(op==null) return;
        
        // Add Room Group row
        if(op.equals(MSG.actionAddRoomGroupPreference())) 
            addRoomGroup(request, frm, errors);
        
        // Add Room Preference row
        if(op.equals(MSG.actionAddRoomPreference())) 
            addRoomPref(request, frm, errors);
        
        // Add Building Preference row
        if(op.equals(MSG.actionAddBuildingPreference())) 
            addBldgPref(request, frm, errors);
        
        // Add Distribution Preference row
        if(op.equals(MSG.actionAddDistributionPreference())) 
            addDistPref(request, frm, errors);

        // Add Room Feature Preference row
        if(op.equals(MSG.actionAddRoomFeaturePreference())) 
            addRoomFeatPref(request, frm, errors);
        
        if(op.equals(MSG.actionAddTimePreference())) 
            addTimePattern(request, frm, errors);
        
        // Delete single preference
        if(op.equals(MSG.actionRemoveBuildingPreference())
        		|| op.equals(MSG.actionRemoveDistributionPreference())
        		|| op.equals(MSG.actionRemoveRoomFeaturePreference())
        		|| op.equals(MSG.actionRemoveRoomGroupPreference())
        		|| op.equals(MSG.actionRemoveRoomPreference())
        		|| op.equals(MSG.actionRemoveTimePattern())
        		|| op.equals(MSG.actionRemoveInstructor())
        		)
            doDelete(request, frm);
        
    }
    
    private void addRoomGroup(HttpServletRequest request, PreferencesForm frm, ActionMessages errors) {
    	
        List lst = frm.getRoomGroups();
        if(frm.checkPrefs(lst)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
	            frm.addToRoomGroups(
	                    Preference.BLANK_PREF_VALUE, 
	                    Preference.BLANK_PREF_VALUE );
            }
            request.setAttribute(HASH_ATTR, HASH_RM_GROUP);
        }
        else {
            errors.add("roomGroup", 
                       new ActionMessage(
                               "errors.generic", 
                               MSG.errorInvalidRoomGroup()));
            saveErrors(request, errors);
        }
		
	}

	/**
     * Add a building preference to the list (UI)
     * @param request
     * @param frm
     * @param errors
     */
    protected void addBldgPref(
            HttpServletRequest request, 
            PreferencesForm frm,
            ActionMessages errors ) {
 
        List lst = frm.getBldgPrefs();
        if(frm.checkPrefs(lst)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
	            frm.addToBldgPrefs(
	                    Preference.BLANK_PREF_VALUE, 
	                    Preference.BLANK_PREF_VALUE );
            }
            request.setAttribute(HASH_ATTR, HASH_BLDG_PREF);
        }
        else {
            errors.add("bldgPrefs", 
                       new ActionMessage(
                               "errors.generic", 
                               MSG.errorInvalidBuildingPreference()) );
            saveErrors(request, errors);
        }
    }
    
    protected void addDistPref(
            HttpServletRequest request, 
            PreferencesForm frm,
            ActionMessages errors ) {
 
        List lst = frm.getDistPrefs();
        if(frm.checkPrefs(lst)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
	            frm.addToDistPrefs(
	                    Preference.BLANK_PREF_VALUE, 
	                    Preference.BLANK_PREF_VALUE );
            }
            request.setAttribute(HASH_ATTR, HASH_DIST_PREF);
        }
        else {
            errors.add("distPrefs", 
                       new ActionMessage(
                               "errors.generic", 
                               MSG.errorInvalidDistributionPreference()) );
            saveErrors(request, errors);
        }
    }

    /**
     * Add a room feature preference to the list (UI)
     * @param request
     * @param frm
     * @param errors
     */
    protected void addRoomFeatPref(
            HttpServletRequest request, 
            PreferencesForm frm,
            ActionMessages errors ) {

        List lst = frm.getRoomFeaturePrefs();
        if(frm.checkPrefs(lst)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
	            frm.addToRoomFeatPrefs(
	                    Preference.BLANK_PREF_VALUE, 
	                    Preference.BLANK_PREF_VALUE );
            }
            request.setAttribute(HASH_ATTR, HASH_RM_FEAT_PREF);
        }
        else {
            errors.add("roomFeaturePrefs", 
                       new ActionMessage(
                               "errors.generic", 
                               MSG.errorInvalidRoomFeaturePreference()) );
            saveErrors(request, errors);
        }
    }
    
    protected void addTimePattern(
            HttpServletRequest request, 
            PreferencesForm frm,
            ActionMessages errors ) {
    	
        if ("-".equals(frm.getTimePattern())) {
            errors.add("timePrefs", 
                    new ActionMessage(
                            "errors.generic", 
                            MSG.errorTimePatternNotSelected()) );
         saveErrors(request, errors);
        } else {
        	if (frm.getTimePatterns()==null)
        		frm.setTimePatterns(new Vector());
        	frm.getTimePatterns().add(frm.getTimePattern());
        	TimePattern tp = (new TimePatternDAO()).get(Long.valueOf(frm.getTimePattern()));
        	if (tp.getTimePatternModel().isExactTime()) {
        		for (Iterator i=frm.getTimePatterns().iterator();i.hasNext();) {
        			String patternId = (String)i.next();
        			TimePattern tpx = (new TimePatternDAO()).get(Long.valueOf(patternId));
        			if (!tpx.getTimePatternModel().isExactTime()) i.remove();
        		}
        	} else {
        		for (Iterator i=frm.getTimePatterns().iterator();i.hasNext();) {
        			String patternId = (String)i.next();
        			TimePattern tpx = (new TimePatternDAO()).get(Long.valueOf(patternId));
        			if (tpx.getTimePatternModel().isExactTime()) i.remove();
        		}
        	}
            request.setAttribute(HASH_ATTR, HASH_TIME_PREF);
        }
    }
        
    /**
     * Add a room preference to the list (UI)
     * @param request
     * @param frm
     * @param errors
     */
    protected void addRoomPref(
            HttpServletRequest request, 
            PreferencesForm frm,
            ActionMessages errors ) {
        
        List lst = frm.getRoomPrefs();
        if(frm.checkPrefs(lst)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
	            frm.addToRoomPrefs(
	                    Preference.BLANK_PREF_VALUE, 
	                    Preference.BLANK_PREF_VALUE );
            }
            request.setAttribute(HASH_ATTR, HASH_RM_PREF);
        }
        else {
            errors.add("roomPrefs", 
                       new ActionMessage(
                               "errors.generic", 
                               MSG.errorInvalidRoomPreference()) );
            saveErrors(request, errors);
        }
    }
    
    /**
     * Redirects to search results screen
     * @param request
     * @param subpartId
     */
    protected void doCancel(
            HttpServletRequest request, 
            String subpartId ) {
        
        SchedulingSubpartDAO sdao = new SchedulingSubpartDAO();
        SchedulingSubpart ss = sdao.get(new Long(subpartId));
        InstructionalOffering io = ss.getInstrOfferingConfig().getInstructionalOffering();
        CourseOffering co = io.getControllingCourseOffering();
        
        InstructionalOfferingListForm frm2 = new InstructionalOfferingListForm();
		frm2.setSubjectAreaIds(new String[] {co.getSubjectArea().getUniqueId().toString()});
        frm2.setSubjectAreaAbbv(co.getSubjectAreaAbbv());
        frm2.setCourseNbr(co.getCourseNbr());
        frm2.setCtrlInstrOfferingId(io.getCtrlCourseId().toString());
        frm2.setIsControl(co.isIsControl());

        request.setAttribute("subjectAreaId", co.getSubjectArea().getUniqueId().toString());
        request.setAttribute("instructionalOfferingListForm", frm2);            
    }

    /**
     * Deletes a preference from the list (UI)
     * @param request
     * @param frm
     */
    protected void doDelete(
            HttpServletRequest request, 
            PreferencesForm frm) {
        
        String deleteType = request.getParameter("deleteType");
        int deleteId = -1;
        
        try {
            deleteId = Integer.parseInt(request.getParameter("deleteId"));
        }
        catch(Exception e) {
            deleteId = -1;
        }
       
        if(deleteType!=null && deleteId>=0) {
            if(deleteType.equals("roomPref")) {
                List lst = frm.getRoomPrefs();
                List lstL = frm.getRoomPrefLevels();
                lst.remove(deleteId);
                lstL.remove(deleteId);
                frm.setRoomPrefs(lst);
                frm.setRoomPrefLevels(lstL);
                request.setAttribute(HASH_ATTR, HASH_RM_PREF);
            }
            if(deleteType.equals("rgPref")) {
                List lst = frm.getRoomGroups();
                List lstL = frm.getRoomGroupLevels();
                lst.remove(deleteId);
                lstL.remove(deleteId);
                frm.setRoomGroups(lst);
                frm.setRoomGroupLevels(lstL);
                request.setAttribute(HASH_ATTR, HASH_RM_GROUP);
            }
            if(deleteType.equals("bldgPref")) {
                List lst = frm.getBldgPrefs();
                List lstL = frm.getBldgPrefLevels();
                lst.remove(deleteId);
                lstL.remove(deleteId);
                frm.setBldgPrefs(lst);
                frm.setBldgPrefLevels(lstL);
                request.setAttribute(HASH_ATTR, HASH_BLDG_PREF);
            }
            if(deleteType.equals("distPref")) {
                List lst = frm.getDistPrefs();
                List lstL = frm.getDistPrefLevels();
                lst.remove(deleteId);
                lstL.remove(deleteId);
                frm.setDistPrefs(lst);
                frm.setDistPrefLevels(lstL);
                request.setAttribute(HASH_ATTR, HASH_DIST_PREF);
            }
            if(deleteType.equals("roomFeaturePref")) {
                List lst = frm.getRoomFeaturePrefs();
                List lstL = frm.getRoomFeaturePrefLevels();
                lst.remove(deleteId);
                lstL.remove(deleteId);
                frm.setRoomFeaturePrefs(lst);
                frm.setRoomFeaturePrefLevels(lstL);
                request.setAttribute(HASH_ATTR, HASH_RM_FEAT_PREF);
            }
            if(deleteType.equals("timePattern")) {
            	List tps = frm.getTimePatterns();
            	tps.remove(deleteId);
            	frm.setTimePatterns(tps);
            	request.setAttribute(HASH_ATTR, HASH_TIME_PREF);
            }
            if(deleteType.equals("dpPref")) {
                List lst = frm.getDatePatternPrefs();
                List lstL = frm.getDatePatternPrefLevels();
                lst.remove(deleteId);
                lstL.remove(deleteId);
                frm.setDatePatternPrefs(lst);
                frm.setDatePatternPrefLevels(lstL);
                request.setAttribute(HASH_ATTR, HASH_RM_GROUP);
            }
        }
    }
    
    /**
     * Updates the scheduling subpart
     * @param request 
     * @param frm
     * @param pg PreferenceGroup object
     * @param s Preferences Set
     * @param t Time Patterns Set
     */
    protected void doUpdate(
            HttpServletRequest request,
            PreferencesForm frm,
            PreferenceGroup pg,
            Set s,
            boolean timeVertical) throws Exception {
        
    	pg.setPreferences(s);
    	
        // Time Prefs
        if (pg instanceof DepartmentalInstructor) {
        	if (frm.getAvailability() != null && (frm.getAvailability().length() == 336 || frm.getAvailability().length() == 2016)) {
        		TimePref tp = new TimePref();
        		tp.setOwner(pg);
        		tp.setPreference(frm.getAvailability());
        		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
        		tp.setTimePattern(null);
        		s.add(tp);
        	}
        } else {
        	Set parentTimePrefs = pg.effectivePreferences(TimePref.class);
            List lst = frm.getTimePatterns();
            for(int i=0; i<lst.size(); i++) {
            	String id = (String)lst.get(i);
            	addToTimePref(request, pg, id, s, i, timeVertical, parentTimePrefs);
            }
            if (parentTimePrefs!=null && !parentTimePrefs.isEmpty()) {
            	for (Iterator i=parentTimePrefs.iterator();i.hasNext();) {
            		TimePref tp = (TimePref)((TimePref)i.next()).clone();
            		tp.setOwner(pg);
            		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
            		s.add(tp);
            	}
            }
        }
            
        // Room Prefs
        List lst = frm.getRoomPrefs();
        List lstL = frm.getRoomPrefLevels();
        Set parentRoomPrefs = pg.effectivePreferences(RoomPref.class);
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);            
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Room: " + id + ": " + pref);

    		LocationDAO rdao = new LocationDAO();
    	    Location room = rdao.get(new Long(id));
            
            RoomPref rp = new RoomPref();
            rp.setOwner(pg);
            rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
            rp.setRoom(room);
            
            RoomPref sameParentRp = null;
            for (Iterator j=parentRoomPrefs.iterator();j.hasNext();) {
            	RoomPref p = (RoomPref)j.next();
            	if (p.isSame(rp)) {
            		if (p.getPrefLevel().equals(rp.getPrefLevel()))
            			sameParentRp = rp;
            		j.remove();
            		break;
            	}
            }

            if (sameParentRp==null)
            	s.add(rp);
        }
        if (parentRoomPrefs!=null && !parentRoomPrefs.isEmpty()) {
        	for (Iterator i=parentRoomPrefs.iterator();i.hasNext();) {
        		RoomPref rp = (RoomPref)((RoomPref)i.next()).clone();
        		rp.setOwner(pg);
        		rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
        		s.add(rp);
        	}
        }
        
        
        // Bldg Prefs
        lst = frm.getBldgPrefs();
        lstL = frm.getBldgPrefLevels();
        Set parentBuildingPrefs = pg.effectivePreferences(BuildingPref.class);
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Bldg: " + id + ": " + pref);

            BuildingDAO bdao = new BuildingDAO();
    	    Building bldg = bdao.get(new Long(id));
            
            BuildingPref bp = new BuildingPref();
            bp.setOwner(pg);
            bp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
            bp.setBuilding(bldg);

            BuildingPref sameParentBp = null;
            for (Iterator j=parentBuildingPrefs.iterator();j.hasNext();) {
            	BuildingPref p = (BuildingPref)j.next();
            	if (p.isSame(bp)) {
            		if (p.getPrefLevel().equals(bp.getPrefLevel()))
            			sameParentBp = bp;
            		j.remove();
            		break;
            	}
            }

            if (sameParentBp==null)
            	s.add(bp);
        }
        if (parentBuildingPrefs!=null && !parentBuildingPrefs.isEmpty()) {
        	for (Iterator i=parentBuildingPrefs.iterator();i.hasNext();) {
        		BuildingPref bp = (BuildingPref)((BuildingPref)i.next()).clone();
        		bp.setOwner(pg);
        		bp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
        		s.add(bp);
        	}
        }
        
        // Dist Prefs
        lst = frm.getDistPrefs();
        lstL = frm.getDistPrefLevels();
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Dist: " + id + ": " + pref);

            DistributionTypeDAO ddao = new DistributionTypeDAO();
            DistributionType dist = ddao.get(new Long(id));
            
            DistributionPref dp = new DistributionPref();
            dp.setOwner(pg);
            dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
            dp.setDistributionType(dist);
            dp.setGrouping(new Integer(DistributionPref.sGroupingNone));

            s.add(dp);
        }

        // Period Prefs
        /*
        lst = frm.getPeriodPrefs();
        lstL = frm.getPeriodPrefLevels();
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Period: " + id + ": " + pref);

            ExamPeriodDAO pdao = new ExamPeriodDAO();
            ExamPeriod period = pdao.get(new Long(id));
            
            ExamPeriodPref xp = new ExamPeriodPref();
            xp.setOwner(pg);
            xp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
            xp.setExamPeriod(period);

            s.add(xp);
        }*/
        if (pg instanceof Exam) { 
            Exam exam = (Exam)pg;
            ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
            ExamAssignment assignment = null;
            if (solver!=null && exam!=null && exam.getUniqueId()!=null)
                assignment = solver.getAssignment(exam.getUniqueId());
            else if (exam.getAssignedPeriod()!=null)
                assignment = new ExamAssignment(exam);
            if (ExamType.sExamTypeMidterm==exam.getExamType().getType()) {
            	MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType(), assignment);
                epx.load(exam);
                epx.load(request);
                epx.save(s, exam);
            } else {
            	PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), assignment, exam.getExamType().getUniqueId());
            	px.load(exam);
            	RequiredTimeTable rtt = new RequiredTimeTable(px);
            	rtt.setName("PeriodPref");
            	rtt.update(request);
            	px.save(s, exam);
            }
        }
        
        // Room Feature Prefs
        lst = frm.getRoomFeaturePrefs();
        lstL = frm.getRoomFeaturePrefLevels();
        Set parentRoomFeaturePrefs = pg.effectivePreferences(RoomFeaturePref.class);
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Room Feat: " + id + ": " + pref);

            RoomFeatureDAO rfdao = new RoomFeatureDAO();
    	    RoomFeature rf = rfdao.get(new Long(id));
            
            RoomFeaturePref rfp = new RoomFeaturePref();
            rfp.setOwner(pg);
            rfp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
            rfp.setRoomFeature(rf);

            RoomFeaturePref sameParentRfp = null;
            for (Iterator j=parentRoomFeaturePrefs.iterator();j.hasNext();) {
            	RoomFeaturePref p = (RoomFeaturePref)j.next();
            	if (p.isSame(rfp)) {
            		if (p.getPrefLevel().equals(rfp.getPrefLevel()))
            			sameParentRfp = rfp;
            		j.remove();
            		break;
            	}
            }

            if (sameParentRfp==null)
            	s.add(rfp);
        }
        if (parentRoomFeaturePrefs!=null && !parentRoomFeaturePrefs.isEmpty()) {
        	for (Iterator i=parentRoomFeaturePrefs.iterator();i.hasNext();) {
        		RoomFeaturePref rp = (RoomFeaturePref)((RoomFeaturePref)i.next()).clone();
        		rp.setOwner(pg);
        		rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
        		s.add(rp);
        	}
        }
        
        // Room Group Prefs
        lst = frm.getRoomGroups();
        lstL = frm.getRoomGroupLevels();
        Set parentRoomGroupPrefs = pg.effectivePreferences(RoomGroupPref.class);
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Roomgr: " + id + ": " + pref);

            RoomGroupDAO gdao = new RoomGroupDAO();
            RoomGroup gr = gdao.get(new Long(id));
            
            RoomGroupPref gp = new RoomGroupPref();
            gp.setOwner(pg);
            gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
            gp.setRoomGroup(gr);

            RoomGroupPref sameParentGp = null;
            for (Iterator j=parentRoomGroupPrefs.iterator();j.hasNext();) {
            	RoomGroupPref p = (RoomGroupPref)j.next();
            	if (p.isSame(gp)) {
            		if (p.getPrefLevel().equals(gp.getPrefLevel()))
            			sameParentGp = gp;
            		j.remove();
            		break;
            	}
            }

            if (sameParentGp==null)
            	s.add(gp);
        }
        if (parentRoomGroupPrefs!=null && !parentRoomGroupPrefs.isEmpty()) {
        	for (Iterator i=parentRoomGroupPrefs.iterator();i.hasNext();) {
        		RoomGroupPref gp = (RoomGroupPref)((RoomGroupPref)i.next()).clone();
        		gp.setOwner(pg);
        		gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
        		s.add(gp);
        	}
        }        

        // Date pattern Prefs
        lst = frm.getDatePatternPrefs();
        lstL = frm.getDatePatternPrefLevels();        
        Set parentDatePatternPrefs = pg.effectivePreferences(DatePatternPref.class);
        
        for(int i=0; i<lst.size(); i++) {
            String id = (String)lst.get(i);
            if (id==null || id.equals(Preference.BLANK_PREF_VALUE) || lstL.get(i).equals(PreferenceLevel.PREF_LEVEL_NEUTRAL))
                continue;
            
            String pref = (String) lstL.get(i);
            Debug.debug("Datepattern: " + id + ": " + pref);

            DatePatternDAO dpdao = new DatePatternDAO();
            DatePattern dp = dpdao.get(new Long(id));           
            
           DatePatternPref dpp = new DatePatternPref();
           dpp.setOwner(pg);
           dpp.setPrefLevel(PreferenceLevel.getPreferenceLevel(Integer.parseInt(pref)));
           dpp.setDatePattern(dp);          
				           
            DatePatternPref sameParentDp = null;
            for (Iterator j=parentDatePatternPrefs.iterator();j.hasNext();) {
            	DatePatternPref p = (DatePatternPref)j.next();
            	if (p.isSame(dpp)) {
            		if (p.getPrefLevel().equals(dpp.getPrefLevel()))
            			sameParentDp = dpp;
            		j.remove();
            		break;
            	}
            }

            if (sameParentDp==null)
            	s.add(dpp);
        }
        if (parentDatePatternPrefs!=null && !parentDatePatternPrefs.isEmpty()) {
        	for (Iterator i=parentDatePatternPrefs.iterator();i.hasNext();) {
        		DatePatternPref gp = (DatePatternPref)((DatePatternPref)i.next()).clone();        		
        		if(!pg.effectiveDatePattern().findChildren().contains(gp.getDatePattern())){
              	   continue;
                 }
        		gp.setOwner(pg);
        		gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral));
        		s.add(gp);
        	}
        } 
        
        // Set values in subpart
        pg.setPreferences(s);
    }
    
    
    /**
     * Add Time Preferences to the set of Preferences
     * @param request
     * @param owner Owner of type PreferenceGroup
     * @param tpat Time Pattern Id
     * @param tag Tag to indicate processing of Alt time pattern
     * @param prefs
     * @param patterns
     * @throws Exception
     */
    protected void addToTimePref(
            HttpServletRequest request,
            PreferenceGroup owner,
            String tpat,
            Set prefs,
            int idx,
            boolean timeVertical, Set parentTimePrefs) throws Exception {
        
		TimePatternDAO timePatternDao = new TimePatternDAO();
		TimePattern timePattern = (tpat.equals("-1")?null:timePatternDao.get(new Long(tpat)));

		// Generate grid prefs
		boolean canUseHardTimePrefs = sessionContext.hasPermission(owner, Right.CanUseHardTimePrefs);
		RequiredTimeTable rtt = (timePattern == null ? TimePattern.getDefaultRequiredTimeTable() : timePattern.getRequiredTimeTable(canUseHardTimePrefs));
		rtt.getModel().setDefaultSelection(RequiredTimeTable.getTimeGridSize(sessionContext.getUser()));
		rtt.setName("p"+idx);
		
		rtt.update(request);
		TimePref tp = new TimePref();
		tp.setOwner(owner);
		tp.setPreference(rtt.getModel().getPreferences());
		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
		tp.setTimePattern(timePattern);
		
		TimePref sameParentTimePref = null;
		if (parentTimePrefs!=null && !parentTimePrefs.isEmpty()) {
			for (Iterator i=parentTimePrefs.iterator();i.hasNext();) {
				TimePref parentTimePref = (TimePref)i.next();
				if (parentTimePref.isSame(tp)) {
					if (parentTimePref.getPreference().equals(tp.getPreference()) && parentTimePref.getPrefLevel().equals(tp.getPrefLevel()))
						sameParentTimePref = parentTimePref; 
					i.remove(); break;
				}
			}
		}
		
		if (sameParentTimePref==null)
			prefs.add(tp);
    }
    
    protected void generateExamPeriodGrid(HttpServletRequest request,
            PreferencesForm frm,
            Exam exam, 
            String op, 
            boolean timeVertical, boolean editable) throws Exception {
        
        ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
        ExamAssignment assignment = null;
        if (solver!=null && exam!=null)
            assignment = solver.getAssignment(exam.getUniqueId());
        else if (exam!=null && exam.getAssignedPeriod()!=null)
            assignment = new ExamAssignment(exam);
        ExamType type = (exam == null ? ExamTypeDAO.getInstance().get(((ExamEditForm)frm).getExamType()) : exam.getExamType());
        if (ExamType.sExamTypeMidterm==type.getType()) {
        	MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam == null ? SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()) : exam.getSession(), type, assignment);
        	if (exam!=null) epx.load(exam);
        	frm.setHasNotAvailable(true);
        	if (!op.equals("init")) epx.load(request);
        	request.setAttribute("ExamPeriodGrid", epx.print(editable, (editable?0:exam.getLength())));
        } else {
            PeriodPreferenceModel px = new PeriodPreferenceModel(exam == null ? SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()) : exam.getSession(), assignment, type.getUniqueId());
            if (exam!=null) px.load(exam);
            px.setAllowHard(sessionContext.hasPermission(exam, Right.CanUseHardTimePrefs));
            frm.setHasNotAvailable(px.hasNotAvailable());
            RequiredTimeTable rtt = new RequiredTimeTable(px);
            rtt.setName("PeriodPref");
            if(!op.equals("init")) rtt.update(request);
            request.setAttribute("ExamPeriodGrid", rtt.print(editable, timeVertical, editable, false));
        }
    }
    
    /**
     * Generate html for displaying time pattern grid
     * @param request
     * @param frm
     * @param pg 
     * @param op
     * @throws Exception
     */
    protected void generateTimePatternGrids(
            HttpServletRequest request,
            PreferencesForm frm,
            PreferenceGroup pg, 
            Set tpat,
            String op, 
            boolean timeVertical, boolean editable, Vector leadInstructors ) throws Exception {
        
		Vector timePrefs = null;
		List tps = null;

		if(op.equals("init")) {
		    Set tp = pg.effectivePreferences(TimePref.class, leadInstructors);
		    
		    if(tp.size()>0) {
		    	timePrefs = new Vector(tp);
		        Collections.sort(timePrefs);
		        
		        tps = new Vector();
		        
		        for (Enumeration e=timePrefs.elements();e.hasMoreElements();) {
		        	TimePref timePref = (TimePref)e.nextElement();
		        	tps.add(timePref.getTimePattern()==null?"-1":timePref.getTimePattern().getUniqueId().toString());
		        }
		    } else if (tpat.size()>0) {
		    	Vector x = new Vector(tpat);
		    	Collections.sort(x);
		    	
		        tps = new Vector();

		        for (Enumeration e=x.elements();e.hasMoreElements();) {
		        	TimePattern pat = (TimePattern)e.nextElement();
		        	tps.add(pat.getUniqueId().toString());
		        }
		    	
		    }
		    
		    frm.setTimePatterns(tps);
		} else {
		    tps = frm.getTimePatterns();			
		}
		
		Assignment assignment = null;
		
		if (pg instanceof Class_) {
			if (sessionContext.hasPermission(Right.ClassAssignments)) {
				ClassAssignmentDetails ca = ClassAssignmentDetails.createClassAssignmentDetails(sessionContext, courseTimetablingSolverService.getSolver(), pg.getUniqueId(), true);
				if (ca!=null) {
					String assignmentTable = SuggestionsAction.getAssignmentTable(sessionContext, courseTimetablingSolverService.getSolver(), ca,false, null, true);
					if (assignmentTable!=null)
						request.setAttribute("Suggestions.assignmentInfo", assignmentTable);
				} else {
					ClassAssignmentProxy cap = classAssignmentService.getAssignment();
					if (cap != null) {
						assignment = cap.getAssignment((Class_)pg);
						if (assignment!=null && assignment.getUniqueId()!=null) {
							ca = ClassAssignmentDetails.createClassAssignmentDetailsFromAssignment(sessionContext, assignment.getUniqueId(), true);
							if (ca!=null) {
								String assignmentTable = SuggestionsAction.getAssignmentTable(sessionContext, courseTimetablingSolverService.getSolver(), ca,false, null, true);
								if (assignmentTable!=null)
									request.setAttribute("Suggestions.assignmentInfo", assignmentTable);
							}
						}
					}
				}
			}
		}
		
		
		// Time Pattern not selected
		if(tps==null || tps.isEmpty()) {
			request.setAttribute(TIME_PATTERN_GRID_ATTR, MSG.errorTimePatternNotSelected());
		}
			
		// Time Pattern value set	
		else {
			int idx = 0;
			int deletedTimePatternIdx = -1;
			if ("timePattern".equals(request.getParameter("deleteType"))) {
		        try {
		        	deletedTimePatternIdx = Integer.parseInt(request.getParameter("deleteId"));
		        } catch(Exception e) {}
			}
			
			for (Iterator i=tps.iterator();i.hasNext();idx++) {
				String tp = (String)i.next();

				// Load TimePattern Object
				TimePatternDAO timePatternDao = new TimePatternDAO();
				TimePattern timePattern = (tp.equals("-1")?null:timePatternDao.get(new Long(tp)));

			// 	Display time grid
				RequiredTimeTable rtt = (timePattern==null?TimePattern.getDefaultRequiredTimeTable():timePattern.getRequiredTimeTable(
						assignment == null ? null : assignment.getTimeLocation(), sessionContext.hasPermission(pg, Right.CanUseHardTimePrefs))); 
				rtt.getModel().setDefaultSelection(sessionContext.getUser().getProperty(UserProperty.GridSize));

				rtt.setName("p"+idx);

			// 	Reload all preferences selected
				String reloadCause = request.getParameter("reloadCause");

				if(reloadCause!=null && reloadCause.equals("timePattern")) 
					request.setAttribute(HASH_ATTR, HASH_TIME_PREF);
            
				if ((reloadCause==null || !reloadCause.equals("timePattern")) && !op.equals("init")) {
					if (deletedTimePatternIdx>=0 && idx>=deletedTimePatternIdx) {
						rtt.setName("p"+(idx+1));
						rtt.update(request);
						rtt.setName("p"+idx);
					} else 
						rtt.update(request);
				}

				// Load existing time preferences
				if (timePrefs!=null && timePrefs.size()>0 && op.equals("init")) {
					//rtt.getModel().setDefaults(user);
					rtt.getModel().setPreferences(((TimePref)timePrefs.elementAt(idx)).getPreference());
				}
				
				request.setAttribute(TIME_PATTERN_GRID_ATTR+"_"+idx, rtt.print(editable, timeVertical, editable, false));
			}
		}
    }
    
    /**
     * Initialize Building, Room, Room Feature and Distribution Preferences
     * @param frm
     * @param ss
     */
    protected void initPrefs(
            PreferencesForm frm,
            PreferenceGroup pg, Vector leadInstructors, boolean addBlankRows) {
        
        if (pg==null) {
            if (addBlankRows) frm.addBlankPrefRows();
            return;
        }
        
    	// Room Prefs
    	frm.getRoomPrefs().clear();
    	frm.getRoomPrefLevels().clear();
        Set roomPrefs = pg.effectivePreferences(RoomPref.class, leadInstructors);
        Iterator iter = roomPrefs.iterator();
        while (iter.hasNext()){
            RoomPref rp = (RoomPref) iter.next();
            Debug.debug("Adding room pref ... " + rp.getRoom().getUniqueId().toString());
            frm.addToRoomPrefs(
                    rp.getRoom().getUniqueId().toString(), 
                    rp.getPrefLevel().getUniqueId().toString() );
        }
        
        // Room Feature Prefs
    	frm.getRoomFeaturePrefs().clear();
    	frm.getRoomFeaturePrefLevels().clear();
        Set roomFeatPrefs = pg.effectivePreferences(RoomFeaturePref.class, leadInstructors);
        iter = roomFeatPrefs.iterator();
        while (iter.hasNext()){
            RoomFeaturePref rfp = (RoomFeaturePref) iter.next();
            Debug.debug("Adding room feature pref ... " + rfp.getRoomFeature().getUniqueId().toString());
            frm.addToRoomFeatPrefs(
                    rfp.getRoomFeature().getUniqueId().toString(), 
                    rfp.getPrefLevel().getUniqueId().toString() );
        }

        // Building Prefs
    	frm.getBldgPrefs().clear();
    	frm.getBldgPrefLevels().clear();
        Set bldgPrefs = pg.effectivePreferences(BuildingPref.class, leadInstructors);
        iter = bldgPrefs.iterator();
        while (iter.hasNext()){
            BuildingPref bp = (BuildingPref) iter.next();
            Debug.debug("Adding building pref ... " + bp.getBuilding().getUniqueId().toString());
            frm.addToBldgPrefs(
                    bp.getBuilding().getUniqueId().toString(), 
                    bp.getPrefLevel().getUniqueId().toString() );
        }
        
        // Distribution Prefs
    	frm.getDistPrefs().clear();
    	frm.getDistPrefLevels().clear();
        Set distPrefs = pg.effectivePreferences(DistributionPref.class, leadInstructors);
        iter = distPrefs.iterator();
        while (iter.hasNext()){
            DistributionPref dp = (DistributionPref) iter.next();
            Debug.debug("Adding distribution pref ... " + dp.getDistributionType().getUniqueId().toString());
            frm.addToDistPrefs(
                    dp.getDistributionType().getUniqueId().toString(), 
                    dp.getPrefLevel().getUniqueId().toString() );
        }

        // Period Prefs
        /*
        frm.getPeriodPrefs().clear();
        frm.getPeriodPrefLevels().clear();
        Set periodPrefs = pg.effectivePreferences(ExamPeriodPref.class, leadInstructors);
        iter = periodPrefs.iterator();
        while (iter.hasNext()){
            ExamPeriodPref xp = (ExamPeriodPref) iter.next();
            Debug.debug("Adding period pref ... " + xp.getExamPeriod().getUniqueId().toString());
            frm.addToPeriodPrefs(
                    xp.getExamPeriod().getUniqueId().toString(), 
                    xp.getPrefLevel().getUniqueId().toString() );
        }
        */
        
        // Room group Prefs
    	frm.getRoomGroups().clear();
    	frm.getRoomGroupLevels().clear();
        Set rgPrefs = pg.effectivePreferences(RoomGroupPref.class, leadInstructors);
        iter = rgPrefs.iterator();
        while (iter.hasNext()){
            RoomGroupPref bp = (RoomGroupPref) iter.next();
            Debug.debug("Adding room group pref ... " + bp.getRoomGroup().getUniqueId().toString());
            frm.addToRoomGroups(
                    bp.getRoomGroup().getUniqueId().toString(), 
                    bp.getPrefLevel().getUniqueId().toString() );
        }

        // Date Pattern Prefs
        Set datePatternPrefs = pg.effectivePreferences(DatePatternPref.class);
    	frm.getDatePatternPrefs().clear();
    	frm.getDatePatternPrefLevels().clear();
    	iter = datePatternPrefs.iterator();
    	while (iter.hasNext()){
    		DatePatternPref dp = (DatePatternPref) iter.next();
    		if (!dp.appliesTo(pg)) continue;
    		Debug.debug("Adding date pattern pref ... " + dp.getDatePattern().getUniqueId().toString());
    		frm.addToDatePatternPrefs(
                dp.getDatePattern().getUniqueId().toString(), 
                dp.getPrefLevel().getUniqueId().toString() );
    	}

        if (addBlankRows) frm.addBlankPrefRows();
    }

    /**
     * Clear Preference Lists
     * @param request
     * @param frm
     */
    protected void clearPrefs(
            HttpServletRequest request,
            PreferencesForm frm) {
        frm.clearPrefs();
    }
}
