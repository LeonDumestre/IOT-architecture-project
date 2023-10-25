#include "MicroBit.h"
#include "ssd1306.h"
#include "bme280.h"
#include <string>
#include "tsl256x.h"


MicroBit uBit;
MicroBitI2C i2c(I2C_SDA0,I2C_SCL0);
MicroBitPin P0(MICROBIT_ID_IO_P0, MICROBIT_PIN_P0, PIN_CAPABILITY_DIGITAL_OUT);

    
ssd1306 screen(&uBit, &i2c, &P0);
bme280 bme(&uBit,&i2c);
tsl256x tsl(&uBit, &i2c);

int main()
{
    // Initialise the micro:bit runtime.
    uBit.init();

    uint32_t pressure = 0;
    int32_t temp = 0;
    uint16_t humidite = 0;


    while(true){



        int temp_integree = uBit.thermometer.getTemperature();


        screen.display_line(1,0,"Steven Arnaud");
        screen.display_line(2,0,"Loic Perrin");

        bme.sensor_read(&pressure, &temp, &humidite);
        int tmp = bme.compensate_temperature(temp);
        
        const char* str = ("thermo:" + to_string(tmp/100) + "." + (tmp > 0 ? to_string(tmp%100): to_string((-tmp)%100))+" C").c_str();
        screen.display_line(3,0,str);

        const char*  str2 = ("integre: " + to_string(temp_integree) + " C").c_str();
        screen.display_line(4, 0, str2);

        long unsigned int lux = 0;
        tsl.sensor_read(NULL, NULL, &lux);
        const char* str3 =  ("lux: " + to_string(lux)).c_str();
        screen.display_line(5, 0, str3);
        screen.update_screen();
        uBit.sleep(1000);
    }

    release_fiber();

}
