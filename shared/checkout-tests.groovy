/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 19-11-2020                                                 *
 *                                                                           *
 ****************************************************************************/
 
void execute() {
    def _cloneOSCMRepository = {
        stage('Build - checkout tests') {
            checkout scm: [
                    $class                           : 'GitSCM',
                    branches                         : [[name: "${REPO_TAG_OSCM}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions                       : [[$class : 'CloneOption',
                                                         noTags : false, reference: '',
                                                         shallow: true]],
                    submoduleCfg                     : [],
                    userRemoteConfigs                : [[url: 'https://github.com/servicecatalog/oscm.git']]
            ]
        }
    }
    _cloneOSCMRepository()
}
return this