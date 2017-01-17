import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory

import jetty.EventSocket

object Server {

  def main(args: Array[String]): Unit = {
    val server = new Server()

    var connector = new ServerConnector(server)
    connector.setPort(8080)
    server.addConnector(connector)

    var context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    server.setHandler(context)

    var holderEvents = new ServletHolder("ws-events", new WebSocketServlet {
      override def configure(factory: WebSocketServletFactory): Unit = factory.register(classOf[EventSocket])
    })

    context.addServlet(holderEvents, "/events/*")

    try {
      server.start()
      server.dump(System.err)
      server.join()
    } catch {
      case t: Exception =>
        t.printStackTrace(System.err);
    }
  }
}
