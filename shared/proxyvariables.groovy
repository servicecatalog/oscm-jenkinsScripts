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
                String[] http;
                String[] https;
	        if ( "${http_proxy}" != ''  && "${https_proxy}" != '') {
                http = "${http_proxy}".replaceAll("http:\\", "").split('\\');
                https = "${https_proxy}".replaceAll("https:\\", "").split('\\');
                env.ANT_OPTS="-Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\" -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\" -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
            } else if ( "${http_proxy}" != '') {
                http = "${http_proxy}".replaceAll("http:\\", "").split(':');
                env.ANT_OPTS="-Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttp.proxyHost=\"${http[0]}\" -Dhttp.proxyPort=\"${http[1]}\""
            } else if ( "${https_proxy}" != '' ) {
                https = "${https_proxy}".replaceAll("https:\\", "").split(':');
                env.ANT_OPTS=" -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
                env.MAVEN_OPTS="-Xmx512m -Duser.home=/build -Dhttps.proxyHost=\"${https[0]}\" -Dhttps.proxyPort=\"${https[1]}\""
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