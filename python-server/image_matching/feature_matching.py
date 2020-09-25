# import the necessary packages
import sqlite3
import numpy as np
import cv2
from matplotlib import pyplot as plt
import io
import os
import numpy as np

class Matching:
	def __init__(self):
		# Converts TEXT to np.array when selecting
		sqlite3.register_converter("ARRAY", self.convert_array)
		# Connecting to database
		self.conn = sqlite3.connect("image_matching/feature.db", detect_types=sqlite3.PARSE_DECLTYPES)
		self.c = self.conn.cursor()

	def convert_array(self, text):
		out = io.BytesIO(text)
		out.seek(0)
		return np.load(out)
	
	def match(self, image, category):
		self.c.execute("SELECT * FROM images WHERE label=?" , (category,))
		data = self.c.fetchall()
		
		# Converting the image to gray scale
		image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

		# Initiate SIFT and SURF detectors
		sift = cv2.xfeatures2d.SIFT_create()
		surf = cv2.xfeatures2d.SURF_create()

		# find the keypoints and descriptors with SIFT or SURF or ORB
		kp1, des1 = surf.detectAndCompute(image, None)

		# FLANN parameters
		FLANN_INDEX_KDTREE = 1
		index_params = dict(algorithm = FLANN_INDEX_KDTREE, trees = 5)
		search_params = dict(checks=50)   # or pass empty dictionary
		flann = cv2.FlannBasedMatcher(index_params,search_params)

		best_ratio , i = 0 , 1
		for row in data:
			matches = flann.knnMatch(des1,row[1],k=2)
			
			# Apply ratio test
			good = []
			for m,n in matches:
				if m.distance < 0.7*n.distance:
					good.append([m])
					
			c_ratio = len(good)/len(row[1])
			print("[{}] {0:.2f}".format(row[0].split(os.path.sep)[-1], c_ratio))
			
			if c_ratio > best_ratio:
				best_ratio = c_ratio
				imagePath = row[0]
				# matches_ = matches

			i += 1

		print("2-[IMAGE MATCHING]       | ratio: {0:.2f}".format(best_ratio))
		
		if best_ratio >= 0.5:
		
			# ref = cv2.imread(imagePath,0)
			# dim = (image.shape[1], image.shape[0])
			# ref = cv2.resize(ref, dim, interpolation = cv2.INTER_AREA)
			
			# kp2, des2 = surf.detectAndCompute(ref, None)
			# matches = flann.knnMatch(des1,des2,k=2)
			
			# Need to draw only good matches, so create a mask
			# matchesMask = [[0,0] for i in range(len(matches))]
			# for i,(m,n) in enumerate(matches):
			#	if m.distance < 0.7*n.distance:
			#		matchesMask[i]=[1,0]
					
			# draw_params = dict(matchColor = (0,255,0),
			#				   singlePointColor = (255,0,0),
			#				   matchesMask = matchesMask,
			#				   flags = 0)

			# output = cv2.drawMatchesKnn(image, kp1, ref, kp2, matches, None, **draw_params)
			# cv2.imwrite("output.jpg", output)

			return imagePath
		else:
			return ""
		
		# self.conn.close()
