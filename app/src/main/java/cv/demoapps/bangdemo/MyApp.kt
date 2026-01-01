package cv.demoapps.bangdemo


import cv.cbglib.CustomApplication
import cv.cbglib.services.JsonAssetService
import cv.demoapps.bangdemo.data.CardDetail
import cv.demoapps.bangdemo.data.SymbolDetail

class MyApp : CustomApplication() {
    val cardDetailsService: JsonAssetService<CardDetail> by lazy {
        JsonAssetService<CardDetail>(
            this,
            fileName = "CardDetails.json",
            serializer = CardDetail.serializer(),
            keySelector = { it.id }
        )
    }

    val symbolDetailsService: JsonAssetService<SymbolDetail> by lazy {
        JsonAssetService<SymbolDetail>(
            this,
            fileName = "SymbolDetails.json",
            serializer = SymbolDetail.serializer(),
            keySelector = { it.id }
        )
    }
}