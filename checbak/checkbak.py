# 这是一个示例 Python 脚本。

# 按 Shift+F10 执行或将其替换为您的代码。
# 按 双击 Shift 在所有地方搜索类、文件、工具窗口、操作和设置。
import os
import configparser
import json
import schedule
import time
import requests
from datetime import datetime,date

from pkg_resources import null_ns_handler

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8')
# 获取配置
syslist = config.get('config', 'syslist')
webhookUrl = config.get('config', 'webhookUrl')
proxyServer = config.get('config', 'proxyServer')
crontime=config.get('config', 'crontime')
current_date = date.today()


def get_latest_modification_time(directory):
    """
    获取目录及其子目录中所有文件的最新修改时间

    Args:
        directory (str): 目录路径

    Returns:
        datetime: 最新的文件修改时间
    """
    latest_time = None

    # 遍历目录及其子目录
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_path = os.path.join(root, file)
            # 获取文件的修改时间
            mod_time = os.path.getmtime(file_path)
            # 转换为datetime对象
            mod_datetime = datetime.fromtimestamp(mod_time)

            # 更新最新的修改时间
            if latest_time is None or mod_datetime > latest_time:
                latest_time = mod_datetime

    return latest_time


def send_wechat_message(content):
    """
    通过企业微信 webhook 发送消息

    Args:
        content (str): 要发送的消息内容
    """
    # 准备消息数据
    message_data = {
        "msgtype": "text",
        "text": {
            "content": content
        }
    }

    # 代理配置
    proxies = {
        "http": proxyServer,
        "https": proxyServer
    }

    try:
        # 发送 POST 请求
        response = requests.post(
            url=webhookUrl,
            data=json.dumps(message_data),
            proxies=proxies,
            timeout=30
        )

        # 检查响应状态
        if response.status_code == 200:
            result = response.json()
            if result.get("errcode") == 0:
                print("消息发送成功")
            else:
                print(f"消息发送失败: {result.get('errmsg')}")
        else:
            print(f"请求失败，状态码: {response.status_code}")

    except Exception as e:
        print(f"发送消息时出错: {e}")

def checkbak():
    """
    检查备份服务器备份
    """
    items = json.loads(syslist)
    content= ""
    for item in items:
        sys=(item["sys"])
        path=(item["path"])
        day=(item["day"])
        latest_mod_time = get_latest_modification_time(path)
        if latest_mod_time is not None:
            differ_day=(current_date-latest_mod_time.date()).days
            if differ_day>day:
                print(f"{sys}系统已经{differ_day}天未上传备份，该系统要求的备份频率为{day}天一次\n")
                content=content+sys+"系统已经"+str(differ_day)+"天未上传备份，该系统要求的备份频率为"+str(day)+"天一次\n"
            else:
                print(f"{sys}备份符合"+str(day)+"天一次要求")
        else:
            print(f"{sys}备系统从未备份过文件，该系统要求的备份频率为{day}天一次\n")
            content=content+sys+"备份系统从未备份过文件，该系统要求的备份频率为"+str(day)+"天一次\n"
    if content=="":
        print("所有系统都符合要求")
    else:
        content="各位同事,你们好！经过检查以下系统不符合备份要求：\n"+ content
        content = content[:-1]
        send_wechat_message(content)
def daily_task():
    """每天10点需要执行的任务"""
    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{current_time}] 执行每日{crontime}定时任务...")
    # 此处添加实际任务逻辑（如数据备份、接口调用等）
    checkbak()
# 定义定时规则：每天10点执行
schedule.every().day.at(crontime).do(daily_task)


# 按装订区域中的绿色按钮以运行脚本。
if __name__ == '__main__':
    print(f"定时任务已启动，每天{crontime}执行。按Ctrl+C终止...")
    try:
        while True:
            schedule.run_pending()  # 检查是否有任务需要执行
            time.sleep(60)  # 每60秒检查一次（减少资源占用）
    except KeyboardInterrupt:
        print("\n程序已手动终止")
