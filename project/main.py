#!/usr/bin/python
#  -*- coding: utf-8 -*-
import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options

from weather import WeatherHandler
from camera import CameraHandler
from albums import AlbumsHandler
from music import MusicHandler
from playcontrol import PlaycontrolHandler
from playlist import PlaylistHandler
from selplaylist import SelPlaylistHandler
from movie import MovieHandler

class IndexHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("chunk")

class Application(tornado.web.Application):
	def __init__(self):

		handlers = [
			(r'/', IndexHandler),
            (r'/weather',WeatherHandler),
            (r'/camera',CameraHandler),
            (r'/albums',AlbumsHandler),
            (r'/music',MusicHandler),
            (r'/playcontrol',PlaycontrolHandler),
            (r'/playlist',PlaylistHandler),
            (r'/selplaylist',SelPlaylistHandler),
            (r'/movie',MovieHandler)
		]

		settings = {

		}

		tornado.web.Application.__init__(self, handlers)

if __name__ == '__main__':
	tornado.options.parse_command_line()
	app = Application()
	server = tornado.httpserver.HTTPServer(app)
	server.listen(8000)
	tornado.ioloop.IOLoop.instance().start()
