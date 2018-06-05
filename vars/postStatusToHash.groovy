def call(String JSON, String user, String repository, String hash, String credentialsToken) {
    script {
        sh "curl -X POST -m 10 -d ${JSON} -u ${credentialsToken} https://api.github.com/repos/${user}/${repository}/statuses/${hash}"
    }
}
