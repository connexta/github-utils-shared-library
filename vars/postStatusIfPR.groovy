def call(String state, String target_url, String description, String context, String user, String repository, String hash, String credentialsToken) {
    script {
        sh "curl -X POST -m 10 -d \'{\"state\": \"${state}\", \"target_url\": \"${target_url}\", \"description\": \"${description}\", \"context\": \"${context}\"}\' -u ${credentialsToken}
    }
}
