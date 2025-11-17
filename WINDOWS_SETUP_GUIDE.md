# Faturalab Web Automation - Windows Kurulum Rehberi

Bu rehber, Windows işletim sisteminde Faturalab Web Automation projesini kurmak ve çalıştırmak için gerekli tüm adımları içermektedir.

## ⚠️ Sistem Gereksinimleri

- **İşletim Sistemi**: Windows 10/11 (64-bit)
- **RAM**: Minimum 8GB, Önerilen 16GB
- **Depolama**: En az 5GB boş alan
- **İnternet Bağlantısı**: Gerekli

## 📦 Gerekli Yazılımların Kurulumu

### 1. Java Development Kit (JDK) 11 Kurulumu

#### Adım 1: JDK 11 İndirme
1. **Oracle JDK** (Önerilen):
   - [Oracle JDK 11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html) adresine gidin
   - "Windows x64 Installer" seçeneğini indirin
   
2. **OpenJDK** (Ücretsiz Alternatif):
   - [Eclipse Temurin JDK 11](https://adoptium.net/temurin/releases/?version=11) adresine gidin
   - "Windows x64" için `.msi` dosyasını indirin

#### Adım 2: JDK Kurulumu
1. İndirilen `.msi` dosyasını çift tıklayarak çalıştırın
2. Kurulum sihirbazını takip edin
3. Varsayılan kurulum yolunu kullanın: `C:\Program Files\Eclipse Adoptium\jdk-11.x.x-hotspot\`

#### Adım 3: JAVA_HOME Ortam Değişkenini Ayarlama
1. **Windows + R** tuşlarına basın ve `sysdm.cpl` yazıp Enter'a basın
2. **Gelişmiş** sekmesine tıklayın
3. **Ortam Değişkenleri** butonuna tıklayın
4. **Sistem değişkenleri** bölümünde **Yeni** butonuna tıklayın
5. Değişken adı: `JAVA_HOME`
6. Değişken değeri: `C:\Program Files\Eclipse Adoptium\jdk-11.x.x-hotspot` (kurulum yolunuza göre)
7. **Tamam** butonuna tıklayın

#### Adım 4: PATH Değişkenini Güncelleme
1. **Sistem değişkenleri** bölümünde **Path** değişkenini seçin ve **Düzenle** butonuna tıklayın
2. **Yeni** butonuna tıklayın
3. `%JAVA_HOME%\bin` yazın
4. **Tamam** butonuna tıklayıp tüm pencereleri kapatın

#### Adım 5: Java Kurulumunu Doğrulama
1. **Başlat** menüsünden **Command Prompt** (cmd) açın
2. Aşağıdaki komutları çalıştırın:
```cmd
java -version
javac -version
```
3. Her ikisi de Java 11 sürümünü göstermelidir

### 2. Apache Maven Kurulumu

#### Adım 1: Maven İndirme
1. [Apache Maven](https://maven.apache.org/download.cgi) adresine gidin
2. "Binary zip archive" linkini tıklayarak `apache-maven-3.x.x-bin.zip` dosyasını indirin

#### Adım 2: Maven Kurulumu
1. İndirilen zip dosyasını `C:\Program Files\` dizinine çıkarın
2. Klasör adını `apache-maven` olarak değiştirin (opsiyonel)

#### Adım 3: MAVEN_HOME Ortam Değişkenini Ayarlama
1. **Windows + R** tuşlarına basın ve `sysdm.cpl` yazıp Enter'a basın
2. **Gelişmiş** → **Ortam Değişkenleri**
3. **Sistem değişkenleri** bölümünde **Yeni**
4. Değişken adı: `MAVEN_HOME`
5. Değişken değeri: `C:\Program Files\apache-maven-3.x.x`

#### Adım 4: PATH Değişkenini Güncelleme
1. **Path** değişkenini düzenleyin
2. **Yeni** butonuna tıklayın
3. `%MAVEN_HOME%\bin` yazın

#### Adım 5: Maven Kurulumunu Doğrulama
```cmd
mvn -version
```

### 3. Git Kurulumu

#### Adım 1: Git İndirme ve Kurulum
1. [Git for Windows](https://git-scm.com/download/win) adresine gidin
2. Otomatik indirme başlamazsa "Click here to download manually" linkine tıklayın
3. İndirilen `.exe` dosyasını çalıştırın
4. Kurulum sırasında varsayılan ayarları kabul edin

#### Adım 2: Git Kurulumunu Doğrulama
```cmd
git --version
```

### 4. IDE Kurulumu (İsteğe Bağlı)

#### IntelliJ IDEA Community Edition (Önerilen)
1. [IntelliJ IDEA](https://www.jetbrains.com/idea/download/#section=windows) adresine gidin
2. **Community Edition**'ı indirin (ücretsiz)
3. Kurulum dosyasını çalıştırın ve varsayılan ayarları kabul edin

#### Eclipse IDE
1. [Eclipse IDE](https://www.eclipse.org/downloads/) adresine gidin
2. "Eclipse IDE for Java Developers" seçeneğini indirin
3. Kurulum dosyasını çalıştırın

## 🚀 Proje Kurulumu

### 1. Projeyi İndirme
```cmd
git clone https://github.com/yourcompany/faturalab-webAutomation.git
cd faturalab-webAutomation
```

### 2. Maven Bağımlılıklarını İndirme
```cmd
mvn clean install
```

### 3. Proje Yapısını Kontrol Etme
```cmd
dir src\main\java\com\faturalab\automation
dir src\test\java\com\faturalab\automation
```

## ⚙️ Tarayıcı Sürücüleri

Bu proje **WebDriverManager** kullandığı için tarayıcı sürücüleri otomatik olarak indirilir. Manuel kurulum gerekmez.

### Desteklenen Tarayıcılar:
- **Chrome** (Önerilen)
- **Firefox**
- **Edge**
- **Safari** (macOS'da)

## 🧪 Testleri Çalıştırma

### 1. Tüm Web Testlerini Çalıştırma
```cmd
mvn clean test -Dtest=TestRunner
```

### 2. API Testlerini Çalıştırma
```cmd
mvn clean test -Dtest=APITestRunner
```

### 3. Belirli Tarayıcıda Test Çalıştırma
```cmd
mvn clean test -Dtest=TestRunner -Dbrowser=chrome
mvn clean test -Dtest=TestRunner -Dbrowser=firefox
mvn clean test -Dtest=TestRunner -Dbrowser=edge
```

### 4. Belirli Tag'li Testleri Çalıştırma
```cmd
mvn clean test -Dtest=TestRunner -Dcucumber.filter.tags="@smoke"
mvn clean test -Dtest=TestRunner -Dcucumber.filter.tags="@regression"
```

### 5. TestNG ile Çalıştırma
```cmd
mvn clean test -DsuiteXmlFile=testng.xml
```

## 📊 Raporları Görüntüleme

### 1. Cucumber HTML Raporları
Testler çalıştıktan sonra:
```cmd
# Rapor klasörünü açma
explorer target\cucumber-reports
```

### 2. Allure Raporları
```cmd
# Allure raporu oluşturma ve görüntüleme
mvn allure:serve
```

## 🔧 Yapılandırma

### 1. Test Yapılandırması
`src/test/resources/config/dev.properties` dosyasını düzenleyin:
```properties
# Uygulama URL'leri
base.url=https://dev.faturalab.com/app

# Zaman aşımı ayarları (saniye)
explicit.wait=10
page.load.timeout=30
implicit.wait=5

# Tarayıcı ayarları
browser=chrome
headless=false
```

### 2. Log Yapılandırması
`src/main/resources/log4j2.xml` dosyasını ihtiyacınıza göre düzenleyin.

## 🚨 Sorun Giderme

### 1. "JAVA_HOME is not set" Hatası
- JAVA_HOME ortam değişkeninin doğru ayarlandığından emin olun
- Bilgisayarınızı yeniden başlatın
- Command Prompt'u yeniden açın

### 2. "mvn command not found" Hatası
- MAVEN_HOME ve PATH değişkenlerini kontrol edin
- Maven'in doğru kurulduğundan emin olun

### 3. WebDriver Hataları
- İnternet bağlantınızı kontrol edin (WebDriverManager için gerekli)
- Antivirus yazılımınızın indirmeleri engellememesini sağlayın

### 4. Test Çalışmazsa
```cmd
# Bağımlılıkları yeniden yükleme
mvn clean install -U

# Önbelleği temizleme
mvn dependency:purge-local-repository
```

### 5. Port Çakışması
Eğer Allure raporları açılmazsa, farklı port kullanın:
```cmd
mvn allure:serve -Dallure.serve.port=8080
```

## 💡 IDE Yapılandırması

### IntelliJ IDEA
1. **File** → **Open** → Proje klasörünü seçin
2. Maven projesini otomatik algılayacaktır
3. **File** → **Settings** → **Build Tools** → **Maven** → JDK sürümünü kontrol edin
4. **File** → **Project Structure** → **Project** → Project SDK'yı Java 11 olarak ayarlayın

### Eclipse
1. **File** → **Import** → **Existing Maven Projects**
2. Proje klasörünü seçin
3. **Project** → **Properties** → **Java Build Path** → JRE sürümünü kontrol edin

## 📝 Ek Bilgiler

### Kullanışlı Komutlar
```cmd
# Proje temizleme
mvn clean

# Sadece derleme
mvn compile

# Test derleme
mvn test-compile

# Bağımlılık ağacını görme
mvn dependency:tree

# Proje bilgilerini görme
mvn help:describe -Dplugin=compiler
```

### Performans İpuçları
- Testleri paralel çalıştırmak için `testng.xml` dosyasındaki `thread-count` değerini artırın
- Headless mod kullanarak testleri daha hızlı çalıştırın: `-Dheadless=true`
- CI/CD ortamında test sonuçlarını saklamak için uygun raporlama formatlarını kullanın

Bu rehberi takip ederek Windows bilgisayarınızda Faturalab Web Automation projesini başarıyla kurabilir ve çalıştırabilirsiniz. 