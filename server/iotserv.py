import time
import socket
import socketserver
import serial
import threading
import sqlite3
import re

HOST = "192.168.1.23"
UDP_PORT = 10000
MICRO_COMMANDS = ["T;L~", "L;T~"]
DATABASE = "dbiot.db"
LAST_VALS = ""
REGEX_DATA = r"^\d+\.\d+\;\d+~$"

# send serial message
SERIALPORT = "/dev/ttyACM0"
BAUDRATE = 115200
ser = serial.Serial()


mutex = threading.Lock()


class ThreadedUDPRequestHandler(socketserver.BaseRequestHandler):

    def handle(self):
        data = self.request[0].strip()
        data = str(data).split("'")[1]
        socket = self.request[1]
        current_thread = threading.current_thread()
        print("{}: client: {}, wrote: {}".format(current_thread.name, self.client_address, data))
        if data != "":
            if data == "getValues()":  # Sent last value received from micro-controller
                try:
                    con = sqlite3.connect('dbiot.db')
                    cursor = con.cursor()
                    # select every data and only keep the first one (most recent)
                    sql = ''' SELECT d.* FROM data d ORDER BY time DESC '''
                    cursor.execute(sql)
                    rows = cursor.fetchall()
                    if (len(rows) <= 0):
                        # send empty string if there is an error
                        sendAndroidMessage(str(-1), self.client_address)
                    else:
                        # send the first result
                        sendAndroidMessage(str(rows[0]), self.client_address)
                    cursor.close()
                    con.close()
                except Exception as e:
                    print("Error while reading from database: {}".format(e))
            else:  # Send message through UART
                try:
                    print("Trying to process message: " + data)
                    sendUARTMessage(data)

                    # Open connection to db and and update the config of thermo, lux, etc.
                    con = sqlite3.connect('dbiot.db')
                    cursor = con.cursor()
                    sql = ''' UPDATE conf SET order = ? WHERE id = ? '''
                    strvar = data.split(";")
                    fullstr = strvar[0] + strvar[1]
                    data_tuple = (str(fullstr), str(1))
                    cursor.execute(sql, data_tuple)
                    con.commit()
                    cursor.close()
                    con.close()
                except Exception as e:
                    print("Error while writing to database: {}".format(e))


class ThreadedUDPServer(socketserver.ThreadingMixIn, socketserver.UDPServer):
    pass


def initUART():
    # ser = serial.Serial(SERIALPORT, BAUDRATE)
    ser.port = SERIALPORT
    ser.baudrate = BAUDRATE
    ser.bytesize = serial.EIGHTBITS  # number of bits per bytes
    ser.parity = serial.PARITY_NONE  # set parity check: no parity
    ser.stopbits = serial.STOPBITS_ONE  # number of stop bits
    ser.timeout = None  # block read

    # ser.timeout = 0             #non-block read
    # ser.timeout = 2              #timeout block read
    ser.xonxoff = False  # disable software flow control
    ser.rtscts = False  # disable hardware (RTS/CTS) flow control
    ser.dsrdtr = False  # disable hardware (DSR/DTR) flow control
    # ser.writeTimeout = 0     #timeout for write
    print('Starting Up Serial Monitor')
    try:
        ser.open()
    except serial.SerialException:
        print("Serial {} port not available".format(SERIALPORT))
        exit()


def sendUARTMessage(msg):
    # lock the serial to not erase the infos
    mutex.acquire()
    ser.write(str.encode(msg))
    time.sleep(1) # ensure that the microbit has some time to read the buffer
    mutex.release()
    print("Message <" + msg + "> sent to micro-controller.")


def sendAndroidMessage(msg, addr):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.sendto(msg.encode(), addr)


# Main program logic follows:
if __name__ == '__main__':
    initUART()
    print('Press Ctrl-C to quit.')

    server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)

    server_thread = threading.Thread(target=server.serve_forever)
    server_thread.daemon = True

    try:
        server_thread.start()
        print("Server started at {} port {}".format(HOST, UDP_PORT))
        while ser.isOpen():
            try:
                # lock the serial because we need it not to flush our input
                mutex.acquire()
                data_str = ser.read_until(b'~')
                ser.flush()
                mutex.release()
                data_str = str(data_str).split("'")[1]

                # if the serial input is not as expected, we ignore it
                if re.match(REGEX_DATA, data_str) == False:
                    pass

                stringtab = data_str.replace("~", "").split(";")

                temperature = float(stringtab[0])
                light =  float(stringtab[1])
                currenttime = time.time()

                print("temperature : " + stringtab[0] + " light : " + stringtab[1])
                
                # open SGBD connection and insert data
                con = sqlite3.connect('dbiot.db')
                cursor = con.cursor()
                sql = ''' INSERT INTO data(temp, light, time) VALUES(?,?,?) '''
                data_tuple = (temperature, light, currenttime)
                cursor.execute(sql, data_tuple)
                con.commit()
                cursor.close()
                con.close()

            except Exception as e:
                print("Error while reading from serial port: {}".format(e))

    except (KeyboardInterrupt, SystemExit):
        server.shutdown()
        server.server_close()
        ser.close()
        exit()
