# language: tr
@api @validation @fatura
Özellik: Fatura API Hata Validasyonları

  Arka Plan:
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı

  @duplicate @buyer
  Senaryo: Duplicate invoice detection - EXIST_INVOICE hatası
    # NOT: API'de DUPLICATED_INVOICE_NO ve EXIST_INVOICE durumları
    # her ikisi de aynı hata kodu ile dönüyor: "EXIST_INVOICE"
    # Bu tek senaryo her iki durumu da test eder
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType |
      |           | 4050604050    | 1000          | E_FATURA    |
    O zaman fatura başarıyla yüklenmiş olmalı
    Eğer ki aynı fatura tekrar yüklenirse
    O zaman hata mesajı alınmalı
    Ve hata kodu 'EXIST_INVOICE' olmalı
    Ve hata mesajı 'Invoice available in the system' içermeli
    Ve fatura yüklenmemiş olmalı

  @amount @buyer
  Senaryo: Geçersiz fatura tutarı - DISCOUNTED_INVOICE_AMOUNT
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | AMT-INV-0001     | 4050604050    | 0             | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_REQUESTED_AMOUNT' olmalı
    Ve hata mesajı 'Invalid invoice assignment amount' içermeli
    Ve fatura yüklenmemiş olmalı

  @currency @buyer
  Senaryo: Geçersiz para birimi - DISCOUNTED_INVOICE_CURRENCY
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | currencyType |
      | CUR-INV-0001     | 4050604050    | 1000          | E_FATURA    | INVALID      |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_INVOICE_CURRENCY_TYPE' olmalı
    Ve hata mesajı 'Currency cannot be passed blank' içermeli
    Ve fatura yüklenmemiş olmalı

  @date @buyer
  # Eski "Geçersiz fatura tarihi" senaryosu üç kavramı karıştırıyordu (bozuk format
  # gönderip boş-tarih mesajı bekliyordu) ve kronik fail'di. API'nin gerçek davranışına
  # göre (kaynak kod: Api.java uploadInvoice) ikiye bölündü:
  #  - Tarih NULL           -> INVALID_INVOICE_DATE (ErrorType.63: "The invoice date cannot be blank.")
  #  - Tarih formatı BOZUK  -> INVALID_REQUEST_PARAMS (deserialize aşamasında reddedilir,
  #                            iş kuralı validasyonuna hiç ulaşmaz)
  Senaryo: Boş fatura tarihi - INVALID_INVOICE_DATE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | invoiceDate |
      | DAT-INV-0001     | 4050604050    | 1000          | E_FATURA    | YOK         |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_INVOICE_DATE' olmalı
    Ve hata mesajı 'The invoice date cannot be blank' içermeli
    Ve fatura yüklenmemiş olmalı

  Senaryo: Bozuk formatlı fatura tarihi - INVALID_REQUEST_PARAMS
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | invoiceDate |
      | DAT-INV-0002     | 4050604050    | 1000          | E_FATURA    | 2025-13-99  |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_REQUEST_PARAMS' olmalı
    Ve hata mesajı 'Invalid request parameter' içermeli
    Ve fatura yüklenmemiş olmalı

  @dueDate @buyer
  Senaryo: Geçersiz vade tarihi - DISCOUNTED_INVOICE_DUE_DATE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | dueDate    |
      | DUE-INV-0001     | 4050604050    | 1000          | E_FATURA    | 1999-01-01 |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVOICE_EXPIRED' olmalı
    Ve hata mesajı 'The due date is invalid' içermeli
    Ve fatura yüklenmemiş olmalı

  @taxno @buyer
  Senaryo: Geçersiz tedarikçi VKN - DISCOUNTED_INVOICE_TAX_NUMBER
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | TAX-INV-0001     | ABCDEFGHIJ    | 1000          | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'NOT_EXIST_TAX_NUMBER' olmalı
    Ve hata mesajı 'Commercial enterprise TIN' içermeli
    Ve fatura yüklenmemiş olmalı

  @type @buyer
  Senaryo: Geçersiz fatura tipi - DISCOUNTED_INVOICE_TYPE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType    |
      | TYP-INV-0001     | 4050604050    | 1000          | INVALID_TYPE   |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_INVOICE_TYPE' olmalı
    Ve hata mesajı 'Invoice type is invalid' içermeli
    Ve fatura yüklenmemiş olmalı

  @hash @buyer
  Senaryo: Geçersiz fatura hash kodu - DISCOUNTED_INVOICE_HASH_CODE (E-Fatura)
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | hashCode |
      | HASH-INV-0001    | 4050604050    | 1000          | E_FATURA    |          |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_HASH_CODE' olmalı
    Ve hata mesajı 'Invalid hash code' içermeli
    Ve fatura yüklenmemiş olmalı

  @taxExclusive @buyer
  Senaryo: Geçersiz KDV'siz tutar - DISCOUNTED_INVOICE_TAX_EXCLUSIVE_AMOUNT (E-Arşiv)
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | taxExclusiveAmount |
      | TXE-INV-0001     | 4050604050    | 1000          | E_ARSIV     | 0                  |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_TAX_EXCLUSIVE_AMOUNT' olmalı
    Ve hata mesajı 'Invalid VAT-free amount' içermeli
    Ve fatura yüklenmemiş olmalı

  @invalid @buyer
  Senaryo: Geçersiz VKN - INVALID_TAX_NUMBER
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType |
      |           |               | -1            | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_TAX_NUMBER' olmalı
    Ve hata mesajı 'Invalid Tax Id' içermeli
    Ve fatura yüklenmemiş olmalı

  @pending @manual
  Senaryo: Fatura ıskontolanmış - DISCOUNTED_INVOICE
    # NOT: API aslında EXIST_INVOICE kodu dönüyor (zaten sistemde olan fatura)
    # Bu senaryo manuel test gerektirir - spesifik discounted invoice durumu için
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | DSC-INV-0001     | 4050604050    | 1000          | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'EXIST_INVOICE' olmalı
    Ve hata mesajı 'Invoice available in the system' içermeli
    Ve fatura yüklenmemiş olmalı

  # ─── Vade / Ek Vade Tarihi Validasyonları ──────────────────────────────────
  # Beklenen kod + mesajlar KAYNAK KODDAN alındı (web-application, origin/vaadin-24):
  #   api/integration/version/v0/Api.java (uploadInvoice) + api/integration/common/ErrorType.java
  #   INVOICE_EXPIRED                     -> Api.227 (dinamik): "...Technical Error Detail: The due date
  #                                          is invalid. (It should be at least {N} days.)" — ALBC buyer
  #                                          daycountbeforeduedate=1 (DB doğrulandı)
  #   INVALID_DUE_DATE                    -> ErrorType.44: "The due date is invalid"
  #                                          (appsettings.MAX_DUE_DAY_LIMIT=1800 aşımı)
  #   INVALID_ADDITIONAL_DUE_DATE_AFTER   -> ErrorType.175: "The additional due date cannot be earlier than the due date."
  #   INVALID_DUE_DATE_HOLIDAY            -> ErrorType.174: "The due date falls on a holiday. Please check."
  #   INVALID_ADDITIONAL_DUE_DATE_HOLIDAY -> ErrorType.176: "The additional due date falls on a holiday. Please check."
  # Tarih token'ları stepdef'te çözülür: TODAY±N, HOLIDAY (bir sonraki 29 Ekim), YOK (alan gönderilmez).
  # NOT: API, additionalDueDate DOLU ise tatil kontrolünü YALNIZCA ek vadeye uygular; dueDate-tatil
  # senaryosunda bu yüzden additionalDueDate=YOK zorunludur.

  @vade @duedate @buyer
  Senaryo: Geçmiş vade tarihi - INVOICE_EXPIRED
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType | dueDate |
      |           | 4050604050    | 1000          | E_FATURA    | TODAY-5 |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVOICE_EXPIRED' olmalı
    Ve hata mesajı 'The due date is invalid. (It should be at least 1 days.)' içermeli
    Ve fatura yüklenmemiş olmalı

  @vade @duedate @buyer
  Senaryo: Bugün vadeli fatura (sınır değer) - INVOICE_EXPIRED
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType | dueDate |
      |           | 4050604050    | 1000          | E_FATURA    | TODAY   |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVOICE_EXPIRED' olmalı
    Ve hata mesajı 'The due date is invalid. (It should be at least 1 days.)' içermeli
    Ve fatura yüklenmemiş olmalı

  @vade @duedate @buyer
  Senaryo: Vade tarihi maksimum limiti aşıyor (1800 gün) - INVALID_DUE_DATE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType | dueDate    |
      |           | 4050604050    | 1000          | E_FATURA    | TODAY+2000 |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_DUE_DATE' olmalı
    Ve hata mesajı 'The due date is invalid' içermeli
    Ve fatura yüklenmemiş olmalı

  @vade @additional-duedate @buyer
  Senaryo: Ek vade tarihi vade tarihinden önce - INVALID_ADDITIONAL_DUE_DATE_AFTER
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType | dueDate  | additionalDueDate |
      |           | 4050604050    | 1000          | E_FATURA    | TODAY+30 | TODAY+20          |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_ADDITIONAL_DUE_DATE_AFTER' olmalı
    Ve hata mesajı 'The additional due date cannot be earlier than the due date.' içermeli
    Ve fatura yüklenmemiş olmalı

  @vade @duedate @holiday @buyer
  Senaryo: Vade tarihi resmi tatile denk geliyor - INVALID_DUE_DATE_HOLIDAY
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType | dueDate | additionalDueDate |
      |           | 4050604050    | 1000          | E_FATURA    | HOLIDAY | YOK               |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_DUE_DATE_HOLIDAY' olmalı
    Ve hata mesajı 'The due date falls on a holiday. Please check.' içermeli
    Ve fatura yüklenmemiş olmalı

  @vade @additional-duedate @holiday @buyer
  Senaryo: Ek vade tarihi resmi tatile denk geliyor - INVALID_ADDITIONAL_DUE_DATE_HOLIDAY
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType | dueDate  | additionalDueDate |
      |           | 4050604050    | 1000          | E_FATURA    | TODAY+30 | HOLIDAY           |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_ADDITIONAL_DUE_DATE_HOLIDAY' olmalı
    Ve hata mesajı 'The additional due date falls on a holiday. Please check.' içermeli
    Ve fatura yüklenmemiş olmalı
