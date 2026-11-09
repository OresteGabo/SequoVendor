package dev.orestegabo.sequovendor
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import dev.orestegabo.sequovendor.core.auth.AuthApiClient
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*
class TestAccountLoginClientTest {
 @Test fun sendsCredentialsForTheExpectedAppFamilyAndDevice()=runBlocking {
  val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0);var payload:JsonObject?=null
  server.createContext("/api/auth/test-accounts/session") {x->
   payload=Json.parseToJsonElement(x.requestBody.readBytes().decodeToString()).jsonObject
   val bytes="""{"accessToken":"fixture-access","refreshToken":"fixture-refresh","expiresIn":3600}""".toByteArray()
   x.responseHeaders.set("Content-Type","application/json");x.sendResponseHeaders(200,bytes.size.toLong());x.responseBody.use{it.write(bytes)}
  };server.start()
  val origin="http://127.0.0.1:${server.address.port}"
  val api=AuthApiClient(baseUrl=origin,httpClient=createSequoVendorHttpClient(origin),deviceIdProvider={"test-device"})
  try {
   assertEquals("fixture-access",api.loginWithTestAccount("fixture-user","fixture-password").accessToken)
   assertEquals("VENDOR",payload!!["appFamily"]!!.jsonPrimitive.content)
   assertEquals("fixture-password",payload!!["password"]!!.jsonPrimitive.content)
   assertEquals("SEQUO_HUB",payload!!["device"]!!.jsonObject["appSource"]!!.jsonPrimitive.content)
   assertEquals("test-device",payload!!["device"]!!.jsonObject["deviceId"]!!.jsonPrimitive.content)
   assertEquals(setOf("username","password","appFamily","device"),payload!!.keys)
  } finally {api.close();server.stop(0)}
 }
}
