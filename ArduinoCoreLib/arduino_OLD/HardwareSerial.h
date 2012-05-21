/*
  HardwareSerial.h - Hardware serial library for Wiring
  Copyright (c) 2006 Nicholas Zambetti.  All right reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

  Modified 28 September 2010 by Mark Sproul
  
  Modified 18 November 2011 by Slavian Petrov, based on suggestions from
    http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1265509976/3 and
    source code from ftp://wookey.org.uk/arduino/
*/

#ifndef HardwareSerial_h
#define HardwareSerial_h

#include <inttypes.h>

#include "Stream.h"

// Experimental interrupt driven output
#define SERIAL_INTERRUPT_OUTPUT
//#undef SERIAL_INTERRUPT_OUTPUT

// Find a generic name for the Uart data register empty interrupt vector
#if defined(SERIAL_INTERRUPT_OUTPUT)
# if defined(SIG_USART0_DATA)
#  define FIRST_UDRE_vect SIG_USART0_DATA
# endif

# if defined(USART0_UDRE_vect)
#  define FIRST_UDRE_vect USART0_UDRE_vect
# endif

# if defined(USART_UDRE_vect)
#  define FIRST_UDRE_vect USART_UDRE_vect
# endif

# if !defined(FIRST_UDRE_vect)
#  error Can not determine what the Data Register Empty vector is called for the first UART
# endif
#endif

struct rx_ring_buffer;
struct tx_ring_buffer;

class HardwareSerial : public Stream
{
  private:
    rx_ring_buffer *_rx_buffer;
#if defined (SERIAL_INTERRUPT_OUTPUT)  
    tx_ring_buffer *_tx_buffer;
#endif
    volatile uint8_t *_ubrrh;
    volatile uint8_t *_ubrrl;
    volatile uint8_t *_ucsra;
    volatile uint8_t *_ucsrb;
    volatile uint8_t *_udr;
    uint8_t _rxen;
    uint8_t _txen;
    uint8_t _rxcie;
    uint8_t _udre;
    uint8_t _u2x;
#if defined (SERIAL_INTERRUPT_OUTPUT)  
	uint8_t _udrie;
#endif
  public:
    HardwareSerial(rx_ring_buffer *rx_buffer,
#if defined (SERIAL_INTERRUPT_OUTPUT)  
      tx_ring_buffer *tx_buffer,
#endif
      volatile uint8_t *ubrrh, volatile uint8_t *ubrrl,
      volatile uint8_t *ucsra, volatile uint8_t *ucsrb,
      volatile uint8_t *udr,
      uint8_t rxen, uint8_t txen, uint8_t rxcie, uint8_t udre, uint8_t u2x
#if defined (SERIAL_INTERRUPT_OUTPUT)  
      , uint8_t udrie
#endif
      );
    void begin(long);
    void end();
    virtual int available(void);
    virtual int peek(void);
    virtual int read(void);
    virtual void flush(void);
    virtual void write(uint8_t);
    using Print::write; // pull in write(str) and write(buf, size) from Print
};

#if defined(UBRRH) || defined(UBRR0H)
  extern HardwareSerial Serial;
#elif defined(USBCON)
  #include "usb_api.h"
#endif
#if defined(UBRR1H)
  extern HardwareSerial Serial1;
#endif
#if defined(UBRR2H)
  extern HardwareSerial Serial2;
#endif
#if defined(UBRR3H)
  extern HardwareSerial Serial3;
#endif

#endif
