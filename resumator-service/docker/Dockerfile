#
#  ╔╦╗┬ ┬┌─┐  ╦═╗┌─┐┌─┐┬ ┬┌┬┐┌─┐┌┬┐┌─┐┬─┐TM
#   ║ ├─┤├┤   ╠╦╝├┤ └─┐│ ││││├─┤ │ │ │├┬┘
#   ╩ ┴ ┴└─┘  ╩╚═└─┘└─┘└─┘┴ ┴┴ ┴ ┴ └─┘┴└─
#  ────────────────────────────────────────
#
# Licensed under MIT by the fine people of Sytac
# http://sytac.io

FROM java:openjdk-8-jre
MAINTAINER Carlo Sciolla <carlo.sciolla@sytac.io>

# Prepare the app root folder
RUN mkdir -p /usr/local/resumator
WORKDIR /usr/local/resumator

# Install the app
COPY resumator-0.1-SNAPSHOT.jar /usr/local/resumator/resumator.jar

# Install the startup script
COPY resumator.sh /usr/local/resumator
RUN chmod +x /usr/local/resumator/resumator.sh

EXPOSE 9090

# Run the app
ENTRYPOINT ["/usr/local/resumator/resumator.sh"]

