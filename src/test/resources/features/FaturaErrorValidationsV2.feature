# language: tr
@api @validation @fatura @v2
Özellik: Fatura API Hata Validasyonları V2

  Arka Plan:
    Diyelim ki "dev.faturalab.v2.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı

  @duplicate @buyer @v2
  Senaryo: V2 - Duplicate invoice detection - EXIST_INVOICE hatası
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType |
      |           | 1234567893    | 1000          | E_FATURA    |
    O zaman fatura başarıyla yüklenmiş olmalı
    Eğer ki aynı fatura tekrar yüklenirse
    O zaman hata mesajı alınmalı
    Ve hata kodu 'EXIST_INVOICE' olmalı
    Ve hata mesajı 'Invoice available in the system' içermeli
    Ve fatura yüklenmemiş olmalı

  @amount @buyer @v2
  Senaryo: V2 - Geçersiz fatura tutarı - DISCOUNTED_INVOICE_AMOUNT
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | AMT-V2-INV-001   | 1234567893    | 0             | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_REQUESTED_AMOUNT' olmalı
    Ve hata mesajı 'Invalid invoice assignment amount' içermeli
    Ve fatura yüklenmemiş olmalı

  @currency @buyer @v2
  Senaryo: V2 - Geçersiz para birimi - DISCOUNTED_INVOICE_CURRENCY
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | currencyType |
      | CUR-V2-INV-0001  | 1234567893    | 1000          | E_FATURA    | INVALID      |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_INVOICE_CURRENCY_TYPE' olmalı
    Ve hata mesajı 'Currency cannot be passed blank' içermeli
    Ve fatura yüklenmemiş olmalı

  @date @buyer @v2
  Senaryo: V2 - Geçersiz fatura tarihi - DISCOUNTED_INVOICE_DATE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | invoiceDate |
      | DAT-V2-INV-0001  | 1234567893    | 1000          | E_FATURA    | 2025-13-99  |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_INVOICE_DATE' olmalı
    Ve hata mesajı 'The invoice date cannot be blank' içermeli
    Ve fatura yüklenmemiş olmalı

  @dueDate @buyer @v2
  Senaryo: V2 - Geçersiz vade tarihi - DISCOUNTED_INVOICE_DUE_DATE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | dueDate    |
      | DUE-V2-INV-0001  | 1234567893    | 1000          | E_FATURA    | 1999-01-01 |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVOICE_EXPIRED' olmalı
    Ve hata mesajı 'The due date is invalid' içermeli
    Ve fatura yüklenmemiş olmalı

  @taxno @buyer @v2
  Senaryo: V2 - Geçersiz tedarikçi VKN - DISCOUNTED_INVOICE_TAX_NUMBER
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | TAX-V2-INV-0001  | ABCDEFGHIJ    | 1000          | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'NOT_EXIST_TAX_NUMBER' olmalı
    Ve hata mesajı 'Commercial enterprise TIN' içermeli
    Ve fatura yüklenmemiş olmalı

  @type @buyer @v2
  Senaryo: V2 - Geçersiz fatura tipi - DISCOUNTED_INVOICE_TYPE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType    |
      | TYP-V2-INV-0001  | 1234567893    | 1000          | INVALID_TYPE   |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_INVOICE_TYPE' olmalı
    Ve hata mesajı 'Invoice type is invalid' içermeli
    Ve fatura yüklenmemiş olmalı

  @hash @buyer @v2
  Senaryo: V2 - Geçersiz fatura hash kodu - DISCOUNTED_INVOICE_HASH_CODE (E-Fatura)
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | hashCode |
      | HASH-V2-INV-001  | 1234567893    | 1000          | E_FATURA    |          |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_HASH_CODE' olmalı
    Ve hata mesajı 'Invalid hash code' içermeli
    Ve fatura yüklenmemiş olmalı

  @taxExclusive @buyer @v2
  Senaryo: V2 - Geçersiz KDV'siz tutar - DISCOUNTED_INVOICE_TAX_EXCLUSIVE_AMOUNT (E-Arşiv)
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | taxExclusiveAmount |
      | TXE-V2-INV-0001  | 1234567893    | 1000          | E_ARSIV     | 0                  |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_TAX_EXCLUSIVE_AMOUNT' olmalı
    Ve hata mesajı 'Invalid VAT-free amount' içermeli
    Ve fatura yüklenmemiş olmalı

  @invalid @buyer @v2
  Senaryo: V2 - Geçersiz VKN - INVALID_TAX_NUMBER
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo | supplierTaxNo | invoiceAmount | invoiceType |
      |           |               | -1            | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'INVALID_TAX_NUMBER' olmalı
    Ve hata mesajı 'Invalid Tax Id' içermeli
    Ve fatura yüklenmemiş olmalı

  @pending @manual @v2
  Senaryo: V2 - Fatura ıskontolanmış - DISCOUNTED_INVOICE
    Eğer ki aşağıdaki alanlarla fatura yüklenmeye çalışılırsa
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | DSC-V2-INV-0001  | 1234567893    | 1000          | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve hata kodu 'EXIST_INVOICE' olmalı
    Ve hata mesajı 'Invoice available in the system' içermeli
    Ve fatura yüklenmemiş olmalı
