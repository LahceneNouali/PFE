import sqlite3
import numpy as np
import progressbar
import cv2
import io
import os

def adapt_array(arr):
    out = io.BytesIO()
    np.save(out, arr)
    out.seek(0)
    return sqlite3.Binary(out.read())

# Converts np.array to TEXT when inserting
sqlite3.register_adapter(np.ndarray, adapt_array)

conn = sqlite3.connect("base.db", detect_types=sqlite3.PARSE_DECLTYPES)
c = conn.cursor()

# Create a table with three attributes
c.execute("CREATE TABLE IF NOT EXISTS images (name TEXT, label TEXT, descriptors ARRAY, PRIMARY KEY(name))")
# Clear the table if it already exists and contains data
c.execute("DELETE FROM images")

# Initiate SIFT, SURF and ORB detectors
sift = cv2.xfeatures2d.SIFT_create()
surf = cv2.xfeatures2d.SURF_create()
orb = cv2.xfeatures2d.SURF_create()

path = 'dataset'
fileNames = os.listdir(path)
pbar = progressbar.ProgressBar()

for name in pbar(fileNames):
	# print(name)
	img = cv2.imread(path+"/"+name,0)
	scale_percent = 29 # percent of original size
	width = int(img.shape[1] * scale_percent / 100)
	height = int(img.shape[0] * scale_percent / 100)
	dim = (width, height)
	# resize image
	img = cv2.resize(img, dim, interpolation = cv2.INTER_AREA)
        
    # find the keypoints and descriptors with SIFT or SURF or ORB
	kp, des = sift.detectAndCompute(img,None)
	c.execute("INSERT OR REPLACE INTO images (name, label, descriptors) VALUES (?, ?, ?)", (name, name, des))

conn.commit()
conn.close()
