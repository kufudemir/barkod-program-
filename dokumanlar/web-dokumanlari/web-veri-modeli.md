# Web Veri Modeli

Bu doküman barkod.space üzerinde çalışan mevcut ve planlanan veri modelini özetler.

## Mevcut Çekirdek Tablolar

### companies
- id
- name
- company_code
- status
- owner_mobile_user_id
- created_via
- created_at
- updated_at

### company_staff_roles
- id
- company_id
- mobile_user_id
- role (`manager`, `cashier`)
- status (`active`, `inactive`)
- created_by_mobile_user_id
- created_at
- updated_at

### devices
- id
- company_id
- device_uid
- device_name
- platform
- activation_token_hash
- is_active
- last_sync_at
- last_seen_at
- created_at
- updated_at

### device_company_histories
- id
- device_uid
- company_id
- first_seen_at
- last_seen_at
- activation_source

### global_products
- barcode
- canonical_name
- group_name
- last_source_company_id
- last_source_device_id
- last_synced_at
- created_at
- updated_at

### global_product_name_candidates
- id
- barcode
- candidate_name
- source_company_id
- source_device_id
- last_seen_at
- seen_count

### company_product_offers
- id
- company_id
- barcode
- sale_price_kurus
- cost_price_kurus
- group_name
- note
- is_active
- source_updated_at
- last_synced_at
- created_at
- updated_at

Not:
- Faz 13A ile mobil artimli katalog senkronu bu tablodaki `updated_at` alanini cursor olarak kullanir.

### sync_batches
- id
- company_id
- device_id
- batch_uuid
- received_event_count
- processed_event_count
- status
- error_summary
- created_at

### sync_event_dedups
- id
- device_id
- event_uuid
- processed_at

### mobile_users
- id
- name
- email
- password
- status
- premium_tier
- premium_source
- premium_activated_at
- premium_expires_at
- premium_license_mask
- consent_version
- consent_accepted_at
- created_at
- updated_at

### mobile_user_access_tokens
- id
- mobile_user_id
- token_hash
- device_uid
- device_name
- last_used_at
- expires_at

### mobile_user_password_resets
- id
- mobile_user_id
- email
- code_hash
- requested_at
- expires_at
- used_at

### system_settings
- key
- value

### web_sales
- id
- company_id
- branch_id
- register_id
- pos_session_id
- created_by_user_id
- created_by_mobile_user_id
- total_items
- total_amount_kurus
- total_cost_kurus
- profit_kurus
- completed_at
- created_at
- updated_at

### web_sale_items
- id
- web_sale_id
- barcode
- product_name_snapshot
- group_name_snapshot
- quantity
- unit_sale_price_kurus_snapshot
- unit_cost_price_kurus_snapshot
- line_total_kurus
- line_profit_kurus
- created_at
- updated_at

## V4 İçin Eklenecek Tablolar

Not:
- `feature_flags`, `license_packages`, `license_package_features`, `company_licenses`, `company_license_feature_overrides`, `license_requests`, `company_license_events` tabloları uygulanmıştır.
- `branches`, `registers`, `pos_sessions` tabloları uygulanmıştır.
- `sale_sessions`, `sale_session_items` tabloları uygulanmıştır.
- `receipt_profiles` tablosu uygulanmıştır.
- `feedback_reports`, `feedback_messages`, `feedback_attachments` tabloları uygulanmıştır.
- `company_staff_roles` tablosu uygulanmıştır.

### feature_flags
Paketler ve firma override yapısı için açılıp kapanabilen özellik kataloğu.
- id
- key
- title
- description
- scope (`mobile`, `web_pos`, `admin`, `shared`)
- is_core
- created_at
- updated_at

### license_packages
Teknik paket şablonları.
- id
- code (`FREE`, `SILVER`, `GOLD`)
- name
- description
- sort_order
- is_active
- created_at
- updated_at

### license_package_features
Paket bazlı açık / kapalı özellikler.
- id
- package_id
- feature_flag_id
- is_enabled
- created_at
- updated_at

### company_licenses
Firmaya atanmış aktif paket/lisans.
- id
- company_id
- package_id
- status (`active`, `suspended`, `expired`, `cancelled`)
- starts_at
- expires_at nullable
- assigned_by_admin_user_id
- source (`manual_bank_transfer`, `manual_admin`)
- note nullable
- created_at
- updated_at

### company_license_feature_overrides
Firma bazında paket dışı açma/kapatma.
- id
- company_license_id
- feature_flag_id
- is_enabled
- reason nullable
- created_at
- updated_at

### license_requests
Landing veya mobil üzerinden gelen lisans talepleri.
- id
- company_id nullable
- requested_by_mobile_user_id nullable
- requester_name
- requester_email
- requester_phone nullable
- requested_package_code (`SILVER`, `GOLD`)
- status (`pending_payment`, `payment_review`, `approved`, `rejected`, `cancelled`)
- bank_reference_note nullable
- admin_note nullable
- created_at
- updated_at

### company_license_events
Lisans geçmişi ve manuel işlem izi.
- id
- company_license_id
- event_type
- payload_json
- created_at

### branches
Firma altında şube yapısı.
- id
- company_id
- name
- code
- status
- created_at
- updated_at

### registers
Şube altındaki kasa / register.
- id
- branch_id
- name
- code
- status
- created_at
- updated_at

### pos_sessions
Aktif web POS oturumları.
- id
- register_id
- opened_by_mobile_user_id
- status (`active`, `closed`)
- opened_at
- closed_at
- last_activity_at

### sale_sessions
Web POS üzerindeki aktif sepet / açık fiş sekmeleri.
- id
- pos_session_id
- source_device_uid nullable
- source_label
- created_by_mobile_user_id nullable
- status (`active`, `held`, `completed`, `cancelled`)
- created_at
- updated_at

### sale_session_items
Sekme bazlı sepet satırları.
- id
- sale_session_id
- barcode
- product_name_snapshot
- group_name_snapshot
- quantity
- base_sale_price_kurus
- applied_sale_price_kurus
- cost_price_kurus
- pricing_mode (`list`, `custom`, `percent_discount`, `fixed_discount`)
- pricing_meta_json
- line_total_kurus
- line_profit_kurus
- created_at
- updated_at

### sale_payments
Satış ödeme satırları.
- id
- sale_id
- method (`cash`, `card`, `other`)
- amount_kurus
- created_at

### receipt_profiles
Firma bazlı fiş şablonları.
- id
- company_id
- branch_id nullable
- name
- paper_size (`58mm`, `80mm`, `a4`)
- header_json
- footer_json
- visible_fields_json
- field_order_json
- print_mode (`browser`, `pdf`)
- is_default
- created_at
- updated_at

### feedback_reports
Kullanıcı ticket üst kaydı.
- id
- type (`bug`, `feature_request`, `general`)
- source (`mobile`, `web_pos`)
- company_id nullable
- mobile_user_id nullable
- device_uid nullable
- app_version nullable
- web_url nullable
- title
- description
- status (`new`, `reviewing`, `answered`, `closed`)
- created_at
- updated_at

### feedback_messages
Ticket mesaj akışı.
- id
- feedback_report_id
- author_type (`user`, `admin`)
- author_id nullable
- message
- is_internal_note
- created_at

### feedback_attachments
Ticket ek dosyaları.
- id
- feedback_report_id
- feedback_message_id nullable
- file_path
- mime_type
- created_at

### company_staff_users
Sonraki faz için firma personel kullanıcıları.
- id
- company_id
- branch_id nullable
- name
- email
- password
- role (`owner`, `manager`, `cashier`)
- status
- created_at
- updated_at

## Paket ve Yetki Kuralı

Özellik erişimi şu sırayla çözülür:
1. `feature_flags.is_core = true` ise her zaman açık
2. firma lisans override varsa override uygulanır
3. yoksa firmanın aktif paketindeki feature kullanılır

## Geçiş Kuralı

Eski `FREE / PRO` sistemi V4'te şu şekilde yorumlanır:
- `FREE` aynı kalır
- `PRO` -> `SILVER`
- `GOLD` yeni web POS lisans katmanıdır
