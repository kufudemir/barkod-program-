<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\GlobalProduct;
use Illuminate\Http\JsonResponse;

class GetGlobalCatalogSuggestionController extends Controller
{
    public function __invoke(string $barcode): JsonResponse
    {
        $product = GlobalProduct::query()->find(trim($barcode));

        if ($product === null) {
            return response()->json([
                'message' => 'Global ürün bulunamadı',
            ], 404);
        }

        return response()->json([
            'barcode' => $product->barcode,
            'name' => $product->canonical_name,
            'groupName' => $product->group_name,
            'updatedAt' => $product->updated_at?->valueOf(),
        ]);
    }
}
