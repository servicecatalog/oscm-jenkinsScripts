/*****************************************************************************
 *                                                                           *
 * Copyright FUJITSU LIMITED 2021                                            *
 *                                                                           *
 * Creation Date: 26-04-2021                                                 *
 *                                                                           *
 ****************************************************************************/
 
void execute() {


    def _setProxyVariables = {
    
        stage('Build - set proxy variables') {
	        script {
	        if ( "${http_proxy}" != ''  && ${https_proxy} != '' && ${http_port} != '' &&  ${https_port} != '' ) {
               env.ANT_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            } else if ( "${http_proxy}" != '' && ${http_port} != '' ) 
               env.ANT_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
            } else if ( && ${https_proxy} != '' &&  ${https_port} != '' ) {
               env.ANT_OPTS=" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            
	        } else {
	               env.ANT_OPTS=""
	               env.MAVEN_OPTS="-Xmx512m -Duser.home=/build"
	        }       
	       }
       }
   }  
              
    _setProxyVariables()
}
return this