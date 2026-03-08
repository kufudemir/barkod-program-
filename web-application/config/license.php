<?php

return [
    'bank_transfer' => [
        'account_name' => env('LICENSE_BANK_ACCOUNT_NAME', 'KolayKasa Teknoloji'),
        'iban' => env('LICENSE_BANK_IBAN', 'TR00 0000 0000 0000 0000 0000 00'),
        'bank_name' => env('LICENSE_BANK_NAME', 'Örnek Banka'),
        'description_hint' => env('LICENSE_BANK_DESCRIPTION_HINT', 'Firma adı + talep no'),
    ],
];
