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
        env.PROXY_OPTS
         sh '''
            if [ -n ${http_proxy} ] && [ -n ${https_proxy} ] && [ -n ${http_port} ] && [ -n ${https_port} ]; then
               PROXY_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            elif [ -n ${http_proxy} ] && [ -n ${http_port} ]; then
                PROXY_OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
            elif [ -n ${https_proxy} ] && [ -n ${https_port} ]; then
                PROXY_OPTS="-Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            else
               PROXY_OPTS=""
            fi;
            '''
       }
       }
   }             
    _setProxyVariables()
}
return this