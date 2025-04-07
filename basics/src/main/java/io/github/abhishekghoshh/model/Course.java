package io.github.abhishekghoshh.model;

import java.util.List;
import java.util.function.Predicate;

public
class Course {

    private String name;
    private String category;
    private int reviewScore;
    private int noOfStudents;

    public Course() {
    }

    public Course(String name, String category, int reviewScore, int noOfStudents) {
        super();
        this.name = name;
        this.category = category;
        this.reviewScore = reviewScore;
        this.noOfStudents = noOfStudents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(int reviewScore) {
        this.reviewScore = reviewScore;
    }

    public int getNoOfStudents() {
        return noOfStudents;
    }

    public void setNoOfStudents(int noOfStudents) {
        this.noOfStudents = noOfStudents;
    }

    @Override
    public String toString() {
        return "Course [name=" + name +
                ", category=" + category +
                ", reviewScore=" + reviewScore +
                ", noOfStudents=" + noOfStudents + "]";
    }

    public static List<Course> courses() {
        return List.of(
                new Course("Spring", "Framework", 98, 20000),
                new Course("Spring Boot", "Framework", 95, 18000),
                new Course("API", "Microservices", 97, 22000),
                new Course("Microservices", "Microservices", 96, 25000),
                new Course("FullStack", "FullStack", 91, 14000),
                new Course("AWS", "Cloud", 92, 21000),
                new Course("Azure", "Cloud", 99, 21000),
                new Course("Docker", "Cloud", 92, 20000),
                new Course("Kubernetes", "Cloud", 91, 20000)
        );
    }

    public static boolean isReviewScoreGreaterThan(Course course, int reviewScore) {
        return course.getReviewScore() > reviewScore;
    }

    public static boolean isReviewScoreLessThan(Course course, int reviewScore) {
        return course.getReviewScore() < reviewScore;
    }

    public static Predicate<Course> isReviewScoreGreaterThan(int reviewScore) {
        return course -> course.getReviewScore() > reviewScore;
    }
}