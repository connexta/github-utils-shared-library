import java.util.regex.Pattern
import java.util.regex.Matcher
import java.lang.Math

def call(String log) {
	script {
		def regexToErrorMessages = [:]
		regexToErrorMessages.put(".*Failed to run task: 'yarn install.*", "Error installing packages from yarn")
		regexToErrorMessages.put(".*Couldn't resolve host 'github.com'.*", "Github Resolution Problem")
		regexToErrorMessages.put(".*One or more dependencies were identified with vulnerabilities that have a CVSS score greater.*", "OWASP: Vulnerability Found")
		regexToErrorMessages.put(".*Could not transfer artifact.*from/to office.*", "An error occurred while downloading dependencies from nexus")
		regexToErrorMessages.put(".*Unable to download the NVD CVE data.*", "OWASP NVD CVE data download failure")
		regexToErrorMessages.put(".*maven-compiler-plugin.*Compilation failure:.*", "Compiler Failure: maven-compiler-plugin failed to compile project")
		regexToErrorMessages.put(".*Could not resolve dependencies for project.*", "Dependency Resolution Error")
		regexToErrorMessages.put(".*project (.*): Failed to run task: 'npm install.*", "NPM Install Failure: npm install task failed on project")
		regexToErrorMessages.put(".*Found.*non-complying files, failing build.*", "Code Formatting Violations (fmt-maven-plugin)")
		regexToErrorMessages.put(".*Automatic merge failed; fix conflicts and then commit the result.*", "Merge Conflict: There was a merge conflict while trying to merge a PR branch into master")
		regexToErrorMessages.put(".*Fail to download sonar.*", "Failed to download Sonarqube tools")
		regexToErrorMessages.put(".*Coverage checks have not been met.*", "Coverage Checks: Coverage checks have not been met (jacoco plugin)")
		regexToErrorMessages.put(".*((Error\\(s\\) found in bundle configuration)|maven-bundle-plugin:.*:bundle failed.).*", "Bundle Creation Failure: Maven bundle plugin failed to create a bundle")
		regexToErrorMessages.put(".*An unexpected error occurred: \"ENOENT: no such file or directory, chmod .*", "yarn dependency not downloaded and installed correctly")
		regexToErrorMessages.put(".*Git branch of name 'refs/remotes/origin/master' not found.*", "Git failed to fetch PR branch or master branch from SCM")
		regexToErrorMessages.put(".*=> has formatting issues.*", "UI Formatting Error")
		regexToErrorMessages.put(".*cnpmjs.org.*", "Bad npm/yarn download location")
		regexToErrorMessages.put(".*Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin.*", "Checkstyle ruleset violation")
		regexToErrorMessages.put(".*\\[ERROR\\] Failures:.*", "Failed Unit Tests")
		regexToErrorMessages.put(".*Failed to run task: 'yarn run test' failed\\..*", "Failed UI Tests")
		def foundFailures = false
		def failureMessage = "<h3>Suspected Failure(s):</h3> <ul>"
		for (mapping in regexToErrorMessages){
			if (log.matches("(?s)" + mapping.key)) {
				failureMessage += "<li>" + mapping.value + "</li>"
				foundFailures = true
			}
		}
		failureMessage += "</ul>"
		failureMessage += getTestFailures(log)

		if (!foundFailures) {
			failureMessage = doGenericErrorSearch(log)
		}
		failureMessage = formatForJSON(failureMessage)
		return failureMessage
	}
}

def formatForJSON(String message) {
	message = message.replaceAll("\"", "\\\\\"")
	message = message.replaceAll("\n", "\\\\n")
	message = message.replaceAll("\t", "\\\\t")
	return message
}

def getTestFailures(String log) {
	def maxStackTraceLength = 500
	def failureStart = log.indexOf("[ERROR] Failures: ")
	def failedTestsMessage = ""
		if (failureStart != -1) {
			def failureEnd = log.indexOf("[ERROR] Tests run:", failureStart)
			failedTestsMessage = "\n\n```\n"
			if (failureEnd - failureStart > maxStackTraceLength) {
				failedTestsMessage += log.substring(failureStart, failureStart + maxStackTraceLength) + "..."
			} else {
				failedTestsMessage += log.substring(failureStart, failureEnd)
			}
			failedTestsMessage += "\n```\n"
		}
	return failedTestsMessage
}

def doGenericErrorSearch(String log) {
	def foundFailure = false
	def maxStackTraceLength = 500
	def errorMessage = "Relevant Output: \n\n```\n"
	def errorPattern = Pattern.compile("(?s)Failures: [1-9]+|(?s)Errors: [1-9]+")
	def errorMatcher = errorPattern.matcher(log)
	if (errorMatcher.find()) {
		foundFailure = true
		errorMessage += log.substring(errorMatcher.start(), Math.min(errorMatcher.start() + maxStackTraceLength, log.length())) + "...\n"
	}

	def exceptionPattern = Pattern.compile("(?s)java\\.*.*exception")
	def exceptionMatcher = exceptionPattern.matcher(log)
	if (exceptionMatcher.find()) {
		foundFailure = true
		errorMessage += log.substring(exceptionMatcher.start(), Math.min(exceptionMatcher.start() + maxStackTraceLength, log.length())) + "...\n"
	}

	errorMessage += "\n```\n"
	if (foundFailure) {
		return errorMessage	
	} else {
		return "Unable to Detect Failure Reason"
	}	
}