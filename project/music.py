#!/usr/bin/python
#  -*- coding: utf-8 -*-
import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
import json
import MusicApi
from static import Redis_conn as rds
from playcontrol import pc

class music_class():
    def __init__(self):
        self.userid = rds.get('music_userid')
        self.nickname = rds.get('music_nickname')

ne = MusicApi.NetEase()
mc = music_class()

class MusicHandler(tornado.web.RequestHandler):
    def get(self):
        get_playing = self.get_argument('playing')
        print "get_playing",get_playing
        img_url, musics = ne.playlist_detail(rds.get("playing_playlist_id" if get_playing == 'true' else "displaying_playlist_id"))
        pc.setDisplayingPlaylist(img_url,musics)
        dicts = {"img_url":img_url,"musics":musics}
        response = json.dumps(dicts)
        mc.playlist_image_url = img_url
        mc.playlist_songs = musics
        self.write(response)
