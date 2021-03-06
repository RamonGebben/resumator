package io.sytac.resumator.employee;

import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import io.sytac.resumator.docx.DocxGenerator;
import io.sytac.resumator.http.BaseResource;
import io.sytac.resumator.model.*;
import io.sytac.resumator.model.Error;
import io.sytac.resumator.organization.OrganizationRepository;
import io.sytac.resumator.security.Identity;
import io.sytac.resumator.security.Roles;
import io.sytac.resumator.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Retrieve information about one employee
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
@Path("employees/{email}")
@RolesAllowed(Roles.USER)
@Slf4j
public class EmployeeQuery extends BaseResource {

    private static final String PATH_PARAM_EMAIL = "email";

    private static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final OrganizationRepository organizations;

    private final DocxGenerator docxGenerator;


    @Inject
    public EmployeeQuery(final OrganizationRepository organizations, final DocxGenerator docxGenerator) {
        this.organizations = organizations;
        this.docxGenerator = docxGenerator;
    }

    @GET
    @Produces(RepresentationFactory.HAL_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getEmployee(@PathParam(PATH_PARAM_EMAIL) final String email,
                                @UserPrincipal Identity identity,
                                @Context final UriInfo uriInfo) {

        return represent(email, identity, uriInfo);
    }

    @GET
    @Produces(CONTENT_TYPE_DOCX)
    public Response getDocxViaContentNegotiation(@PathParam(PATH_PARAM_EMAIL) final String email, @UserPrincipal final Identity identity) {
        return getEmployee(email, identity).map(employee -> {
            try {
                return Response.ok(docxGenerator.generate(getTemplateStream(), getPlaceholderMappings(employee)),
                                CONTENT_TYPE_DOCX)
                        .header("content-disposition", String.format("attachment; filename = %s_%s.docx", employee.getName(), employee.getSurname()))
                        .build();
            } catch (IOException exc) {
                log.error("The following exception occurred while generating a PDF: ", exc);
                return Response.serverError().entity(new Error("Could not generate pdf, please try again later")).build();
            }
        }).orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @Path("docx")
    @GET
    @Produces(CONTENT_TYPE_DOCX)
    public Response getDocxViaCustomUrl(@PathParam(PATH_PARAM_EMAIL) final String email, @UserPrincipal final Identity identity) {
        return getDocxViaContentNegotiation(email, identity);
    }

    /**
     * Translates an {@link Employee} into its HAL representation
     *
     * @param email The email of desired employee
     * @param identity The current identity
     * @param uriInfo  The current REST endpoint information
     * @return The {@link Representation} of the {@link Employee}
     */
    private Response represent(final String email, final Identity identity, final UriInfo uriInfo) {
        final Optional<Employee> employee = getEmployee(email, identity);

        if (employee.isPresent()) {
            final Employee emp = employee.get();
            final List<Education> educations = Optional.ofNullable(emp.getEducations()).orElse(Collections.emptyList());
            final List<Course> courses = Optional.ofNullable(emp.getCourses()).orElse(Collections.emptyList());
            final List<Experience> experiences = Optional.ofNullable(emp.getExperiences()).orElse(Collections.emptyList());
            final List<Language> languages = Optional.ofNullable(emp.getLanguages()).orElse(Collections.emptyList());

            final String representation = rest.newRepresentation()
                    .withProperty("type", emp.getType())
                    .withProperty("title", emp.getTitle())
                    .withProperty("name", emp.getName())
                    .withProperty("surname", emp.getSurname())
                    .withProperty("email", emp.getEmail())
                    .withProperty("phonenumber", emp.getPhoneNumber())
                    .withProperty("github", emp.getGitHub())
                    .withProperty("linkedin", emp.getLinkedIn())
                    .withProperty("dateOfBirth", emp.getDateOfBirth())
                    .withProperty("nationality", emp.getNationality())
                    .withProperty("aboutMe", emp.getAboutMe())
                    .withProperty("currentResidence", emp.getCurrentResidence())
                    .withProperty("education", educations)
                    .withProperty("courses", courses)
                    .withProperty("experience", experiences)
                    .withProperty("languages", languages)
                    .withProperty("admin", (identity.hasRole(Roles.ADMIN) || emp.isAdmin()))
                    .withLink("self", uriInfo.getRequestUri().toString())
                    .toString(RepresentationFactory.HAL_JSON);

            return Response.ok(representation).build();
        } else {
            return Response.ok().status(HttpStatus.NOT_FOUND_404).build();
        }
    }

    private Optional<Employee> getEmployee(String email, Identity identity) {
        return organizations
                .get(identity.getOrganizationId())
                .map(org -> org.getEmployeeByEmail(email));
    }

    private InputStream getTemplateStream() {
        return getClass().getClassLoader().getResourceAsStream("resume-template.docx");
    }

    private Map<String, String> getPlaceholderMappings(Employee employee) {
        Map<String, String> result = new HashMap<>();

        result.putAll(getPersonaliaMappings(employee));
        result.putAll(getExperienceMappings(employee.getExperiences()));
        result.putAll(getEducationMappings(employee.getEducations()));
        result.putAll(getCourseMappings(employee.getCourses()));
        result.putAll(getLanguageMappings(employee.getLanguages()));

        return result;
    }

    private Map<String, String> getPersonaliaMappings(Employee employee) {
        Map<String, String> result = new HashMap<>();
        result.put("JobTitle", employee.getTitle());
        result.put("FirstName", employee.getName());
        result.put("LastName", employee.getSurname());
        result.put("YearOfBirth", String.valueOf(getYearOfDate(employee.getDateOfBirth())));
        result.put("CurrentResidence", employee.getCurrentResidence());
        result.put("Nationality", employee.getNationality().toString());
        result.put("Bio", employee.getAboutMe());
        return result;
    }

    private Map<String, String> getExperienceMappings(List<Experience> experiences) {
        Map<String, String> result = new HashMap<>();

        for (int i = 1; i <= experiences.size(); i++) {
            Experience experience = experiences.get(i - 1);
            result.put("Experience.Position" + i, experience.getTitle());
            result.put("Experience.Period" + i, getPeriod(experience.getStartDate(), experience.getEndDate()));
            result.put("Experience.CompanyName" + i, experience.getCompanyName());
            result.put("Experience.City" + i, experience.getCity());
            result.put("Experience.Country" + i, experience.getCountry());
            result.put("Experience.Description" + i, experience.getShortDescription());
            result.put("Experience.Technologies" + i, StringUtils.join(experience.getTechnologies(), ", "));
            result.put("Experience.Methodologies" + i, StringUtils.join(experience.getMethodologies(), ", "));
        }

        return result;
    }

    private Map<String, String> getEducationMappings(List<Education> educations) {
        Map<String, String> result = new HashMap<>();

        for (int i = 1; i <= educations.size(); i++) {
            Education education = educations.get(i - 1);
            result.put("Education.Degree" + i, education.getDegree().asText());
            result.put("Education.Field" + i, education.getFieldOfStudy());
            result.put("Education.School" + i, education.getSchool());
            result.put("Education.City" + i, education.getCity());
            result.put("Education.Country" + i, education.getCountry());
            result.put("Education.Period" + i, getPeriod(education.getStartYear(), education.getEndYear()));
        }

        return result;
    }

    private Map<String, String> getCourseMappings(List<Course> courses) {
        Map<String, String> result = new HashMap<>();

        for (int i = 1; i <= courses.size(); i++) {
            Course course = courses.get(i - 1);

            result.put("Course.Name" + i, course.getName());
            result.put("Course.Year" + i, String.valueOf(course.getYear()));
        }

        return result;
    }

    private Map<String, String> getLanguageMappings(List<Language> languages) {
        Map<String, String> result = new HashMap<>();

        for (int i = 1; i <= languages.size(); i++) {
            Language language = languages.get(i - 1);

            result.put("Language.Name" + i, language.getName());
            result.put("Language.Proficiency" + i, language.getProficiency().asText());
        }

        return result;
    }

    private String getPeriod(int startYear, int endYear) {
        if (startYear != endYear) {
            return startYear + " - " + endYear;
        } else {
            return String.valueOf(startYear);
        }
    }

    private String getPeriod(Date startDate, Optional<Date> endDate) {
        SimpleDateFormat df = new SimpleDateFormat("MMM. yyyy");

        final String startDateStr = df.format(startDate);
        final String endDateStr = endDate.map(df::format).orElse("present");

        if (endDateStr.equals(startDateStr)) {
            return startDateStr;
        } else {
            return startDateStr + " - " + endDateStr;
        }
    }

    private int getYearOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }
}
