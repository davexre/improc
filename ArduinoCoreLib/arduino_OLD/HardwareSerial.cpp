/*
  HardwareSerial.cpp - Hardware serial library for Wiring
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
  
  Modified 23 November 2006 by David A. Mellis
  Modified 28 September 2010 by Mark Sproul

  Modified 18 November 2011 by Slavian Petrov, based on suggestions from
    http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1265509976/3 and
    source code from ftp://wookey.org.uk/arduino/
*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include "wiring.h"
#include "wiring_private.h"

// this next line disables the entire HardwareSerial.cpp, 
// this is so I can support Attiny series and any other chip without a uart
#if defined(UBRRH) || defined(UBRR0H) || defined(UBRR1H) || defined(UBRR2H) || defined(UBRR3H)

#include "HardwareSerial.h"

// Define constants and variables for buffering incoming serial data.  We're
// using a ring buffer (I think), in which rx_buffer_head is the index of the
// location to which to write the next incoming character and rx_buffer_tail
// is the index of the location from which to read.
#if (RAMEND < 1000)
  #define RX_BUFFER_SIZE 32
  #define TX_BUFFER_SIZE 32
#else
  #define RX_BUFFER_SIZE 64
  #define TX_BUFFER_SIZE 64
#endif

#define BufferIndex uint8_t

struct rx_ring_buffer
{
  unsigned char buffer[RX_BUFFER_SIZE];
  BufferIndex head;
  BufferIndex tail;
};

struct tx_ring_buffer
{
  unsigned char buffer[TX_BUFFER_SIZE];
  BufferIndex head;
  volatile BufferIndex tail;
};

#if defined(UBRRH) || defined(UBRR0H)
  rx_ring_buffer rx_buffer  =  { { 0 }, 0, 0 };
  #if defined (SERIAL_INTERRUPT_OUTPUT)
     tx_ring_buffer tx_buffer  =  { { 0 }, 0, 0 };
  #endif
#endif
#if defined(UBRR1H)
  rx_ring_buffer rx_buffer1  =  { { 0 }, 0, 0 };
  #if defined (SERIAL_INTERRUPT_OUTPUT)
     tx_ring_buffer tx_buffer1  =  { { 0 }, 0, 0 };
  #endif
#endif
#if defined(UBRR2H)
  rx_ring_buffer rx_buffer2  =  { { 0 }, 0, 0 };
  #if defined (SERIAL_INTERRUPT_OUTPUT)
     tx_ring_buffer tx_buffer2  =  { { 0 }, 0, 0 };
  #endif
#endif
#if defined(UBRR3H)
  rx_ring_buffer rx_buffer3  =  { { 0 }, 0, 0 };
  #if defined (SERIAL_INTERRUPT_OUTPUT)
     tx_ring_buffer tx_buffer3  =  { { 0 }, 0, 0 };
  #endif
#endif

inline void store_char(unsigned char c, rx_ring_buffer *rx_buffer)
{
	BufferIndex i = rx_buffer->head + 1;
	if (i >= RX_BUFFER_SIZE)
		i = 0;

  // if we should be storing the received character into the location
  // just before the tail (meaning that the head would advance to the
  // current location of the tail), we're about to overflow the buffer
  // and so we don't write the character or advance the head.
  if (i != rx_buffer->tail) {
    rx_buffer->buffer[rx_buffer->head] = c;
    rx_buffer->head = i;
  }
}

#if defined(USART_RX_vect)
  SIGNAL(USART_RX_vect)
  {
  #if defined(UDR0)
    unsigned char c  =  UDR0;
  #elif defined(UDR)
    unsigned char c  =  UDR;  //  atmega8535
  #else
    #error UDR not defined
  #endif
    store_char(c, &rx_buffer);
  }
#elif defined(SIG_USART0_RECV) && defined(UDR0)
  SIGNAL(SIG_USART0_RECV)
  {
    unsigned char c  =  UDR0;
    store_char(c, &rx_buffer);
  }
#elif defined(SIG_UART0_RECV) && defined(UDR0)
  SIGNAL(SIG_UART0_RECV)
  {
    unsigned char c  =  UDR0;
    store_char(c, &rx_buffer);
  }
//#elif defined(SIG_USART_RECV)
#elif defined(USART0_RX_vect)
  // fixed by Mark Sproul this is on the 644/644p
  //SIGNAL(SIG_USART_RECV)
  SIGNAL(USART0_RX_vect)
  {
  #if defined(UDR0)
    unsigned char c  =  UDR0;
  #elif defined(UDR)
    unsigned char c  =  UDR;  //  atmega8, atmega32
  #else
    #error UDR not defined
  #endif
    store_char(c, &rx_buffer);
  }
#elif defined(SIG_UART_RECV)
  // this is for atmega8
  SIGNAL(SIG_UART_RECV)
  {
  #if defined(UDR0)
    unsigned char c  =  UDR0;  //  atmega645
  #elif defined(UDR)
    unsigned char c  =  UDR;  //  atmega8
  #endif
    store_char(c, &rx_buffer);
  }
#elif defined(USBCON)
  #warning No interrupt handler for usart 0
  #warning Serial(0) is on USB interface
#else
  #error No interrupt handler for usart 0
#endif

#if defined (SERIAL_INTERRUPT_OUTPUT)
  ISR(FIRST_UDRE_vect)
  {
    if (tx_buffer.head == tx_buffer.tail) {
  	// Buffer empty, so disable interrupts
      cbi(UCSR0B, UDRIE0);
    }
    else {
      // There is more data in the output buffer. Send the next byte
      unsigned char c = tx_buffer.buffer[tx_buffer.tail];
//      tx_buffer.tail = (tx_buffer.tail + 1) % TX_BUFFER_SIZE;
      BufferIndex t = tx_buffer.tail + 1;
      tx_buffer.tail = t >= TX_BUFFER_SIZE ? 0 : t;

      UDR0 = c;
    }
  }

  #if defined(SIG_USART1_DATA)
    ISR(SIG_USART1_DATA)
    {
      if (tx_buffer1.head == tx_buffer1.tail) {
    	// Buffer empty, so disable interrupts
        cbi(UCSR1B, UDRIE1);
      }
      else {
        // There is more data in the output buffer. Send the next byte
        unsigned char c = tx_buffer1.buffer[tx_buffer1.tail];
        BufferIndex t = tx_buffer1.tail + 1;
        tx_buffer1.tail = t >= TX_BUFFER_SIZE ? 0 : t;
        UDR1 = c;
      }
    }
  #endif
  
  #if defined(SIG_USART2_DATA)
    ISR(SIG_USART2_DATA)
    {
      if (tx_buffer2.head == tx_buffer2.tail) {
    	// Buffer empty, so disable interrupts
        cbi(UCSR2B, UDRIE2);
      }
      else {
        // There is more data in the output buffer. Send the next byte
        unsigned char c = tx_buffer2.buffer[tx_buffer2.tail];
        BufferIndex t = tx_buffer2.tail + 1;
        tx_buffer2.tail = t >= TX_BUFFER_SIZE ? 0 : t;
    	
        UDR2 = c;
      }
    }
  #endif

  #if defined(SIG_USART3_DATA)
    ISR(SIG_USART3_DATA)
    {
      if (tx_buffer3.head == tx_buffer3.tail) {
    	// Buffer empty, so disable interrupts
        cbi(UCSR3B, UDRIE3);
      }
      else {
        // There is more data in the output buffer. Send the next byte
        unsigned char c = tx_buffer3.buffer[tx_buffer3.tail];
        BufferIndex t = tx_buffer3.tail + 1;
        tx_buffer3.tail = t >= TX_BUFFER_SIZE ? 0 : t;
    	
        UDR3 = c;
      }
    }
  #endif
#endif


//#if defined(SIG_USART1_RECV)
#if defined(USART1_RX_vect)
  //SIGNAL(SIG_USART1_RECV)
  SIGNAL(USART1_RX_vect)
  {
    unsigned char c = UDR1;
    store_char(c, &rx_buffer1);
  }
#elif defined(SIG_USART1_RECV)
  #error SIG_USART1_RECV
#endif

#if defined(USART2_RX_vect) && defined(UDR2)
  SIGNAL(USART2_RX_vect)
  {
    unsigned char c = UDR2;
    store_char(c, &rx_buffer2);
  }
#elif defined(SIG_USART2_RECV)
  #error SIG_USART2_RECV
#endif

#if defined(USART3_RX_vect) && defined(UDR3)
  SIGNAL(USART3_RX_vect)
  {
    unsigned char c = UDR3;
    store_char(c, &rx_buffer3);
  }
#elif defined(SIG_USART3_RECV)
  #error SIG_USART3_RECV
#endif



// Constructors ////////////////////////////////////////////////////////////////

HardwareSerial::HardwareSerial(rx_ring_buffer *rx_buffer,
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
  )
{
  _rx_buffer = rx_buffer;
  _ubrrh = ubrrh;
  _ubrrl = ubrrl;
  _ucsra = ucsra;
  _ucsrb = ucsrb;
  _udr = udr;
  _rxen = rxen;
  _txen = txen;
  _rxcie = rxcie;
  _udre = udre;
  _u2x = u2x;
#if defined (SERIAL_INTERRUPT_OUTPUT)
  _tx_buffer = tx_buffer;
  _udrie = udrie;
#endif
}

// Public Methods //////////////////////////////////////////////////////////////

void HardwareSerial::begin(long baud)
{
  uint16_t baud_setting;
  bool use_u2x = true;

#if F_CPU == 16000000UL
  // hardcoded exception for compatibility with the bootloader shipped
  // with the Duemilanove and previous boards and the firmware on the 8U2
  // on the Uno and Mega 2560.
  if (baud == 57600) {
    use_u2x = false;
  }
#endif
  
  if (use_u2x) {
    *_ucsra = 1 << _u2x;
    baud_setting = (F_CPU / 4 / baud - 1) / 2;
  } else {
    *_ucsra = 0;
    baud_setting = (F_CPU / 8 / baud - 1) / 2;
  }

  // assign the baud_setting, a.k.a. ubbr (USART Baud Rate Register)
  *_ubrrh = baud_setting >> 8;
  *_ubrrl = baud_setting;

  sbi(*_ucsrb, _rxen);
  sbi(*_ucsrb, _txen);
  sbi(*_ucsrb, _rxcie);
#if defined (SERIAL_INTERRUPT_OUTPUT)
  cbi(*_ucsrb, _udrie);
#endif
}

void HardwareSerial::end()
{
  cbi(*_ucsrb, _rxen);
  cbi(*_ucsrb, _txen);
  cbi(*_ucsrb, _rxcie);  
#if defined (SERIAL_INTERRUPT_OUTPUT)
  cbi(*_ucsrb, _udrie);
#endif
}

int HardwareSerial::available(void)
{
//  return (unsigned int)(RX_BUFFER_SIZE + _rx_buffer->head - _rx_buffer->tail) % RX_BUFFER_SIZE;
	return _rx_buffer->head != _rx_buffer->tail;
}

int HardwareSerial::peek(void)
{
  if (_rx_buffer->head == _rx_buffer->tail) {
    return -1;
  } else {
    return _rx_buffer->buffer[_rx_buffer->tail];
  }
}

int HardwareSerial::read(void)
{
  // if the head isn't ahead of the tail, we don't have any characters
  if (_rx_buffer->head == _rx_buffer->tail) {
    return -1;
  } else {
    unsigned char c = _rx_buffer->buffer[_rx_buffer->tail];
    BufferIndex t = _rx_buffer->tail + 1;
    _rx_buffer->tail = t >= RX_BUFFER_SIZE ? 0 : t;
    return c;
  }
}

void HardwareSerial::flush()
{
  // don't reverse this or there may be problems if the RX interrupt
  // occurs after reading the value of rx_buffer_head but before writing
  // the value to rx_buffer_tail; the previous value of rx_buffer_head
  // may be written to rx_buffer_tail, making it appear as if the buffer
  // don't reverse this or there may be problems if the RX interrupt
  // occurs after reading the value of rx_buffer_head but before writing
  // the value to rx_buffer_tail; the previous value of rx_buffer_head
  // may be written to rx_buffer_tail, making it appear as if the buffer
  // were full, not empty.
  _rx_buffer->head = _rx_buffer->tail;
}

#if defined(SERIAL_INTERRUPT_OUTPUT)
void HardwareSerial::write(uint8_t c)
{
//  bool empty = (_tx_buffer->head == _tx_buffer->tail);
//  int i = (_tx_buffer->head + 1) % TX_BUFFER_SIZE;
    BufferIndex i = _tx_buffer->head + 1;
	if (i >= TX_BUFFER_SIZE)
		i = 0;

  // If the output buffer is full, there's nothing for it other than to
  // wait for the interrupt handler to empty it a bit
  while (i == _tx_buffer->tail)
	  ;

  _tx_buffer->buffer[_tx_buffer->head] = c;
  _tx_buffer->head = i;

//  if (empty) {
    // The buffer was empty, so enable interrupt on
    // USART Data Register empty. The interrupt handler will take it from there
    sbi(*_ucsrb, _udrie);
//  }
}
#else
void HardwareSerial::write(uint8_t c)
{
  while (!((*_ucsra) & (1 << _udre)))
    ;

  *_udr = c;
}
#endif

// Preinstantiate Objects //////////////////////////////////////////////////////

#if defined(UBRRH) && defined(UBRRL)
  #if defined(SERIAL_INTERRUPT_OUTPUT)
    HardwareSerial Serial(&rx_buffer, &tx_buffer, &UBRRH, &UBRRL, &UCSRA, &UCSRB, &UDR, RXEN, TXEN, RXCIE, UDRE, U2X, UDRIE);
  #else
    HardwareSerial Serial(&rx_buffer, &UBRRH, &UBRRL, &UCSRA, &UCSRB, &UDR, RXEN, TXEN, RXCIE, UDRE, U2X);
  #endif
#elif defined(UBRR0H) && defined(UBRR0L)
  #if defined(SERIAL_INTERRUPT_OUTPUT)
    HardwareSerial Serial(&rx_buffer, &tx_buffer, &UBRR0H, &UBRR0L, &UCSR0A, &UCSR0B, &UDR0, RXEN0, TXEN0, RXCIE0, UDRE0, U2X0, UDRIE0);
  #else
    HardwareSerial Serial(&rx_buffer, &UBRR0H, &UBRR0L, &UCSR0A, &UCSR0B, &UDR0, RXEN0, TXEN0, RXCIE0, UDRE0, U2X0);
  #endif
#elif defined(USBCON)
  #warning no serial port defined  (port 0)
#else
  #error no serial port defined  (port 0)
#endif

#if defined(UBRR1H)
  #if defined(SERIAL_INTERRUPT_OUTPUT)
    HardwareSerial Serial1(&rx_buffer1, &tx_buffer1, &UBRR1H, &UBRR1L, &UCSR1A, &UCSR1B, &UDR1, RXEN1, TXEN1, RXCIE1, UDRE1, U2X1, UDRIE01);
  #else
    HardwareSerial Serial1(&rx_buffer1, &UBRR1H, &UBRR1L, &UCSR1A, &UCSR1B, &UDR1, RXEN1, TXEN1, RXCIE1, UDRE1, U2X1);
  #endif
#endif
#if defined(UBRR2H)
  #if defined(SERIAL_INTERRUPT_OUTPUT)
    HardwareSerial Serial2(&rx_buffer2, &tx_buffer2, &UBRR2H, &UBRR2L, &UCSR2A, &UCSR2B, &UDR2, RXEN2, TXEN2, RXCIE2, UDRE2, U2X2, UDRIE2);
  #else
    HardwareSerial Serial2(&rx_buffer2, &UBRR2H, &UBRR2L, &UCSR2A, &UCSR2B, &UDR2, RXEN2, TXEN2, RXCIE2, UDRE2, U2X2);
  #endif
#endif
#if defined(UBRR3H)
  #if defined(SERIAL_INTERRUPT_OUTPUT)
    HardwareSerial Serial3(&rx_buffer3, &tx_buffer3, &UBRR3H, &UBRR3L, &UCSR3A, &UCSR3B, &UDR3, RXEN3, TXEN3, RXCIE3, UDRE3, U2X3, UDRIE3);
  #else
    HardwareSerial Serial3(&rx_buffer3, &UBRR3H, &UBRR3L, &UCSR3A, &UCSR3B, &UDR3, RXEN3, TXEN3, RXCIE3, UDRE3, U2X3);
  #endif
#endif

#endif // whole file
