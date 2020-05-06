package com.example.demo.app

import com.example.demo.view.MainView
import tornadofx.*

class MyApp : App(MainView::class, Styles::class) {

    val api: Rest by inject()

    init {
        api.baseURI = "https://www.mixamo.com/api/v1"

        api.engine.requestInterceptor = { request ->
            val token = "eyJ4NXUiOiJpbXNfbmExLWtleS0xLmNlciIsImFsZyI6IlJTMjU2In0.eyJpZCI6IjE1ODg3NzMxMDEzOTFfOThlNGMwOGEtMGM0My00ODMxLTkwYTAtZTkwZGY3NDIwYzVlX3VlMSIsImNsaWVudF9pZCI6Im1peGFtbzEiLCJ1c2VyX2lkIjoiQTY1QjQ1MjY1QzAzMDY0QzBBNDk1Q0NGQEFkb2JlSUQiLCJ0eXBlIjoiYWNjZXNzX3Rva2VuIiwiYXMiOiJpbXMtbmExIiwiZmciOiJVTkZKN1laUlhMTzU1NzYyQzZRTFFQUUEzND09PT09PSIsInNpZCI6IjE1ODg3MTUwNTkyMDFfNTMzYzk4NzEtNDFlZi00ZGNhLWE2OTItNWUzZGRlMjhmZjc1X3VlMSIsInJ0aWQiOiIxNTg4NzczMTAxMzkxXzYxNDdiNzI3LWFlNGUtNDcwYy1hMjg1LTRkNjQ4NzcxNjY0NV91ZTEiLCJvYyI6InJlbmdhKm5hMXIqMTcxZWE0MTkzOWUqSDdOR0FZWTQ5RDFYWDZONVdER0dQNlhaRVciLCJydGVhIjoiMTU4OTk4MjcwMTM5MSIsIm1vaSI6ImM5YTc0ZmU0IiwiYyI6IkpoMDVTL3p6Z0dFRXJhZWRFRFZNeWc9PSIsImV4cGlyZXNfaW4iOiI4NjQwMDAwMCIsInNjb3BlIjoiY3JlYXRpdmVfc2RrLG9wZW5pZCxzYW8ubWl4YW1vIiwiY3JlYXRlZF9hdCI6IjE1ODg3NzMxMDEzOTEifQ.XWhxWI5JiOZJPn2m4kEkxJDwYRJ1s5xrItEPqKeIIX8Mj5G4KJtJH9rFnBXu2wU2XuI2eQ5WayXiGama7M2ycHwu37O8xZYeUD_P5THhuUlw-gbk2DIKFDBQWqMWBDeBm5qjyvGRdN1QMk8XGt6uqSvjPFLIsMPN9bwnaVV8pEimx27yiXDtnybM0wDM277XfraQnzHfY_-Gn33QdhuV047oqf6vNDw9jMDDks9_3XyyoFDsS66QqKpq-X76eSbOooRUfb_Z2LJYxrvQiPUBAUf8zZq98zhdqTRJsOCURPLgOePy5BL_r8cXQTJwRrw0ejRv-7UIwBdJ-m8ipylEmg"
            request.addHeader("Authorization", "Bearer $token")
            request.addHeader("X-Api-Key", "mixamo2 ")
        }
    }
}

fun main(args: Array<String>) {
    launch<MyApp>(args)
}
