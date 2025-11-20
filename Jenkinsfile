pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📦 Checking out code from GitHub...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 Building the project...'
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile'
                    } else {
                        bat 'mvn clean compile'
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                echo '🧪 Running all tests...'
                script {
                    if (isUnix()) {
                        // Headless mode for Linux server
                        sh 'export DISPLAY=:99 && Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 & mvn clean test -Dheadless=true'
                    } else {
                        bat 'mvn clean test'
                    }
                }
            }
        }
        
        stage('Archive Results') {
            steps {
                echo '📊 Archiving test results...'
                // Archive Cucumber JSON reports
                archiveArtifacts artifacts: 'target/cucumber-reports/*.json', allowEmptyArchive: true
                
                // Archive Cucumber HTML reports
                archiveArtifacts artifacts: 'target/cucumber-reports/*.html', allowEmptyArchive: true
                
                // Archive screenshots
                archiveArtifacts artifacts: 'screenshots/*.png', allowEmptyArchive: true
                
                // Publish JUnit test results
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            }
        }
    }
    
    post {
        always {
            echo '🧹 Cleaning up...'
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}

