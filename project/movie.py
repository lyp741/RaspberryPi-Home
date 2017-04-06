#!/usr/bin/python
#  -*- coding: utf-8 -*-
import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
import os
import os.path
import json
from static import Redis_conn as rds
import cv2
movies_path = "./movies/"

class MovieHandler(tornado.web.RequestHandler):
    def get(self):
        movies = []
        for parent,dirnames,filenames in os.walk(movies_path):
            for filename in filenames:
                #filename = filename.split('.')[0]
                print parent
                if not os.path.exists("./thumb_imgs/"+filename+".jpg"):
                    vc = cv2.VideoCapture(movies_path + filename) #读入视频文件
                    if vc.isOpened(): #判断是否正常打开
                        for i in range(100):
                            rval , frame = vc.read()
                        shape = frame.shape
                        wid = int(50 * ((shape[1]*1.0)/(shape[0]*1.0)))
                        print wid
                        frame = cv2.resize(frame,(wid,50))
                        print frame.shape
                        width_st = wid/2-25
                        print width_st
                        frame = frame[:,width_st:width_st+50]
                        print frame.shape
                        cv2.imwrite('./thumb_imgs/'+ filename + '.jpg',frame)
                movies.append(filename)
        movies.sort()
        movies.reverse()
        response = json.dumps(movies)
        rds.set("playing_movie_list",response)
        self.write(response)
