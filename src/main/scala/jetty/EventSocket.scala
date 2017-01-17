package jetty

import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}

class EventSocket extends WebSocketAdapter {

  var thread: Thread = _

  override def onWebSocketConnect(sess: Session) = {
    super.onWebSocketConnect(sess)

    thread = new Thread() {
      override def run() = {
        while (true) {
          getRemote.sendString("asdf")
          try {
            this.wait(100)
          } catch {
            case _: Exception => ()
          }
        }
      }
    }

    thread.run()
  }

  override def onWebSocketText(message: String) = {
    super.onWebSocketText(message)
    getRemote.sendString("foo")
  }

  override def onWebSocketClose(statusCode: Int, reason: String) = {
    super.onWebSocketClose(statusCode, reason)
    thread.interrupt()
  }

  override def onWebSocketError(cause: Throwable) = {
    super.onWebSocketError(cause)
    thread.interrupt()
  }
}