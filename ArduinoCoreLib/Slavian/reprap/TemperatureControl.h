#ifndef TemperatureControl_h
#define TemperatureControl_h

#include "DigitalIO.h"
#include "SoftwarePWM.h"

////////// TemperatureSensor

class TemperatureSensor {
public:
	virtual float getTemperature() = 0;
};

////////// TemperatureSensor_TC1047

class TemperatureSensor_TC1047 : public TemperatureSensor {
	uint8_t analogArduinoPin;
public:
	TemperatureSensor_TC1047(const uint8_t analogArduinoPin);

	virtual float getTemperatureCelsius();
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
};

#endif
