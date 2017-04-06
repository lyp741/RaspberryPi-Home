#!/usr/bin/python
#  -*- coding: utf-8 -*-
import sys, os, urllib2, json

import re, urllib
import sys
import threading
import time
from static import Redis_conn as rds
reload(sys)
sys.setdefaultencoding('utf8')
class Get_public_ip:
    def getip(self):
        try:
            myip = self.visit("http://1212.ip138.com/ic.asp")
        except:
            myip = "So sorry!!!"
        return myip
    def visit(self,url):
        request = urllib2.Request(url)
        response = urllib2.urlopen(request)
        str = response.read()
        print str
        pattern = re.compile('''\[(.*?)]''')
        items = re.findall(pattern , str)
        return items[0]
def getip():
    getmyip = Get_public_ip()
    return getmyip.getip()

def get_ip_area(ip):
    try:
        apiurl = "http://ip.taobao.com/service/getIpInfo.php?ip=%s" %ip
        content = urllib2.urlopen(apiurl).read()
        data = json.loads(content)['data']
        code = json.loads(content)['code']
        if code == 0:   # success
            print(data['country_id'])
            print(data['area'])
            print(data['city'])
            print(data['region'])
            return data['city']
        else:
            print(data)
    except Exception as ex:
        print(ex)

class weather(object):
    weather_uri = "http://apistore.baidu.com/microservice/weather?cityid="
    def mainHandle(self,day):
        city = rds.get("current_city")
        city_name = city[0:len(city)-3]
        print "len city",len(city)
        print "city",city
        print "city_name",city_name
        code_uri = "http://php.weather.sina.com.cn/xml.php?"+urllib.urlencode({'city':city_name.encode('gb2312')})+"&password=DJOYnieT8234jlsK&day="+day
        uri = code_uri
        print uri
        url = urllib2.urlopen(uri).read().decode('utf-8')
        return url

def getCity():
    while True:
        try:
            ip = getip()
            city = get_ip_area(ip)
            print ip
            print "len",len(city)
            rds.set("current_ip",ip)
            rds.set("current_city",city)
        except:
            print "error getCity"
        time.sleep(600)

t = threading.Thread(target=getCity)
t.start()
def getWeather(day):
    wt = weather()
    return wt.mainHandle(day)
