#include "MicroBit.h"
#include "ssd1306.h"
#include "bme280.h"
#include "tsl256x.h"
#include "aes.hpp"
#include "crypt.hpp"
#include <string>

char order[3] = "TL";

uint8_t key[16] = { 0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c };

const string BLANK = "         ";
uint8_t xtemp = 1;
uint8_t xlux = 3;

MicroBit uBit;
MicroBitI2C i2c(I2C_SDA0,I2C_SCL0);
MicroBitPin P0(MICROBIT_ID_IO_P0, MICROBIT_PIN_P0, PIN_CAPABILITY_DIGITAL_OUT);

    
ssd1306 screen(&uBit, &i2c, &P0);
bme280 bme(&uBit,&i2c);
tsl256x tsl(&uBit, &i2c);

void onData(MicroBitEvent) {
    uBit.display.scroll("R");
    ManagedString managed_temp_string = uBit.radio.datagram.recv();
    string message = decrypt((char *) managed_temp_string.toCharArray(), key);

    //uBit.serial.printf("Received: %s\r\n", message.c_str());
    
    // Change position on screen according to the user choice
    if(message == "L;T"){
        xlux = 1;
        xtemp = 3;
    } else if(message == "T;L"){
        xlux = 3;
        xtemp = 1;
    }

    // Remove the ';' from the message
    //message.erase(remove(message.begin(), message.end(), ';'), message.end());
    //uBit.serial.printf("After erasing: %s\n", message.c_str());
    //strncpy(order, message.c_str(), 3);
}

// Convert a temperature in int to a string
string tempToString(int tmp) {
    return to_string(tmp/100) + "." + (tmp > 0 ? to_string(tmp%100): to_string((-tmp)%100));
}

void sendData(int tmp, int lux) {
    //uBit.serial.printf("senData\r\n");
    string msg;

    for(size_t i=0; i<strlen(order); i++){
        if (i != 0) {
            msg += ";";
        }
        switch(order[i]) {
            case 'T':
                msg += tempToString(tmp);
            break;
            case 'L':
                msg += to_string(lux);
            break;
        }
    }


    //uBit.display.scroll("S");

    //uBit.serial.printf("Cyphering: %s\r\n", msg.c_str());

    string cyphered_msg = encrypt((char *) msg.c_str(), key);
    uBit.radio.datagram.send((char*) cyphered_msg.c_str());
}

int main(){
    uBit.init();
    uBit.radio.enable();
    uBit.radio.setGroup(28);

    // Adding a listener to the radio
    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onData);

    uint32_t pressure = 0;
    int32_t temp = 0;
    uint16_t humidite = 0;
    long unsigned int lux = 0;

    while(true){
        bme.sensor_read(&pressure, &temp, &humidite);
        int tmp = bme.compensate_temperature(temp);
        
        string temperature_str = tempToString(tmp);
        string message_temperature = ("therm:" + temperature_str + " C" + BLANK);


        screen.display_line(xtemp, 0, message_temperature.c_str());
        tsl.sensor_read(NULL, NULL, &lux);

        const char* lux_line =  ("lux: " + to_string(lux) + BLANK).c_str();
        screen.display_line(xlux, 0, lux_line);
        screen.update_screen();

        sendData(tmp, lux);

        uBit.sleep(10000);
    }

    release_fiber();
}