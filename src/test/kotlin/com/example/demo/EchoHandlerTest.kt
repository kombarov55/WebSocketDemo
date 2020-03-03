package com.example.demo

import org.springframework.boot.test.context.SpringBootTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo
import org.springframework.boot.runApplication
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler

import java.util.concurrent.ArrayBlockingQueue


@SpringBootTest
class EchoHandlerTest: FunSpec({
    val uri = "ws://localhost:8080/echo"
    val rq = "hello world"


    beforeSpec {
        runApplication<DemoApplication>()
    }


    test("Requested echo line should be equal to response echo line") {
        val rs = doSyncWebsocketRequest(uri, rq)
        rs shouldBeEqualComparingTo rq
    }

    test("request echo line should not be equal to response echo line") {
        val rs = doSyncWebsocketRequest(uri, rq + " SHIT")
        rq shouldNotBeEqualComparingTo rs
    }
})

class TestHandler (
        private val onConnect: (WebSocketSession) -> Unit,
        private val onReceivingResult: (String) -> Unit
) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        onConnect.invoke(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        onReceivingResult.invoke(message.payload)
    }
}

fun doSyncWebsocketRequest(uri: String, msg: String): String {
    val q = ArrayBlockingQueue<String>(1)
    StandardWebSocketClient().doHandshake(TestHandler({ it.sendMessage(TextMessage(msg)) }, { q.put(it) }), uri)
    return q.take()
}