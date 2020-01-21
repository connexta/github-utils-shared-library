def call(String message, String user, String repository, String credentialsToken, String branch, String tag, String artifactPath) {
	
    // Provides a library var to create a Github release and push an artifact up to Github
    //
    // This will be done by:
    // . Add the "github.com/connexta/github-utils-shared-library@master" library to the top of the release job
    // . Call the var passing the following strings:
    //   .  path to release notes
    //   .  repo owner
    //   .  repo name
    //   .  ssh key to the github repo
    //   .  branch
    //   .  tag
    //   .  full path to the workspace artifact

    script {
            sh """
                release=\$(curl -XPOST -H "Authorization:token ${credentialsToken}" --data "{\\"tag_name\\": \\"${tag}\\", \\"target_commitish\\": \\"${branch}\\", \\"name\\": \\"${tag}\\", \\"body\\": \\"${message}\\", \\"draft\\": false, \\"prerelease\\": false}" https://api.github.com/repos/${user}/${repository}/releases)

                # Extract the id of the release from the creation response
                RELEASE_ID=\$(echo "\$release" | sed -n -e 's/"id":\\ \\([0-9]\\+\\),/\\1/p' | head -n 1 | sed 's/[[:blank:]]//g')

                curl -XPOST -H "Authorization:token ${credentialsToken}" -H "Content-Type:application/octet-stream" --data-binary @${artifactPath} https://uploads.github.com/repos/${user}/${repository}/releases/\$RELEASE_ID/assets?name=${tag}.zip
			"""
    }
}
