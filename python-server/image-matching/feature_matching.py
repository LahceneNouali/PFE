import sqlite3
import numpy as np
import cv2
from matplotlib import pyplot as plt
import io
import numpy as np

def convert_array(text):
    out = io.BytesIO(text)
    out.seek(0)
    return np.load(out)

# Converts TEXT to np.array when selecting
sqlite3.register_converter("ARRAY", convert_array)

conn = sqlite3.connect("base.db", detect_types=sqlite3.PARSE_DECLTYPES)
c = conn.cursor()

c.execute("SELECT * FROM images")
data = c.fetchall()

img1 = cv2.imread('dataset/test/IMG_20190923_171316.jpg',0)
scale_percent = 29 # percent of original size
width = int(img1.shape[1] * scale_percent / 100)
height = int(img1.shape[0] * scale_percent / 100)
dim = (width, height)
# resize image
img1 = cv2.resize(img1, dim, interpolation = cv2.INTER_AREA)

# Initiate SIFT, SURF and ORB detectors
sift = cv2.xfeatures2d.SIFT_create()
surf = cv2.xfeatures2d.SURF_create()
orb = cv2.ORB_create()

# find the keypoints and descriptors with SIFT or SURF or ORB
kp1, des1 = sift.detectAndCompute(img1, None)

# FLANN parameters
FLANN_INDEX_KDTREE = 1
index_params = dict(algorithm = FLANN_INDEX_KDTREE, trees = 5)
search_params = dict(checks=50)   # or pass empty dictionary
flann = cv2.FlannBasedMatcher(index_params,search_params)

best , i = 0 , 1
for row in data:
    matches = flann.knnMatch(des1,row[2],k=2)
    
    # Apply ratio test
    good = []
    for m,n in matches:
        if m.distance < 0.7*n.distance:
            good.append([m])
    print("Image{} ==> {}".format(i,len(good)))
	
    if len(good) > best:
        best = len(good)
        name = row[0]

    i += 1

#-- ------------------------- Drawing ----------------------------  
img2 = cv2.imread('dataset/images/'+name,0)
scale_percent = 29 # percent of original size
width = int(img2.shape[1] * scale_percent / 100)
height = int(img2.shape[0] * scale_percent / 100)
dim = (width, height)
# resize image
img2 = cv2.resize(img2, dim, interpolation = cv2.INTER_AREA)

kp2, des2 = sift.detectAndCompute(img2, None)
matches = flann.knnMatch(des1,des2,k=2)
# Need to draw only good matches, so create a mask
matchesMask = [[0,0] for i in range(len(matches))]
for i,(m,n) in enumerate(matches):
    if m.distance < 0.7*n.distance:
        matchesMask[i]=[1,0]
		
draw_params = dict(matchColor = (0,255,0),
                   singlePointColor = (255,0,0),
                   matchesMask = matchesMask,
                   flags = 0)

img3 = cv2.drawMatchesKnn(img1,kp1,img2,kp2,matches,None,**draw_params)
plt.imshow(img3,),plt.show()
#-- --------------------------------------------------------------

conn.close()
