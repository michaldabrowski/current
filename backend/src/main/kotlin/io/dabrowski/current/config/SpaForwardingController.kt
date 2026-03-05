package io.dabrowski.current.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SpaForwardingController : ErrorController {
    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): Any {
        val status = request.getAttribute("jakarta.servlet.error.status_code") as? Int
        if (status == HttpStatus.NOT_FOUND.value()) {
            val uri =
                request.getAttribute("jakarta.servlet.error.request_uri") as? String ?: ""
            if (!uri.startsWith("/api/") && !uri.startsWith("/actuator/")) {
                return "forward:/index.html"
            }
        }
        return ResponseEntity.status(status ?: 500).build<Void>()
    }
}
