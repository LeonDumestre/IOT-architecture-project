#include "MicroBit.h"
#include <string>

MicroBit uBit;

int main() {
    // Initialise the micro:bit runtime.
    uBit.init();
    uBit.radio.enable();
    uBit.radio.setGroup(28);
    while(true){
        int temp_integree = uBit.thermometer.getTemperature();
        uBit.radio.datagram.send((to_string(temp_integree) + ".C").c_str());
        uBit.sleep(100);
    }

    release_fiber();

}