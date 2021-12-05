/**
 * Copyright(c) 2021 All rights reserved by Jungho Kim in MyungJi University 
 */

package Components.Student;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Scanner;

import Framework.Event;
import Framework.EventId;
import Framework.EventQueue;
import Framework.RMIEventBus;

public class StudentMain {
	public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException {
		RMIEventBus eventBus = (RMIEventBus) Naming.lookup("EventBus");
		long componentId = eventBus.register();
		System.out.println("** StudentMain(ID:" + componentId + ") is successfully registered. \n");

		StudentComponent studentsList = new StudentComponent("Students.txt");
		Event event = null;
		boolean done = false;
		while (!done) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			EventQueue eventQueue = eventBus.getEventQueue(componentId);
			for (int i = 0; i < eventQueue.getSize(); i++) {
				event = eventQueue.getEvent();
				switch (event.getEventId()) {
				case ListStudents:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, makeStudentList(studentsList)));
					break;
				case RegisterStudents:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, registerStudent(studentsList, event.getMessage())));
					break;
				case DeleteStudents:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, deleteStudent(studentsList, event.getMessage())));
					break;
				case CheckStudent:
					printLogEvent("Get", event);
					eventBus.sendEvent(new Event(EventId.ClientOutput, enrollment(studentsList, event.getMessage())));
					break;
				case QuitTheSystem:
					printLogEvent("Get", event);
					eventBus.unRegister(componentId);
					done = true;
					break;
				default:
					break;
				}
			}
		}
	}
	private static String enrollment(StudentComponent studentsList, String message) {
		Scanner sc = new Scanner(message);
		String courseId = sc.next();
		String stuentId = sc.next();
		String isExistCourse = sc.next();
		ArrayList<String> prerequisitesCourses = new ArrayList<String>();
		while(sc.hasNext()) {prerequisitesCourses.add(sc.next());}
		if(isExistCourse.matches("success")) {return "This course isn't registered";}
		for(Student student : studentsList.vStudent) {
			if(student.studentId.matches(stuentId)) {
				if(checkPrerequisites(student, prerequisitesCourses)) {
					return "Enrollment Success";
				}
				return "No prerequisite courses";
			}
		}
		return "This student isn't registered";
	}
	private static boolean checkPrerequisites(Student student, ArrayList<String> prerequisites) {
		boolean isDonePrerequisites = true;
		for(String prerequisite : prerequisites) {
			if(isDonePrerequisites) {
				isDonePrerequisites = false;
				for(String completed : student.getCompletedCourses()) {
					if(prerequisite.matches(completed)) {isDonePrerequisites = true;}
				}
			}
		}
		return isDonePrerequisites;
	}
	private static String deleteStudent(StudentComponent studentsList, String message) {
		for(int i = 0; i < studentsList.vStudent.size(); i++) {
			if(message.matches(studentsList.vStudent.get(i).studentId)) {
				studentsList.vStudent.remove(i);
				return "This student is successfully deleted.";
			}
		}
		return "This student isn't registered.";
	}
	private static String registerStudent(StudentComponent studentsList, String message) {
		Student student = new Student(message);
		if (!studentsList.isRegisteredStudent(student.studentId)) {
			studentsList.vStudent.add(student);
			return "This student is successfully added.";
		} else
			return "This student is already registered.";
	}
	private static String makeStudentList(StudentComponent studentsList) {
		String returnString = "";
		for (int j = 0; j < studentsList.vStudent.size(); j++) {
			returnString += studentsList.getStudentList().get(j).getString() + "\n";
		}
		return returnString;
	}
	private static void printLogEvent(String comment, Event event) {
		System.out.println(
				"\n** " + comment + " the event(ID:" + event.getEventId() + ") message: " + event.getMessage());
	}
}
