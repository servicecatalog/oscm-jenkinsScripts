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
 Name: DOCKER_SRC_TAG
 Type: String
 Default: latest
 Description: Docker tag name for source images
 ===
 Name: DOCKER_SRC_REGISTRY
 Type: String
 Default: artifactory.intern.est.fujitsu.com:5003
 Description: Registry host and port for source images.
 ===
 Name: DOCKER_SRC_ORGANIZATION
 Type: String
 Default: oscmdocker
 Description: Organization name for source images.
 ===
 Name: DOCKER_DST_TAG
 Type: String
 Default: latest
 Description: Docker tag name for destination images
 ===
 Name: DOCKER_DST_ORGANIZATION
 Type: String
 Description: Organization name for destination images.
 ===
 Name: USERNAME
 Type: String
 Description: Username for the target <DOCKER_DST_REGISTRY>
 ===
 Name: PASSWORD
 Type: String
 Description: Password for the target <DOCKER_DST_REGISTRY>
 ===
**/

node("${NODE_NAME}") {
    def push = evaluate readTrusted('shared/push.groovy')

    push.execute(true, true)
}
