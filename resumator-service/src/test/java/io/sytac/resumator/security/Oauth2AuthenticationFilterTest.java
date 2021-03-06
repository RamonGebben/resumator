package io.sytac.resumator.security;

import io.sytac.resumator.events.EventPublisher;
import io.sytac.resumator.model.Event;
import io.sytac.resumator.organization.NewOrganizationCommand;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class Oauth2AuthenticationFilterTest {

    private Oauth2AuthenticationFilter filter;
    private Oauth2SecurityService service;

    @Mock
    private EventPublisher eventPublisherMock;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(eventPublisherMock.publish(any(NewOrganizationCommand.class))).thenReturn(mock(Event.class));
        service = mock(Oauth2SecurityService.class);
        filter = new Oauth2AuthenticationFilter(service);
    }

    @Test
    public void anonymousHasNoRoles() throws IOException {
        final ContainerRequestContext ctx = mock(ContainerRequestContext.class);
        when(ctx.getCookies()).thenReturn(Collections.emptyMap());
        setURLMock(ctx);

        filter.filter(ctx);
        verify(service, never()).authenticateUser(anyString());
    }

    @Test
    public void wrongAuthenticationCookieGetsAnonymous() throws IOException {
        final Map<String, Cookie> wrongCookies = new HashMap<>();
        final Cookie cookie = new Cookie(Oauth2AuthenticationFilter.AUTHENTICATION_COOKIE, "I haz no permission!");
        wrongCookies.put(Oauth2AuthenticationFilter.AUTHENTICATION_COOKIE, cookie);

        final ContainerRequestContext ctx = mock(ContainerRequestContext.class);
       
        setURLMock(ctx);
        
        when(service.authenticateUser(eq(cookie.getValue()))).thenReturn(Optional.empty());
        when(ctx.getCookies()).thenReturn(wrongCookies);

        filter.filter(ctx);
        verify(ctx, times(1)).setSecurityContext(argThat(arg -> {
                    final SecurityContext sc = ((SecurityContext) arg);
                    return sc.getUserPrincipal().equals(Identity.ANONYMOUS);
                }
        ));
    }

	private void setURLMock(final ContainerRequestContext ctx) {
		final UriInfo uriInfo = mock(UriInfo.class);
        URI uri=null;
        try {
			 uri = new URI("http://resumator.sytac.io:8000/#/");
		} catch (URISyntaxException e) {
			
			e.printStackTrace();
		}
        

        when(ctx.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getRequestUri()).thenReturn(uri);
	}
}