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
	TemperatureSensor_TC1047(const uint8_t analogArduinoPin);

	virtual int getTemperatureCelsius();
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
	void initialize(TemperatureSensor *temperatureSensor, DigitalOutputPin *heaterPin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);

	void setTargetTemperature(int targetTemperatureCelsius);

	inline int getTargetTemperature() {
		return targetTemperatureCelsius;
	}

	inline int getTemperature() {
		return temperatureSensor->getTemperatureCelsius();
	}

	void stop(void);
};

#endif
