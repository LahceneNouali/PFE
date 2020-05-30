import socket
import cv2

# The @IP of the server must be fixed on 192.168.1.78
# https://stackoverflow.com/questions/54522699/image-transferring-from-android-to-python-socket-issue

address = ('', 8080)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(address)
s.listen(1000)

running = True
while running:
    conn1, addr = s.accept()
	
    with open('tst.jpg', 'wb') as img:
        while True:
            data = conn1.recv(1024)
            if not data:
                break
            img.write(data)

    img = cv2.imread('tst.jpg', cv2.IMREAD_UNCHANGED)
 
    #print('Original Dimensions : ',img.shape)
 
    #scale_percent = 29 # percent of original size
    #width = int(img.shape[1] * scale_percent / 100)
    #height = int(img.shape[0] * scale_percent / 100)
    #dim = (width, height)
    # resize image
    #img = cv2.resize(img, dim, interpolation = cv2.INTER_AREA)
 
    #print('Resized Dimensions : ',resized.shape)
 
    cv2.imshow("Received image", img)
    cv2.waitKey(1)
	
	# Heavy work here
	# ...
	
	conn1.sendall('welcom')
    conn1.close()
