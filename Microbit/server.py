import time
import argparse
import signal
import sys
import serial
import threading
import socket
import sqlite3

FILENAME = "values.txt"
LAST_VALUE = ""

# send serial message 
SERIALPORT = "/dev/ttyACM0"
BAUDRATE = 115200
IP = "127.0.0.1"
PORT = 10000
ser = serial.Serial()
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((IP, PORT))


def main():
        initUART()
        f= open(FILENAME,"a")
        print ('Press Ctrl-C to quit.')
        try:
            con = sqlite3.connect('dbiot.db')
            user_input = "ask"
            while ser.isOpen() :
                data_android, addr = sock.recvfrom(1024)

                # Gestion Android
                ser.write(data_android)
                if(data_android != NULL):
                    cursor = con.cursor()
                    sql = ''' UPDATE conf SET order = ? WHERE id = ? '''
                    if(data_android[1] == 'L'):
                        data_tuple = ("LT", 1)
                        cursor.execute(sql, data_tuple)
                    else:
                        data_tuple = ("TL", 1)
                        cursor.execute(sql, data_tuple)
                    con.commit()
                    cursor.close()
                    
                # Gestion UART
                if (ser.inWaiting() > 0): # if incoming bytes are waiting 
                    data_str = ser.read(ser.inWaiting())
                    stringtab = data_str.split(";")
                    temperature = stringtab[0]
                    light = stringtab[1]
                    currenttime = time.time()

                    cursor = con.cursor()
                    sql = ''' INSERT INTO data(temp, light, time) VALUES(?,?,?) '''
                    data_tuple = (temperature, light, currenttime)
                    cursor.execute(sql, data_tuple)
                    con.commit()
                    cursor.close()

                    f.write(data_str)
                    LAST_VALUE = data_str
                    print(data_str)

        except (KeyboardInterrupt, SystemExit):
                f.close()
                ser.close()
                con.close()
                exit()
        except sqlite3.Error as error:
                print("Erreur lors de la connexion a SQLite", error)()


def initUART():
        # ser = serial.Serial(SERIALPORT, BAUDRATE)
        ser.port=SERIALPORT
        ser.baudrate=BAUDRATE
        ser.bytesize = serial.EIGHTBITS #number of bits per bytes
        ser.parity = serial.PARITY_NONE #set parity check: no parity
        ser.stopbits = serial.STOPBITS_ONE #number of stop bits
        ser.timeout = None          #block read

        # ser.timeout = 0             #non-block read
        # ser.timeout = 2              #timeout block read
        ser.xonxoff = False     #disable software flow control
        ser.rtscts = False     #disable hardware (RTS/CTS) flow control
        ser.dsrdtr = False       #disable hardware (DSR/DTR) flow control
        #ser.writeTimeout = 0     #timeout for write
        print('Starting Up Serial Monitor')
        try:
            ser.open()
        except serial.SerialException:
            print("Serial {} port not available".format(SERIALPORT))
            exit()



def sendUARTMessage(msg):
    ser.write(msg)
    print("Message <" + msg + "> sent to micro-controller." )

# Main program logic follows:
if __name__ == '__main__':
        main()