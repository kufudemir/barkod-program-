# Room Entity Planı

## Ana Tablolar
- `products`
- `sales`
- `sale_items`
- `app_settings`
- `sync_outbox`

## Ürün
Alanlar:
- barcode
- name
- groupName
- salePriceKurus
- costPriceKurus
- stockQty
- minStockQty
- note
- createdAt
- updatedAt
- isActive

## Satış
Alanlar:
- id
- totalAmountKurus
- totalProfitKurus
- createdAt

## Satış Kalemi
Alanlar:
- id
- saleId
- barcode
- productNameSnapshot
- quantity
- salePriceKurus
- costPriceKurus

## Ayarlar
Alanlar:
- key
- value

## Senkron Kuyruğu
Alanlar:
- outboxId
- eventUuid
- eventType
- payloadJson
- createdAt
- attemptCount
- lastAttemptAt
- status
- errorMessage
