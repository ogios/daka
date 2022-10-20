# coding=utf-8

import sqlite3
import sys
DEFAULT_DATA = {
    "fxdj": "低风险地区",
    "sfyz": "0",
    "mqszd": "陕西省西安市`雁塔区东仪路8号西安欧亚学院",
    "mqsxzd": "",
    "xxdz": "",
    "fxstzk": "",
    "qtstyc": "无",
    "jtdz": "",
    "sfyxgl": "0",
    "sfzs": "1",
    "sfjchbr": "0",
    "sfjjgl": "0",
    "sfys": "0",
    "sfqz": "0",
    "sfbs": "0",
    "mqcs": "",
    "bz1": "",
    "bz2": "",
    "xm": "",
    "xh": "",
    "xb": "1",
    "jtzz": "陕西省西安市",
    "lxdh": "",
    "rctw": "37",
    "sffr": "0",
    "sfjz": "0",
    "sfks": "0",
    "sffl": "0",
    "sfjcwhry": "0",
    "sffx": "1",
    "fxrq": "",
    "jtgj": "",
    "cc": "",
    "fdygh": "96012097",
    "jcsj": "",
    "jchbsj": "",
    "mqsxdz": "西安欧亚学院"
}

data = {
    "fxdq": "风险地区",
    "mqszd": "目前所在地",
    "xxdz": "详细地址",
    "xm": "姓名",
    "xh": "学号",
    "jtzz": "家庭住址(省市)",
    "lxdh": "联系电话",
    "sfzs": "是否在陕西",
    "mqsxdz": "目前陕西地址",
    "rctw": "体温",
}


def color(string, color):
    dic = {
        'white': '\033[30m',
        'red': '\033[31m',
        'green': '\033[32m',
        'yellow': '\033[33m',
        'blue': '\033[34m',
        'purple': '\033[35m',
        'cyan': '\033[36m',
        'black': '\033[37m'
    }
    return dic[color]+string+'\033[0m'


MAIN_PAGE = '''
+----------------
| 1. 添加用户
| 2. 修改用户配置
| 3. 删除用户
| 4. 退出
| 5. 已有用户
+----------------
>>> '''


PAGE_2 = '''
+------------------
| 1. 修改用户名与密码
| 2. 修改用户打卡信息
| 3. <-
| 4. 退出
+------------------
>>> '''


MAIN_PAGE = color(MAIN_PAGE, "green")
PAGE_2 = color(PAGE_2, "green")


USER_INFO = "UserInfo"
USER_DATA = "UserData"


def addUser(uid, pwd):
    res = getUser(uid)
    if res:
        db.execute(
            f"update UserInfo set uid='{uid}', pwd='{pwd}' where uid='{uid}'")
        conn.commit()
        print("数据已更新")
        return 1
    else:
        db.execute(f"insert into UserInfo(uid,pwd) values('{uid}','{pwd}')")
        print(db.fetchall())
        conn.commit()
        return 1


def getUsers():
    db.execute("select * from UserInfo")
    return db.fetchall()


def getUser(uid):
    db.execute(f"select * from UserInfo where uid='{uid}'")
    return db.fetchall()


def deletUser(uid):
    if not getUser(uid):
        return 0
    else:
        db.execute(f"delete from UserInfo where uid='{uid}'")
        if getData:
            db.execute(f"delete from UserData where uid='{uid}'")
        conn.commit()
        return 1


def getData(uid):
    db.execute(f"select * from UserData where uid='{uid}'")
    return db.fetchall()


def InputUserInfo(isChange=True):
    uid = input(color("学号:\n>>> ", "green"))
    pwd = input(color("密码:\n>>> ", "green"))
    if isChange:
        if not getUser(uid):
            while 1:
                yn = input(color("用户不存在，是否创建(y/n)", "green"))
                match yn:
                    case "y":
                        if addUser(uid, pwd):
                            print("添加成功")
                            return 1
                        else:
                            print("添加失败")
                            return 0
                    case "n":
                        return 1
                    case _:
                        print("输入有误!")
        else:
            if addUser(uid, pwd):
                print("添加成功")
                return 1
            else:
                print("添加失败")
                return 0
    else:
        if getUser(uid):
            print("用户已存在")
        else:
            if addUser(uid, pwd):
                print("添加成功")
                addEmptyData(uid)
                return 1
            else:
                print("添加失败")
                return 0


def ChangeUserData():
    tmp = {}
    res = input(color("学号:\n>>> ", "green"))
    tmp["uid"] = res
    tmp["xh"] = res
    uid = res

    for i in ["xm", "lxdh", "jtzz", "xxdz"]:
        res = input(color(data[i]+":\n>>> ", "green"))
        tmp[i] = res
    if not getData(uid):
        db.execute(
            f"insert into UserData{str(tuple(tmp.keys()))} values{str(tuple(tmp.values()))}")
    else:
        set = "set "
        for i in tmp:
            set += f"{i}='{tmp[i]}',"
        set = set[:-1]
        db.execute(f"update UserData {set} where uid='{uid}'")
    conn.commit()
    return 1


def addEmptyData(uid):
    db.execute(f"insert into UserData(uid) values('{uid}')")
    conn.commit()


def main_ADDUSER():
    if InputUserInfo(isChange=False):
        while 1:
            yn = input(color("是否继续修改已有/默认打卡配置(y/n)", "green"))
            match yn:
                case "y":
                    ChangeUserData()
                    return
                case "n":
                    return
                case _:
                    print("输入有误!")


def main_DeleteUser():
    uid = input(color("学号:\n>>> ", "green"))
    if not deletUser(uid):
        print("用户不存在")


def page2():
    while 1:
        btn2 = input(PAGE_2)
        match btn2:
            case "1":
                if InputUserInfo():
                    return
            case "2":
                ChangeUserData()
            case "3":
                return
            case "4":
                sys.exit()
            case _:
                print("输入有误!")


def main():
    while 1:
        btn1 = input(MAIN_PAGE)
        match btn1:
            case "1":
                main_ADDUSER()
            case "2":
                page2()
            case "3":
                main_DeleteUser()
            case "4":
                sys.exit()
            case "5":
                uids = [i[1] for i in getUsers()]
                for i in uids:
                    print(" -"+i)
            case _:
                print("输入有误!")


if __name__ == "__main__":
    conn = sqlite3.connect("test")
    db = conn.cursor()
    main()
