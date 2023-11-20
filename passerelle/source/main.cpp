#include "MicroBit.h"
#include <string>
#include "crypt.hpp"

MicroBit uBit;

const uint8_t key[16] = { 0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c };
string transit_radio;
string transit_serial;

void onRadioReceive(MicroBitEvent) {
    transit_radio = ((ManagedString)uBit.radio.datagram.recv()).toCharArray();
    transit_radio = decrypt((char*)transit_radio.c_str(), (uint8_t*)key);

    uBit.serial.printf("%s", transit_radio.c_str());
}


void transit(const char* input){
    transit_radio = encrypt((char*)input, (uint8_t*)key);
    uBit.radio.datagram.send(transit_radio.c_str());
}

void onSerialReceive(MicroBitEvent) {
    transit_serial = uBit.serial.readUntil("~").toCharArray();
    transit_serial.erase(remove(transit_serial.begin(), transit_serial.end(), '~'), transit_serial.end());
    transit(transit_serial.c_str());
}


int main() {
    uBit.init();

    uBit.display.scroll("INIT");


    uBit.serial.setRxBufferSize(uBit.serial.getRxBufferSize());
    uBit.serial.eventOn("~");

    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onRadioReceive);
    uBit.messageBus.listen(MICROBIT_ID_SERIAL, MICROBIT_SERIAL_EVT_DELIM_MATCH, onSerialReceive);
    
    uBit.radio.enable();
    uBit.radio.setGroup(28);

    while(true) {
        uBit.sleep(1000);
    }

    release_fiber();
}