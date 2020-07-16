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
 Description: Nodes used to run the pipeline
 ===
 Name: DOCKER_TAG
 Type: String
 Default: latest
 Description: Docker tag name for created images
 ===
 Name: DOCKER_REGISTRY
 Type: String
 Default: artifactory.intern.est.fujitsu.com:5003
 Description: Registry host and port for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
 ===
 Name: DOCKER_ORGANIZATION
 Type: String
 Default: oscmdocker
 Description: Organization name for the final Docker image. Example: <DOCKER_REGISTRY>/<DOCKER_ORGANIZATION>/oscm-core:<DOCKER_TAG>
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
**/

node("${NODE_NAME}") {
	def clean = evaluate readTrusted('shared/cleanup.groovy')
    def pull = evaluate readTrusted('shared/pull.groovy')
    def start = evaluate readTrusted('shared/start.groovy')

    clean.execute()
    pull.execute()
    start.execute()
}
