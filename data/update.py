import sqlite3
import numpy as np
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

path = 'images'
fileNames = os.listdir(path)

# Initiate SIFT or SURF detector
sift = cv2.xfeatures2d.SIFT_create()
# surf = cv2.xfeatures2d.SURF_create()

for name in fileNames:
	img = cv2.imread(path+"/"+name)
    # find the keypoints and descriptors with SIFT or SURF
	kp, des = sift.detectAndCompute(img,None)
	# kp_, des_ = surf.detectAndCompute(img, None)
	
	c.execute("INSERT OR REPLACE INTO images (name, label, descriptors) VALUES (?, ?, ?)", (name, name, des))
	print(name+" was added!")

conn.commit()
conn.close()
