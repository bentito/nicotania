
from lightblue import *

bd_addr = "00:06:66:46:B6:A7"

port = 1

sock=socket(RFCOMM)
sock.connect((bd_addr, port))

sock.send("hello!!")

sock.close()
