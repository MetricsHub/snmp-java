# SNMP Java Client

![GitHub release (with filter)](https://img.shields.io/github/v/release/metricshub/snmp-java)
![Build](https://img.shields.io/github/actions/workflow/status/metricshub/snmp-java/deploy.yml)
![GitHub top language](https://img.shields.io/github/languages/top/metricshub/snmp-java)
![License](https://img.shields.io/github/license/metricshub/snmp-java)

This project is a fork of the excellent [Westhawk's SNMP Library for Java](https://snmp.westhawk.co.uk/) ([see also](https://code.google.com/archive/p/snmp123)).

The SNMP Java client is designed to interact with remote hosts implementing SNMP v1, v2c, or v3 and allows to execute SNMP requests, including `Get` and `GetNext` requests as well as `Walk` and `Table` functionalities that represent an enhancements over traditional SNMP `Get` and `GetNext` methods.

See [Project Documentation](https://metricshub.org/snmp-java/) and the [Javadoc](https://metricshub.org/snmp-java/apidocs/) for more information on how to use this library in your code.
## Build instructions

This is a simple Maven project. Build with:

```bash
mvn verify
```

## Release instructions

The artifact is deployed to Sonatype's [Maven Central](https://central.sonatype.com/).

The actual repository URL is https://s01.oss.sonatype.org/, with server Id `ossrh` and requires credentials to deploy
artifacts manually.

But it is strongly recommended to only use [GitHub Actions "Release to Maven Central"](actions/workflows/release.yml) to perform a release:

* Manually trigger the "Release" workflow
* Specify the version being released and the next version number (SNAPSHOT)
* Release the corresponding staging repository on [Sonatype's Nexus server](https://s01.oss.sonatype.org/)
* Merge the PR that has been created to prepare the next version

## License

License is GNU General Lesser Public License (LGPL) version 3.0. Each source file includes the LGPL-3 header (build will fail otherwise). To update source files with the proper header, simply execute the below command:

```bash
mvn license:update-file-header
```
