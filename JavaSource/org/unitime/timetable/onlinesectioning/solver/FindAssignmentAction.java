/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;
import net.sf.cpsolver.studentsct.reservation.Reservation;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningService;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerImpl.DummyReservation;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerImpl.EnrollmentSectionComparator;

/**
 * @author Tomas Muller
 */
public class FindAssignmentAction implements OnlineSectioningAction<List<ClassAssignmentInterface>>{
	private CourseRequestInterface iRequest;
	private Collection<ClassAssignmentInterface.ClassAssignment> iAssignment;
	private Hashtable<Long, int[]> iLastSectionLimit = new Hashtable<Long, int[]>();
	
	public FindAssignmentAction(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> assignment) {
		iRequest = request;
		iAssignment = assignment;
	}
	
	public CourseRequestInterface getRequest() {
		return iRequest;
	}
	
	public Collection<ClassAssignmentInterface.ClassAssignment> getAssignment() {
		return iAssignment;
	}

	@Override
	public List<ClassAssignmentInterface> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		long t0 = System.currentTimeMillis();
		DataProperties config = new DataProperties(ApplicationProperties.getProperties());
		config.setProperty("Neighbour.BranchAndBoundTimeout", "1000");
		config.setProperty("Extensions.Classes", DistanceConflict.class.getName() + ";" + TimeOverlapsCounter.class.getName());
		config.setProperty("StudentWeights.Class", StudentSchedulingAssistantWeights.class.getName());
		config.setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name()));
		config.setProperty("Reservation.CanAssignOverTheLimit", "true");
		StudentSectioningModel model = new StudentSectioningModel(config);

		Student student = new Student(getRequest().getStudentId() == null ? -1l : getRequest().getStudentId());

		Lock readLock = server.readLock();
		try {
			Student original = (getRequest().getStudentId() == null ? null : server.getStudent(getRequest().getStudentId()));
			for (CourseRequestInterface.Request c: getRequest().getCourses())
				addRequest(server, model, student, original, c, false, false);
			if (student.getRequests().isEmpty()) throw new SectioningException(SectioningExceptionType.EMPTY_COURSE_REQUEST);
			for (CourseRequestInterface.Request c: getRequest().getAlternatives())
				addRequest(server, model, student, original, c, true, false);
			model.addStudent(student);
		} finally {
			readLock.release();
		}
		
		Hashtable<CourseRequest, Set<Section>> preferredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		HashSet<FreeTimeRequest> requiredFreeTimes = new HashSet<FreeTimeRequest>();

		for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext();) {
			Request r = (Request)e.next();
			if (r instanceof CourseRequest) {
				CourseRequest cr = (CourseRequest)r;
				HashSet<Section> preferredSections = new HashSet<Section>();
				HashSet<Section> requiredSections = new HashSet<Section>();
				a: for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
					if (!a.isFreeTime() && cr.getCourse(a.getCourseId()) != null && a.getClassId() != null) {
						Section section = cr.getSection(a.getClassId());
						if (section == null || section.getLimit() == 0) {
							continue a;
						}
						if (a.isPinned() || a.isSaved()) 
							requiredSections.add(section);
						preferredSections.add(section);
						cr.getSelectedChoices().add(section.getChoice());
					}
				}
				preferredSectionsForCourse.put(cr, preferredSections);
				requiredSectionsForCourse.put(cr, requiredSections);
			} else {
				FreeTimeRequest ft = (FreeTimeRequest)r;
				for (ClassAssignmentInterface.ClassAssignment a: getAssignment()) {
					if (a.isFreeTime() && a.isPinned() && ft.getTime() != null &&
						ft.getTime().getStartSlot() == a.getStart() &&
						ft.getTime().getLength() == a.getLength() && 
						ft.getTime().getDayCode() == DayCode.toInt(DayCode.toDayCodes(a.getDays())))
						requiredFreeTimes.add(ft);
				}
			}
		}
		long t1 = System.currentTimeMillis();
		
			
        Solution solution = new Solution(model,0,0);
        
        Solver solver = new Solver(model.getProperties());
        solver.setInitalSolution(solution);
        solver.initSolver();
        
		long t2 = System.currentTimeMillis();

        SuggestionSelection onlineSelection = new SuggestionSelection(model.getProperties(), preferredSectionsForCourse, requiredSectionsForCourse, requiredFreeTimes);
        onlineSelection.init(solver);

        BranchBoundSelection.Selection selection = onlineSelection.getSelection(student); 
        BranchBoundNeighbour neighbour = selection.select();
        neighbour.assign(0);
        helper.info("Solution: " + neighbour);
        
		long t3 = System.currentTimeMillis();

		if (neighbour == null) throw new SectioningException(SectioningExceptionType.NO_SOLUTION);
        
		ClassAssignmentInterface ret = convert(server, model, student, neighbour, requiredSectionsForCourse, requiredFreeTimes, getEnrolledClasses(server, getRequest().getStudentId()));

		long t4 = System.currentTimeMillis();
		helper.info("Sectioning took "+(t4-t0)+"ms (model "+(t1-t0)+"ms, solver init "+(t2-t1)+"ms, sectioning "+(t3-t2)+"ms, conversion "+(t4-t3)+"ms)");

		List<ClassAssignmentInterface> rets = new ArrayList<ClassAssignmentInterface>(1);
		rets.add(ret);
		return rets;
	}
	
	@SuppressWarnings("unchecked")
	protected Set<Long> getEnrolledClasses(OnlineSectioningServer server, Long studentId) {
		if (studentId == null) return null;
		Student student = server.getStudent(studentId);
		if (student == null) return null;
		Set<Long> ret = new HashSet<Long>();
		for (Request r: student.getRequests())
			if (r.getInitialAssignment() != null && r.getInitialAssignment().isCourseRequest())
				for (Section s: r.getInitialAssignment().getSections())
					ret.add(s.getId());
		return ret;
	}

	@SuppressWarnings("unchecked")
	protected Course clone(Course course, long studentId, Student originalStudent) {
		Offering clonedOffering = new Offering(course.getOffering().getId(), course.getOffering().getName());
		int courseLimit = course.getLimit();
		if (courseLimit >= 0) {
			courseLimit -= course.getEnrollments().size();
			if (courseLimit < 0) courseLimit = 0;
			for (Iterator<Enrollment> i = course.getEnrollments().iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
				if (enrollment.getStudent().getId() == studentId) { courseLimit++; break; }
			}
		}
		Course clonedCourse = new Course(course.getId(), course.getSubjectArea(), course.getCourseNumber(), clonedOffering, courseLimit, course.getProjected());
		Hashtable<Config, Config> configs = new Hashtable<Config, Config>();
		Hashtable<Subpart, Subpart> subparts = new Hashtable<Subpart, Subpart>();
		Hashtable<Section, Section> sections = new Hashtable<Section, Section>();
		for (Iterator<Config> e = course.getOffering().getConfigs().iterator(); e.hasNext();) {
			Config config = e.next();
			int configLimit = config.getLimit();
			if (configLimit >= 0) {
				configLimit -= config.getEnrollments().size();
				if (configLimit < 0) configLimit = 0;
				for (Iterator<Enrollment> i = config.getEnrollments().iterator(); i.hasNext();) {
					Enrollment enrollment = i.next();
					if (enrollment.getStudent().getId() == studentId) { configLimit++; break; }
				}
			}
			Config clonedConfig = new Config(config.getId(), configLimit, config.getName(), clonedOffering);
			configs.put(config, clonedConfig);
			for (Iterator<Subpart> f = config.getSubparts().iterator(); f.hasNext();) {
				Subpart subpart = f.next();
				Subpart clonedSubpart = new Subpart(subpart.getId(), subpart.getInstructionalType(), subpart.getName(), clonedConfig,
						(subpart.getParent() == null ? null: subparts.get(subpart.getParent())));
				clonedSubpart.setAllowOverlap(subpart.isAllowOverlap());
				subparts.put(subpart, clonedSubpart);
				for (Iterator<Section> g = subpart.getSections().iterator(); g.hasNext();) {
					Section section = g.next();
					int limit = section.getLimit();
					if (limit >= 0) {
						// limited section, deduct enrollments
						limit -= section.getEnrollments().size();
						if (limit < 0) limit = 0; // over-enrolled, but not unlimited
						for (Iterator<Enrollment> i = section.getEnrollments().iterator(); i.hasNext();) {
							Enrollment enrollment = i.next();
							if (enrollment.getStudent().getId() == studentId) { limit++; break; }
						}
					}
					Section clonedSection = new Section(section.getId(), limit,
							section.getName(), clonedSubpart, section.getPlacement(),
							section.getChoice().getInstructorIds(), section.getChoice().getInstructorNames(),
							(section.getParent() == null ? null : sections.get(section.getParent())));
					clonedSection.setSpaceExpected(section.getSpaceExpected());
					clonedSection.setSpaceHeld(section.getSpaceHeld());
					clonedSection.setPenalty(section.getOnlineSectioningPenalty());
					sections.put(section, clonedSection);
				}
			}
		}
		if (course.getOffering().hasReservations()) {
			for (Reservation reservation: course.getOffering().getReservations()) {
				int reservationLimit = (int)Math.round(reservation.getLimit());
				if (reservationLimit >= 0) {
					reservationLimit -= reservation.getEnrollments().size();
					if (reservationLimit < 0) reservationLimit = 0;
					for (Iterator<Enrollment> i = reservation.getEnrollments().iterator(); i.hasNext();) {
						Enrollment enrollment = i.next();
						if (enrollment.getStudent().getId() == studentId) { reservationLimit++; break; }
					}
					if (reservationLimit <= 0) continue;
				}
				Reservation clonedReservation = new DummyReservation(reservation.getId(), clonedOffering,
						reservation.getPriority(), reservation.canAssignOverLimit(), reservationLimit, 
						originalStudent != null && reservation.isApplicable(originalStudent));
				for (Config config: reservation.getConfigs())
					clonedReservation.addConfig(configs.get(config));
				for (Map.Entry<Subpart, Set<Section>> entry: reservation.getSections().entrySet()) {
					Set<Section> clonedSections = new HashSet<Section>();
					for (Section section: entry.getValue())
						clonedSections.add(sections.get(section));
					clonedReservation.getSections().put(
							subparts.get(entry.getKey()),
							clonedSections);
				}
			}
		}
		return clonedCourse;
	}
	
	@SuppressWarnings("unchecked")
	private void updateLimits(OnlineSectioningServer server, Course course, boolean updateFromCache) {
		if (!OnlineSectioningService.sUpdateLimitsUsingSectionLimitProvider || OnlineSectioningService.sSectionLimitProvider == null) return;
		Hashtable<Long, Section> classes = new Hashtable<Long, Section>();
		for (Iterator<Config> e = course.getOffering().getConfigs().iterator(); e.hasNext();) {
			Config config = e.next();
			for (Iterator<Subpart> f = config.getSubparts().iterator(); f.hasNext();) {
				Subpart subpart = f.next();
				for (Iterator<Section> g = subpart.getSections().iterator(); g.hasNext();) {
					Section section = g.next();
					classes.put(section.getId(), section);
				}
			}
		}
		Map<Long, int[]> limits = (updateFromCache ? 
				OnlineSectioningService.sSectionLimitProvider.getSectionLimitsFromCache(server.getAcademicSession(), course.getId(), classes.values()) :
					OnlineSectioningService.sSectionLimitProvider.getSectionLimits(server.getAcademicSession(), course.getId(), classes.values()));
		for (Map.Entry<Long, int[]> entry: limits.entrySet()) {
			classes.get(entry.getKey()).setLimit(Math.max(0 , entry.getValue()[1] - entry.getValue()[0]));
			iLastSectionLimit.put(entry.getKey(), entry.getValue());
		}
	}
	
	protected void addRequest(OnlineSectioningServer server, StudentSectioningModel model, Student student, Student originalStudent, CourseRequestInterface.Request request, boolean alternative, boolean updateFromCache) {
		if (request.hasRequestedFreeTime()) {
			for (CourseRequestInterface.FreeTime freeTime: request.getRequestedFreeTime()) {
				int dayCode = 0;
				for (DayCode d: DayCode.values()) {
					if (freeTime.getDays().contains(d.getIndex()))
						dayCode |= d.getCode();
				}
				TimeLocation freeTimeLoc = new TimeLocation(dayCode, freeTime.getStart(), freeTime.getLength(), 0, 0, 
						-1l, "", server.getAcademicSession().getFreeTimePattern(), 0);
				new FreeTimeRequest(student.getRequests().size() + 1, student.getRequests().size(), alternative, student, freeTimeLoc);
			}
		} else if (request.hasRequestedCourse()) {
			CourseInfo courseInfo = server.getCourseInfo(request.getRequestedCourse());
			Course course = null;
			if (courseInfo != null) course = server.getCourse(courseInfo.getUniqueId());
			if (course != null) {
				Vector<Course> cr = new Vector<Course>();
				cr.add(clone(course, student.getId(), originalStudent));
				if (request.hasFirstAlternative()) {
					CourseInfo ci = server.getCourseInfo(request.getFirstAlternative());
					if (ci != null) {
						Course x = server.getCourse(ci.getUniqueId());
						if (x != null) cr.add(clone(x, student.getId(), originalStudent));
					}
				}
				if (request.hasSecondAlternative()) {
					CourseInfo ci = server.getCourseInfo(request.getSecondAlternative());
					if (ci != null) {
						Course x = server.getCourse(ci.getUniqueId());
						if (x != null) cr.add(clone(x, student.getId(), originalStudent));
					}
				}
				for (Course clonedCourse: cr)
					updateLimits(server, clonedCourse, updateFromCache);
				new CourseRequest(student.getRequests().size() + 1, student.getRequests().size(), alternative, student, cr, false);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private int[] getLimit(OnlineSectioningServer server, Section section, Long studentId) {
		if (OnlineSectioningService.sUpdateLimitsUsingSectionLimitProvider) {
			int[] limit = iLastSectionLimit.get(section.getId());
			if (limit != null) return limit;
		}
		Section original = server.getSection(section.getId());
		int actual = original.getEnrollments().size();
		/*
		if (studentId != null) {
			for (Iterator<Enrollment> i = original.getEnrollments().iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
				if (enrollment.getStudent().getId() == studentId) { actual--; break; }
			}
		}
		*/
		return new int[] {actual, original.getLimit()};
	}
	
	@SuppressWarnings("unchecked")
	protected ClassAssignmentInterface convert(OnlineSectioningServer server, Enrollment[] enrollments,
			Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse, HashSet<FreeTimeRequest> requiredFreeTimes,
			boolean computeOverlaps,
			DistanceConflict dc, Set<Long> savedClasses) throws SectioningException {
        ClassAssignmentInterface ret = new ClassAssignmentInterface();
		int nrUnassignedCourses = 0;
		int nrAssignedAlt = 0;
		for (Enrollment enrollment: enrollments) {
			if (enrollment == null) continue;
			if (enrollment.getRequest().isAlternative() && nrAssignedAlt >= nrUnassignedCourses &&
				(enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty())) continue;
			if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) {
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				if (enrollment.getRequest() instanceof CourseRequest) {
					CourseRequest r = (CourseRequest)enrollment.getRequest();
					Course course = enrollment.getCourse();
					ca.setAssigned(false);
					ca.setCourseId(course.getId());
					ca.setSubject(course.getSubjectArea());
					ca.setCourseNbr(course.getCourseNumber());
					if (computeOverlaps) {
						TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
							public int compare(Enrollment e1, Enrollment e2) {
								return e1.getRequest().compareTo(e2.getRequest());
							}
						});
						Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
						Collection<Enrollment> avEnrls = r.getAvaiableEnrollmentsSkipSameTime();
						for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							overlaps: for (Enrollment x: enrollments) {
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
								if (x == enrollment) continue;
						        for (Iterator<Assignment> i = x.getAssignments().iterator(); i.hasNext();) {
						        	Assignment a = i.next();
									if (a.isOverlapping(enrl.getAssignments())) {
										overlap.add(x);
										if (x.getRequest() instanceof CourseRequest) {
											CourseRequest cr = (CourseRequest)x.getRequest();
											TreeSet<Section> ss = overlapingSections.get(cr);
											if (ss == null) { ss = new TreeSet<Section>(); overlapingSections.put(cr, ss); }
											ss.add((Section)a);
										}
										break overlaps;
									}
						        }
							}
						}
						for (Enrollment q: overlap) {
							if (q.getRequest() instanceof FreeTimeRequest) {
								FreeTimeRequest f = (FreeTimeRequest)q.getRequest();
								ca.addOverlap("Free Time " +
									DayCode.toString(f.getTime().getDayCode()) + " " + 
									f.getTime().getStartTimeHeader() + " - " +
									f.getTime().getEndTimeHeader());
							} else {
								CourseRequest cr = (CourseRequest)q.getRequest();
								Course o = q.getCourse();
								String ov = o.getSubjectArea() + " " + o.getCourseNumber();
								if (overlapingSections.get(cr).size() == 1)
									for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
										Section s = i.next();
										ov += " " + s.getSubpart().getName();
										if (i.hasNext()) ov += ",";
									}
								ca.addOverlap(ov);
							}
						}
						nrUnassignedCourses++;
						int alt = nrUnassignedCourses;
						for (Enrollment x: enrollments) {
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
							if (x == enrollment) continue;
							if (x.getRequest().isAlternative() && x.getRequest() instanceof CourseRequest) {
								if (--alt == 0) {
									Course o = x.getCourse();
									ca.setInstead(o.getSubjectArea() + " " +o.getCourseNumber());
									break;
								}
							}
						}
						if (avEnrls.isEmpty()) ca.setNotAvailable(true);
					}
					ret.add(ca);
				} else {
					FreeTimeRequest r = (FreeTimeRequest)enrollment.getRequest();
					ca.setAssigned(false);
					ca.setCourseId(null);
					if (computeOverlaps) {
						overlaps: for (Enrollment x: enrollments) {
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
							if (x == enrollment) continue;
					        for (Iterator<Assignment> i = x.getAssignments().iterator(); i.hasNext();) {
					        	Assignment a = i.next();
								if (r.isOverlapping(a)) {
									if (x.getRequest() instanceof FreeTimeRequest) {
										FreeTimeRequest f = (FreeTimeRequest)x.getRequest();
										ca.addOverlap("Free Time " +
											DayCode.toString(f.getTime().getDayCode()) + " " + 
											f.getTime().getStartTimeHeader() + " - " +
											f.getTime().getEndTimeHeader());
									} else {
										Course o = x.getCourse();
										Section s = (Section)a;
										ca.addOverlap(o.getSubjectArea() + " " + o.getCourseNumber() + " " + s.getSubpart().getName());
									}
									break overlaps;
								}
					        }
						}
					}
					ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
					a.setAlternative(r.isAlternative());
					for (DayCode d : DayCode.toDayCodes(r.getTime().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(r.getTime().getStartSlot());
					a.setLength(r.getTime().getLength());
					ret.add(ca);
				}
			} else if (enrollment.getRequest() instanceof CourseRequest) {
				CourseRequest r = (CourseRequest)enrollment.getRequest();
				Set<Section> requiredSections = null;
				if (requiredSectionsForCourse != null) requiredSections = requiredSectionsForCourse.get(r);
				if (r.isAlternative() && r.isAssigned()) nrAssignedAlt++;
				TreeSet<Section> sections = new TreeSet<Section>(new EnrollmentSectionComparator());
				sections.addAll(enrollment.getSections());
				Course course = enrollment.getCourse();
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				ca.setAssigned(true);
				ca.setCourseId(course.getId());
				ca.setSubject(course.getSubjectArea());
				ca.setCourseNbr(course.getCourseNumber());
				boolean hasAlt = false;
				if (r.getCourses().size() > 1) {
					hasAlt = true;
				} else if (course.getOffering().getConfigs().size() > 1) {
					hasAlt = true;
				} else {
					for (Iterator<Subpart> i = ((Config)course.getOffering().getConfigs().get(0)).getSubparts().iterator(); i.hasNext();) {
						Subpart s = i.next();
						if (s.getSections().size() > 1) { hasAlt = true; break; }
					}
				}
				for (Iterator<Section> i = sections.iterator(); i.hasNext();) {
					Section section = (Section)i.next();
					ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
					a.setAlternative(r.isAlternative());
					a.setClassId(section.getId());
					a.setSubpart(section.getSubpart().getName());
					a.setSection(section.getName(course.getId()));
					a.setLimit(getLimit(server, section, r.getStudent().getId()));
					if (section.getTime() != null) {
						for (DayCode d : DayCode.toDayCodes(section.getTime().getDayCode()))
							a.addDay(d.getIndex());
						a.setStart(section.getTime().getStartSlot());
						a.setLength(section.getTime().getLength());
						a.setBreakTime(section.getTime().getBreakTime());
						a.setDatePattern(section.getTime().getDatePatternName());
					}
					if (section.getRooms() != null) {
						for (Iterator<RoomLocation> e = section.getRooms().iterator(); e.hasNext(); ) {
							RoomLocation rm = e.next();
							a.addRoom(rm.getName());
						}
					}
					if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
						String[] instructors = section.getChoice().getInstructorNames().split(":");
						for (String instructor: instructors) {
							String[] nameEmail = instructor.split("\\|");
							a.addInstructor(nameEmail[0]);
							a.addInstructoEmailr(nameEmail.length < 2 ? "" : nameEmail[1]);
						}
					}
					if (section.getParent() != null)
						a.setParentSection(section.getParent().getName(course.getId()));
					if (requiredSections != null && requiredSections.contains(section)) a.setPinned(true);
					a.setSubpartId(section.getSubpart().getId());
					a.setHasAlternatives(hasAlt);
					int dist = 0;
					String from = null;
					for (Enrollment x: enrollments) {
						if (x == null || !x.isCourseRequest() || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
						for (Iterator<Section> j=x.getSections().iterator(); j.hasNext();) {
							Section s = j.next();
							if (s == section || s.getTime() == null) continue;
							int d = distance(server, s, section);
							if (d > dist) {
								dist = d;
								from = "";
								for (Iterator<RoomLocation> k = s.getRooms().iterator(); k.hasNext();)
									from += k.next().getName() + (k.hasNext() ? ", " : "");
							}
							if (d > s.getTime().getBreakTime()) {
								a.setDistanceConflict(true);
							}
						}
					}
					a.setBackToBackDistance(dist);
					a.setBackToBackRooms(from);
					// if (dist > 0.0) a.setDistanceConflict(true);
					if (savedClasses != null && savedClasses.contains(section.getId())) a.setSaved(true);
					if (a.getParentSection() == null)
						a.setParentSection(server.getCourseInfo(course.getId()).getConsent());
					a.setExpected(section.getSpaceExpected());
				}
				ret.add(ca);
			} else {
				FreeTimeRequest r = (FreeTimeRequest)enrollment.getRequest();
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				ca.setAssigned(true);
				ca.setCourseId(null);
				ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
				a.setAlternative(r.isAlternative());
				for (DayCode d : DayCode.toDayCodes(r.getTime().getDayCode()))
					a.addDay(d.getIndex());
				a.setStart(r.getTime().getStartSlot());
				a.setLength(r.getTime().getLength());
				if (requiredFreeTimes != null && requiredFreeTimes.contains(r)) a.setPinned(true);
				ret.add(ca);
			}
		}
		
		return ret;	
	}

    @SuppressWarnings("unchecked")
	private ClassAssignmentInterface convert(OnlineSectioningServer server, StudentSectioningModel model, Student student, BranchBoundNeighbour neighbour,
			Hashtable<CourseRequest, Set<Section>> requiredSectionsForCourse, HashSet<FreeTimeRequest> requiredFreeTimes, Set<Long> savedClasses) throws SectioningException {
        Enrollment [] enrollments = neighbour.getAssignment();
        if (enrollments == null || enrollments.length == 0)
        	throw new SectioningException(SectioningExceptionType.NO_SOLUTION);
        int idx=0;
        for (Iterator<Request> e = student.getRequests().iterator(); e.hasNext(); idx++) {
        	Request r = e.next();
        	if (enrollments[idx] == null) {
        		Config c = null;
        		if (r instanceof CourseRequest)
        			c = (Config)((Course)((CourseRequest)r).getCourses().get(0)).getOffering().getConfigs().get(0);
        		enrollments[idx] = new Enrollment(r, 0, c, null);
        	}
        }
        
        return convert(server, enrollments, requiredSectionsForCourse, requiredFreeTimes, true, model.getDistanceConflict(), savedClasses);
	}
    
	private int distance(OnlineSectioningServer server, Section s1, Section s2) {
        if (s1.getPlacement()==null || s2.getPlacement()==null) return 0;
        TimeLocation t1 = s1.getTime();
        TimeLocation t2 = s2.getTime();
        if (!t1.shareDays(t2) || !t1.shareWeeks(t2)) return 0;
        int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
        if (a1+t1.getNrSlotsPerMeeting()==a2) {
            return Placement.getDistanceInMinutes(server.getDistanceMetric(), s1.getPlacement(), s2.getPlacement());
        }
        /*
        else if (a2+t2.getNrSlotsPerMeeting()==a1) {
        	return Placement.getDistance(s1.getPlacement(), s2.getPlacement());
        }
        */
        return 0;
    }
	
	@Override
	public String name() {
		return "section";
	}
}
