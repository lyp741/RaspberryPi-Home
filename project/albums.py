import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
import os
import os.path
import json

images_path = "./camera_imgs/"
class AlbumsHandler(tornado.web.RequestHandler):
    def get(self):
        imglists = []
        for parent,dirnames,filenames in os.walk(images_path):
            for filename in filenames:
                filename = filename.split('.')[0]
                imglists.append(filename)
        imglists.sort()
        imglists.reverse()
        response = json.dumps(imglists, skipkeys=False, ensure_ascii=True, check_circular=True, allow_nan=True, cls=None, indent=None, separators=None, encoding='utf-8', default=None, sort_keys=False)
        print response
        self.write(response)
