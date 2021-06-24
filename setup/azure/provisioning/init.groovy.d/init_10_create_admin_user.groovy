/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2020                                            *
 *                                                                           *
 * Creation Date: 22.04.2021                                                 *
 *                                                                           *
 ****************************************************************************/
/** Copyright (c) 2020 Darin Pope **/

import jenkins.model.*
import hudson.security.*

File disableScript = new File(Jenkins.get().getRootDir(), ".disable-create-admin-user")
if (disableScript.exists()) {
    return
}

def instance = Jenkins.get()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('JENKINS-USER-ID','JENKINS-USER-PWD')

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)

instance.setSecurityRealm(hudsonRealm)
instance.setAuthorizationStrategy(strategy)
instance.save()
disableScript.createNewFile()