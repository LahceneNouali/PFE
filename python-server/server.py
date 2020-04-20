import sqlite3
import numpy as np
import cv2
import socket
import io

def convert_array(text):
    out = io.BytesIO(text)
    out.seek(0)
    return np.load(out)

# Converts TEXT to np.array when selecting
sqlite3.register_converter("ARRAY", convert_array)

# address = ('', 1234)
# s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# s.bind(address)
# s.listen(1000)

conn = sqlite3.connect("data/base.db", detect_types=sqlite3.PARSE_DECLTYPES)
c = conn.cursor()

c.execute("SELECT * FROM images")
data = c.fetchall()

img1 = cv2.imread('test.jpg',0)          # queryImage

# Initiate SIFT or SURF detector
sift = cv2.xfeatures2d.SIFT_create()
# surf = cv2.xfeatures2d.SURF_create()

# find the keypoints and descriptors with SIFT or SURF
kp, des = sift.detectAndCompute(img1,None)
# kp_, des_ = surf.detectAndCompute(img1, None)

best , i = 0 , 1
for row in data:
    # BFMatcher with default params
    bf = cv2.BFMatcher()
    matches = bf.knnMatch(des,row[2], k=2)

    # Apply ratio test
    good = []
    for m,n in matches:
        if m.distance < 0.75*n.distance:
            good.append([m])

    print("Image{} has {} good maches".format(i,len(good)))

    if len(good) > best:
        best = len(good)
        name = row[0]          # The name of the image in DB that has the most number of similar features

    i += 1
    
img2 = cv2.imread('data/images/'+name,cv2.IMREAD_UNCHANGED)          # trainImage
cv2.imshow(name, img2)
cv2.waitKey(10000)

conn.close()