package frontlinesms2.message

import frontlinesms2.*
import java.util.regex.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class QuickMessageFSpec extends grails.plugin.geb.GebSpec {
	def "quick message link opens the popup to send messages"() {
		when:
			launchQuickMessageDialog()
		then:
			at QuickMessageDialog
	}

	def "should select the next tab on click of next"() {
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
			addressField.value("+919544426000")
			addAddressButton.click()
		then:
			waitFor { selectRecipientsTab.displayed }
		when:
			toConfirmTab()
		then:
			waitFor { confirmTab.displayed }
	}

	def "should not send message when no recipients are selected"() {
		when:
			launchQuickMessageDialog()
			$("#tabs a", text: "Confirm").click()
		then:
			waitFor { confirmTab.displayed }
		when:
			doneButton.click()
		then:
			waitFor { confirmTab.displayed }
		when:
			$(".confirm-tab").click()
		then:
			waitFor { confirmTab.displayed }
	}

	def "should select the previous tab on click of back"() {
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
			addressField.value("+919544426000")
			addAddressButton.click()
			toConfirmTab()
		then:
			waitFor { confirmTab.displayed }
		when:
			$("#prevPage").click()
		then:
			waitFor { selectRecipientsTab.displayed }

	}

	def "should add the manually entered contacts to the list "() {
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
			addressField.value("+919544426000")
			addAddressButton.click()
		then:
			waitFor { $('div#contacts div')[0].find('input', type:'checkbox').value() == "+919544426000" }
			$("#recipient-count").text() == "1"
	}

	def "should send the message to the selected recipients"() {
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
			addressField.value("+919544426000")
			addAddressButton.click()
		then:
			waitFor { $('div#contacts div')[0].find('input', type:'checkbox').value() == "+919544426000" }
		when:
			toConfirmTab()
			doneButton.click()
		then:
			waitFor { messagesQueuedNotification.displayed }
		when:
			$("#confirmation").click()
			$("a", text: "Inbox").click()
		then:
			waitFor{ title == "Inbox" }
			$("a", text: "Pending").hasClass("send-failed")
		when:
			$("a", text: "Pending").click()
		then:
			waitFor{ title == "Pending" }
			$("#message-list tbody tr").size() == 1
			$("#message-list tbody tr")[0].hasClass("send-failed")
	}

	def "should select members belonging to the selected group"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
		then:
			$("input[name=groups]").displayed
		when:
			$("input[name=groups]").value("group1")
		then:
			waitFor { $("#recipient-count").text() == "2" }
	}

	def "should deselect all member recipients when a group is un checked"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
		then:
			$("input[name=groups]").displayed
		when:
			$("input[name=groups]").value("group1")
		then:
			waitFor { $("#recipient-count").text() == "2" }
		when:
			$("input[value=group1]").click()
		then:
			waitFor { $("#recipient-count").text() == "0" }
	}

	def "should not allow to proceed if the recipients are not selected in the quick message screen"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
		then:
			!$(".error-panel").displayed
		when:
			nextPageButton.click()
		then:
			waitFor { $(".error-panel").displayed }
	}

	def "selected group should get unchecked when a member drops off"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
			$("input[value='group1']").click()
			$("input[value='group2']").click()
		then:
			$("#recipient-count").text() == "2"
		when:
			$("input[value='12345678']").click()
		then:
			!$("input[value='group1']").checked
			!$("input[value='group2']").checked
			$("#recipient-count").text() == "1"
	}

	def "should not deselect common members across groups when one of the group is unchecked"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
			toSelectRecipientsTab()
			$("input[value='group1']").click()
			$("input[value='group2']").click()
		then:
			$("#recipient-count").text() == "2"
		when:
			$("input[value='group1']").click()
		then:
			!$("input[value='group1']").checked
			$("input[value='group2']").checked
			$("#recipient-count").text() == "2"

	}

	def "should launch announcement screen from create new activity link" () { // FIXME why is this test here??
		when:
			to PageMessageInbox
			$("a", text:"Create new activity").click()
		then:
			waitFor { $("#activity-list").displayed }
		when:
			$("input", class: "announcement").click()
			$("#choose").click()
		then:
			waitFor { $("#ui-dialog-title-modalBox").text() == "New announcement" }
	}
	
	def "should show the character count of each message"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
		then:
			waitFor { characterCount.text() == "0 characters (1 SMS message)" }
		when:
			$("#messageText").value("h")
		then:
			waitFor { characterCount.text() == "1 characters (1 SMS message)" }
		when:
			$("#messageText").value('a' * 120)
		then:
			waitFor { characterCount.text() == "120 characters (1 SMS message)" }
		when:
			def longText = '0123abc[]@' * 16
			$("#messageText").value(longText)
		then:
			waitFor { characterCount.text() == "160 characters (1 SMS message)" }
		when:
			$("#messageText") << 'a'
		then:
			waitFor { characterCount.text() == "161 characters (2 SMS messages)" }
	}
	
	def "should show the total number of message to be sent to recipients"() {
		setup:
			createData()
		when:
			launchQuickMessageDialog()
			def longText = 'w' * 161
			$("#messageText").value(longText)
		then:
			waitFor { characterCount.text() == "161 characters (2 SMS messages)" }
		when:
			toSelectRecipientsTab()
			$("input[value='group1']").click()
			$("input[value='group2']").click()
			nextPageButton.click()
		then:
			messagesCount.text() == "4"
	}

	private def createData() {
		def group = new Group(name: "group1").save(flush: true)
		def group2 = new Group(name: "group2").save(flush: true)
		def alice = new Contact(name: "alice", primaryMobile: "12345678").save(flush: true)
		def bob = new Contact(name: "bob", primaryMobile: "567812445").save(flush: true)
		group.addToMembers(alice)
		group2.addToMembers(alice)
		group.addToMembers(bob)
		group2.addToMembers(bob)
		group.save(flush: true)
		group2.save(flush: true)
	}
	
	def launchQuickMessageDialog() {
		to PageMessageInbox
		$("a", text:"Quick message").click()
		waitFor { at QuickMessageDialog }
	}
	
	def toSelectRecipientsTab() {
		$('a', text:'Select Recipients').click()
		waitFor { selectRecipientsTab.displayed }
	}
	
	def toConfirmTab() {
		$('a', text:'Confirm').click()
		waitFor { confirmTab.displayed }
	}
}

class QuickMessageDialog extends geb.Page {
	static at = {
		$("#ui-dialog-title-modalBox").text() == 'Quick Message'
	}
	static content = {
		selectRecipientsTab { $('div#tabs-2') }
		confirmTab { $('div#tabs-3') }
		messagesQueuedNotification { $("div#tabs-4.quick-message-summary") }
		
		addressField { $('#address') }
		addAddressButton { $('.add-address') }
		
		doneButton { $("#done") }
		nextPageButton { $("#nextPage") }
		characterCount { $("#message-stats")}
		messagesCount { $("#messages-count")}
	}
}

class SentMessagesPage extends geb.Page {
	static url = 'message/sent'
}


