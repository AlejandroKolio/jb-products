package com.ashakhov.app.jbproducts.controller

import com.ashakhov.app.jbproducts.service.ReleasedProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.streams.toList

@Controller
@RequestMapping("/")
class DefaultController(val productService: ReleasedProductService) {

    @GetMapping
    fun getProductsInfo(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "1") pageNumber: Int,
        @RequestParam(value = "size", required = false, defaultValue = "10") size: Int, model: Model
    ): String {
        val builds = productService.getAll().stream().flatMap { it.releasedBuilds.stream() }.toList()
        model.addAttribute("builds", builds)
        model.addAttribute("products", productService.getAll())
        return "index"
    }
}