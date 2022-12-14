import datetime
from threading import Thread
import requests
import json
import time
import sqlite3
import os
import logging
import sys
import traceback
from conf import DEFAULT_DATA, color

logging.basicConfig(level=logging.INFO, filename="daka.log")

with open("pid", "w") as f:
    f.write(str(os.getpid()))

global URL_LOGIN
global URL_CLOCKIN
global USER_DATA
global USER_INFO
global conn
global db
global OUT_OF_DATES
URL_LOGIN = r"https://stu.eurasia.edu/yqsb/login/in"
URL_CLOCKIN = "https://stu.eurasia.edu/yqsb/jkdj/save"
USER_INFO = "UserInfo"
USER_DATA = "UserData"
OUT_OF_DATES = []


def gettime():
    return str(datetime.datetime.now().strftime("%d/%m/%Y %H:%M:%S"))


def uop(string):
    return f' [{gettime()}] - '+string


def getUser(uid):
    db.execute(f"select * from UserInfo where uid='{uid}'")
    return db.fetchall()


def getData(uid):
    db.execute(f"select * from UserData where uid='{uid}'")
    return db.fetchall()


def getColumns(table):
    db.execute(f"pragma table_info({table})")
    return [i[1] for i in db.fetchall()]


def putToken(uid, token):
    logging.info(uid)
    logging.info(token)
    db.execute(f"update UserInfo set token='{token}' where uid='{uid}'")
    conn.commit()


def getSQLToken(uid):
    db.execute(f"select token from UserInfo where uid='{uid}'")
    return db.fetchall()[0][0]


class stu:
    def __init__(self, uid):
        self.uid = uid
        self.pwd = None
        self.token = None
        self.tokenTime = None
        self.headers = None
        self.userdata = None
        self.defaultdata = None
        self.data = None
        self.isClockin = None
        self.isClockinTime = None
        self.isTokenCheck = False

    def initData(self):
        if getUser(self.uid) and getData(self.uid):
            # logging.info(getUser(self.uid))
            self.pwd = getUser(self.uid)[0][2]
            self.data = DEFAULT_DATA.copy()
            self.userdata = getData(self.uid)[0][2:]
            cols = getColumns(USER_DATA)[2:]
            for i in range(len(cols)):
                self.data[cols[i]] = self.userdata[i]
            token = getSQLToken(self.uid)
            if not token:
                self.getToken()
            else:
                self.token = token
                self.headers = {
                    "Authorization": token,
                    "Content-Type": "application/json;",
                    "token": token,
                }
            return self
        else:
            logging.info(logging.info(self.print(self.uid+"???????????????")))
            return None

    def print(self, string):
        return self.uid+" - "+string

    def getToken(self):
        global URL_LOGIN
        params = {
            "zh": self.uid,
            "mm": self.pwd,
        }

        _token = requests.get(URL_LOGIN, params=params)

        if _token.status_code == 200:
            res = _token.json()
            if res["success"]:
                token = res["token"]
                self.token = token
                self.tokenTime = time.strftime(
                    "%Y-%m-%d", time.localtime(time.time()))
                self.headers = {
                    "Authorization": token,
                    "Content-Type": "application/json;",
                    "token": token,
                }
                putToken(self.uid, token)
                logging.info(self.print("token????????????"))
                return 1
            else:
                logging.info(self.print("token???????????? - "+res["msg"]))
                return 0

        else:
            logging.info(self.print("??????"+str(_token.status_code)))
        return 0

    def clockIn(self):

        if self.token:
            if self._clockIn() == 1:
                logging.info(self.print("?????????"))
            else:
                logging.info(self.print("????????????"))

    def _clockIn(self):
        global URL_CLOCKIN
        global OUT_OF_DATES
        res = requests.post(URL_CLOCKIN, headers=self.headers,
                            data=json.dumps(self.data))
        js = res.json()
        if res.status_code == 200:
            if js["success"] == True:
                self.changeClockin(1)
                return 1
            else:
                if "????????????" in js["msg"] or "??????????????????" in js["msg"]:
                    logging.info(self.print("?????????????????????????????????token"))
                    OUT_OF_DATES += [self]
                    return 0

                elif "???????????????" in js["msg"]:
                    logging.info(self.print(js["msg"]))
                    self.changeClockin(1)
                    return 1
                else:
                    logging.info(self.print(js["msg"]))
                    return 0
        else:
            logging.info(self.print("POST???????????? - "+res.text))
            return 0

    def changeClockin(self, isclock):
        if isclock == 1:
            self.isClockin = True
            self.isClockinTime = time.strftime(
                "%Y-%m-%d", time.localtime(time.time()))


def getUsers():
    db.execute("select * from UserInfo")
    return db.fetchall()


def createThreads(users):
    ths = []
    for i in users:
        th = Thread(target=ClockIn, args=(i,))
        th.daemon = True
        th.start()
        ths += [th]
    return ths


def createUsers(uids):
    users = []
    for i in uids:
        user = stu(i).initData()
        if user:
            users += [user]
    return users


def ClockIn(user: stu):
    user.clockIn()


def main():
    uids = [i[1] for i in getUsers()]
    users = createUsers(uids)
    if not users:
        logging.info("???????????????")
    ths = createThreads(users)
    for i in ths:
        i.join()
    if OUT_OF_DATES:
        for i in OUT_OF_DATES:
            i.getToken()
        ths = createThreads(OUT_OF_DATES)
        for i in ths:
            i.join()


if __name__ == "__main__":
    try:
        while 1:
            # logging.info()
            start = uop("????????????... ")
            TIME_NOW = time.strftime("%H", time.localtime(time.time()))
            if TIME_NOW == "06" or TIME_NOW == "07":
                logging.info(start+"yes.")
                conn = sqlite3.connect("test", check_same_thread=False)
                db = conn.cursor()
                logging.info(uop(color("????????????", "green")))
                main()
                db.close()
                conn.close()
                logging.info(uop("??????????????????"))
            else:
                logging.info(start+"no.")
            # break
            time.sleep(3600)
    except Exception as e:
        logging.exception(traceback.format_exc())
