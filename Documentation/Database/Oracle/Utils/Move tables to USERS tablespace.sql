/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC
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

/**
 * If the database was created using the IMP tool from an older version of wobegon.dat or blank.dat (prior to UniTime
 * 3.4), run this script to move the tables from SYSTEM tablespace to USERS.
 **/ 
alter table EVENT_NOTE_MEETING move tablespace users;
alter table DATE_MAPPING move tablespace users;
alter table POS_ACAD_AREA_MAJOR move tablespace users;
alter table POS_ACAD_AREA_MINOR move tablespace users;
alter table POS_MAJOR move tablespace users;
alter table POS_MINOR move tablespace users;
alter table PREFERENCE_LEVEL move tablespace users;
alter table RELATED_COURSE_INFO move tablespace users;
alter table ROLES move tablespace users;
alter table ROOM move tablespace users;
alter table ROOM_DEPT move tablespace users;
alter table ROOM_FEATURE move tablespace users;
alter table ROOM_FEATURE_PREF move tablespace users;
alter table ROOM_GROUP move tablespace users;
alter table ROOM_GROUP_PREF move tablespace users;
alter table ROOM_GROUP_ROOM move tablespace users;
alter table ROOM_JOIN_ROOM_FEATURE move tablespace users;
alter table ROOM_PREF move tablespace users;
alter table ROOM_TYPE move tablespace users;
alter table ROOM_TYPE_OPTION move tablespace users;
alter table SAVED_HQL move tablespace users;
alter table SCHEDULING_SUBPART move tablespace users;
alter table SECTIONING_INFO move tablespace users;
alter table SECTIONING_LOG move tablespace users;
alter table SECTIONING_STATUS move tablespace users;
alter table SESSIONS move tablespace users;
alter table SETTINGS move tablespace users;
alter table SOLUTION move tablespace users;
alter table SOLVER_GROUP move tablespace users;
alter table SOLVER_GR_TO_TT_MGR move tablespace users;
alter table SOLVER_INFO move tablespace users;
alter table SOLVER_INFO_DEF move tablespace users;
alter table SOLVER_PARAMETER move tablespace users;
alter table SOLVER_PARAMETER_DEF move tablespace users;
alter table SOLVER_PARAMETER_GROUP move tablespace users;
alter table SOLVER_PREDEF_SETTING move tablespace users;
alter table SPONSORING_ORGANIZATION move tablespace users;
alter table STAFF move tablespace users;
alter table STANDARD_EVENT_NOTE move tablespace users;
alter table STUDENT move tablespace users;
alter table STUDENT_ACAD_AREA move tablespace users;
alter table STUDENT_ACCOMODATION move tablespace users;
alter table STUDENT_CLASS_ENRL move tablespace users;
alter table STUDENT_ENRL move tablespace users;
alter table STUDENT_ENRL_MSG move tablespace users;
alter table STUDENT_GROUP move tablespace users;
alter table CLASS_ move tablespace users;
alter table CLASS_INSTRUCTOR move tablespace users;
alter table CLASS_WAITLIST move tablespace users;
alter table CONSTRAINT_INFO move tablespace users;
alter table COURSE_CATALOG move tablespace users;
alter table COURSE_CREDIT_TYPE move tablespace users;
alter table COURSE_CREDIT_UNIT_CONFIG move tablespace users;
alter table COURSE_CREDIT_UNIT_TYPE move tablespace users;
alter table COURSE_DEMAND move tablespace users;
alter table COURSE_OFFERING move tablespace users;
alter table COURSE_REQUEST move tablespace users;
alter table COURSE_REQUEST_OPTION move tablespace users;
alter table COURSE_SUBPART_CREDIT move tablespace users;
alter table CRSE_CREDIT_FORMAT move tablespace users;
alter table DATE_PATTERN move tablespace users;
alter table DATE_PATTERN_DEPT move tablespace users;
alter table DATE_PATTERN_PARENT move tablespace users;
alter table DATE_PATTERN_PREF move tablespace users;
alter table DEMAND_OFFR_TYPE move tablespace users;
alter table DEPARTMENT move tablespace users;
alter table DEPARTMENTAL_INSTRUCTOR move tablespace users;
alter table DEPT_STATUS_TYPE move tablespace users;
alter table DEPT_TO_TT_MGR move tablespace users;
alter table DESIGNATOR move tablespace users;
alter table DISTRIBUTION_OBJECT move tablespace users;
alter table DISTRIBUTION_PREF move tablespace users;
alter table DISTRIBUTION_TYPE move tablespace users;
alter table DIST_TYPE_DEPT move tablespace users;
alter table EVENT move tablespace users;
alter table EVENT_CONTACT move tablespace users;
alter table EVENT_JOIN_EVENT_CONTACT move tablespace users;
alter table EVENT_NOTE move tablespace users;
alter table EXACT_TIME_MINS move tablespace users;
alter table EXAM move tablespace users;
alter table EXAM_INSTRUCTOR move tablespace users;
alter table EXAM_LOCATION_PREF move tablespace users;
alter table EXAM_OWNER move tablespace users;
alter table EXAM_PERIOD move tablespace users;
alter table EXAM_PERIOD_PREF move tablespace users;
alter table EXAM_ROOM_ASSIGNMENT move tablespace users;
alter table EXTERNAL_BUILDING move tablespace users;
alter table EXTERNAL_ROOM move tablespace users;
alter table SCRIPT move tablespace users;
alter table SCRIPT_PARAMETER move tablespace users;
alter table SESSION_CONFIG move tablespace users;
alter table FEATURE_TYPE move tablespace users;
alter table EXTERNAL_ROOM_DEPARTMENT move tablespace users;
alter table EXTERNAL_ROOM_FEATURE move tablespace users;
alter table FREE_TIME move tablespace users;
alter table HISTORY move tablespace users;
alter table INSTRUCTIONAL_OFFERING move tablespace users;
alter table INSTR_OFFERING_CONFIG move tablespace users;
alter table ITYPE_DESC move tablespace users;
alter table JENRL move tablespace users;
alter table LASTLIKE_COURSE_DEMAND move tablespace users;
alter table MANAGER_SETTINGS move tablespace users;
alter table MEETING move tablespace users;
alter table NON_UNIVERSITY_LOCATION move tablespace users;
alter table OFFERING_COORDINATOR move tablespace users;
alter table OFFR_CONSENT_TYPE move tablespace users;
alter table OFFR_GROUP move tablespace users;
alter table OFFR_GROUP_OFFERING move tablespace users;
alter table POSITION_TYPE move tablespace users;
alter table RIGHTS move tablespace users;
alter table SECTIONING_COURSE_TYPES move tablespace users;
alter table MESSAGE_LOG move tablespace users;
alter table COURSE_TYPE move tablespace users;
alter table STUDENT_MAJOR move tablespace users;
alter table STUDENT_MINOR move tablespace users;
alter table STUDENT_SECT_HIST move tablespace users;
alter table STUDENT_TO_ACOMODATION move tablespace users;
alter table STUDENT_TO_GROUP move tablespace users;
alter table SUBJECT_AREA move tablespace users;
alter table TIMETABLE_MANAGER move tablespace users;
alter table TIME_PATTERN move tablespace users;
alter table TIME_PATTERN_DAYS move tablespace users;
alter table TIME_PATTERN_DEPT move tablespace users;
alter table TIME_PATTERN_TIME move tablespace users;
alter table TIME_PREF move tablespace users;
alter table TMTBL_MGR_TO_ROLES move tablespace users;
alter table TRAVEL_TIME move tablespace users;
alter table USERS move tablespace users;
alter table USER_DATA move tablespace users;
alter table WAITLIST move tablespace users;
alter table XCONFLICT move tablespace users;
alter table XCONFLICT_EXAM move tablespace users;
alter table XCONFLICT_INSTRUCTOR move tablespace users;
alter table XCONFLICT_STUDENT move tablespace users;
alter table ACADEMIC_AREA move tablespace users;
alter table ACADEMIC_CLASSIFICATION move tablespace users;
alter table APPLICATION_CONFIG move tablespace users;
alter table ASSIGNED_INSTRUCTORS move tablespace users;
alter table ASSIGNED_ROOMS move tablespace users;
alter table ASSIGNMENT move tablespace users;
alter table BUILDING move tablespace users;
alter table BUILDING_PREF move tablespace users;
alter table CHANGE_LOG move tablespace users;
alter table EXAM_TYPE move tablespace users;
alter table ROOM_EXAM_TYPE move tablespace users;

alter index PK_ROOM_EXAM_TYPE rebuild tablespace users;
alter index PK_EXAM_TYPE rebuild tablespace users;
alter index PK_ROOM_TYPE_OPTION rebuild tablespace users;
alter index PK_RIGHTS rebuild tablespace users;
alter index PK_SCRIPT rebuild tablespace users;
alter index PK_DATE_MAPPING rebuild tablespace users;
alter index PK_EVENT_NOTE_MEETING rebuild tablespace users;
alter index PK_FEATURE_TYPE rebuild tablespace users;
alter index PK_SECTIONING_COURSE_TYPES rebuild tablespace users;
alter index PK_COURSE_TYPE rebuild tablespace users;
alter index IDX_MESSAGE_LOG rebuild tablespace users;
alter index PK_MESSAGE_LOG rebuild tablespace users;
alter index IDX_DEPARTMENT_STATUS_TYPE rebuild tablespace users;
alter index IDX_DEPARTMENT_SOLVER_GRP rebuild tablespace users;
alter index UK_DEPARTMENT_DEPT_CODE rebuild tablespace users;
alter index PK_DEPARTMENT rebuild tablespace users;
alter index UK_DEMAND_OFFR_TYPE_REF rebuild tablespace users;
alter index UK_DEMAND_OFFR_TYPE_LABEL rebuild tablespace users;
alter index PK_DEMAND_OFFR_TYPE rebuild tablespace users;
alter index PK_DATE_PATTERN_PREF rebuild tablespace users;
alter index PK_DATE_PATTERN_PARENT rebuild tablespace users;
alter index PK_DATE_PATTERN_DEPT rebuild tablespace users;
alter index IDX_DATE_PATTERN_SESSION rebuild tablespace users;
alter index PK_DATE_PATTERN rebuild tablespace users;
alter index IDX_BUILDING_PREF_OWNER rebuild tablespace users;
alter index UK_CRSE_CREDIT_FORMAT_REF rebuild tablespace users;
alter index PK_CRSE_CREDIT_FORMAT rebuild tablespace users;
alter index PK_COURSE_SUBPART_CREDIT rebuild tablespace users;
alter index IDX_COURSE_REQUEST_OPTION_REQ rebuild tablespace users;
alter index PK_COURSE_REQUEST rebuild tablespace users;
alter index PK_COURSE_REQUEST_OPTION rebuild tablespace users;
alter index IDX_COURSE_REQUEST_OFFERING rebuild tablespace users;
alter index IDX_COURSE_REQUEST_DEMAND rebuild tablespace users;
alter index PK_DEPT_STATUS rebuild tablespace users;
alter index IDX_DEPT_INSTR_POSITION_TYPE rebuild tablespace users;
alter index IDX_DEPT_INSTR_DEPT rebuild tablespace users;
alter index PK_DEPT_INSTR rebuild tablespace users;
alter index PK_DISTRIBUTION_TYPE rebuild tablespace users;
alter index IDX_DISTRIBUTION_PREF_TYPE rebuild tablespace users;
alter index IDX_DISTRIBUTION_PREF_OWNER rebuild tablespace users;
alter index IDX_DISTRIBUTION_PREF_LEVEL rebuild tablespace users;
alter index IDX_COURSE_OFFERING_INSTR_OFFR rebuild tablespace users;
alter index IDX_COURSE_OFFERING_DEMD_OFFR rebuild tablespace users;
alter index IDX_OFFR_GROUP_SESSION rebuild tablespace users;
alter index IDX_OFFR_GROUP_DEPT rebuild tablespace users;
alter index PK_OFFR_GROUP_UID rebuild tablespace users;
alter index UK_OFFR_CONSENT_TYPE_REF rebuild tablespace users;
alter index UK_OFFR_CONSENT_TYPE_LABEL rebuild tablespace users;
alter index PK_OFFR_CONSENT_TYPE rebuild tablespace users;
alter index PK_OFFERING_COORDINATOR rebuild tablespace users;
alter index IDX_LOCATION_PERMID rebuild tablespace users;
alter index IDX_NON_UNIV_LOC_SESSION rebuild tablespace users;
alter index PK_NON_UNIV_LOC rebuild tablespace users;
alter index PK_MEETING_UNIQUEID rebuild tablespace users;
alter index IDX_MANAGER_SETTINGS_MANAGER rebuild tablespace users;
alter index IDX_MANAGER_SETTINGS_KEY rebuild tablespace users;
alter index PK_MANAGER_SETTINGS rebuild tablespace users;
alter index IDX_COURSE_DEMAND_STUDENT rebuild tablespace users;
alter index IDX_COURSE_DEMAND_FREE_TIME rebuild tablespace users;
alter index PK_COURSE_DEMAND rebuild tablespace users;
alter index UK_CRS_CRDT_UNIT_TYPE_REF rebuild tablespace users;
alter index PK_CRS_CRDT_UNIT_TYPE rebuild tablespace users;
alter index IDX_CRS_CRDT_UNIT_CFG_OWNER rebuild tablespace users;
alter index IDX_CRS_CRDT_UNIT_CFG_IO_OWN rebuild tablespace users;
alter index IDX_CRS_CRDT_UNIT_CFG_CRD_TYPE rebuild tablespace users;
alter index PK_CRS_CRDT_UNIT_CFG rebuild tablespace users;
alter index UK_COURSE_CREDIT_TYPE_REF rebuild tablespace users;
alter index PK_COURSE_CREDIT_TYPE rebuild tablespace users;
alter index IDX_COURSE_CATALOG rebuild tablespace users;
alter index PK_COURSE_CATALOG rebuild tablespace users;
alter index IDX_CONSTRAINT_INFO rebuild tablespace users;
alter index UK_CONSTRAINT_INFO_SOLV_ASSGN rebuild tablespace users;
alter index IDX_CLASS_WAITLIST_STUDENT rebuild tablespace users;
alter index IDX_CLASS_WAITLIST_REQ rebuild tablespace users;
alter index UK_COURSE_OFFERING_SUBJ_CRS rebuild tablespace users;
alter index UK_BUILDING rebuild tablespace users;
alter index PK_BUILDING rebuild tablespace users;
alter index PK_APPLICATION_CONFIG rebuild tablespace users;
alter index PK_ACAD_CLASS rebuild tablespace users;
alter index UK_ACADEMIC_AREA rebuild tablespace users;
alter index PK_ACADEMIC_AREA rebuild tablespace users;
alter index PK_SESSION_CONFIG rebuild tablespace users;
alter index PK_SCRIPT_PARAMETER rebuild tablespace users;
alter index IDX_STUDENT_ENRL_MSG_DEM rebuild tablespace users;
alter index PK_STUDENT_ENRL_MSG rebuild tablespace users;
alter index IDX_STUDENT_ENRL_CLASS rebuild tablespace users;
alter index IDX_STUDENT_ENRL rebuild tablespace users;
alter index PK_STUDENT_ENRL rebuild tablespace users;
alter index IDX_STUDENT_CLASS_ENRL_COURSE rebuild tablespace users;
alter index IDX_STUDENT_CLASS_ENRL_STUDENT rebuild tablespace users;
alter index IDX_STUDENT_CLASS_ENRL_REQ rebuild tablespace users;
alter index IDX_STUDENT_CLASS_ENRL_CLASS rebuild tablespace users;
alter index PK_STUDENT_CLASS_ENRL rebuild tablespace users;
alter index PK_STUDENT_ACCOMODATION rebuild tablespace users;
alter index IDX_STUDENT_ACAD_AREA rebuild tablespace users;
alter index UK_STUDENT_ACAD_AREA rebuild tablespace users;
alter index PK_STUDENT_ACAD_AREA rebuild tablespace users;
alter index IDX_STUDENT_SESSION rebuild tablespace users;
alter index PK_STUDENT rebuild tablespace users;
alter index PK_STANDARD_EVENT_NOTE rebuild tablespace users;
alter index PK_STAFF rebuild tablespace users;
alter index PK_SPONSORING_ORGANIZATION rebuild tablespace users;
alter index PK_SOLV_PREDEF_SETTG rebuild tablespace users;
alter index PK_SOLVER_PARAM_GROUP rebuild tablespace users;
alter index PK_TIME_PREF rebuild tablespace users;
alter index IDX_TIME_PATTERN_TIME rebuild tablespace users;
alter index PK_TIME_PATTERN_TIME rebuild tablespace users;
alter index PK_TIME_PATTERN_DEPT rebuild tablespace users;
alter index IDX_TIME_PATTERN_DAYS rebuild tablespace users;
alter index PK_TIME_PATTERN_DAYS rebuild tablespace users;
alter index IDX_TIME_PATTERN_SESSION rebuild tablespace users;
alter index PK_TIME_PATTERN rebuild tablespace users;
alter index UK_TIMETABLE_MANAGER_PUID rebuild tablespace users;
alter index PK_TIMETABLE_MANAGER rebuild tablespace users;
alter index IDX_SUBJECT_AREA_DEPT rebuild tablespace users;
alter index UK_SUBJECT_AREA rebuild tablespace users;
alter index PK_SUBJECT_AREA rebuild tablespace users;
alter index PK_STUDENT_TO_GROUP rebuild tablespace users;
alter index PK_STUDENT_TO_ACOMODATION rebuild tablespace users;
alter index IDX_STUDENT_SECT_HIST_STUDENT rebuild tablespace users;
alter index PK_STUDENT_SECT_HIST rebuild tablespace users;
alter index PK_STUDENT_MINOR rebuild tablespace users;
alter index PK_STUDENT_MAJOR rebuild tablespace users;
alter index UK_STUDENT_GROUP_SESSION_SIS rebuild tablespace users;
alter index PK_STUDENT_GROUP rebuild tablespace users;
alter index PK_XCONFLICT_INSTRUCTOR rebuild tablespace users;
alter index IDX_XCONFLICT_EXAM rebuild tablespace users;
alter index PK_XCONFLICT_EXAM rebuild tablespace users;
alter index PK_XCONFLICT rebuild tablespace users;
alter index IDX_WAITLIST_STUDENT rebuild tablespace users;
alter index IDX_WAITLIST_OFFERING rebuild tablespace users;
alter index PK_WAITLIST rebuild tablespace users;
alter index PK_USER_DATA rebuild tablespace users;
alter index PK_USERS rebuild tablespace users;
alter index PK_TRAVEL_TIME rebuild tablespace users;
alter index UK_TMTBL_MGR_TO_ROLES_MGR_ROLE rebuild tablespace users;
alter index PK_TMTBL_MGR_TO_ROLES rebuild tablespace users;
alter index IDX_TIME_PREF_TIME_PTRN rebuild tablespace users;
alter index IDX_TIME_PREF_PREF_LEVEL rebuild tablespace users;
alter index IDX_TIME_PREF_OWNER rebuild tablespace users;
alter index IDX_SOLV_PARAM_DEF_GR rebuild tablespace users;
alter index PK_SOLV_PARAM_DEF rebuild tablespace users;
alter index IDX_SOLVER_PARAM_SOLUTION rebuild tablespace users;
alter index IDX_SOLVER_PARAM_PREDEF rebuild tablespace users;
alter index IDX_SOLVER_PARAM_DEF rebuild tablespace users;
alter index PK_SOLVER_INFO_DEF rebuild tablespace users;
alter index IDX_SOLVER_INFO_SOLUTION rebuild tablespace users;
alter index IDX_SOLVER_INFO rebuild tablespace users;
alter index PK_SOLVER_INFO rebuild tablespace users;
alter index PK_SOLVER_GR_TO_TT_MGR rebuild tablespace users;
alter index IDX_SOLVER_GROUP_SESSION rebuild tablespace users;
alter index PK_SOLVER_GROUP rebuild tablespace users;
alter index IDX_SOLUTION_OWNER rebuild tablespace users;
alter index PK_SOLUTION rebuild tablespace users;
alter index PK_SETTINGS rebuild tablespace users;
alter index IDX_SESSIONS_STATUS_TYPE rebuild tablespace users;
alter index IDX_SESSIONS_DATE_PATTERN rebuild tablespace users;
alter index PK_SESSIONS rebuild tablespace users;
alter index PK_SECT_STATUS rebuild tablespace users;
alter index IDX_SECTIONING_LOG rebuild tablespace users;
alter index PK_SECTIONING_LOG rebuild tablespace users;
alter index PK_SECTIONING_INFO rebuild tablespace users;
alter index IDX_SCHED_SUBPART_PARENT rebuild tablespace users;
alter index IDX_SCHED_SUBPART_ITYPE rebuild tablespace users;
alter index IDX_SCHED_SUBPART_DATE_PATTERN rebuild tablespace users;
alter index PK_SAVED_HQL rebuild tablespace users;
alter index PK_ROOM_TYPE rebuild tablespace users;
alter index IDX_ROOM_PREF_OWNER rebuild tablespace users;
alter index IDX_ROOM_PREF_LEVEL rebuild tablespace users;
alter index UK_ROOM_JOIN_ROOM_FEAT_RM_FEAT rebuild tablespace users;
alter index PK_ROOM_GROUP_ROOM rebuild tablespace users;
alter index IDX_ROOM_GROUP_PREF_ROOM_GRP rebuild tablespace users;
alter index IDX_ROOM_GROUP_PREF_OWNER rebuild tablespace users;
alter index IDX_ROOM_GROUP_PREF_LEVEL rebuild tablespace users;
alter index PK_ROOM_GROUP_PREF rebuild tablespace users;
alter index IDX_ROOM_GROUP_SESSION rebuild tablespace users;
alter index IDX_ROOM_GROUP_DEPT rebuild tablespace users;
alter index PK_ROOM_GROUP_UID rebuild tablespace users;
alter index IDX_ROOM_FEAT_PREF_ROOM_FEAT rebuild tablespace users;
alter index IDX_ROOM_FEAT_PREF_OWNER rebuild tablespace users;
alter index IDX_ROOM_FEAT_PREF_LEVEL rebuild tablespace users;
alter index PK_ROOM_FEAT_PREF rebuild tablespace users;
alter index IDX_ROOM_FEATURE_DEPT rebuild tablespace users;
alter index PK_ROOM_FEATURE rebuild tablespace users;
alter index IDX_ROOM_DEPT_ROOM rebuild tablespace users;
alter index IDX_ROOM_DEPT_DEPT rebuild tablespace users;
alter index PK_ROOM_DEPT rebuild tablespace users;
alter index IDX_ROOM_PERMID rebuild tablespace users;
alter index IDX_ROOM_BUILDING rebuild tablespace users;
alter index UK_ROOM rebuild tablespace users;
alter index PK_ROOM rebuild tablespace users;
alter index UK_ROLES_REFERENCE rebuild tablespace users;
alter index UK_ROLES_ABBV rebuild tablespace users;
alter index PK_ROLES rebuild tablespace users;
alter index IDX_EVENT_OWNER_OWNER rebuild tablespace users;
alter index IDX_EVENT_OWNER_EVENT rebuild tablespace users;
alter index PK_RELATED_CRS_INFO rebuild tablespace users;
alter index UK_PREFERENCE_LEVEL_PREF_ID rebuild tablespace users;
alter index PK_PREFERENCE_LEVEL rebuild tablespace users;
alter index PK_POS_MINOR rebuild tablespace users;
alter index PK_POS_MAJOR rebuild tablespace users;
alter index PK_POS_ACAD_AREA_MINOR rebuild tablespace users;
alter index PK_POS_ACAD_AREA_MAJOR rebuild tablespace users;
alter index UK_POSITION_TYPE_REF rebuild tablespace users;
alter index UK_POSITION_TYPE_LABEL rebuild tablespace users;
alter index PK_POSITION_TYPE rebuild tablespace users;
alter index PK_OFFR_GROUP_OFFERING rebuild tablespace users;
alter index PK_DISTRIBUTION_PREF rebuild tablespace users;
alter index IDX_DISTRIBUTION_OBJECT_PREF rebuild tablespace users;
alter index IDX_DISTRIBUTION_OBJECT_PG rebuild tablespace users;
alter index PK_DISTRIBUTION_OBJECT rebuild tablespace users;
alter index UK_DESIGNATOR_CODE rebuild tablespace users;
alter index PK_DESIGNATOR rebuild tablespace users;
alter index PK_DEPT_TO_TT_MGR_UID rebuild tablespace users;
alter index IDX_CLASS_WAITLIST_CLASS rebuild tablespace users;
alter index PK_CLASS_WAITLIST rebuild tablespace users;
alter index IDX_CLASS_INSTRUCTOR_INSTR rebuild tablespace users;
alter index IDX_CLASS_INSTRUCTOR_CLASS rebuild tablespace users;
alter index IDX_BUILDING_PREF_LEVEL rebuild tablespace users;
alter index IDX_BUILDING_PREF_BLDG rebuild tablespace users;
alter index PK_BUILDING_PREF rebuild tablespace users;
alter index IDX_ASSIGNMENT_TIME_PATTERN rebuild tablespace users;
alter index IDX_ASSIGNMENT_SOLUTION_INDEX rebuild tablespace users;
alter index IDX_ASSIGNMENT_CLASS rebuild tablespace users;
alter index PK_ASSIGNMENT rebuild tablespace users;
alter index IDX_ASSIGNED_ROOMS rebuild tablespace users;
alter index PK_ASSIGNED_ROOMS rebuild tablespace users;
alter index IDX_ASSIGNED_INSTRUCTORS rebuild tablespace users;
alter index PK_ASSIGNED_INSTRUCTORS rebuild tablespace users;
alter index PK_EXAM_INSTRUCTOR rebuild tablespace users;
alter index PK_EXAM rebuild tablespace users;
alter index IDX_EXACT_TIME_MINS rebuild tablespace users;
alter index PK_EXACT_TIME_MINS rebuild tablespace users;
alter index PK_EVENT_NOTE_UNIQUEID rebuild tablespace users;
alter index PK_EVENT_CONTACT_UNIQUEID rebuild tablespace users;
alter index IDX_EVENT_EXAM rebuild tablespace users;
alter index IDX_EVENT_CLASS rebuild tablespace users;
alter index PK_EVENT_UNIQUEID rebuild tablespace users;
alter index PK_DIST_TYPE_DEPT rebuild tablespace users;
alter index UK_DISTRIBUTION_TYPE_REQ_ID rebuild tablespace users;
alter index PK_INSTR_OFFR rebuild tablespace users;
alter index IDX_HISTORY_SESSION rebuild tablespace users;
alter index PK_HISTORY rebuild tablespace users;
alter index PK_FREE_TIME rebuild tablespace users;
alter index PK_EXTERNAL_ROOM_FEATURE rebuild tablespace users;
alter index PK_EXTERNAL_ROOM_DEPT rebuild tablespace users;
alter index IDX_EXTERNAL_ROOM rebuild tablespace users;
alter index PK_EXTERNAL_ROOM rebuild tablespace users;
alter index IDX_EXTERNAL_BUILDING rebuild tablespace users;
alter index PK_EXTERNAL_BLDG rebuild tablespace users;
alter index PK_EXAM_ROOM_ASSIGNMENT rebuild tablespace users;
alter index PK_EXAM_PERIOD_PREF rebuild tablespace users;
alter index PK_EXAM_PERIOD rebuild tablespace users;
alter index IDX_EXAM_OWNER_COURSE rebuild tablespace users;
alter index IDX_EXAM_OWNER_OWNER rebuild tablespace users;
alter index IDX_EXAM_OWNER_EXAM rebuild tablespace users;
alter index PK_EXAM_OWNER rebuild tablespace users;
alter index IDX_EXAM_LOCATION_PREF rebuild tablespace users;
alter index PK_EXAM_LOCATION_PREF rebuild tablespace users;
alter index PK_XCONFLICT_STUDENT rebuild tablespace users;
alter index IDX_SCHED_SUBPART_CONFIG rebuild tablespace users;
alter index PK_SCHED_SUBPART rebuild tablespace users;
alter index PK_ROOM_PREF rebuild tablespace users;
alter index IDX_CLASS_MANAGING_DEPT rebuild tablespace users;
alter index PK_CLASS_INSTRUCTOR_UNIQUEID rebuild tablespace users;
alter index IDX_CLASS_DATEPATT rebuild tablespace users;
alter index PK_CLASS rebuild tablespace users;
alter index IDX_CHANGE_LOG_SUBJAREA rebuild tablespace users;
alter index IDX_CHANGE_LOG_SESSIONMGR rebuild tablespace users;
alter index IDX_CHANGE_LOG_OBJECT rebuild tablespace users;
alter index IDX_CHANGE_LOG_DEPARTMENT rebuild tablespace users;
alter index PK_CHANGE_LOG rebuild tablespace users;
alter index IDX_LL_COURSE_DEMAND_PERMID rebuild tablespace users;
alter index IDX_LL_COURSE_DEMAND_STUDENT rebuild tablespace users;
alter index IDX_LL_COURSE_DEMAND_COURSE rebuild tablespace users;
alter index PK_LASTLIKE_COURSE_DEMAND rebuild tablespace users;
alter index IDX_JENRL_CLASS2 rebuild tablespace users;
alter index IDX_JENRL_CLASS1 rebuild tablespace users;
alter index IDX_JENRL rebuild tablespace users;
alter index PK_JENRL rebuild tablespace users;
alter index PK_ITYPE_DESC rebuild tablespace users;
alter index IDX_INSTR_OFFR_CFG_INSTR_OFFR rebuild tablespace users;
alter index UK_INSTR_OFFR_CFG_NAME rebuild tablespace users;
alter index PK_INSTR_OFFR_CFG rebuild tablespace users;
alter index IDX_CLASS_SUBPART_ID rebuild tablespace users;
alter index IDX_COURSE_OFFERING_CONTROL rebuild tablespace users;
alter index IDX_CLASS_PARENT rebuild tablespace users;
alter index PK_COURSE_OFFERING rebuild tablespace users;