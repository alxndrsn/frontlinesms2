package frontlinesms2

import frontlinesms2.enums.MessageStatus

class PollControllerIntegrationSpec extends grails.plugin.spock.IntegrationSpec {
	def controller
	def setup() {
		controller = new PollController()
	}

	def "can save new poll"() {
		setup:
			controller.params.title = "poll"
			controller.params.choiceA = "yes"
			controller.params.choiceB = "no"
			controller.params.choiceC = "maybe"
			controller.params.autoReplyText = "automatic reply text"
		when:
			controller.save()
			def poll = Poll.findByTitle("poll")
		then:
			poll
			poll.autoReplyText == "automatic reply text"
			(poll.responses*.value).containsAll(['yes', 'no', 'maybe'])
	}

}