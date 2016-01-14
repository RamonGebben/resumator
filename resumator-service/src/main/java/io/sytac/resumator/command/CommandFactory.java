package io.sytac.resumator.command;

import io.sytac.resumator.employee.NewEmployeeCommand;
import io.sytac.resumator.organization.NewOrganizationCommand;

import java.util.Date;
import java.util.Map;

/**
 * Creates command descriptors and publishes the related event
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
public class CommandFactory {

    public NewEmployeeCommand newEmployeeCommand(final Map<String, String> input, final String organizationDomain) {
        final String name = input.get("name");
        final String surname = input.get("surname");
        final String yearOfBirth = input.get("dateOfBirth");
        final String nationality = input.get("nationality");
        final String currentResident = input.get("nationality");
        final String timestamp = String.valueOf(new Date().getTime());
        return new NewEmployeeCommand(organizationDomain, name, surname, yearOfBirth, nationality, currentResident, timestamp);
    }

    public NewOrganizationCommand newOrganizationCommand(final Map<String, String> input) {
        final String name = input.get("name");
        final String domain = input.get("domain");
        final String timestamp = input.get("timestamp");
        return new NewOrganizationCommand(name, domain, timestamp);
    }
}
