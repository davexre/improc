#include "TemperatureControl.h"

////////// TemperatureSensor_TC1047

TemperatureSensor_TC1047::TemperatureSensor_TC1047(const uint8_t analogArduinoPin) {
	this->analogArduinoPin = analogArduinoPin;
}

float TemperatureSensor_TC1047::getTemperatureCelsius() {
	return analogRead(analogArduinoPin) * (1024.0 / (5.0 * 100.0));
}

////////// TemperatureControl

void TemperatureControl::initialize(TemperatureSensor *temperatureSensor, DigitalOutputPin *heaterPin) {
	this->temperatureSensor = temperatureSensor;
	this->targetTemperatureCelsius = 0;
	fullPowerHeatBand = 4;
	spwm.initialize(heaterPin, 1);
	spwm.setValue(0);
}

void TemperatureControl::update() {
	int curTemp = (int) temperatureSensor->getTemperature();
	int error = targetTemperatureCelsius - curTemp;
	if ((targetTemperatureCelsius <= 0) || (error < 0)) {
		spwm.setValue(0);
	} else if (error > fullPowerHeatBand) {
		spwm.setValue(255);
	} else {
		int pulseWidth = constrain((error / fullPowerHeatBand) * 255, 0, 255);
		spwm.setValue(pulseWidth);
	}
	spwm.update();
}

void TemperatureControl::setTargetTemperature(int targetTemperatureCelsius) {
	this->targetTemperatureCelsius = targetTemperatureCelsius;
}
