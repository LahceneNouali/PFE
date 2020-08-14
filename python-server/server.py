print("Loading...")
from deep_learning.test_network import Classification
from image_matching.feature_matching import Matching
from matplotlib import pyplot as plt
import socket
import cv2

address = ('', 8080)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(address)
s.listen(1000)

c = Classification()
# m = Matching()
# -- ----------------------------------------------------------------
i = cv2.imread('dataset/test/1/3L027240.jpg', cv2.IMREAD_UNCHANGED)
classlabel = c.classify(i)
# filename = m.match(i, '1')
# -- ----------------------------------------------------------------
print("Waiting...")
while False:
    print("")
    conn, addr = s.accept()
	
    with open('input.jpg', 'wb') as img:
        while True:
            data = conn.recv(1024)
            # print("Receiving data")
            if not data:
                break
            img.write(data)
			
    # -- ----------------------------------------------------------------			
    i = cv2.imread('input.jpg', cv2.IMREAD_UNCHANGED)	
    classlabel = c.classify(i)
    filepath = m.match(i, classlabel)
    # -- ----------------------------------------------------------------
    if filepath != "":
	    o = cv2.imread(filepath, cv2.IMREAD_UNCHANGED)
	    cv2.imwrite("output.jpg", o)
	    filename = filepath.split(os.path.sep)[-1][:2]
    else:
	    filename = ""
	
    response = classlabel+", "+filename
    conn.sendall(response.encode())
    conn.close()

