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
         sh '''
            if [ -n ${http_proxy} ] && [ -n ${https_proxy} ] && [ -n ${http_port} ] && [ -n ${https_port} ]; then
               OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\" -Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            elif [ -n ${http_proxy} ] && [ -n ${http_port} ]; then
                OPTS="-Dhttp.proxyHost=\"${http_proxy}\" -Dhttp.proxyPort=\"${http_port}\""
            elif [ -n ${https_proxy} ] && [ -n ${https_port} ]; then
                OPTS="-Dhttps.proxyHost=\"${https_proxy}\" -Dhttps.proxyPort=\"${https_port}\""
            else
               OPTS=""
            fi;
            '''
          env.PROXY_OPTS = $OPTS
       }
       }
   }             
    _setProxyVariables()
}
return this