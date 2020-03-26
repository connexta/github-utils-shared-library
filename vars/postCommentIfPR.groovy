import groovy.json.StringEscapeUtils

def call(String message, String user, String repository, String credentialsToken) {
    script {
        if (env.CHANGE_ID != null) {
            sh "curl -X POST -m 10 -d \'{ \"body\" : \"${StringEscapeUtils.escapeJavaScript(message)}\" }\' -u ${credentialsToken} https://api.github.com/repos/${user}/${repository}/issues/${CHANGE_ID}/comments"
        }
    }
}
