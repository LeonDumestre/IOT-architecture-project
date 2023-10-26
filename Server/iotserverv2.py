# Program to control passerelle between Android application
# and micro-controller through USB tty
import time
import argparse
import signal
import sys
import socket
import socketserver
import serial
import threading
import sqlite3

HOST           = "0.0.0.0"
UDP_PORT       = 10000
MICRO_COMMANDS = ["TL" , "LT"]
FILENAME        = "values.txt"
DATABASE        = "dbiot.db"
LAST_VALUE      = ""

class ThreadedUDPRequestHandler(socketserver.BaseRequestHandler):

    def handle(self):
        data = self.request[0].strip()
        socket = self.request[1]
        current_thread = threading.current_thread()
        print("{}: client: {}, wrote: {}".format(current_thread.name, self.client_address, data))
        if data != "":
                        if data in MICRO_COMMANDS: # Send message through UART
                                con = sqlite3.connect('dbiot.db')
                                cursor = con.cursor()
                                sql = ''' UPDATE conf SET order = ? WHERE id = ? '''
                                data_tuple = ("LT", 1)
                                cursor.execute(sql, data_tuple)
                                sendUARTMessage(data)
                                con.commit()
                                cursor.close() 
                                con.close()
                                
                        elif data == "getValues()": # Sent last value received from micro-controller
                                con = sqlite3.connect('dbiot.db')
                                cursor = con.cursor()
                                sql = ''' SELECT * FROM data WHERE time = (SELECT MAX(time) FROM data)'''
                                cursor.execute(sql)
                                rows = cursor.fetchall()
                                # TODO: Send rows to Android application    
                        else:
                                print("Unknown message: ",data)

class ThreadedUDPServer(socketserver.ThreadingMixIn, socketserver.UDPServer):
    pass


# send serial message 
SERIALPORT = ""
BAUDRATE = 115200
ser = serial.Serial()

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
        print ('Starting Up Serial Monitor')
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
        initUART()
        f= open(FILENAME,"a")
        print ('Press Ctrl-C to quit.')

        server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)

        server_thread = threading.Thread(target=server.serve_forever)
        server_thread.daemon = True

        try:
                server_thread.start()
                print("Server started at {} port {}".format(HOST, UDP_PORT))
                while ser.isOpen() : 
                        if (ser.inWaiting() > 0): # if incoming bytes are waiting 
                                data_str = ser.read(ser.inWaiting()) 
                                f.write(data_str)
                                LAST_VALUE = data_str
                                print(data_str)
        except (KeyboardInterrupt, SystemExit):
                server.shutdown()
                server.server_close()
                f.close()
                ser.close()
                exit()