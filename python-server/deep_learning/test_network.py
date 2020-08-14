# import the necessary packages
from keras.preprocessing.image import img_to_array
from keras.models import load_model
import numpy as np
import argparse
import imutils
import cv2
import tensorflow as tf

class Classification:
	def classify(self, image):
		# orig = image.copy()

		# pre-process the image for classification
		image = cv2.resize(image, (224, 224))
		image = image.astype("float") / 255.0
		image = img_to_array(image)
		image = np.expand_dims(image, axis=0)

		# class labels
		classes = ['1', '2', '3', '4']

		# load the trained convolutional neural network
		model = tf.keras.models.load_model("deep_learning/model.h5", custom_objects=None, compile=True)

		# classify the input image
		prediction = model.predict(image)[0]
		
		#return position of max
		MaxPosition = np.argmax(prediction)  
		prediction_label = classes[MaxPosition]
		proba = prediction[MaxPosition]
		
		if proba >= 0.3:
			label = "{} {:.2f}%".format(prediction_label, proba * 100)
			print("1-[IMAGE CLASSIFICATION] | class:", label)
			
			# draw the label on the image
			# output = imutils.resize(orig, width=400)
			# cv2.putText(output, label, (10, 25),  cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
			# cv2.imwrite("output.jpg", output)
			# cv2.imshow("Output", output)
			# cv2.waitKey(0)
			
			return prediction_label
		else:
			return ""



