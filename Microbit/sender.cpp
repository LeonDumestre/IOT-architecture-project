#include "MicroBit.h"
#include <string>

MicroBit uBit;

int main() {
    // Initialise the micro:bit runtime.
    uBit.init();
    uBit.radio.enable();
    uBit.radio.setGroup(28);

    while(true){
        String message = "message";
        uBit.radio.datagram.send(message.c_str());
        uBit.sleep(100);
    }

    release_fiber();

}