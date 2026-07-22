pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        // Jenkins credentials (ID'ler mevcut)
        TEAMS_WEBHOOK      = credentials('teams-webhook-url')
        NETLIFY_AUTH_TOKEN = credentials('netlify-auth-token')
        NETLIFY_SITE_ID    = '6eef8988-c5f1-44b2-92cd-b8d594a9311c'
        REPORT_DIR         = 'target/cucumber-reports/cucumber-html-reports'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Kod GitHub\'dan cekiliyor...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Derleniyor...'
                sh 'mvn -B clean compile'
            }
        }

        stage('Test: API + Fatura Yukleme') {
            steps {
                echo '🧪 @api testleri kosuluyor (fatura yukleme dahil)...'
                // -Dcucumber.filter.tags runner tag'ini override eder → sadece @api
                // verify fazi maven-cucumber-reporting ile extended raporu uretir
                // testFailureIgnore: test fail'i pipeline'i hard-fail etmez (junit sonucu UNSTABLE yapar)
                sh '''
                    export DISPLAY=:99
                    Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
                    mvn -B verify -Dheadless=true -Dmaven.test.failure.ignore=true -Dcucumber.filter.tags="@api"
                '''
            }
        }

        stage('Rapor: Netlify Deploy') {
            steps {
                script {
                    // root URL calissin diye overview'i index.html yap
                    sh "cp ${REPORT_DIR}/overview-features.html ${REPORT_DIR}/index.html 2>/dev/null || true"
                    // Netlify'a deploy (token env'den otomatik okunur). Cikti netlify-out.json'a.
                    def rc = sh(returnStatus: true, script: """
                        npx --yes netlify-cli deploy --dir=${REPORT_DIR} --prod \
                            --site=${NETLIFY_SITE_ID} --json > netlify-out.json 2> netlify-err.log
                    """)
                    if (rc == 0) {
                        echo '✅ Netlify deploy tamam (link mail scriptinde netlify-out.json\'dan okunacak).'
                    } else {
                        echo '⚠️ Netlify deploy basarisiz/atlandi (mail Jenkins build linkine duser):'
                        sh 'tail -5 netlify-err.log 2>/dev/null || true'
                    }
                }
            }
        }

        stage('Arsivle') {
            steps {
                archiveArtifacts artifacts: 'target/cucumber-reports/**', allowEmptyArchive: true
                archiveArtifacts artifacts: 'netlify-out.json', allowEmptyArchive: true
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            echo '📧 Rapor maili gonderiliyor...'
            script {
                def result = currentBuild.currentResult   // SUCCESS / UNSTABLE / FAILURE
                // Mail scripti: cucumber JSON'dan ozet + netlify-out.json'dan link + guzel HTML
                sh "BUILD_RESULT='${result}' python3 ci/send_report_mail.py || true"
            }
            cleanWs()
        }
    }
}
