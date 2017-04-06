import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
from uuid import uuid4
# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import io
import threading
import datetime
from PIL import Image

pressStop = False
capturePressed = False

class CameraHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        thread = threading.Thread(target=self.startRecord)
        thread.setDaemon(True)
        thread.start()

    def on_close(self):
        pass

    def on_message(self, message):
        print message
        global capturePressed
        if message == "capture":
            capturePressed = True

    def callback(self, count):
        self.write_message(count.getvalue(),binary=True)

    def startRecord(self):
        camera = PiCamera()
        camera.resolution = (320, 320)
        camera.framerate = 5
        #rawCapture = PiRGBArray(camera, size=(640, 480))
        print "rawCapture"
        # allow the camera to warmup
        time.sleep(0.1)
        stream = io.BytesIO()
        i = 0
        global capturePressed
        # capture frames from the camera
        try:
            for frame in camera.capture_continuous(stream, 'jpeg',use_video_port=True):
                print type(frame)
                print type(stream)
                print "for frame"
                if capturePressed == True:
                    stream.seek(0)
                    capturePressed = False
                    now = datetime.datetime.now()
                    cur_time = now.strftime('%Y-%m-%d %H:%M:%S')
                    file_name = "./camera_imgs/" + cur_time + ".jpg"
                    myimage = Image.open(frame)
                    myimage.save(file_name,option={'progression':True,'quality':60,'optimize':True})
                    time.sleep(2)
                self.callback(frame)
                stream.seek(0)
                stream.truncate(0)
                #i+=1

            camera.close()
        except Exception,e:
            camera.close()
            print Exception,":",e
