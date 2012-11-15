package frontlinesms2.domain

import frontlinesms2.*
import spock.lang.*
import org.apache.camel.*

class WebconnectionISpec extends grails.plugin.spock.IntegrationSpec {
	def webCService = Mock(WebconnectionService)
	def camelContext = Mock(CamelContext)

	def 'incoming message matching keyword should trigger http message sending'() {
		given:
			def k = new Keyword(value:'FORWARD')
			def webconnection = new GenericWebconnection(name:"Sync", url:"www.frontlinesms.com/sync",httpMethod:Webconnection.HttpMethod.GET).addToKeywords(k).save(failOnError:true)
			webconnection.webconnectionService = webCService
			webconnection.save(failOnError:true)
			def incomingMessage = Fmessage.build(text:"FORWARD ME", src:'123123')
		when:
			webconnection.processKeyword(incomingMessage, k)
		then:
			1 * webCService.send(incomingMessage)
	}

	def 'incoming message should match if keyword is blank and exactmatch == false'() {
		given:
			def k = new Keyword(value:'')
			def webconnection = new GenericWebconnection(name:"Sync", url:"www.frontlinesms.com/sync",httpMethod:Webconnection.HttpMethod.GET).addToKeywords(k).save(failOnError:true)
			webconnection.webconnectionService = webCService
			webconnection.save(failOnError:true)
			def incomingMessage = Fmessage.build(text:"FORWARD ME", src:'123123')
			webconnection.addToMessages(incomingMessage).save(failOnError:true)
		when:
			webconnection.processKeyword(incomingMessage, k)
		then:
			1 * webCService.send(incomingMessage)
	}

	def 'testRoute should shut down route after execution'() {
		given:
			def params = [url: "www.frontlinesms.com/sync", httpMethod:Webconnection.HttpMethod.GET]
			params.'param-name' = ['username', 'password'] as String[]
			params.'param-value' = ['bob','secret'] as String[]
		when:
			Webconnection.testRoute(params)
		then:
			1 * camelContext.addRouteDefinitions(_)
			camelContext.routes.findAll { it.id == "test-webconnection-null"} //This might be modified based on implementation
			1 * camelContext.stopRoute(_)
			webCService.testRoute() == false
			!Fmessage.count()
	}
}
