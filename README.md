# Madlen AI Chat Platform

Bu proje, konteyner tabanlı bir yapıda çalışan, arka tarafta Spring Boot, önde ise Docker içinde nginx ile servis edilen bir React arayüzü barındıran basit bir sohbet uygulamasıdır. Amaç, dışarıdan sağlanan bir LLM/OpenRouter benzeri API anahtarı ile konuşabilen, bu anahtarı imajın içine gömmeyen ve yerel ortamda kolayca ayağa kaldırılabilen bir iskelet sunmaktır.

Uygulama üç ana bileşenden oluşur:

1. backend: Spring Boot 3 tabanlı servis. Konuşma uç noktalarını sağlar, veri tabanını yönetir, izleme için Jaeger’e trace yollar.
2. frontend: React/Vite ile geliştirilmiş, build edildikten sonra nginx içinde 80. porttan servis edilen arayüz.
3. jaeger: Gözlemlenebilirlik için all-in-one Jaeger bileşeni.

Bu doküman şunları anlatır:
- Projeyi neden bu teknolojilerle kurduğumuz,
- Yerel ortamda nasıl çalıştırabileceğiniz,
- Gizli anahtarları nasıl yönetmeniz gerektiği,
- Proje yapısı,
- Docker ile çalıştırma adımları,
- Geliştirme sırasında izlenen süreç.

## 1. Kullanılan Teknolojiler ve Tercih Gerekçeleri

### 1.1. Spring Boot 3 (Backend)
Spring Boot seçilmesinin başlıca nedenleri:
- Java ekosisteminde hızlı prototipleme ve üretim ortamına yakın geliştirme imkanı sağlar.
- Tek jar çıktısı verdiği için Docker imajına koyup çalıştırmak çok kolaydır.
- Spring Data JPA ile H2 gibi gömülü/veri dosyasına yazan veritabanlarını hemen kullanabildiğimiz için ek yapılandırmaya gerek kalmaz.
- Spring’in configuration mekanizması sayesinde dışarıdan environment variable ile API key alabiliriz. Böylece jar’ın içine gizli bilgi gömülmez.

### 1.2. React + Vite (Frontend)
- Hızlı geliştirme deneyimi sağlar.
- Üretim ortamında SPA’yi doğrudan nginx ile servis etmek mümkündür.
- Dockerfile iki aşamalıdır: önce Node ile build, sonra sadece dist klasörünü nginx imajına kopyalarız. Böylece prod imajı küçük olur.

### 1.3. Docker Compose
- Tüm bileşenleri (backend, frontend, jaeger) tek komutla ayağa kaldırmak için kullanılmıştır.
- Her servisin portları dışarıya açıldığı için Windows/WSL ortamında da rahat çalışır.
- Ortak volume’ler (örneğin uploads, data) host ile paylaşıldı ki container silinse bile veriler kaybolmasın.

### 1.4. Jaeger
- Spring uygulamasından gelen trace’leri görmek için eklenmiştir.
- Geliştirme ortamında neler olup bittiğini izlemek, özellikle de dış servislere istek atarken gecikmeleri görmek için yararlıdır.

## 2. Mimari Görünüm

Basitleştirilmiş akış şu şekildedir:

Kullanıcı → (tarayıcı) → frontend (nginx, 5173:80) → backend (Spring Boot, 8083) → harici LLM/OpenRouter API

Backend tarafında model listesi veya istek atılacak model bilgisi dinamik tutulacak şekilde tasarlanmıştır. API anahtarı uygulamanın içine hard-code edilmemiştir. Bunun yerine docker-compose.yml üzerinden ortam değişkeni olarak verilecek şekilde ayarlanmıştır. Böylece repoya bu anahtarın girmesi engellenmiştir.

## 3. Gizli Bilgilerin Yönetimi

Bu projede API anahtarının doğrudan kaynak koduna yazılmaması özellikle hedeflenmiştir. Bunun nedeni:
- Git geçmişine giren bir anahtarın tamamen silinmesi zordur.
- Public GitHub deposunda kalması halinde anahtar kötü amaçla kullanılabilir.
- Çoğu servis, sızan anahtarları otomatik tespit edip hesabı kısıtlar.

Bu yüzden aşağıdaki yöntem kullanılmalıdır:

- `docker-compose.yml` içinde `OPENROUTER_API_KEY` gibi bir environment değeri tanımlanır.
- Spring Boot içinde bu değer environment’tan okunur.
- Depoya atarken bu değeri gerçek anahtar olarak bırakmamalısınız. Örnek bir anahtar veya tamamen boş bırakılmış değişken şeklinde paylaşmalısınız.
- İsterseniz `.env` dosyası oluşturup oradan okutabilir ve `.env` dosyasını Git’e eklemezsiniz.

Önemli zorunluluk: Proje çalışsın istiyorsanız kendi ortamınızda bir API key yazmanız gerekir. Bu anahtar repoda yoktur, bilerek yoktur. Çalıştırmadan önce ya `docker-compose.yml` içine gerçek anahtarı ekleyin ya da `.env` dosyası oluşturup aşağıdaki gibi doldurun:

```env
OPENROUTER_API_KEY=buraya-kendi-anahtariniz
SPRING_PROFILES_ACTIVE=default
```

Ardından compose tarafında bu değeri kullanırsınız. Aksi halde backend ayağa kalksa bile dış LLM servisiyle konuşamaz.

## 4. Proje Yapısı

Aşağıdaki yapıya benzer bir düzenin olması beklenir:

```
madlen/
├─ docker-compose.yml
├─ README.md
├─ backend/ veya bootcamptoprod/bootcamptoprod
│  ├─ src/
│  ├─ pom.xml
│  └─ Dockerfile
├─ frontend/chat-ui
│  ├─ src/
│  ├─ package.json
│  └─ Dockerfile
├─ uploads/          (host ile paylaşılan klasör)
└─ data/             (H2 dosyaları için volume)
```

Compose içinde örnek olarak şöyle bir backend tanımı vardır:

```yaml
backend:
  build:
    context: ./bootcamptoprod/bootcamptoprod
    dockerfile: Dockerfile
  container_name: madlen-backend
  ports:
    - "8083:8083"
  environment:
    - SPRING_PROFILES_ACTIVE=default
    # Bu değişkeni gerçek ortamda doldurmanız gerekir.
    - OPENROUTER_API_KEY=${OPENROUTER_API_KEY}
  volumes:
    - ./uploads:/app/uploads
    - ./data:/app/data
  depends_on:
    - jaeger
```

Bu örnekte anahtar doğrudan yazılmamıştır, dışarıdan geleceği varsayılmıştır.

## 5. Projeyi Çalıştırma

### 5.1. Önkoşullar
- Docker ve Docker Compose yüklü olmalı.
- Git ile projeyi çekmiş olmalısınız.
- OpenRouter veya benzeri sağlayıcıdan alınmış bir API key elinizde olmalı.
- Çalıştırmadan önce bu key’i ya `docker-compose.yml` içine yazmalı ya da `.env` dosyasına koymalısınız. Bu adımı yapmazsanız uygulama dış servise bağlanamaz.

### 5.2. Repoyu Klonlama

```bash
git clone https://github.com/<kullanici-adi>/madlen-ai-chat.git
cd madlen-ai-chat
```

### 5.3. Ortam Değişkenini Ayarlama

İki yol vardır:

1. `docker-compose.yml` içindeki `OPENROUTER_API_KEY` satırını yerel anahtarınızla değiştirin.
2. `.env` dosyası oluşturup şu içerikle kaydedin:

```env
OPENROUTER_API_KEY=buraya-kendi-anahtariniz
SPRING_PROFILES_ACTIVE=default
```

ve compose içinde `- OPENROUTER_API_KEY=${OPENROUTER_API_KEY}` satırının olduğundan emin olun.

### 5.4. Derleme ve Çalıştırma

Proje kökünde:

```bash
docker compose build
docker compose up
```

Compose başarılıysa aşağıdakiler olur:
- backend 8083 portunda ayağa kalkar,
- frontend nginx içinde 80 portunda ayağa kalkar ve host’tan 5173’e map edildiği için http://localhost:5173 adresinden ulaşılır,
- jaeger 16687 portunda açılır.

Tarayıcıdan şu adresler denenebilir:

- Frontend: http://localhost:5173
- Backend: http://localhost:8083
- Jaeger: http://localhost:16687

Eğer frontend boş geliyorsa nginx imajının dist klasörünü görmediği ya da host’taki 5173 portunun dolu olduğu anlamına gelir. Bu durumda compose içindeki portu değiştirebilirsiniz.

## 6. Geliştirme Sürecinde İzlenen Yol

- İlk derlemede Spring Boot bir property bulamadığı için uygulama başlarken hata verdi: `spring.ai.openai.api-key`. Bu değer dışarıdan verilmediği için hata aldı.
- DTO tarafında kullanılan `OpenRouterModel` sınıfının yapıcı metodu ile servis içinde yapılan kullanım birbirini tutmadığı için Maven derleme aşamasında hata verdi.
- Bu hatalar giderildikten sonra Spring Boot imajı düzgün build edildi.
- Compose içindeki servis bağımlılıkları tanımlandı. Backend’in jaeger’e bağımlı olması sağlandı.
- API key compose’tan verildiğinde uygulama çalıştı.
- Güvenlik açısından, gerçek API anahtarının repoya girmemesi için compose’taki değer temizlendi. Bu, GitHub’a çıkmadan önce yapılması gereken kritik adımdır.

Bu süreç README’de tutulmuştur ki projeyi gören kişi, neden bazı property’lerin dinamik olduğunu ve neden compose içinde gerçek bir key bulunmadığını anlasın.

## 7. Git ile Yayınlama Adımları

1. Yerel depoyu başlatın:
   ```bash
   git init
   ```

2. Dosyaları ekleyin:
   ```bash
   git add .
   ```

3. Commit oluşturun:
   ```bash
   git commit -m "Initial commit"
   ```

4. GitHub’da boş bir repo açın.

5. Remote’u doğru adresle ekleyin:
   ```bash
   git remote add origin https://github.com/<kullanici-adi>/madlen-ai-chat.git
   ```

6. Ana branch’i gönderin:
   ```bash
   git branch -M main
   git push -u origin main
   ```

Gerçek API anahtarını hiçbir zaman commit etmeyin. Bunu her zaman ya ortam değişkeni ya da Git’e girmeyen bir `.env` ile verin.

## 8. Karşılaşılabilecek Sorunlar

- Hata: `fatal: not a git repository`: Bulunduğunuz klasörde `git init` çalıştırmamışsınızdır.
- Hata: `400` veya `403` push: Remote adresi yanlıştır veya GitHub token/pat gerektiriyordur.
- Docker build sırasında Maven derlemesi başarısızsa log içinde Java compilation error’larına bakılmalıdır.
- Frontend açılmıyorsa container log’una bakılmalı ya da 5173 portu daha önce Vite tarafından kullanılıyorsa port değiştirilmelidir.

## 9. Sonuç

Bu repo, üretime hazır son halinden çok, iyi yapılandırılmış bir başlangıç projesi olarak düşünülmelidir. Konteynerler arası iletişim, dış API anahtarının dışarıdan verilmesi, izlenebilirlik servisinin eklenmesi ve frontend’in ayrı bir imaja alınması gibi noktalar gerçek hayattaki senaryoların basitleştirilmiş hâlidir. Bu yüzden repoya bakan biri kolayca kendi LLM endpoint’ini entegre edebilir ve sadece compose’taki environment’ı değiştirerek yeni anahtarlarla deneyebilir.
