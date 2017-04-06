#!/usr/bin/python
#  -*- coding: utf-8 -*-
import re
import os
import json
import time
import hashlib
import random
import base64
import binascii

from static import Redis_conn as rds
from Crypto.Cipher import AES
from http.cookiejar import LWPCookieJar
from bs4 import BeautifulSoup
import requests

nonce = '0CoJUm6Qyw8W8jud'
pubKey = '010001'
modulus = ('00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7'
           'b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280'
           '104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932'
           '575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b'
           '3ece0462db0a22b8e7')
default_timeout = 10

# 歌曲加密算法, 基于https://github.com/yanunon/NeteaseCloudMusic脚本实现
def encrypted_id(id):
    magic = bytearray('3go8&$8*3*3h0k(2)2', 'u8')
    song_id = bytearray(id, 'u8')
    magic_len = len(magic)
    for i, sid in enumerate(song_id):
        song_id[i] = sid ^ magic[i % magic_len]
    m = hashlib.md5(song_id)
    result = m.digest()
    result = base64.b64encode(result)
    result = result.replace(b'/', b'_')
    result = result.replace(b'+', b'-')
    return result.decode('utf-8')


# 登录加密算法, 基于https://github.com/stkevintan/nw_musicbox脚本实现
def encrypted_request(text):
    text = json.dumps(text)
    secKey = createSecretKey(16)
    encText = aesEncrypt(aesEncrypt(text, nonce), secKey)
    encSecKey = rsaEncrypt(secKey, pubKey, modulus)
    data = {'params': encText, 'encSecKey': encSecKey}
    return data


def aesEncrypt(text, secKey):
    pad = 16 - len(text) % 16
    text = text + chr(pad) * pad
    encryptor = AES.new(secKey, 2, '0102030405060708')
    ciphertext = encryptor.encrypt(text)
    ciphertext = base64.b64encode(ciphertext).decode('utf-8')
    return ciphertext


def rsaEncrypt(text, pubKey, modulus):
    text = text[::-1]
    rs = pow(int(binascii.hexlify(text), 16), int(pubKey, 16), int(modulus, 16))
    return format(rs, 'x').zfill(256)


def createSecretKey(size):
    return binascii.hexlify(os.urandom(size))[:16]

# 获取高音质mp3 url
def geturl(song):
    quality = 0
    if song['hMusic'] and quality <= 0:
        music = song['hMusic']
        quality = 'HD'
    elif song['mMusic'] and quality <= 1:
        music = song['mMusic']
        quality = 'MD'
    elif song['lMusic'] and quality <= 2:
        music = song['lMusic']
        quality = 'LD'
    else:
        return song['mp3Url'], ''

    quality = quality + ' {0}k'.format(music['bitrate'] // 1000)
    song_id = str(music['dfsId'])
    enc_id = encrypted_id(song_id)
    url = 'http://m%s.music.126.net/%s/%s.mp3' % (random.randrange(1, 3),
                                                  enc_id, song_id)
    return url, quality


class NetEase(object):
    def __init__(self):
        self.header = {
            'Accept': '*/*',
            'Accept-Encoding': 'gzip,deflate,sdch',
            'Accept-Language': 'zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4',
            'Connection': 'keep-alive',
            'Content-Type': 'application/x-www-form-urlencoded',
            'Host': 'music.163.com',
            'Referer': 'http://music.163.com/search/',
            'User-Agent':
            'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36'  # NOQA
        }
        self.cookiefile = './cookie/cookie'
        self.cookies = {'appver': '1.5.2'}
        self.playlist_class_dict = {}
        self.session = requests.Session()
        self.session.cookies = LWPCookieJar(self.cookiefile)
        try:
            print 'load cookie'
            self.session.cookies.load()
            print 'noerror'
            cookie = ''
            if os.path.isfile(self.cookiefile):
                self.file = open(self.cookiefile, 'r')
                cookie = self.file.read()
                print cookie
                self.file.close()
            expire_time = re.compile(r'\d{4}-\d{2}-\d{2}').findall(cookie)
            if expire_time:
                if expire_time[0] < time.strftime('%Y-%m-%d', time.localtime(time.time())):
                    self.storage.database['user'] = {
                        'username': '',
                        'password': '',
                        'user_id': '',
                        'nickname': '',
                    }
                    os.remove(self.cookiefile)
        except IOError as e:
            print 'error'
            response = self.login('13372877202',hashlib.md5('950228'.encode('utf-8')).hexdigest())
            if response['code'] != 200 :
                print "login error"
            else:
                print "login success"
                self.session.cookies.save()
                userid = response['account']['id']
                nickname = response['profile']['nickname']
                print userid
                print nickname
                static.Redis_conn.set('music_userid',userid)
                static.Redis_conn.set('music_nickname',nickname)

    def httpRequest(self,
                    method,
                    action,
                    query=None,
                    urlencoded=None,
                    callback=None,
                    timeout=None):
        connection = json.loads(
            self.rawHttpRequest(method, action, query, urlencoded, callback, timeout)
        )
        return connection

    def rawHttpRequest(self,
                       method,
                       action,
                       query=None,
                       urlencoded=None,
                       callback=None,
                       timeout=None):
        if method == 'GET':
            url = action if query is None else action + '?' + query
            connection = self.session.get(url,
                                          headers=self.header,
                                          timeout=default_timeout)

        elif method == 'POST':
            connection = self.session.post(action,
                                           data=query,
                                           headers=self.header,
                                           timeout=default_timeout)

        elif method == 'Login_POST':
            connection = self.session.post(action,
                                           data=query,
                                           headers=self.header,
                                           timeout=default_timeout)

        connection.encoding = 'UTF-8'
        return connection.text

    # 登录
    def login(self, username, password):
        pattern = re.compile(r'^0\d{2,3}\d{7,8}$|^1[34578]\d{9}$')
        if pattern.match(username):
            return self.phone_login(username, password)
        action = 'https://music.163.com/weapi/login?csrf_token='
        self.session.cookies.load()
        text = {
            'username': username,
            'password': password,
            'rememberLogin': 'true'
        }
        data = encrypted_request(text)
        try:
            return self.httpRequest('Login_POST', action, data)
        except requests.exceptions.RequestException as e:

            return {'code': 501}

    # 手机登录
    def phone_login(self, username, password):
        action = 'https://music.163.com/weapi/login/cellphone'
        text = {
            'phone': username,
            'password': password,
            'rememberLogin': 'true'
        }
        data = encrypted_request(text)
        #print data
        try:
            response = self.httpRequest('Login_POST', action, data)
            print type(response)
            return response
        except Exception as e:
            print "error",e.message
            return {'code': 501}

    # 用户歌单
    def user_playlist(self, uid, offset=0, limit=100):
        action = 'http://music.163.com/api/user/playlist/?offset={}&limit={}&uid={}'.format(  # NOQA
            offset, limit, uid)
        try:
            data = self.httpRequest('GET', action)
            return data['playlist']
        except (requests.exceptions.RequestException, KeyError) as e:

            return -1

    # 歌单详情
    def playlist_detail(self, playlist_id):
        action = 'http://music.163.com/api/playlist/detail?id={}'.format(
            playlist_id)
        try:
            rawdata = self.httpRequest('GET', action)
            playlist_img = rawdata['result']['coverImgUrl']
            data = rawdata['result']['tracks']
            temp = []
            for i in range(0, len(data)):
                url, quality = geturl(data[i])

                if data[i]['album'] is not None:
                    album_name = data[i]['album']['name']
                    album_id = data[i]['album']['id']
                else:
                    album_name = '未知专辑'
                    album_id = ''

                song_info = {
                    'song_id': data[i]['id'],
                    'artist': [],
                    'song_name': data[i]['name'],
                    'album_name': album_name,
                    'album_id': album_id,
                    'mp3_url': url,
                    'quality': quality,
                    'order': i+1
                }
                if 'artist' in data[i]:
                    song_info['artist'] = data[i]['artist']
                elif 'artists' in data[i]:
                    song_info['artist'] = data[i]['artists'][0]['name']
                else:
                    song_info['artist'] = '未知艺术家'

                temp.append(song_info)

            return playlist_img, temp
        except requests.exceptions.RequestException as e:
            return []

    def songs_detail_new_api(self, music_ids, bit_rate=320000):
        action = 'http://music.163.com/weapi/song/enhance/player/url?csrf_token='  # NOQA
        self.session.cookies.load()
        csrf = ''
        for cookie in self.session.cookies:
            if cookie.name == '__csrf':
                csrf = cookie.value
        if csrf == '':
            notify('You Need Login', 1)
        action += csrf
        data = {'ids': music_ids, 'br': bit_rate, 'csrf_token': csrf}
        connection = self.session.post(action,
                                       data=encrypted_request(data),
                                       headers=self.header, )
        result = json.loads(connection.text)
        return result['data']
        # 歌单（网友精选碟） hot||new http://music.163.com/#/discover/playlist/
    def top_playlists(self, category='全部', order='hot', offset=0, limit=100):
        action = 'http://music.163.com/api/playlist/list?cat={}&order={}&offset={}&total={}&limit={}'.format(  # NOQA
            category, order, offset, 'true',limit
            )  # NOQA
        if category == "我的":
            data = self.user_playlist(rds.get("music_userid"))
        else:
            data = self.httpRequest('GET', action)
            data = data['playlists']
        try:
            temp = []
            for i in range(0, len(data)):
                    playlists_info = {
                        'playlist_id': data[i]['id'],
                        'playlists_name': data[i]['name'],
                        'creator_name': data[i]['creator']['nickname'],
                        'coverImgUrl':data[i]['coverImgUrl']
                    }
                    temp.append(playlists_info)
            return temp
        except Exception , e:
            print Exception , e
            return []
    def playlist_classes(self):
        action = 'http://music.163.com/discover/playlist/'
        try:
            print "enter classes"
            data = self.rawHttpRequest('GET', action)
            soup = BeautifulSoup(data, 'lxml')
            dls = soup.select('dl.f-cb')
            temp = []
            for dl in dls:
                title = dl.dt.text
                sub = [item.text for item in dl.select('a')]
                dicts = {"title":title, "sub":sub}
                temp.append(dicts)
            return temp
        except Exception, e:
            print Exception , e
            return []

if __name__ == '__main__':
    ne = NetEase()
    #response = ne.login('18525464022',hashlib.md5('zxcj153264'.encode('utf-8')).hexdigest())
    #response = ne.login('13372877202',hashlib.md5('950228'.encode('utf-8')).hexdigest())
    #ne.username = response['profile']['nickname']
    #ne.uid = response['account']['id']
    img_url, musics = ne.playlist_detail(30749020)
