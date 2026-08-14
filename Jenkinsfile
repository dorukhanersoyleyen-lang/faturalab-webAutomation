pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        // Jenkins credentials (ID'ler mevcut)
        TEAMS_WEBHOOK = credentials('teams-webhook-url')
        REPORT_DIR    = 'target/cucumber-reports/cucumber-html-reports'
        // Rapor kendi dev sunucumuzdan yayinlanir (nginx :8090, VPN ici — Netlify bagimliligi kaldirildi)
        PUBLISH_ROOT  = '/var/www/qa-reports'
        REPORT_URL    = 'http://192.168.97.33:8090/latest/'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
    }

    triggers {
        // Her gun 08:00 (Istanbul) otomatik kosu + kod degisiminde SCM polling
        cron('TZ=Europe/Istanbul\n0 8 * * *')
        pollSCM('H/2 * * * *')
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

        stage('Test: Fatura API + Yukleme + TZF + DTS') {
            steps {
                echo '🧪 Fatura API + Yukleme + TZF + DTS testleri kosuluyor...'
                // -Dcucumber.filter.tags runner tag'ini override eder → sadece @api
                // verify fazi maven-cucumber-reporting ile extended raporu uretir
                // testFailureIgnore: test fail'i pipeline'i hard-fail etmez (junit sonucu UNSTABLE yapar)
                // @dts: DTS-001 (DRAFT->COMPLETED tam yaşam döngüsü) + DTS_API-001 (batch REST) +
                // dts-fk-crud/dts-bayi-crud/dts-limit-crud (Bayi/Finansal Kurum/Bayi Limitleri CRUD) —
                // hepsi Test Otomasyon Sadece Tedarikçi (company.id=998) izole test firmasında koşar,
                // Petek A.Ş.'ye dokunmaz. ~10 dk ek süre (5 UI/API senaryo) — OP#5878.
                // @dtf: DTF-001 (izole DTF zinciri — Ana Firma buyer.id=145 / Ara Tedarikçi
                // company.id=998+buyer.id=151 / Alt Tedarikçiler 401+243) — TZF ile AYNI izole
                // kimlikleri reuse eder, ~3.5-4 dk ek süre — OP#5800 (2026-08-13, 2/2 yeşil doğrulandı).
                sh '''
                    export DISPLAY=:99
                    Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
                    mvn -B verify -Dheadless=true -Dmaven.test.failure.ignore=true -Dcucumber.filter.tags="(@fatura and not @company and not @onay) or @tzf or @dfp-001 or @dts or @dtf"
                '''
            }
        }

        stage('Rapor: Dev Sunucu Yayini') {
            steps {
                // Jenkins dev_ci'de kosuyor → yayin = yerel kopya (dis bagimlilik/token yok).
                // http://192.168.97.33:8090/latest/ her zaman son build; build-N gecmisi tutulur.
                sh '''
                    cp ${REPORT_DIR}/overview-features.html ${REPORT_DIR}/index.html 2>/dev/null || true
                    DEST=${PUBLISH_ROOT}/build-${BUILD_NUMBER}
                    mkdir -p "$DEST"
                    cp -r ${REPORT_DIR}/. "$DEST"/
                    ln -sfn "$DEST" ${PUBLISH_ROOT}/latest
                    # Son 20 build'i tut, eskileri temizle
                    ls -dt ${PUBLISH_ROOT}/build-* 2>/dev/null | tail -n +21 | xargs -r rm -rf
                    echo "Rapor yayinda: ${REPORT_URL} (kalici: .../build-${BUILD_NUMBER}/)"
                '''
            }
        }

        stage('Arsivle') {
            steps {
                archiveArtifacts artifacts: 'target/cucumber-reports/**', allowEmptyArchive: true
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            echo '📧 Rapor maili gonderiliyor...'
            script {
                def result = currentBuild.currentResult   // SUCCESS / UNSTABLE / FAILURE
                // Mail scripti: cucumber JSON'dan ozet + REPORT_URL (dev sunucu) linki + guzel HTML
                sh "BUILD_RESULT='${result}' REPORT_URL='${REPORT_URL}' python3 ci/send_report_mail.py || true"
            }
            cleanWs()
        }
    }
}
