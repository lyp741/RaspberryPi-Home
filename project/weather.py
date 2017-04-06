#!/usr/bin/python
#  -*- coding: utf-8 -*-
import tornado.web
import tornado.websocket
import tornado.httpserver
import tornado.ioloop
import tornado.options
import xml.dom.minidom as minidom
import ip
import json
import static
import dht11
import time
import threading

rds = static.Redis_conn


def write_redis(strtime, temp, humid):
    rds.rpush(strtime+'_Temp',str(temp))
    rds.rpush(strtime+'_Humid',str(humid))
    if rds.llen(strtime+'_Temp')>12:
        rds.lpop(strtime+'_Temp')
    if rds.llen(strtime+'_Humid')>12:
        rds.lpop(strtime+'_Humid')
    print 'write_redis'+strtime

def timerDHT11():
    while True:
        print "进入DHT"
        cur_time = time.localtime()
        if cur_time.tm_min % 5 == 0:
            temp, humid = dht11.measure_dht11()
            write_redis('Hour',temp,humid)
            if cur_time.tm_hour % 2 == 0 and cur_time.tm_min == 5:
                write_redis('Day',temp,humid)
                if cur_time.tm_hour == 12:
                    write_redis('Week', temp, humid)
        time.sleep(60)

class WeatherHandler(tornado.web.RequestHandler):
    def get(self):
        response = ip.getWeather('0')
        print response
        doc = minidom.parseString(response, parser=None).documentElement
        status = doc.getElementsByTagName('status1')[0].childNodes[0].data  #天气状况  晴
        city = doc.getElementsByTagName('city')[0].childNodes[0].data       #大连
        tigandu = doc.getElementsByTagName('tgd1')[0].childNodes[0].data        #6
        ganmaozhishu = doc.getElementsByTagName('gm_s')[0].childNodes[0].data   #感冒指数详细说明  天气很凉，季节转换的气候，慎重增加衣服；较易引起感冒；
        wind_direction = doc.getElementsByTagName('direction1')[0].childNodes[0].data   #西北风
        tiganzhishu = doc.getElementsByTagName('ssd_l')[0].childNodes[0].data  #体感指数概述      偏冷
        pollution = doc.getElementsByTagName('pollution_l')[0].childNodes[0].data   #空气优
        day_temp_today = doc.getElementsByTagName('temperature1')[0].childNodes[0].data   #12
        night_temp_today = doc.getElementsByTagName('temperature2')[0].childNodes[0].data     #5

        response = ip.getWeather('1')
        print response
        doc = minidom.parseString(response, parser=None).documentElement
        status_tomorrow = doc.getElementsByTagName('status1')[0].childNodes[0].data
        pollution_tomorrow = doc.getElementsByTagName('pollution_l')[0].childNodes[0].data
        day_temp_tomorrow = doc.getElementsByTagName('temperature1')[0].childNodes[0].data
        night_temp_tomorrow = doc.getElementsByTagName('temperature2')[0].childNodes[0].data

        response = ip.getWeather('2')
        doc = minidom.parseString(response, parser=None).documentElement
        status_afterday = doc.getElementsByTagName('status1')[0].childNodes[0].data
        pollution_afterday = doc.getElementsByTagName('pollution_l')[0].childNodes[0].data
        day_temp_afterday = doc.getElementsByTagName('temperature1')[0].childNodes[0].data
        night_temp_afterday = doc.getElementsByTagName('temperature2')[0].childNodes[0].data

        hour_temp = rds.lrange('Hour_Temp',0,12)
        hour_humid = rds.lrange('Hour_Humid',0,12)
        day_temp = rds.lrange('Day_Temp',0,12)
        day_humid = rds.lrange('Day_Humid',0,12)
        week_temp = rds.lrange('Week_Temp',0,12)
        week_humid = rds.lrange('Week_Humid',0,12)

        hour_temp = map(int,hour_temp)
        hour_humid = map(int,hour_humid)
        day_temp = map(int,day_temp)
        day_humid = map(int,day_humid)
        week_temp = map(int,week_temp)
        week_humid = map(int,week_humid)

        dicts = {'city':city+"|"+status, 'tigandu':tigandu+'°', 'ganmaozhishu':ganmaozhishu, 'wind_direction':wind_direction,
                'tiganzhishu':tiganzhishu,'pollution':"空气"+pollution, 'temp_today':day_temp_today+'/'+night_temp_today+'°',"status_today":status+" | "+pollution,
                'status_tomorrow':status_tomorrow+" | "+pollution_tomorrow, 'temp_tomorrow':day_temp_tomorrow+'/'+night_temp_tomorrow+'°',
                'status_afterday':status_afterday+" | "+pollution_afterday, 'temp_afterday':day_temp_afterday+'/'+night_temp_afterday+'°',
                'hour_temp':hour_temp, 'hour_humid':hour_humid, 'day_temp':day_temp, 'day_humid':day_humid, 'week_temp':week_temp, 'week_humid':week_humid
        }
        result = json.dumps(dicts, skipkeys=False, ensure_ascii=True, check_circular=True, allow_nan=True, cls=None, indent=None, separators=None, encoding='utf-8', default=None, sort_keys=False)
        self.write(result)

t = threading.Thread(group=None, target=timerDHT11, name=None, args=(), kwargs=None, verbose=None)
t.setDaemon(True)
t.start()
