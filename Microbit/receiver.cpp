#include "MicroBit.h"

MicroBit uBit;

void onData(MicroBitEvent) {
    ManagedString s = uBit.radio.datagram.recv();

    uBit.display.print(s);
}

int main() {
    // Initialise the micro:bit runtime.
    uBit.init();

    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onData);
    uBit.radio.enable();
    uBit.radio.setGroup(28);

    while(1){
        uBit.sleep(1000);
    }
}