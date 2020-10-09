/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 16-07-2020                                                 *
 *                                                                           *
 ****************************************************************************/

/**
 How to use:
 1. Create a job of type Pipeline
 2. As the pipeline script paste this script
 3. Create the following parameters (adjust defaults as you wish):

 Required environment variables:
 ===
 Name: NODE_NAME
 Type: Option list
 Default: -
 Description: Nodes used to run the pipeline.
 ===
 Name: DOCKER_TAG
 Type: String
 Default: latest
 Description: Docker tag name for created images
 ===
 Name: DOCKER_ORGANIZATION
 Type: String
 Default: oscmdocker
 Description: Organization name for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
 ===
 Name: REPO_TAG_OSCM
 Type: String
 Default: master
 Description: Branch or tag in the oscm git repository: https://github.com/servicecatalog/oscm
 ===
 Name: REPO_TAG_DOCKERBUILD
 Type: String
 Default: master
 Description: Branch or tag in the oscm-dockerbuild git repository: https://github.com/servicecatalog/oscm-dockerbuild
 ===
 Name: REPO_TAG_OSCM_APP
 Type: String
 Default: master
 Description: Branch or tag in the oscm-app git repository: https://github.com/servicecatalog/oscm-app
 ===
 Name: REPO_TAG_APP_SHELL
 Type: String
 Default: master
 Description: Branch or tag in the oscm-app-shell git repository: https://github.com/servicecatalog/oscm-app-shell
 ===
 Name: REPO_TAG_APPROVAL
 Type: String
 Default: master
 Description: Branch or tag in the oscm-approval git repository: https://github.com/servicecatalog/oscm-approval
 ===
 Name: REPO_TAG_IDENTITY
 Type: String
 Default: master
 Description: Branch or tag in the oscm-identity git repository: https://github.com/servicecatalog/oscm-identity
 ===
 Name: REPO_TAG_REST_API
 Type: String
 Default: master
 Description: Branch or tag in the oscm-identity git repository: https://github.com/servicecatalog/oscm-rest-api
 ===
 Name: REPO_TAG_DOCUMENTATION
 Type: String
 Default: master
 Description: Branch or tag in the documentation git repository" https://github.com/servicecatalog/documentation
 ===
 Name: TRUSTED_CERTS_PATH
 Type: String
 Default: /opt/trusted_certs
 Description: Path to the directory with certificates that will be imported as trusted by oscm
 ===
 Name: SAMPLE_DATA
 Type: boolean
 Default: false
 Description: Load sample data?
 ===
 Name: TOMEE_DEBUG
 Type: boolean
 Default: false
 Description: Value ignored when glassfish chosen.
 ===
 Name: AUTH_MODE
 Type: Option list (INTERNAL, OIDC)
 Default: INTERNAL
 Description: Auth mode used to communicate with OSCM web services.
 ===
 Name: COMPLETE_CLEANUP
 Default: enabled
 Description: Delete the database and whole workspace
 ===
 Name: TECH_SERVICE_PATH
 Type: String
 Default: /build/oscm-ui-tests/src/test/resources/TechnicalService.xml
 Description: Localization of Technical Service definition stored in .xml file
 ===
 Name: WEBTEST_PROPERTIES_LOCALIZATION
 Type: String
 Default: $WORKSPACE/oscm-ui-tests/src/test/resources/webtest.properties
 Description: Localization of webtest.properties file from oscm-ui-tests oscm module
 ===
 **/

node("${NODE_NAME}") {

    // Build and push
    def clean = evaluate readTrusted('shared/cleanup.groovy')
    def build = evaluate readTrusted('shared/build.groovy')
    def push = evaluate readTrusted('shared/push.groovy')

    // Pull and start
    def pull = evaluate readTrusted('shared/pull.groovy')
    def start = evaluate readTrusted('shared/start.groovy')
    
    // Run integration tests
    def tests = evaluate readTrusted('tests/portal-integration-tests.groovy')

    clean.execute()
    build.execute()
    push.execute()

    pull.execute()
    start.execute('localhost')

    tests.execute()
    clean.execute()
}
