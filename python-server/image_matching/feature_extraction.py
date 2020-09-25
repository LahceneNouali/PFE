# import the necessary packages
from imutils import paths
import numpy as np
import sqlite3
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

# get current directory 
path = os.getcwd() 
# database path
feature = os.path.join(path, 'feature.db')

conn = sqlite3.connect(feature, detect_types=sqlite3.PARSE_DECLTYPES)
c = conn.cursor()

# Create a table with three attributes
c.execute("CREATE TABLE IF NOT EXISTS images (path TEXT, descriptors ARRAY, label TEXT, PRIMARY KEY(path))")
# Clear the table if it already exists and contains data
c.execute("DELETE FROM images")

# Initiate SIFT and SURF detectors
sift = cv2.xfeatures2d.SIFT_create()
surf = cv2.xfeatures2d.SURF_create()

# parent directory
parent = os.path.dirname(os.getcwd())
# train_set path
train_set = os.path.join(parent, 'dataset', 'train')
# grab the list of images in our dataset directory
imagePaths = list(paths.list_images(train_set))

# loop over the image paths
count = 1
for imagePath in imagePaths:
	print('{}/{}'.format(count, len(imagePaths))); count += 1
	img = cv2.imread(imagePath,0)
        
    # find the keypoints and descriptors with SIFT or SURF
	kp, des = surf.detectAndCompute(img,None)
	c.execute("INSERT OR REPLACE INTO images (path, descriptors, label) VALUES (?, ?, ?)",
	(imagePath, des, imagePath.split(os.path.sep)[-2]))

conn.commit()
conn.close()
