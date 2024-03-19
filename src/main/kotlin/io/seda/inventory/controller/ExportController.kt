package io.seda.inventory.controller

import io.seda.inventory.data.ItemRepository
import io.seda.inventory.services.ProductService
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.toList
import org.dhatim.fastexcel.Workbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream


@RestController
@RequestMapping("/export")
class ExportController {
    @Autowired lateinit var itemRepository: ItemRepository;

    @GetMapping
    suspend fun export(): ResponseEntity<ByteArray> {
        val stream = ByteArrayOutputStream();
        val wb = Workbook(stream, "MyApplication", "1.0")


        val ws1 = wb.newWorksheet("Overview")
        val ws2 = wb.newWorksheet("Detailed")


        ws2.value(0, 0, "ID")
        ws2.value(0, 1, "카탈로그 ID")
        ws2.value(0, 2, "위치 ID")
        ws2.value(0, 3, "이름")
        ws2.value(0, 4, "위치")
        ws2.value(0, 5, "갯수")
        ws1.value(0, 0, "카탈로그 ID")
        ws1.value(0, 1, "이름")
        ws1.value(0, 2, "총 갯수")


        val list = itemRepository.findAllByAnyhow().toList()
        var map = hashMapOf<Long, Long>() // product id to count
        var product = hashMapOf<Long, ProductService.SimpleProduct>()
        var i = 1;
        for (item in list) {
            ws2.value(i, 0, item.id)
            ws2.value(i, 1, item.productId)
            ws2.value(i, 2, item.locationId)
            ws2.value(i, 3, item.product?.name)
            ws2.value(i, 4, item.location?.name)
            ws2.value(i, 5, item.count)
            i++
            val res = map[item.productId] ?: 0
            map[item.productId] = res + item.count
            if (item.product != null)
                product[item.productId] = item.product
        }
        i = 1
        for (mutableEntry in product) {
            ws1.value(i ,0, mutableEntry.value.id)
            ws1.value(i ,1, mutableEntry.value.name)
            ws1.value(i ,2, map[mutableEntry.key] ?: 0)
            i++
        }

        wb.finish()

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(stream.size().toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("output.xlsx")
                    .build().toString())
            .body(stream.toByteArray());
    }
}