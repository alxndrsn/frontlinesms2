package frontlinesms2.status

import frontlinesms2.*

import serial.mock.MockSerial

class StatusDetectModemSpec extends grails.plugin.geb.GebSpec {
	def deviceDetectionService

	def 'DETECT MODEMS button should be visible on STATUS tab'() {
		when:
			to PageStatus
		then:
			at PageStatus
			detectModems.displayed
	}
	
	def 'DETECTED DEVICES section should be visible on STATUS tab'() {
		when:
			to PageStatus
		then:
			detectedDevicesSection.displayed
			detectionTitle.equalsIgnoreCase('Detected devices')
			noDevicesDetectedNotification.displayed
			noDevicesDetectedNotification.text() == 'No devices have been detected yet.'
	}

	def 'DETECTED DEVICES list should appear when a device has been detected'() {
		setup:
			MockSerial.reset()
			MockSerial.setIdentifier('COM1', new serial.mock.PermanentlyOwnedCommPortIdentifier('COM1', 'a naughty windows application'))
		when:
			to PageStatus
		then:
			noDevicesDetectedNotification.displayed
		when:
			detectModems.click()
		then:
			waitFor { !noDevicesDetectedNotification.displayed }
		when:
			go 'status/resetDetection'
		then:
			noDevicesDetectedNotification.displayed
		cleanup:
			MockSerial.reset()
	}
}