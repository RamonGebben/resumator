package io.sytac.resumator.http;

import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory;
import io.sytac.resumator.Configuration;
import io.sytac.resumator.http.dto.ServiceInfoDTO;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

/**
 * Main entry point for the API: provides hypermedia links to the other resources
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
@Path("info")
public class ServiceInfo extends BaseResource {

    private final Configuration config;

    @Inject
    public ServiceInfo(final Configuration configuration) {
        this.config = configuration;
    }

    @GET
    @Produces(RepresentationFactory.HAL_JSON)
    public Representation helloWorld(@Context final UriInfo uriInfo) {
        return rest.newRepresentation()
                .withProperty("app-name", config.getProperty("resumator.service.name").orElse("--"))
                .withProperty("app-version", config.getProperty("resumator.service.version").orElse("--"))
                .withLink("self", uriInfo.getRequestUri())
                .withLink("employees", resourceLink(uriInfo, EmployeesResource.class));
    }
}
