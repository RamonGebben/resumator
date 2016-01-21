package io.sytac.resumator.employee;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sytac.resumator.command.CommandPayload;
import io.sytac.resumator.model.Course;
import io.sytac.resumator.model.Education;
import io.sytac.resumator.model.Experience;
import io.sytac.resumator.model.Language;
import lombok.Getter;

import java.util.List;

/**
 * Defines the payload of a new Employee command
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
@Getter
public class EmployeeCommandPayload implements CommandPayload {

    private final String title;
    private final String name;
    private final String surname;
    private final String email;
    private final String phonenumber;
    private final String github;
    private final String linkedin;
    private final String dateOfBirth;
    private final String nationality;
    private final String currentResidence;
    private final String aboutMe;
    private final List<Education> education;
    private final List<Course> courses;
    private final List<Experience> experience;
    private final List<Language> languages;

    @JsonCreator
    public EmployeeCommandPayload(@JsonProperty("title") final String title,
                                  @JsonProperty("name") final String name,
                                  @JsonProperty("surname") final String surname,
                                  @JsonProperty("email") final String email,
                                  @JsonProperty("phonenumber") final String phonenumber,
                                  @JsonProperty("github") final String github,
                                  @JsonProperty("linkedin") final String linkedin,
                                  @JsonProperty("dateOfBirth") final String dateOfBirth,
                                  @JsonProperty("nationality") final String nationality,
                                  @JsonProperty("currentResidence") final String currentResidence,
                                  @JsonProperty("aboutMe") final String aboutMe,
                                  @JsonProperty("education") final List<Education> education,
                                  @JsonProperty("courses") final List<Course> courses,
                                  @JsonProperty("experience") final List<Experience> experience,
                                  @JsonProperty("languages") final List<Language> languages) {

        this.title = title;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phonenumber = phonenumber;
        this.github = github;
        this.linkedin = linkedin;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.currentResidence = currentResidence;
        this.aboutMe = aboutMe;
        this.education = education;
        this.courses = courses;
        this.experience = experience;
        this.languages = languages;
    }
}