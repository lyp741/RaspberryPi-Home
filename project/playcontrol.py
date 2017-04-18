#!/usr/bin/python
#  -*- coding: utf-8 -*-
import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
import json
import threading
import re
import os
import time
import subprocess
from static import Redis_conn as rds
import music
class PlayControl():
    def setcallback(self,callback):
        self.callback = callback

    def __init__(self):
        try:
            self.popens = []
            print "enter pc"
            rds.set("playing_status","paused")
            self.playing_playlist = json.loads(rds.get("playing_playlist"),encoding='utf-8')
        except:
            print "error playcontrol init"

    def status(self):
        try:
            dicts = {}
            playing_stat = rds.get("playing_status")
            if playing_stat is None:
                rds.set("playing_status","stopped")
                playing_stat = rds.get("playing_status")
            dicts["playing_status"] = playing_stat
            playing_media = rds.get("playing_media")
            print "status  playing_media:",playing_media
            if playing_media is None:
                rds.set("playing_media","none")
                playing_media = rds.get("playing_media")
            dicts["playing_media"] = playing_media
            if playing_media == "music":
                dicts["playing_songid"] = int(rds.get("playing_songid"))
            elif playing_media == "movie":
                dicts["playing_moviename"] = rds.get("playing_moviename")
        except Exception,e:
            print Exception,":",e
        return dicts

    def get_music_playing_order(self,songid):
        try:
            songid = int(songid)
            for song_info in self.playing_playlist:
                song_id = song_info['song_id']
                print "get song_id",type(song_id)
                print "  para songid",type(songid)
                if song_id == songid:
                    print song_id
                    index = self.playing_playlist.index(song_info)
                    break
            if locals().has_key('index'):
                return index
            else:
                print "no songid"
                return -1
        except Exception,e:
            print "get_music_playing_order error"
            print Exception,":",e
            return -1

    def get_music_playing_songinfo(self,songid):
        try:
            songid = int(songid)
            for song_info in self.playing_playlist:
                song_id = song_info['song_id']
                if song_id == songid:
                    index = song_info
                    break
            if index:
                return index
            else:
                print "no songid"
                return None
        except:
            print "get_music_playing_songinfo error"
            return None

    def togglePause(self):
        if rds.get("playing_media") == "music":
            print "pause"
            try:
                self.popen_handler.stdin.write(b'P\n')
                self.popen_handler.stdin.flush()
                if rds.get("playing_status") == "playing":
                    rds.set("playing_status","paused")
                elif rds.get("playing_status") == "paused":
                    rds.set("playing_status", "playing")
            except Exception,e:
                print Exception,":",e
                print "pause music error"
                self.playmusic(rds.get("playing_songid"))
        elif rds.get("playing_media") == "movie":
            print "pause movie"
            try:
                self.popen_handler.stdin.write(b'p')
                self.popen_handler.stdin.flush()
                if rds.get("playing_status") == "playing":
                    rds.set("playing_status","paused")
                elif rds.get("playing_status") == "paused":
                    rds.set("playing_status", "playing")
            except Exception, e:
                print Exception, e
                self.playmovie(rds.get("playing_moviename"))
    def setDisplayingPlaylist(self, playlist_image_url, playlist):
        try:
            rds.set("displaying_list_image_url",playlist_image_url)
            rds.delete("displaying_playlist")
            print "displaying_playlist:"
            print json.dumps(playlist)
            rds.set("displaying_playlist",json.dumps(playlist))
        except:
            print "set displaying_playlist failed"

    def next(self):
        if rds.get("playing_media") == "music":
            print "next"
            index = self.get_music_playing_order(rds.get("playing_songid"))
            if index != -1:
                if index == len(self.playing_playlist) - 1:
                    next_index = 0
                else:
                    next_index = index + 1
                song_id = self.playing_playlist[next_index]['song_id']
                rds.set("playing_songid",song_id)
                if not self.playmusic(song_id):
                    self.next()
                self.callback.getStatus()
        elif rds.get("playing_media") == 'movie':
            print "next movie"
            try:
                movielist = json.loads(rds.get("playing_movie_list"))
                order = movielist.index(rds.get("playing_moviename"))
                if order == len(movielist) -1:
                    prev_idx = 0
                else:
                    prev_idx = order + 1
                if not self.playmovie(movielist[prev_idx]):
                    self.next()
            except Exception, e:
                print Exception, e

    def prev(self):
        if rds.get("playing_media") == "music":
            print "prev"
            index = self.get_music_playing_order(rds.get("playing_songid"))
            if index != -1:
                if index == 0:
                    playlist_len = len(self.playing_playlist)
                    prev_index = playlist_len - 1
                else:
                    prev_index = index - 1
                song_id = self.playing_playlist[prev_index]['song_id']
                rds.set("playing_songid",song_id)
                if not self.playmusic(song_id):
                    self.prev()
        elif rds.get("playing_media") == 'movie':
            print "prev movie"
            try:
                movielist = json.loads(rds.get("playing_movie_list"))
                order = movielist.index(rds.get("playing_moviename"))
                if order == 0:
                    prev_idx = len(movielist) - 1
                else:
                    prev_idx = order - 1
                if not self.playmovie(movielist[prev_idx]):
                    self.prev()
            except Exception, e:
                print Exception, e

    def stop(self):
        #if rds.get("playing_media") == 'music':
        try:
            print len(self.popens)
            for op in self.popens:
                op.kill()
                print "terminated"
            self.popens = []
            if self.popen_handler:
                if rds.get("playing_media") == 'music':
                    self.popen_handler.stdin.write(b'Q\n')
                else:
                    self.popen_handler.stdin.write(b'q')
                self.popen_handler.stdin.flush()
                self.popen_handler.kill()
                os.system('sudo killall omxplayer.bin')
                print "stoped"
            time.sleep(0.01)
        except Exception,e:
            print Exception,":",e
            return

    def playmovie(self,movie_filename):
        def newThread():
            while True:
                strout = self.popen_handler.stdout.readline().decode('utf-8')
                print strout
                if strout == "":
                    print "returned none so break"
                    break
                if re.match('have a nice day*',strout):
                    self.popen_handler.stdin.write(b'Q\n')
                    self.popen_handler.stdin.flush()
                    self.popen_handler.kill()
                    self.next()
                    break
        self.stop()
        print "start movie"
        print movie_filename
        para = ['omxplayer', '-o','local', "/home/pi/project/movies/" + movie_filename]
        try:
            self.popen_handler = subprocess.Popen(para,
                                                      stdin=subprocess.PIPE,
                                                      stdout=subprocess.PIPE,
                                                      stderr=subprocess.PIPE)
            self.popens.append(self.popen_handler)
            rds.set("playing_status","playing")
            rds.set("playing_media","movie")
            rds.set("playing_moviename",movie_filename)
            t = threading.Thread(target=newThread)
            t.start()
            return True
        except Exception,e:
            print Exception, e
            return False

    def playmusic(self,songid):
        def newThread():
            while True:
                strout = self.popen_handler.stdout.readline().decode('utf-8')
                if strout == "" :
                    break
                #print strout
                if re.match('^\@F.*$', strout):
                    process_data = strout.split(' ')
                    process_location = float(process_data[4])
                    continue
                elif strout == '@P 0\n':
                    self.popen_handler.stdin.write(b'Q\n')
                    self.popen_handler.stdin.flush()
                    self.popen_handler.kill()
                    self.next()
                    break
        self.stop()
        print "start popen"
        try:
            para = ['mpg123', '-R']
            self.popen_handler = subprocess.Popen(para,
                                                      stdin=subprocess.PIPE,
                                                      stdout=subprocess.PIPE,
                                                      stderr=subprocess.PIPE)
            self.popen_handler.stdin.write(b'V ' + str(100).encode('utf-8') + b'\n')
            self.popens.append(self.popen_handler)
            rds.set("playing_songid",songid)
            song_info = self.get_music_playing_songinfo(songid)
            print "song_info:"
            print song_info
            if song_info:
                #print song_info['mp3_url']
                new_url = music.ne.songs_detail_new_api([songid])[0]['url']
                print new_url
                self.popen_handler.stdin.write(b'L ' + new_url.encode('utf-8') + b'\n')
                self.popen_handler.stdin.flush()
            rds.set("playing_songid", songid)
            rds.set("playing_media","music")
            rds.set("playing_status","playing")
            t = threading.Thread(target=newThread)
            t.start()
            return True
        except Exception,e:
            print Exception,":",e
            if new_url is None:
                return False

    def changePlayinglist(self,media):
        if media == 'music':
            rds.delete("playing_playlist")
            print "deleted"
            rds.set('playing_media',"music")
            displaylist_dumps = rds.get("displaying_playlist")
            print "dump success"
            rds.set("playing_playlist",displaylist_dumps)
            print "restore success"
            self.playing_playlist = json.loads(rds.get("playing_playlist"), encoding='utf-8', cls=None, object_hook=None, parse_float=None, parse_int=None, parse_constant=None, object_pairs_hook=None)

    def changeToMedia(self,media):
        playing_media = rds.get("playing_media")
        if media != playing_media:
            playing_status = rds.get("playing_status")
            if playing_status == 'playing':
                self.stop()



pc = PlayControl()

class PlaycontrolHandler(tornado.websocket.WebSocketHandler):
    def getStatus(self):
        stats = pc.status()
        dicts = {"cmd":"status", 'data':stats}
        self.callback(json.dumps(dicts,encoding='utf-8'))

    def open(self):
        print("open")
        pc.setcallback(self)
        self.getStatus()
        print("open finished")

    def on_close(self):
        pass

    def on_message(self, message):
        try:
            js = json.loads(message, encoding='utf-8', cls=None, object_hook=None, parse_float=None, parse_int=None, parse_constant=None, object_pairs_hook=None)
            print "load success"
            cmd = js['cmd']
            if cmd == 'pause':
                pc.togglePause()
            elif cmd == 'prev':
                pc.prev()
            elif cmd == 'next':
                pc.next()
            elif cmd == 'play':
                media = js['data']['media']
                pc.changeToMedia(media)
                if media == 'music':
                    pc.changePlayinglist(media)
                    print "changed pl!"
                    if not pc.playmusic(js['data']['songid']):
                        pc.next()
                    print "playmusic success"
                    rds.set("playing_playlist_id",rds.get("displaying_playlist_id"))
                elif media == 'movie':
                    movie_name = js['data']['filename']
                    if not pc.playmovie(movie_name):
                        pc.next()
            elif cmd == 'playlist_id':
                rds.set("displaying_playlist_id",js['data']['playlist_id'])
            self.getStatus()
        except Exception, e:
            print "on_message error, message is:"
            print message
            print Exception, e

    def callback(self, count):
        try:
            self.write_message(count,binary=False)
        except:
            print "write_message error"
