/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC
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

/*
 * Generated by the following SQL:
 *
 * select 'alter table ' || lower(table_name) || ' modify ' || lower(column_name) || ' varchar2(' || data_length || ' char);'
 * from all_tab_columns where owner='TIMETABLE' and data_type='VARCHAR2' and char_used='B';
 *
 * select '		<plsql>alter table %SCHEMA%.' || lower(table_name) || ' modify ' || lower(column_name) || ' varchar2(' || data_length || ' char)</plsql>'
 * from all_tab_columns where owner='TIMETABLE' and data_type='VARCHAR2' and char_used='B';
 */

alter table application_config modify name varchar2(1000 char);
alter table course_credit_type modify reference varchar2(20 char);
alter table course_credit_type modify label varchar2(60 char);
alter table course_credit_unit_type modify reference varchar2(20 char);
alter table course_credit_unit_type modify label varchar2(60 char);
alter table course_credit_unit_type modify abbreviation varchar2(10 char);
alter table crse_credit_format modify reference varchar2(20 char);
alter table crse_credit_format modify label varchar2(60 char);
alter table demand_offr_type modify reference varchar2(20 char);
alter table demand_offr_type modify label varchar2(60 char);
alter table dept_status_type modify reference varchar2(20 char);
alter table dept_status_type modify label varchar2(60 char);
alter table designator modify code varchar2(6 char);
alter table distribution_type modify reference varchar2(20 char);
alter table distribution_type modify label varchar2(60 char);
alter table distribution_type modify sequencing_required varchar2(1 char);
alter table distribution_type modify description varchar2(2048 char);
alter table distribution_type modify abbreviation varchar2(20 char);
alter table event_contact modify external_id varchar2(40 char);
alter table event_contact modify email varchar2(200 char);
alter table event_contact modify phone varchar2(25 char);
alter table event_contact modify firstname varchar2(100 char);
alter table event_contact modify middlename varchar2(100 char);
alter table event_contact modify lastname varchar2(100 char);
alter table exam_type modify reference varchar2(20 char);
alter table exam_type modify label varchar2(60 char);
alter table feature_type modify reference varchar2(20 char);
alter table feature_type modify label varchar2(60 char);
alter table history modify subclass varchar2(10 char);
alter table offr_consent_type modify reference varchar2(20 char);
alter table offr_consent_type modify label varchar2(60 char);
alter table offr_group modify name varchar2(20 char);
alter table offr_group modify description varchar2(200 char);
alter table position_type modify reference varchar2(20 char);
alter table position_type modify label varchar2(60 char);
alter table rights modify value varchar2(200 char);
alter table roles modify reference varchar2(20 char);
alter table roles modify abbv varchar2(40 char);
alter table room_feature modify discriminator varchar2(10 char);
alter table room_type modify reference varchar2(20 char);
alter table room_type modify label varchar2(60 char);
alter table sectioning_status modify reference varchar2(20 char);
alter table sectioning_status modify label varchar2(60 char);
alter table solver_group modify name varchar2(50 char);
alter table solver_group modify abbv varchar2(50 char);
alter table solver_parameter_group modify condition varchar2(250 char);
alter table sponsoring_organization modify name varchar2(100 char);
alter table sponsoring_organization modify email varchar2(200 char);
alter table staff modify pos_code varchar2(20 char);
alter table standard_event_note modify reference varchar2(20 char);
alter table standard_event_note modify note varchar2(1000 char);
alter table timetable_manager modify external_uid varchar2(40 char);
alter table timetable_manager modify first_name varchar2(100 char);
alter table timetable_manager modify middle_name varchar2(100 char);
alter table timetable_manager modify last_name varchar2(100 char);
alter table timetable_manager modify email_address varchar2(200 char);
alter table users modify username varchar2(15 char);
alter table users modify password varchar2(25 char);
alter table users modify external_uid varchar2(40 char);
alter table user_data modify external_uid varchar2(12 char);
alter table user_data modify name varchar2(100 char);

/*
 * Update database version
 */

update application_config set value='140' where name='tmtbl.db.version';

commit;