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

class PlaylistHandler(tornado.web.RequestHandler):
    def get(self):
        try:
            playlist_id = self.get_argument("playlist_id",default="")
            playing = self.get_argument("playing",default="true")
            if playlist_id == "":
                print "Playlist_id is None"
                class_name = rds.get("displaying_playlist_class")
                print class_name
                playlist = json.dumps(music.ne.top_playlists(class_name))
                self.write(playlist)
            else:
                rds.set("displaying_playlist_id",playlist_id)
                self.write("ok")
        except Exception,e:
            print Exception,":",e
