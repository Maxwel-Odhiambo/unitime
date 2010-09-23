package org.unitime.timetable.solver.curricula;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

public class StudentCourseRequests implements StudentCourseDemands {
	protected Hashtable<Long, Hashtable<Long, Set<WeightedStudentId>>> iDemands = new Hashtable<Long, Hashtable<Long, Set<WeightedStudentId>>>();
	protected org.hibernate.Session iHibSession = null;
	protected Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = null;
	protected Long iSessionId = null;
	
	public StudentCourseRequests(DataProperties conf) {
	}

	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Set<InstructionalOffering> offerings) {
		iHibSession = hibSession;
		iSessionId = session.getUniqueId();
	}
	
	protected Hashtable<Long, Set<WeightedStudentId>> loadDemandsForSubjectArea(SubjectArea subjectArea) {
		Hashtable<Long, Set<WeightedStudentId>> demands = new Hashtable<Long, Set<WeightedStudentId>>();
		for (Object[] o: (List<Object[]>) iHibSession.createQuery(
					"select distinct e.courseOffering.uniqueId, s.uniqueId, a.academicAreaAbbreviation, f.code, m.code from StudentClassEnrollment e inner join e.student s " +
					"left outer join s.academicAreaClassifications c left outer join s.posMajors m left outer join c.academicArea a left outer join c.academicClassification f " +
					"where e.courseOffering.subjectArea.uniqueId = :subjectId")
					.setLong("subjectId", subjectArea.getUniqueId()).setCacheable(true).list()) {
			Long courseId = (Long)o[0];
			Long studentId = (Long)o[1];
			String areaAbbv = (String)o[2];
			String clasfCode = (String)o[3];
			String majorCode = (String)o[4];
			Set<WeightedStudentId> students = demands.get(courseId);
			if (students == null) {
				students = new HashSet<WeightedStudentId>();
				demands.put(courseId, students);
			}
			WeightedStudentId student = new WeightedStudentId(studentId);
			student.setStats(areaAbbv, clasfCode, majorCode);
			student.setCurriculum(areaAbbv == null ? null : majorCode == null ? areaAbbv : areaAbbv + "/" + majorCode);
			students.add(student);
		}
		return demands;
	}

	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		if (iStudentRequests == null) {
			iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
			for (Object[] o : (List<Object[]>)iHibSession.createQuery(
					"select distinct e.student.uniqueId, e.courseOffering " +
					"from StudentClassEnrollment e where e.student.session.uniqueId = :sessionId")
					.setLong("sessionId", iSessionId).setCacheable(true).list()) {
				Long sid = (Long)o[0];
				CourseOffering co = (CourseOffering)o[1];
				Set<WeightedCourseOffering> courses = iStudentRequests.get(sid);
				if (courses == null) {
					courses = new HashSet<WeightedCourseOffering>();
					iStudentRequests.put(sid, courses);
				}
				courses.add(new WeightedCourseOffering(co));
			}
		}
		return iStudentRequests.get(studentId);
	}

	@Override
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		Hashtable<Long, Set<WeightedStudentId>> demands = iDemands.get(course.getSubjectArea().getUniqueId());
		if (demands == null) {
			demands = loadDemandsForSubjectArea(course.getSubjectArea());
			iDemands.put(course.getSubjectArea().getUniqueId(), demands);
		}
		return demands.get(course.getUniqueId());
	}

	@Override
	public boolean isMakingUpStudents() {
		return false;
	}

	@Override
	public boolean isWeightStudentsToFillUpOffering() {
		return false;
	}
	
	@Override
	public boolean canUseStudentClassEnrollmentsAsSolution() {
		return true;
	}

}
