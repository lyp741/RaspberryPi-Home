import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
import json
import threading
import re
import subprocess
from static import Redis_conn as rds
import music

class SelPlaylistHandler(tornado.web.RequestHandler):
    def get(self):
        print "SelPlaylistHandler"
        playlist_class = self.get_argument("playlist_class",default="")
        print playlist_class
        if playlist_class == "":
            classes = music.ne.playlist_classes()
            self.write(json.dumps(classes))
        else:
            rds.set("displaying_playlist_class",playlist_class)
            self.write("ok")
