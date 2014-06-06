#ifndef TemperatureControl_h
#define TemperatureControl_h

#include "DigitalIO.h"
#include "SoftwarePWM.h"

#define InvalidTemperature -300

////////// TemperatureSensor

class TemperatureSensor {
public:
	virtual int getTemperatureCelsius() = 0;
};

////////// TemperatureSensor_TC1047

class TemperatureSensor_TC1047 : public TemperatureSensor {
	uint8_t analogArduinoPin;
public:
	TemperatureSensor_TC1047(const uint8_t analogArduinoPin) : analogArduinoPin(analogArduinoPin) { }

	virtual int getTemperatureCelsius() {
		long result = analogRead(analogArduinoPin);
		result *= 1024;
		return result / 500L;
	//	return analogRead(analogArduinoPin) * (1024.0 / (5.0 * 100.0));
	}
};

////////// TemperatureControl

class TemperatureControl {
private:
	TemperatureSensor *temperatureSensor;
	SoftwarePWM spwm;

	int targetTemperatureCelsius;
	int fullPowerHeatBand;
public:
	/**
	 * Initializes the class.
	 */
	void initialize(TemperatureSensor *temperatureSensor, DigitalOutputPin *heaterPin) {
		this->temperatureSensor = temperatureSensor;
		this->targetTemperatureCelsius = InvalidTemperature;
		fullPowerHeatBand = 4;
		spwm.initialize(heaterPin, 1);
		spwm.setValue(0);
	}

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void) {
		int curTemp = temperatureSensor->getTemperatureCelsius();
		int error = targetTemperatureCelsius - curTemp;
		if ((targetTemperatureCelsius <= InvalidTemperature) || (error < 0)) {
			spwm.setValue(0);
		} else if (error > fullPowerHeatBand) {
			spwm.setValue(255);
		} else {
			int pulseWidth = constrain((error / fullPowerHeatBand) * 255, 0, 255);
			spwm.setValue(pulseWidth);
		}
		spwm.update();
	}

	void setTargetTemperature(int targetTemperatureCelsius) {
		this->targetTemperatureCelsius = targetTemperatureCelsius;
	}

	inline int getTargetTemperature() {
		return targetTemperatureCelsius;
	}

	inline int getTemperature() {
		return temperatureSensor->getTemperatureCelsius();
	}

	void stop(void) {
		targetTemperatureCelsius = 0;
	}
};

#endif
